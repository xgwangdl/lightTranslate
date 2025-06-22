package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_checkin_log")
@Data
public class UserCheckinLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String openid;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "learned_count")
    private Integer learnedCount = 0;

    @Column(name = "reviewed_count")
    private Integer reviewedCount = 0;

    @Column(name = "source", length = 50)
    private String source = "review";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "book_id", nullable = false, length = 100)
    private String bookId;
}