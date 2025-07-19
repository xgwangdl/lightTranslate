package com.light.translate.communicate.ali;

import lombok.SneakyThrows;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
public class SpeakerAssistant {
    private final ChatClient chatClient;

    @SneakyThrows
    public SpeakerAssistant(ChatClient.Builder modelBuilder, ChatMemory chatMemory,
                              @Value("classpath:prompt/Spearker-System-Prompt.st") Resource systemText) {

        this.chatClient = modelBuilder.defaultSystem(systemText)
                .defaultAdvisors(
                        new PromptChatMemoryAdvisor(chatMemory)
                )
                .build();
    }


    public String chat(String chatId, String userMessageContent, String systemParams) {

        return this.chatClient.prompt()
                .system(s -> s.param("character_description", systemParams))
                .user(userMessageContent)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call().content();
    }

}
