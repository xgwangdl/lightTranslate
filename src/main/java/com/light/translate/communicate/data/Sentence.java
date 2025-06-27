package com.light.translate.communicate.data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Sentence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String en;    // 英文句子
    private String cn;    // 中文翻译
    private String tip;   // 语法点说明
    private String word;  // 词汇解释

    private String url;   // 相关链接，如图片或分享地址

    private LocalDateTime createTime;  // 生成时间

    @PrePersist
    public void prePersist() {
        if (this.createTime == null) {
            this.createTime = LocalDateTime.now();
        }
    }
}

