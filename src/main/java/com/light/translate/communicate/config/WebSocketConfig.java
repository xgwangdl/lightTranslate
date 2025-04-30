package com.light.translate.communicate.config;

import com.light.translate.communicate.handler.TranslationWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(translationWebSocketHandler(), "/ws/translation")
                .setAllowedOrigins("*");

        registry.addHandler(translationWebSocketHandler(), "/ws/realtimetranslation")
                .setAllowedOrigins("*");
    }


    @Bean
    public TranslationWebSocketHandler translationWebSocketHandler() {
        return new TranslationWebSocketHandler();
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024); // 10MB
        return container;
    }
}