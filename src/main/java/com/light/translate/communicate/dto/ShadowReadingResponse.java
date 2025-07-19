package com.light.translate.communicate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ShadowReadingResponse {
    private String originalText;
    private List<String> spokenSentences;
    private List<SentenceMatchResult> results;
}
