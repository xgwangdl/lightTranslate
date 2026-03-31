package com.light.translate.communicate.services;

import com.alibaba.dashscope.audio.omni.*;
import com.alibaba.dashscope.audio.qwen_tts_realtime.*;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.google.gson.JsonObject;
import com.light.translate.communicate.controller.TranslationController;
import com.light.translate.communicate.dto.SpeakerResponse;
import com.light.translate.communicate.utils.OssUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OmniService {
    private static final Logger log = LoggerFactory.getLogger(OmniService.class);

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;
    @Autowired
    private OssUtil ossUtil;
    @Value("${aliyun.oss.bucketName.temp}")
    private String bucketName;

    private static final Map<String, OmniRealtimeConversation> conversations = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<CountDownLatch>> latchMap = new ConcurrentHashMap<>();
    // 保存每个 openid 当前一次请求的输出缓冲区（文本 + 音频）
    private final Map<String, AtomicReference<StringBuilder>> textMap = new ConcurrentHashMap<>();
    private final Map<String, AtomicReference<ByteArrayOutputStream>> audioMap = new ConcurrentHashMap<>();
    private String content;

    /**
     * 为一个前端 session 创建并连接一个 OmniRealtimeConversation。
     *
     */
    public void createConversationForSession(String openid, String level) throws InterruptedException, IOException {
        OmniRealtimeParam param= OmniRealtimeParam.builder()
                .model("qwen3-omni-flash-realtime")
                .apikey(apiKey)
                .build();

        OmniRealtimeConversation conversation = new OmniRealtimeConversation(param,new
                OmniRealtimeCallback(){
                    @Override
                    public void onOpen(){
                        System.out.println("[Omni] Connection opened ");
                    }

                    @Override
                    public void onEvent(JsonObject message){
                        AtomicReference<StringBuilder> sbRef = textMap.computeIfAbsent(openid, k -> new AtomicReference<>());
                        AtomicReference<ByteArrayOutputStream> audioRef = audioMap.computeIfAbsent(openid, k -> new AtomicReference<>());
                        AtomicReference<CountDownLatch> latchRef = latchMap.computeIfAbsent(openid, k -> new AtomicReference<>());

                        try{
                            String type=message.get("type").getAsString();
                            switch(type){
                                case"session.created":
                                    System.out.println("[Omni] session.created: "+message);
                                    break;
                                case"conversation.item.input_audio_transcription.completed":
                                    if(message.has("transcript")){
                                        String transcript=message.get("transcript").getAsString();
                                        System.out.println("[Omni] transcript: "+transcript);
                                    }
                                    break;
                                case"response.audio_transcript.delta":
                                    sbRef.get().append(message.get("delta").getAsString());
                                    break;
                                case"response.audio.delta":
                                    if(message.has("delta")){
                                        String recvAudioB64 = message.get("delta").getAsString();
                                        byte[] audioChunk = Base64.getDecoder().decode(recvAudioB64);
                                        audioRef.get().write(audioChunk);
                                    }
                                    break;
                                case"input_audio_buffer.speech_started":
                                    //可根据需求通知前端（例如中断播放等）
                                    System.out.println("[Omni] VAD speech started");
                                    break;
                                case"response.done":
                                    CountDownLatch latch = latchRef.get();
                                    if (latch != null) latch.countDown();
                                    System.out.println("[Omni] response.done ");
                                    break;
                                default:

                                    break;
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onClose(int code,String reason){
                        AtomicReference<CountDownLatch> latchRef = latchMap.computeIfAbsent(openid, k -> new AtomicReference<>());
                        conversations.remove(openid);
                        CountDownLatch latch = latchRef.get();
                        if (latch != null) latch.countDown();
                        System.out.println("[Omni] Closed, code="+code+", reason="+
                                reason);
                    }
                });

        try{
            conversation.connect();
        }catch(NoApiKeyException | InterruptedException e) {
            //连接失败通常是因为未配置APIKey
            System.err.println("[Omni] connect failed: NoApiKeyException - " + e.getMessage());
            e.printStackTrace();
        }

        String promptContent = loadPrompt("/prompt/English-Teaching-Prompt.st").replace("{{level}}", level);

        OmniRealtimeConfig config=OmniRealtimeConfig.builder()
                .modalities(Arrays.asList(OmniRealtimeModality.AUDIO,OmniRealtimeModality.TEXT))
                .voice("Jennifer")
                .enableTurnDetection(true)
                .enableInputAudioTranscription(true)
                .InputAudioTranscription("gummy-realtime-v1")
                .parameters(new HashMap<String, Object>() {{
                    put("instructions",promptContent);
                    put("smooth_output", true);
                }})
                .build();
        try{
            conversation.updateSession(config);
        }catch(Exception e){
            System.err.println("[Omni] updateSession failed: "+e.getMessage());
            e.printStackTrace();
        }
        this.conversations.put(openid, conversation);
    }

    public SpeakerResponse sendMessage(String openid, File tmpOut) throws IOException, InterruptedException {
        OmniRealtimeConversation conversation = conversations.get(openid);

        AtomicReference<CountDownLatch> latchRef = latchMap.computeIfAbsent(openid, k -> new AtomicReference<>());
        CountDownLatch responseDoneLatch = new CountDownLatch(1);
        latchRef.set(responseDoneLatch);
        AtomicReference<StringBuilder> sbRef = textMap.computeIfAbsent(openid, k -> new AtomicReference<>());
        AtomicReference<ByteArrayOutputStream> audioRef = audioMap.computeIfAbsent(openid, k -> new AtomicReference<>());
        sbRef.set(new StringBuilder());
        audioRef.set(new ByteArrayOutputStream());

        FileInputStream fis = new FileInputStream(tmpOut);
        byte[] buffer = new byte[3200];
        int bytesRead;
        // Loop to read chunks of the file
        while ((bytesRead = fis.read(buffer)) != -1) {
            ByteBuffer byteBuffer;
            if (bytesRead < buffer.length) {
                byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
            } else {
                byteBuffer = ByteBuffer.wrap(buffer);
            }
            // Send the ByteBuffer to the recognition instance
            String audioB64 = Base64.getEncoder().encodeToString(byteBuffer.array());
            conversation.appendAudio(audioB64);
            buffer = new byte[3200];
        }

        conversation.commit();
        conversation.createResponse(null, null);
        responseDoneLatch.await();

        byte[] pcmBytes = audioRef.get().toByteArray();
        byte[] mp3Bytes = convertPcmToMp3(pcmBytes);

        InputStream is = new ByteArrayInputStream(mp3Bytes);
        String url = ossUtil.uploadForBucketName(bucketName, "mp3", is, "audio.mp3");

        SpeakerResponse speakerResponse = new SpeakerResponse(url, sbRef.get().toString());
        return speakerResponse;
    }

    public void stop(String openid) {
        OmniRealtimeConversation conversation = conversations.get(openid);
        if (conversation != null) {
            conversation.close(1000, "bye");
            conversations.remove(openid);
        }
        AtomicReference<CountDownLatch> latchRef = latchMap.computeIfAbsent(openid, k -> new AtomicReference<>());
        CountDownLatch latch = latchRef.get();
        if (latch != null) latch.countDown();
    }

    private byte[] convertPcmToMp3(byte[] pcmData) throws IOException, InterruptedException {
        // 1. 写入临时 PCM 文件
        File tmpPcm = File.createTempFile("audio", ".pcm");
        try (FileOutputStream fos = new FileOutputStream(tmpPcm)) {
            fos.write(pcmData);
        }

        // 2. 输出文件
        File tmpMp3 = File.createTempFile("audio", ".mp3");

        // 3. 使用 FFmpeg 转换
        // 注意：ffmpeg 需要知道输入参数 -f s16le -ar 24000 -ac 1
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-f", "s16le",
                "-ar", "24000",
                "-ac", "1",
                "-i", tmpPcm.getAbsolutePath(),
                "-codec:a", "libmp3lame",
                "-b:a", "64k",
                tmpMp3.getAbsolutePath()
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        // 打印 ffmpeg 输出，方便调试
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exitCode = p.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 转换失败，退出码: " + exitCode);
        }

        // 4. 读取 mp3 文件字节
        byte[] mp3Data = Files.readAllBytes(tmpMp3.toPath());

        // 5. 清理临时文件
        tmpPcm.delete();
        tmpMp3.delete();

        return mp3Data;
    }

    private String loadPrompt(String path) throws IOException {
        InputStream is = getClass().getResourceAsStream(path);
        if(is == null) return "";
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }


    public SpeakerResponse getVoice(Flux<String> userMessageContent,String voice) throws IOException, InterruptedException {
        ByteArrayOutputStream audioOut = new ByteArrayOutputStream();
        StringBuilder sb = new StringBuilder();
        QwenTtsRealtimeParam param = QwenTtsRealtimeParam.builder()
                .model("qwen3-tts-flash-realtime")
                .url("wss://dashscope.aliyuncs.com/api-ws/v1/realtime")
                .apikey(apiKey)
                .build();
        AtomicReference<CountDownLatch> completeLatch = new AtomicReference<>(new CountDownLatch(1));
        final AtomicReference<QwenTtsRealtime> qwenTtsRef = new AtomicReference<>(null);

        QwenTtsRealtime qwenTtsRealtime = new QwenTtsRealtime(param, new QwenTtsRealtimeCallback() {
            @Override
            public void onOpen() {
                // 连接建立时的处理
            }
            @Override
            public void onEvent(JsonObject message) {
                String type = message.get("type").getAsString();
                switch(type) {
                    case "session.created":
                        // 会话创建时的处理
                        break;
                    case "response.audio.delta":
                        String recvAudioB64 = message.get("delta").getAsString();
                        byte[] audioChunk = Base64.getDecoder().decode(recvAudioB64);
                        try {
                            audioOut.write(audioChunk);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "response.done":
                        // 响应完成时的处理
                        break;
                    case "session.finished":
                        // 会话结束时的处理
                        completeLatch.get().countDown();
                    default:
                        break;
                }
            }
            @Override
            public void onClose(int code, String reason) {
                // 连接关闭时的处理
                completeLatch.get().countDown();
            }
        });
        qwenTtsRef.set(qwenTtsRealtime);
        try {
            qwenTtsRealtime.connect();
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        QwenTtsRealtimeConfig config = QwenTtsRealtimeConfig.builder()
                .voice(voice)
                .languageType("Auto")
                .responseFormat(QwenTtsRealtimeAudioFormat.PCM_24000HZ_MONO_16BIT)
                .mode("server_commit")
                .volume(100)
                .build();
        qwenTtsRealtime.updateSession(config);

        userMessageContent
                .doOnNext(text -> {
                    qwenTtsRealtime.appendText(text);
                    sb.append(text);
                })
                .doOnError(error -> log.error("语音合成失败", error))
                .doOnComplete(() -> log.info("语音合成完成"))
                .blockLast();
        qwenTtsRealtime.finish();
        completeLatch.get().await();
        qwenTtsRealtime.close();

        byte[] pcmBytes = audioOut.toByteArray();
        byte[] mp3Bytes = convertPcmToMp3(pcmBytes);

        InputStream is = new ByteArrayInputStream(mp3Bytes);
        String url = ossUtil.uploadForBucketName(bucketName, "mp3", is, "audio.mp3");

        SpeakerResponse speakerResponse = new SpeakerResponse(url, sb.toString());
        return speakerResponse;
    }
}
