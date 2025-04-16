package com.light.translate.communicate.translate;

import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerParam;
import com.alibaba.dashscope.audio.asr.translation.TranslationRecognizerRealtime;
import com.alibaba.dashscope.audio.asr.translation.results.Translation;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

@Service
public class TranslateSpeechService {
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
                        });
        byte[] finalAudioBytes = mergedAudio.toByteArray();
        translateResult.put("audioBytes",fixWavHeader(finalAudioBytes));
        translateResult.put("orignText",orignText.toString());
        translateResult.put("translateText",translateText.toString());
        return translateResult;
    }

    private byte[] fixWavHeader(byte[] wavData) {
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
}
