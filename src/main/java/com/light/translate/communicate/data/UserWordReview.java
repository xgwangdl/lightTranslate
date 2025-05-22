package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_word_reviews", indexes = {
        @Index(name = "idx_openid_word", columnList = "openid, word_id", unique = true)
})
@Data
public class UserWordReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String openid;

    @Column(name = "word_id")
    private String wordId;

    @Column(name = "book_id")
    private String bookId;

    private Integer reviewTimes = 0;

    private Float memoryStrength = 0f;

    private Float easeFactor = 2.5f;

    private Integer lastQuality = 0;

    private LocalDate nextReviewDate;

    private LocalDateTime lastReviewTime;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

