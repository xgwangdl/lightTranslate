package com.light.translate.communicate.dto;

import lombok.Data;

@Data
public class ShareRequest {
    private String openid;
    private String shareType;
    private String relatedId;
}