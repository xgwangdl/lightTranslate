package com.light.translate.communicate.controller;

import com.alibaba.dashscope.exception.NoApiKeyException;
import com.light.translate.communicate.services.DictService;
import com.light.translate.communicate.translate.AudioConverter;
import com.light.translate.communicate.translate.TranslateSpeechService;
import com.light.translate.communicate.utils.OssUtil;
import com.light.translate.communicate.vo.TranslateVO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/translation")
public class TranslationController {

    @Autowired
    private TranslateSpeechService translationService;
    @Autowired
    private DictService dictService;
    @Autowired
    private OssUtil ossUtil;

    @PostMapping("/translate")
    public ResponseEntity<TranslateVO> translateAudio(
            @RequestParam("audio") MultipartFile audioFile,
            @RequestParam("sourceLanguage") String sourceLanguage,
            @RequestParam("targetLanguage") String targetLanguage,
            HttpServletResponse response) throws IOException, NoApiKeyException, InterruptedException {
        // 保存临时文件
        File tempFile = File.createTempFile("upload", ".aac");
        audioFile.transferTo(tempFile);
        File convertFile = AudioConverter.convertToWav(tempFile);

        String lang = this.dictService.getDictValue("lang", targetLanguage);
        Map<String,Object> translateResult = this.translationService.startRecordingAndTranslation(convertFile.getAbsolutePath(), targetLanguage, lang);

        tempFile.delete();
        convertFile.delete();

        byte[] bytes = (byte[])translateResult.get("audioBytes");
        InputStream is = new ByteArrayInputStream(bytes);
        String url = ossUtil.upload(is, "audio.mp3");

        // 将音频数据转换为Base64
        String base64Audio = Base64.getEncoder().encodeToString(bytes);

        TranslateVO translateVO = new TranslateVO();
        translateVO.setAudioData(base64Audio);
        translateVO.setAudioUrl(url);
        translateVO.setOriginText((String)translateResult.get("orignText"));
        translateVO.setTranslateText((String)translateResult.get("translateText"));
        // 返回JSON响应
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(translateVO);

    }
}