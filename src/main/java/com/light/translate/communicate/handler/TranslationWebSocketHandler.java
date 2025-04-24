package com.light.translate.communicate.handler;

import com.light.translate.communicate.translate.TranslateSpeechWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.Date;

@Component
public class TranslationWebSocketHandler extends BinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(TranslationWebSocketHandler.class);

    @Autowired
    private TranslateSpeechWebSocketService webSocketService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        webSocketService.initSession(session);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        webSocketService.processAudioChunk(session, message.getPayload().array());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理文本指令，如开始/停止等
        webSocketService.handleCommand(session, message.getPayload());
    }


    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("WebSocket error", exception);
        webSocketService.sendError(session, "Transport error: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        webSocketService.removeSession(session);
    }
}