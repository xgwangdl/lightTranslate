package com.light.translate.communicate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SentenceMatchResult {
    private int sentenceIndex;
    private String originalSentence;
    private String spokenSentence;
    private double similarity; // 0.0 ~ 1.0
    private List<String> missingWords;
}
