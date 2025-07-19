package com.light.translate.communicate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SpeakerResponse {
    private String audioUrl;
    private String content;
}
