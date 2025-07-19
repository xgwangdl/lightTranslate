package com.light.translate.communicate.controller;

import com.light.translate.communicate.ali.SpeakerAssistant;
import com.light.translate.communicate.data.SceneTheme;
import com.light.translate.communicate.dto.SpeakerResponse;
import com.light.translate.communicate.services.AliSpeechRecognizer;
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
import java.util.List;

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

    @PostMapping("/chat")
    public SpeakerResponse chat(@RequestParam("openid") String openid,
                                @RequestParam("systemParams") String systemParams,
                                @RequestParam("voice") String voice,
                                @RequestPart("audio") MultipartFile audioFile) throws IOException, InterruptedException {
        // 保存临时文件
        File tempFile = File.createTempFile("upload", ".wav");
        audioFile.transferTo(tempFile);
        File convertFile = AudioConverter.convertToWav(tempFile);

        String userMessageContent = aliSpeechRecognizer.recognizeSentences(convertFile);
        String content =  speakerAssistant.chat(openid, userMessageContent, systemParams);
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
}
