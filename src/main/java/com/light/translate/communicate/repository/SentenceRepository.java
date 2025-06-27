package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    List<Sentence> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end);
    Sentence findTopByOrderByCreateTimeDesc();
}
