package com.light.translate.communicate.services;

import com.light.translate.communicate.data.UserWordReview;
import com.light.translate.communicate.repository.UserWordReviewRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final UserWordReviewRepository repository;

    public ReviewService(UserWordReviewRepository repository) {
        this.repository = repository;
    }

    public UserWordReview reviewWord(String openid, String wordId, String bookId, int quality) {
        // 质量打分 0~5
        quality = Math.max(0, Math.min(5, quality));
        LocalDateTime now = LocalDateTime.now();

        UserWordReview review = repository.findByOpenidAndWordIdAndBookId(openid, wordId,  bookId)
                .orElseGet(() -> {
                    UserWordReview r = new UserWordReview();
                    r.setOpenid(openid);
                    r.setWordId(wordId);
                    return r;
                });

        // SM-2 算法
        float ef = review.getEaseFactor();
        ef = ef + (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f));
        ef = Math.max(1.3f, ef);
        review.setEaseFactor(ef);

        review.setReviewTimes(review.getReviewTimes() + 1);
        review.setLastQuality(quality);
        review.setLastReviewTime(now);

        if (quality < 3) {
            review.setNextReviewDate(LocalDate.now().plusDays(1));
        } else {
            int interval;
            if (review.getReviewTimes() == 1) interval = 1;
            else if (review.getReviewTimes() == 2) interval = 6;
            else interval = Math.round((review.getReviewTimes() - 1) * ef);

            review.setNextReviewDate(LocalDate.now().plusDays(interval));
        }

        review.setMemoryStrength(quality / 5.0f);

        return repository.save(review);
    }

    public List<UserWordReview> getTodayReviewList(String openid, String bookId) {
        return repository.findByOpenidAndBookIdAndNextReviewDateLessThanEqual(openid, bookId, LocalDate.now());
    }

    public UserWordReview learnNewWord(String openid, String wordId, String bookId) {
        Optional<UserWordReview> existing = repository.findByOpenidAndWordIdAndBookId(openid, wordId, bookId);
        if (existing.isPresent()) {
            return existing.get(); // 已经学习过了，直接返回
        }

        UserWordReview review = new UserWordReview();
        review.setOpenid(openid);
        review.setWordId(wordId);
        review.setBookId(bookId);
        review.setReviewTimes(0);
        review.setMemoryStrength(0f);
        review.setEaseFactor(2.5f);
        review.setLastQuality(0);
        review.setLastReviewTime(LocalDateTime.now());
        review.setNextReviewDate(LocalDate.now()); // 设置为今天就可以进入复习计划

        return repository.save(review);
    }

}

