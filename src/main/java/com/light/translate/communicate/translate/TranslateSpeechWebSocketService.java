package com.light.translate.communicate.translate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.translate.communicate.services.DictService;
import com.light.translate.communicate.utils.OssUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TranslateSpeechWebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(TranslateSpeechWebSocketService.class);

    // 会话状态存储
    private final Map<String, SessionState> sessionStates = new ConcurrentHashMap<>();

    @Autowired
    private TranslateSpeechService translateSpeechService;

    @Autowired
    private OssUtil ossUtil;
    @Autowired
    private DictService dictService;

    public static class SessionState {
        ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();
        StringBuilder originText = new StringBuilder();
        StringBuilder translatedText = new StringBuilder();
        String targetLanguage;
        String uuid;
    }

    public void initSession(WebSocketSession session) {
        sessionStates.put(session.getId(), new SessionState());
    }

    public void handleCommand(WebSocketSession session, String command) {
        try {
            String[] parts = command.split("\\|");
            if (parts.length >= 3 && "START".equals(parts[0])) {
                SessionState state = sessionStates.get(session.getId());
                state.targetLanguage = parts[2];
                state.uuid = parts[3];
                session.sendMessage(new TextMessage("STATUS|READY"));
            }
        } catch (Exception e) {
            sendError(session, "Command error: " + e.getMessage());
        }
    }

    public void processAudioChunk(WebSocketSession session, byte[] audioChunk) {
        SessionState state = sessionStates.get(session.getId());
        if (state == null) {
            sendError(session, "Session not initialized");
            return;
        }

        String voice = this.dictService.getDictValue("lang", state.targetLanguage);

        try {
            // 2. 创建临时文件处理当前分片
            File tempFile = File.createTempFile("chunk_", ".wav");
            try {
                Files.write(tempFile.toPath(), audioChunk);
                File convertFile = AudioConverter.convertToWav(tempFile);

                // 3. 调用翻译服务
                translateSpeechService.processChunk(
                        session,
                        state,
                        convertFile.getAbsolutePath(),
                        state.targetLanguage,
                        voice
                );

                this.cleanupSession(session);
            } finally {
                tempFile.delete();
            }

        } catch (Exception e) {
            logger.error("Process audio chunk failed", e);
            sendError(session, "Processing error: " + e.getMessage());
        }
    }

    private void cleanupSession(WebSocketSession session) {
        try {
            SessionState state = sessionStates.get(session.getId());
            if (state != null && state.audioBuffer.size() > 0) {
                // 上传最终音频到OSS
                byte[] finalAudio = translateSpeechService.fixWavHeader(state.audioBuffer.toByteArray());
                String ossUrl = ossUtil.upload(new ByteArrayInputStream(finalAudio),
                        "audio_" + session.getId() + "_" + System.currentTimeMillis() + ".mp3");

                // 发送最终结果
                Map<String, String> finalResult = Map.of(
                        "type", "FINAL",
                        "audioUrl", ossUrl,
                        "uuid", state.uuid
                );
                session.sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(finalResult)));
            }
        } catch (Exception e) {
            logger.error("Final upload failed", e);
        }
    }

    public void removeSession(WebSocketSession session) {
        sessionStates.remove(session.getId());
    }

    public void sendError(WebSocketSession session, String message) {
        try {
            session.sendMessage(new TextMessage("ERROR|" + message));
        } catch (IOException e) {
            logger.error("Send error failed", e);
        }
    }
}