package com.light.translate.communicate.data;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "scene_theme")
@Data
public class SceneTheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 50)
    private String voice;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Column(name = "audio_url", length = 255)
    private String audioUrl;

    @Column(name = "img_url", length = 255)
    private String imgUrl;

    @Column(name = "system_params", length = 100, nullable = false)
    private String systemParams;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

}
