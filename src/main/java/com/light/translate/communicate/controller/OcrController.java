package com.light.translate.communicate.controller;

import com.light.translate.communicate.baidu.BaiduOcrService;
import com.light.translate.communicate.baidu.OcrRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Base64;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {
    private final BaiduOcrService ocrService;

    public OcrController(BaiduOcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/recognize")
    public String recognize(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "languageType", defaultValue = "CHN_ENG") String languageType,
                            @RequestParam(value = "detectDirection", defaultValue = "false") boolean detectDirection,
                            @RequestParam(value = "detectLanguage", defaultValue = "false") boolean detectLanguage,
                            @RequestParam(value = "paragraph", defaultValue = "false") boolean paragraph,
                            @RequestParam(value = "probability", defaultValue = "false") boolean probability) throws IOException {
        byte[] bytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(bytes);
        String urlSafeBase64 = URLEncoder.encode(base64Image, "UTF-8");

        OcrRequest ocrRequest = new OcrRequest();
        ocrRequest.setLanguageType(languageType);
        ocrRequest.setDetectDirection(detectDirection);
        ocrRequest.setDetectLanguage(detectLanguage);
        ocrRequest.setParagraph(paragraph);
        ocrRequest.setProbability(probability);

        return ocrService.recognizeText(urlSafeBase64,ocrRequest);
    }
}
