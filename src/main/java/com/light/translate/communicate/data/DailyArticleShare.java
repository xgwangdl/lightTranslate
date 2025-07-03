package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_article_share")
@Data
public class DailyArticleShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", name = "content_en")
    private String contentEn;

    @Column(columnDefinition = "TEXT", name = "content_zh")
    private String contentZh;

    @Column(columnDefinition = "JSON")
    private String central;

    @Column(columnDefinition = "TEXT")
    private String question1;

    @Column(columnDefinition = "JSON")
    private String options1;

    @Column(length = 10)
    private String answer1;

    @Column(columnDefinition = "TEXT")
    private String question2;

    @Column(columnDefinition = "JSON")
    private String options2;

    @Column(length = 10)
    private String answer2;

    @Column(length = 512, name = "image_url")
    private String imageUrl;

    @Column(length = 512, name = "audio_url")
    private String audioUrl;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}
