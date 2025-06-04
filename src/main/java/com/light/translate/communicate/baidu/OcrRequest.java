package com.light.translate.communicate.baidu;

import lombok.Data;

@Data
public class OcrRequest {
    private String languageType = "CHN_ENG";   // 默认中英文混合
    private boolean detectDirection = false;
    private boolean detectLanguage = false;
    private boolean paragraph = false;
    private boolean probability = false;
}
