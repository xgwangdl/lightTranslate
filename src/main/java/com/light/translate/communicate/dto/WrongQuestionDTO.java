package com.light.translate.communicate.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class WrongQuestionDTO {
    private Integer id;
    private String openid;
    private String bookId;
    private String bookTitle;
    private String question;
    private String correctAnswer;
    private String userAnswer;
    private String explanation;
    private String questionData;
    private Boolean isMastered;
    private Integer reviewCount;
    private Timestamp lastReviewTime;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

