package com.light.translate.communicate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.translate.communicate.baidu.BaiduOcrService;
import com.light.translate.communicate.baidu.BaiduTranslateService;
import com.light.translate.communicate.baidu.OcrRequest;
import com.light.translate.communicate.dto.BaiduTranslateDTO;
import com.light.translate.communicate.dto.OcrResponseDTO;
import com.light.translate.communicate.translate.TextToImageService;
import com.light.translate.communicate.utils.OssUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {
    private final BaiduOcrService ocrService;
    private final BaiduTranslateService baiduTranslateService;
    private final TextToImageService textToImageService;
    private final OssUtil ossUtil;

    public OcrController(BaiduOcrService ocrService, BaiduTranslateService baiduTranslateService,
                         TextToImageService textToImageService, OssUtil ossUtil) {
        this.ocrService = ocrService;
        this.baiduTranslateService = baiduTranslateService;
        this.textToImageService = textToImageService;
        this.ossUtil = ossUtil;
    }

    @PostMapping("/recognize")
    public String recognize(@RequestParam("file") MultipartFile file,
                            @RequestParam(value = "languageType", defaultValue = "CHN_ENG") String languageType,
                            @RequestParam(value = "detectDirection", defaultValue = "false") boolean detectDirection,
                            @RequestParam(value = "detectLanguage", defaultValue = "false") boolean detectLanguage,
                            @RequestParam(value = "paragraph", defaultValue = "false") boolean paragraph,
                            @RequestParam(value = "probability", defaultValue = "false") boolean probability,
                            @RequestParam("sourceLanguage") String sourceLanguage,
                            @RequestParam("targetLanguage") String targetLanguage) throws IOException {
        byte[] bytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(bytes);
        String urlSafeBase64 = URLEncoder.encode(base64Image, "UTF-8");

        OcrRequest ocrRequest = new OcrRequest();
        ocrRequest.setLanguageType(languageType);
        ocrRequest.setDetectDirection(detectDirection);
        ocrRequest.setDetectLanguage(detectLanguage);
        ocrRequest.setParagraph(paragraph);
        ocrRequest.setProbability(probability);

        String orcResult = ocrService.recognizeText(urlSafeBase64,ocrRequest);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(orcResult);
        if (rootNode.has("error_msg")) {
            String error = rootNode.get("error_msg").asText();
            throw new RuntimeException("OCR接口错误：" + error);
        } else {
            OcrResponseDTO dto = objectMapper.readValue(orcResult, OcrResponseDTO.class);
            dto.getWords_result().forEach(wordResult -> {
                BaiduTranslateDTO translate = baiduTranslateService.translate(wordResult.getWords(), sourceLanguage, targetLanguage);
                wordResult.setTargetWords(translate.getTrans_result().get(0).getDst());
            });
            String jsonString = objectMapper.writeValueAsString(dto);
            Path tempFile = Files.createTempFile("ocr_data", ".json");
            Files.write(tempFile, jsonString.getBytes(StandardCharsets.UTF_8));
            byte[]  images = this.textToImageService.makeImage(tempFile.toString());
            InputStream is = new ByteArrayInputStream(images);
            String url = ossUtil.upload(is, "translate.png");
            tempFile.toFile().delete();
            return url;
        }
    }
}
