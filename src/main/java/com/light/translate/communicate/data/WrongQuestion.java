package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "wrong_questions")
@Data
public class WrongQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String openid;

    @Column(name = "book_id")
    private String bookId;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(name = "correct_answer")
    private String correctAnswer;

    @Column(name = "user_answer")
    private String userAnswer;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "question_data", columnDefinition = "json")
    private String questionData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "is_mastered")
    private Boolean isMastered = false;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @Column(name = "last_review_time")
    private Timestamp lastReviewTime;
}

