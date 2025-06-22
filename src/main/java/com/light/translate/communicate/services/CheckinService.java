package com.light.translate.communicate.services;

import com.light.translate.communicate.data.UserCheckinLog;
import com.light.translate.communicate.data.UserWordReview;
import com.light.translate.communicate.data.WordBook;
import com.light.translate.communicate.dto.WordBookDTO;
import com.light.translate.communicate.repository.UserCheckinLogRepository;
import com.light.translate.communicate.repository.UserWordReviewRepository;
import com.light.translate.communicate.utils.OssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final UserCheckinLogRepository checkinLogRepo;
    private final UserWordReviewRepository wordReviewRepo;
    private final WordBookService bookListService;
    private final OssUtil ossUtil;
    /**
     * 查询今天是否打卡及相关数据
     */
    public Map<String, Object> getTodayCheckin(String openid,String bookId) {
        LocalDate today = LocalDate.now();
        Optional<UserCheckinLog> todayLog = checkinLogRepo.findByOpenidAndBookIdAndCheckinDate(openid, bookId, today);

        Map<String, Object> result = new HashMap<>();
        result.put("checkedIn", todayLog.isPresent());
        result.put("wordCount", todayLog.map(UserCheckinLog::getLearnedCount).orElse(0));
        result.put("source", todayLog.map(UserCheckinLog::getSource).orElse(""));

        return result;
    }

    /**
     * 查询用户单词复习总览
     */
    public Map<String, Object> getUserOverview(String openid, String bookId) {
        List<UserWordReview> reviews = wordReviewRepo.findByOpenidAndBookId(openid,bookId);

        int total = reviews.size();
        int mastered = (int) reviews.stream().filter(w -> w.getMemoryStrength() >= 0.8).count();
        int reviewing = (int) reviews.stream().filter(w -> w.getMemoryStrength() > 0 && w.getMemoryStrength() < 0.8).count();

        // 今日学习词数（按最后复习时间是今天）
        int todayLearned = (int) reviews.stream().filter(w -> {
            return w.getLastReviewTime() != null &&
                    w.getLastReviewTime().toLocalDate().equals(LocalDate.now());
        }).count();

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("mastered", mastered);
        result.put("reviewing", reviewing);
        result.put("today", todayLearned);
        return result;
    }

    /**
     * 获取最近7天的打卡历史
     */
    public List<Map<String, Object>> getCheckinHistory(String openid, String bookId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        List<UserCheckinLog> logs = checkinLogRepo.findByOpenidAndBookIdAndCheckinDateBetween(openid, bookId, weekAgo, today);

        // 转换为 map 列表
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekAgo.plusDays(i);
            int learnedCount = logs.stream()
                    .filter(log -> log.getCheckinDate().equals(date))
                    .map(UserCheckinLog::getLearnedCount)
                    .findFirst()
                    .orElse(0);

            int reviewedCount = logs.stream()
                    .filter(log -> log.getCheckinDate().equals(date))
                    .map(UserCheckinLog::getReviewedCount)
                    .findFirst()
                    .orElse(0);

            Map<String, Object> day = new HashMap<>();
            day.put("date", date.toString());
            day.put("learned", learnedCount);  // 如果将来区分 new/review 可以细分
            day.put("reviewed", reviewedCount);     // 可从其他逻辑推
            result.add(day);
        }
        return result;
    }

    public Map<String, Object> getWordbookStats(String openid, String bookId) {
        List<UserWordReview> reviews = wordReviewRepo.findByOpenidAndBookId(openid, bookId);

        int total = reviews.size();
        int learned = (int) reviews.stream().filter(r -> r.getReviewTimes() > 0).count();
        int mastered = (int) reviews.stream().filter(r -> r.getMemoryStrength() >= 0.8).count();
        int pending = total - learned;

        WordBook book = bookListService.getBookById(bookId);

        // 拼接 OSS 图片 URL
        String iconUrl = ossUtil.getUrl("book-png/" + book.getBookId() + ".png") ;

        Map<String, Object> map = new HashMap<>();
        map.put("total", book.getWordCount());
        map.put("learned", learned);
        map.put("mastered", mastered);
        map.put("pending", pending);
        map.put("iconUrl", iconUrl);
        return map;
    }

    public Map<String, Object> getTodayTaskStats(String openid, String bookId) {
        List<UserWordReview> reviews = wordReviewRepo.findByOpenidAndBookId(openid, bookId);

        LocalDate today = LocalDate.now();
        int todayNew = 0;
        int todayReview = 0;

        for (UserWordReview review : reviews) {
            if (review.getLastReviewTime() != null &&
                    review.getLastReviewTime().toLocalDate().equals(today)) {

                if (review.getReviewTimes() == 0) {
                    todayNew++;
                } else {
                    todayReview++;
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("todayNew", todayNew);
        result.put("todayReview", todayReview);
        return result;
    }

    public void autoCheckinIfNeeded(String openid, String bookId) {
        LocalDate today = LocalDate.now();

        // 获取今天学习的词汇（根据 lastReviewTime）
        List<UserWordReview> todayLearned = wordReviewRepo.findByOpenidAndBookId(openid, bookId).stream()
                .filter(r -> r.getLastReviewTime() != null &&
                        r.getLastReviewTime().toLocalDate().equals(today) &&
                        r.getReviewTimes() == 0)
                .toList();

        List<UserWordReview> todayReviews = wordReviewRepo.findByOpenidAndBookId(openid, bookId).stream()
                .filter(r -> r.getLastReviewTime() != null &&
                        r.getLastReviewTime().toLocalDate().equals(today) &&
                        r.getReviewTimes() > 0)
                .toList();

        if (todayReviews.isEmpty() && todayLearned.isEmpty()) return; // 今天没有学习，无需打卡

        int reviewCount = todayReviews.size();
        int learnedCount = todayLearned.size();

        // 查询是否已有打卡记录
        Optional<UserCheckinLog> existing = checkinLogRepo.findByOpenidAndCheckinDateAndBookId(openid, today, bookId);

        if (existing.isPresent()) {
            // ✅ 已存在 → 更新 wordCount
            UserCheckinLog log = existing.get();
            log.setReviewedCount(reviewCount);
            log.setLearnedCount(learnedCount);
            log.setSource("review");
            checkinLogRepo.save(log);
        } else {
            // ❌ 不存在 → 插入新记录
            UserCheckinLog log = new UserCheckinLog();
            log.setOpenid(openid);
            log.setBookId(bookId);
            log.setCheckinDate(today);
            log.setReviewedCount(reviewCount);
            log.setLearnedCount(learnedCount);
            log.setSource("review");
            checkinLogRepo.save(log);
        }
    }

}
