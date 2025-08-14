package com.light.translate.communicate.services;

import com.light.translate.communicate.data.UserDailyQuota;
import com.light.translate.communicate.repository.UserDailyQuotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class QuotaService {

    @Autowired
    private UserDailyQuotaRepository quotaRepository;

    /**
     * 获取用户当天的 quota 记录（如果没有则新建）
     */
    public UserDailyQuota getTodayQuota(String openid) {
        LocalDate today = LocalDate.now();
        return quotaRepository.findByOpenidAndDate(openid, today)
                .orElseGet(() -> {
                    UserDailyQuota quota = new UserDailyQuota();
                    quota.setOpenid(openid);
                    quota.setDate(today);
                    quota.setUsedCount(0);
                    quota.setShared(false);
                    quota.setCreateTime(LocalDateTime.now());
                    quota.setUpdateTime(LocalDateTime.now());
                    return quotaRepository.save(quota);
                });
    }

    /**
     * 是否还可以继续练习
     */
    public boolean canPractice(String openid) {
        UserDailyQuota quota = getTodayQuota(openid);
        int max = quota.getShared() ? 10 : 5;
        return quota.getUsedCount() < max;
    }

    /**
     * 增加一次练习次数
     */
    public void incrementPractice(String openid) {
        UserDailyQuota quota = getTodayQuota(openid);
        int max = quota.getShared() ? 10 : 5;
        if (quota.getUsedCount() >= max) {
            throw new IllegalStateException("今日练习次数已用完");
        }
        quota.setUsedCount(quota.getUsedCount() + 1);
        quota.setUpdateTime(LocalDateTime.now());
        quotaRepository.save(quota);
    }

    /**
     * 分享解锁额外练习机会
     */
    public void unlockByShare(String openid) {
        UserDailyQuota quota = getTodayQuota(openid);
        if (!quota.getShared()) {
            quota.setShared(true);
            quota.setUpdateTime(LocalDateTime.now());
            quotaRepository.save(quota);
        }
    }

    /**
     * 获取剩余次数
     */
    public int getRemaining(String openid) {
        UserDailyQuota quota = getTodayQuota(openid);
        int max = quota.getShared() ? 10 : 5;
        return Math.max(0, max - quota.getUsedCount());
    }

    /**
     * 返回整块配额信息
     */
    public UserDailyQuota getQuotaInfo(String openid) {
        return getTodayQuota(openid);
    }
}

