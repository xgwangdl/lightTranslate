package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_word_collect")
@Data
public class UserCollection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String openid; // 微信openid

    @Column(name = "word_id", nullable = false)
    private String wordId;

    @Column(length = 20)
    private String category = "default"; // 分类标签

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "word_id", referencedColumnName = "wordId", insertable = false, updatable = false)
    private WordTranslationView word;
}
