package com.light.translate.communicate.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.translate.communicate.dto.QuestionDTO;
import com.light.translate.communicate.utils.JsonParserUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class QuestionService {
    private final ChatClient chatClient;
    @Autowired
    private ObjectMapper objectMapper;

    public QuestionService(ChatClient.Builder modelBuilder,
                           @Value("classpath:prompt/question-system-prompt.st") Resource systemText) {
        this.chatClient = modelBuilder.defaultSystem(systemText).build();
    }

    public QuestionDTO ask(String headWord,  String bookName) throws JsonProcessingException {
        String userMessageContent = "模拟{key1}出一道{key2}的考题";
        Map<String, Object> variables = Map.of(
                "key1", bookName,
                "key2", headWord
        );
        String content = this.chatClient.prompt()
                .user(u -> u.text(userMessageContent).params(variables))
                .call().content();
        QuestionDTO questionDTO = JsonParserUtil.parseJson(content, QuestionDTO.class);
        return questionDTO;
    }
}
