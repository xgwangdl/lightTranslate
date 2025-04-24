package com.light.translate.communicate.translate;

import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerParam;
import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerRealtime;
import com.alibaba.dashscope.audio.asr.translation.results.Translation;
import com.alibaba.dashscope.audio.asr.translation.results.TranslationRecognizerResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.Map;

@Service
public class TranslateSpeechService {
    private static final Logger logger = LoggerFactory.getLogger(TranslateSpeechService.class);

    @Value("${spring.ai.dash-scope.audio.api-key}")
    private String apiKey;

    @Value("${spring.ai.dash-scope.audio.realtime.model}")
    private String model;

    @Autowired
    private TextToSpeechService textToSpeechService;

    public Map<String,Object> startRecordingAndTranslation(String filePath, String targetLanguage, String voice)
            throws ApiException, NoApiKeyException {
        Map<String,Object> translateResult = new HashMap<>();
        ByteArrayOutputStream mergedAudio = new ByteArrayOutputStream();
        StringBuilder orignText = new StringBuilder();
        StringBuilder translateText = new StringBuilder();

        // Create a Flowable<ByteBuffer> for streaming audio data
        Flowable<ByteBuffer> audioSource = createAudioSourceWithControlFromFile(filePath);
        // 创建Recognizer
        TranslationRecognizerRealtime translator = new TranslationRecognizerRealtime();
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
                                            byte[] tts = textToSpeechService.tts(targetTranslation.getText(), voice);
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
        if (mergedAudio.size() != 0) {
            byte[] finalAudioBytes = mergedAudio.toByteArray();
            translateResult.put("audioBytes",fixWavHeader(finalAudioBytes));
        } else {
            translateResult.put("audioBytes",null);
        }

        translateResult.put("orignText",orignText.toString());
        translateResult.put("translateText",translateText.toString());
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

        ByteArrayOutputStream audioOutput = new ByteArrayOutputStream();
        StringBuilder originText = new StringBuilder();
        StringBuilder translatedText = new StringBuilder();

        Flowable<ByteBuffer> audioSource = createAudioSourceWithControlFromFile(filePath);

        TranslationRecognizerRealtime translator = new TranslationRecognizerRealtime();
        TranslationRecognizerParam param = buildRecognizerParam(targetLanguage);

        translator.streamCall(param, audioSource)
                .blockingForEach(recognitionResult -> {
                    processRecognitionResult(
                            recognitionResult,
                            targetLanguage,
                            voice,
                            originText,
                            translatedText,
                            audioOutput,
                            session,
                            state
                    );
                });
        state.audioBuffer.write(audioOutput.toByteArray());
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
            StringBuilder originText,
            StringBuilder translatedText,
            ByteArrayOutputStream audioOutput,
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
                    byte[] ttsAudio = textToSpeechService.tts(text, voice);
                    if (ttsAudio != null && ttsAudio.length > 44) {
                        if (audioOutput.size() == 0) {
                            audioOutput.write(ttsAudio);
                        } else {
                            audioOutput.write(ttsAudio, 44, ttsAudio.length - 44);
                        }
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
}
