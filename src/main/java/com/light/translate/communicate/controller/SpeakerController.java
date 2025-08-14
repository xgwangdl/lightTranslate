package com.light.translate.communicate.controller;

import com.light.translate.communicate.ali.SpeakerAssistant;
import com.light.translate.communicate.data.SceneTheme;
import com.light.translate.communicate.data.UserDailyQuota;
import com.light.translate.communicate.dto.SpeakerResponse;
import com.light.translate.communicate.services.AliSpeechRecognizer;
import com.light.translate.communicate.services.QuotaService;
import com.light.translate.communicate.services.SceneThemeService;
import com.light.translate.communicate.translate.AudioConverter;
import com.light.translate.communicate.translate.TextToSpeechService;
import com.light.translate.communicate.utils.OssUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dict/speaker")
@RequiredArgsConstructor
public class SpeakerController {

    private final SpeakerAssistant speakerAssistant;
    private final TextToSpeechService textToSpeechService;
    private final OssUtil ossUtil;
    @Value("${aliyun.oss.bucketName.temp}")
    private String bucketName;
    private final AliSpeechRecognizer aliSpeechRecognizer;
    private final SceneThemeService service;
    private final QuotaService quotaService;

    @PostMapping("/chat")
    public SpeakerResponse chat(@RequestParam("openid") String openid,
                                @RequestParam("systemParams") String systemParams,
                                @RequestParam("voice") String voice,
                                @RequestParam("level") String level,
                                @RequestPart("audio") MultipartFile audioFile) throws IOException, InterruptedException {
        // 保存临时文件
        File tempFile = File.createTempFile("upload", ".wav");
        audioFile.transferTo(tempFile);
        File convertFile = AudioConverter.convertToWav(tempFile);

        String userMessageContent = aliSpeechRecognizer.recognizeSentences(convertFile);
        String content =  speakerAssistant.chat(openid, userMessageContent, systemParams, level);
        convertFile.delete();

        byte[] bytes = textToSpeechService.tts(content, voice);

        InputStream is = new ByteArrayInputStream(bytes);
        String url = ossUtil.uploadForBucketName(bucketName, "mp3", is, "audio.mp3");

        return new SpeakerResponse(url,content);
    }

    @PostMapping("/tts")
    public ResponseEntity<byte[]> makeTTS(@RequestParam("text") String text,
                                          @RequestParam("voice") String voice) {
        byte[] bytes = textToSpeechService.tts(text, voice);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg")); // 设置为 mp3
        headers.setContentLength(bytes.length);
        headers.setContentDisposition(ContentDisposition
                .builder("inline") // or "attachment" to force download
                .filename("speech.mp3")
                .build());

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/all")
    public List<SceneTheme> getAllEnabledSorted() {
        return service.getAllEnabledSorted();
    }

    @GetMapping("/random-three")
    public List<SceneTheme> getRandomThreeEnabled() {
        return service.getRandomThreeEnabled();
    }

    // 获取今日剩余次数和分享状态
    @GetMapping("/remaining")
    public ResponseEntity<Map<String, Object>> getRemaining(@RequestParam String openid) {
        UserDailyQuota quota = quotaService.getQuotaInfo(openid);
        int remaining = quotaService.getRemaining(openid);
        Map<String, Object> result = new HashMap<>();
        result.put("remaining", remaining);
        result.put("shared", quota.getShared());
        return ResponseEntity.ok(result);
    }

    // 使用一次练习机会
    @PostMapping("/use")
    public ResponseEntity<?> usePractice(@RequestParam String openid) {
        if (!quotaService.canPractice(openid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("今日练习次数已达上限");
        }
        quotaService.incrementPractice(openid);
        return ResponseEntity.ok("使用成功");
    }

    // 分享成功后调用，解锁额外机会
    @PostMapping("/share")
    public ResponseEntity<?> shareSuccess(@RequestParam String openid) {
        quotaService.unlockByShare(openid);
        return ResponseEntity.ok("分享成功，已解锁额外练习次数");
    }
}
