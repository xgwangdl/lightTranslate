package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_point_log")
@Data
public class UserPointLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String openid;

    private Integer points;

    private String reason;

    private LocalDateTime createdAt = LocalDateTime.now();
}