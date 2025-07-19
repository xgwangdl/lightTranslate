package com.light.translate.communicate.services;

import com.alibaba.dashscope.audio.asr.recognition.Recognition;
import com.alibaba.dashscope.audio.asr.recognition.RecognitionParam;
import com.light.translate.communicate.utils.JacksonMapperUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AliSpeechRecognizer {
    @Value("${spring.ai.dash-scope.audio.api-key}")
    private String apiKey;

    /**
     * 音频转文字
     * @param audioFile 音频
     * @return 文字
     * @throws IOException
     */
    public String recognizeSentences(File audioFile) throws IOException {
        String result = "";
        // 创建Recognition实例
        Recognition recognizer = new Recognition();
        // 创建RecognitionParam
        RecognitionParam param =
                RecognitionParam.builder()
                        .apiKey(apiKey)
                        .model("paraformer-realtime-v2")
                        .format("wav")
                        .sampleRate(16000)
                        // “language_hints”只支持paraformer-v2和paraformer-realtime-v2模型
                        .parameter("language_hints", new String[]{"zh", "en"})
                        .build();

        try {
            String content = recognizer.call(param, audioFile);
            Map<String, Object> stringObjectMap = JacksonMapperUtils.json2map(content);
            List<Map<String, Object>> sentences = (List<Map<String, Object>>) stringObjectMap.get("sentences");


            result = sentences.stream()
                    .map(s -> (String) s.get("text"))
                    .collect(Collectors.joining(""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
