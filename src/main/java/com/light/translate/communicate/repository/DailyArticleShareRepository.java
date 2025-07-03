package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.DailyArticleShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DailyArticleShareRepository extends JpaRepository<DailyArticleShare, Long> {
    List<DailyArticleShare>  findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
    DailyArticleShare findTopByOrderByCreateTimeDesc();
}
