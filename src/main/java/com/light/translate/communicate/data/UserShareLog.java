package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_share_log")
@Data
public class UserShareLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String openid;

    private String shareType;

    private String relatedId;

    private LocalDateTime createdAt = LocalDateTime.now();
}