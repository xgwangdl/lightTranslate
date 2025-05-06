package com.light.translate.communicate.translate;

import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerParam;
import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerRealtime;
import com.alibaba.dashscope.audio.asr.translation.results.Translation;
import com.alibaba.dashscope.audio.asr.translation.results.TranslationRecognizerResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.translate.communicate.utils.OssUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class TranslateSpeechService {
    private static final Logger logger = LoggerFactory.getLogger(TranslateSpeechService.class);

    @Value("${spring.ai.dash-scope.audio.api-key}")
    private String apiKey;

    @Value("${spring.ai.dash-scope.audio.realtime.model}")
    private String model;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Autowired
    private OssUtil ossUtil;

    ExecutorService executorService = Executors.newFixedThreadPool(4);
    List<CompletableFuture<byte[]>> futures = new ArrayList<>();

    public Map<String,Object> startRecordingAndTranslation(String filePath, String targetLanguage, String voice)
            throws ApiException, NoApiKeyException {
        Map<String,Object> translateResult = new HashMap<>();
        ByteArrayOutputStream mergedAudio = new ByteArrayOutputStream();
        StringBuilder orignText = new StringBuilder();
        StringBuilder translateText = new StringBuilder();

        logger.debug("createAudioSourceWithControlFromFile start");
        // Create a Flowable<ByteBuffer> for streaming audio data
        Flowable<ByteBuffer> audioSource = createAudioSourceWithControlFromFile(filePath);
        logger.debug("createAudioSourceWithControlFromFile end");
        // 创建Recognizer
        TranslationRecognizerRealtime translator = new TranslationRecognizerRealtime();
        logger.debug("创建TranslationRecognizerParam start");
        // 创建TranslationRecognizerParam，audioFrames参数中传入上面创建的Flowable<ByteBuffer>
        TranslationRecognizerParam param =
                TranslationRecognizerParam.builder()
                        .model(model)
                        .format("pcm") // 'pcm'、'wav'、'opus'、'speex'、'aac'、'amr', you
                        // can check the supported formats in the document
                        .sampleRate(16000) // supported 8000、16000
                        .apiKey(apiKey)
                        .transcriptionEnabled(true)
                        .translationEnabled(true)
                        .translationLanguages(new String[] {targetLanguage})
                        .build();
        logger.debug("创建TranslationRecognizerParam end");

        logger.debug("recognizer start");
        // Stream call interface for streaming audio to recognizer
        translator
                .streamCall(param, audioSource)
                .blockingForEach(
                        result -> {

                            if (result.getTranscriptionResult() != null) {
                                // 打印最终结果
                                if (result.isSentenceEnd()) {
                                    orignText.append(result.getTranscriptionResult().getText());
                                }
                            }
                            if (result.getTranslationResult() != null) {
                                Translation targetTranslation =
                                        result.getTranslationResult().getTranslation(targetLanguage);
                                if (targetTranslation != null) {
                                    if (result.isSentenceEnd()) {
                                        if (StringUtils.hasText(targetTranslation.getText())) {
                                            logger.debug("tts start");
                                            byte[] tts = textToSpeechService.tts(targetTranslation.getText(), voice);
                                            logger.debug("tts start");
                                            if (tts != null && tts.length > 44) {
                                                if (mergedAudio.size() == 0) {
                                                    // 第一段：完整保留
                                                    mergedAudio.write(tts);
                                                } else {
                                                    // 后续段：去掉 44 字节的 WAV header
                                                    mergedAudio.write(tts, 44, tts.length - 44);
                                                }
                                            }
                                            translateText.append(targetTranslation.getText());
                                        }
                                    }
                                }
                            }
                        });
        logger.debug("recognizer end");
        if (mergedAudio.size() != 0) {
            byte[] finalAudioBytes = mergedAudio.toByteArray();
            translateResult.put("audioBytes",fixWavHeader(finalAudioBytes));
        } else {
            translateResult.put("audioBytes",null);
        }

        translateResult.put("orignText",orignText.toString());
        translateResult.put("translateText",translateText.toString());
        logger.debug("startRecordingAndTranslation end");
        return translateResult;
    }

    public byte[] fixWavHeader(byte[] wavData) {
        int totalAudioLen = wavData.length - 44;
        int totalDataLen = totalAudioLen + 36;

        ByteBuffer buffer = ByteBuffer.wrap(wavData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(4, totalDataLen);      // ChunkSize = total data - 8
        buffer.putInt(40, totalAudioLen);    // Subchunk2Size = raw audio size

        return buffer.array();
    }

    private Flowable<ByteBuffer> createAudioSourceWithControlFromFile(
            String filePath) {

        // Create a Flowable<ByteBuffer> for streaming audio data from file
        return Flowable.create(
                emitter -> {
                    try {
                        // Open the WAV file
                        File audioFile = new File(filePath);
                        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                        AudioFormat audioFormat = audioInputStream.getFormat();
                        byte[] buffer = new byte[1024];

                        // Read audio data from file and emit as ByteBuffer
                        int bytesRead;
                        while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                            emitter.onNext(byteBuffer);
                        }

                        // Close the input stream after processing
                        audioInputStream.close();

                        // Complete the emission
                        emitter.onComplete();
                    } catch (Exception e) {
                        emitter.onError(e);
                        e.printStackTrace();
                    }
                },
                BackpressureStrategy.BUFFER
        );
    }
    public void processChunk(WebSocketSession session, TranslateSpeechWebSocketService.SessionState state,
                             String filePath, String targetLanguage, String voice)
            throws ApiException, NoApiKeyException, IOException {

        futures = new ArrayList<>();
        Flowable<ByteBuffer> audioSource = createAudioSourceWithControlFromFile(filePath);

        TranslationRecognizerRealtime translator = new TranslationRecognizerRealtime();
        TranslationRecognizerParam param = buildRecognizerParam(targetLanguage);

        logger.debug("streamCall start");
        translator.streamCall(param, audioSource)
                .blockingForEach(recognitionResult -> {
                    processRecognitionResult(
                            recognitionResult,
                            targetLanguage,
                            voice,
                            session,
                            state
                    );
                });
        for (CompletableFuture<byte[]> future : futures) {
            try {
                logger.debug("future.get() start");
                byte[] tts = future.get();
                logger.debug("future.get() End");
                if (tts != null && tts.length > 44) {
                    if (state.audioBuffer.size() == 0) {
                        state.audioBuffer.write(tts);
                    } else {
                        state.audioBuffer.write(tts, 44, tts.length - 44);
                    }
                }
            } catch (Exception e) {
                // 处理异常
            }
        }
        this.cleanupSession(session, state);

    }
    private TranslationRecognizerParam buildRecognizerParam(String targetLanguage) {
        return TranslationRecognizerParam.builder()
                .model(model)
                .format("pcm")
                .sampleRate(16000)
                .apiKey(apiKey)
                .transcriptionEnabled(true)
                .translationEnabled(true)
                .translationLanguages(new String[]{targetLanguage})
                .build();
    }

    private void processRecognitionResult(
            TranslationRecognizerResult result,
            String targetLanguage,
            String voice,
            WebSocketSession session,
            TranslateSpeechWebSocketService.SessionState state) throws IOException {

        Map<String, Object> translateResult = new HashMap<>();

        // 处理原始文本
        if (result.getTranscriptionResult() != null && result.isSentenceEnd()) {
            String text = result.getTranscriptionResult().getText();
            if (StringUtils.hasText(text)) {
                translateResult.put("originText", text);
            }
        }

        // 处理翻译文本和TTS
        if (result.getTranslationResult() != null) {
            Translation translation = result.getTranslationResult().getTranslation(targetLanguage);
            if (translation != null && result.isSentenceEnd()) {
                String text = translation.getText();
                if (StringUtils.hasText(text)) {
                    if (!state.realTimeTranslate) {
                        logger.debug("tts start");
                        CompletableFuture<byte[]> future = CompletableFuture.supplyAsync(() -> {
                            return textToSpeechService.tts(text, voice);
                        }, executorService);
                        futures.add(future);
                        logger.debug("tts end");
                    }

                    translateResult.put("translatedText", text);
                    sendResults(session, state, translateResult);
                }
            }
        }
    }
    private void sendResults(WebSocketSession session, TranslateSpeechWebSocketService.SessionState state, Map<String, Object> result) {
        try {
            // 发送原始文本
            String newOrigin = (String) result.get("originText");
            if (StringUtils.hasText(newOrigin) && !newOrigin.equals(state.originText.toString())) {
                state.originText.setLength(0);
                state.originText.append(newOrigin);
            }

            // 发送翻译文本
            String newTranslated = (String) result.get("translatedText");
            if (StringUtils.hasText(newTranslated) && !newTranslated.equals(state.translatedText.toString())) {
                state.translatedText.setLength(0);
                state.translatedText.append(newTranslated);
            }
            Map<String, String> middleResult = Map.of(
                    "type", "INPROCESS",
                    "originText", state.originText.toString(),
                    "translatedText", state.translatedText.toString(),
                    "uuid", state.uuid
            );
            session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(middleResult)));
        } catch (IOException e) {
            logger.error("Send results failed", e);
        }
    }

    private void cleanupSession(WebSocketSession session, TranslateSpeechWebSocketService.SessionState state) {
        try {
            if (state != null && state.audioBuffer.size() > 0) {
                // 上传最终音频到OSS
                byte[] finalAudio = this.fixWavHeader(state.audioBuffer.toByteArray());
                String ossUrl = ossUtil.upload(new ByteArrayInputStream(finalAudio),
                        "audio_" + session.getId() + "_" + System.currentTimeMillis() + ".mp3");

                // 发送最终结果
                Map<String, String> finalResult = Map.of(
                        "type", "FINAL",
                        "audioUrl", ossUrl,
                        "uuid", state.uuid
                );
                session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(finalResult)));
            } else {
                Map<String, String> finalResult = Map.of(
                        "type", "NOFIND"
                );
                session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(finalResult)));
            }
        } catch (Exception e) {
            logger.error("Final upload failed", e);
        }
    }
}
