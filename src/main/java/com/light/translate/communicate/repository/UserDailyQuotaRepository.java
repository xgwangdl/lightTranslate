package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.UserDailyQuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface UserDailyQuotaRepository extends JpaRepository<UserDailyQuota, Long> {
    Optional<UserDailyQuota> findByOpenidAndDate(String openid, LocalDate date);
}

