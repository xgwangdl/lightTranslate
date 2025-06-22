package com.light.translate.communicate.dto;

import lombok.Data;

@Data
public class CheckinRequest {
    private String openid;
    private int wordCount;
    private String source;
}
