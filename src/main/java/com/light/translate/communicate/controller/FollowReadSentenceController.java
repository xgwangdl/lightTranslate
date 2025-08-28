package com.light.translate.communicate.controller;

import com.light.translate.communicate.data.FollowReadSentence;
import com.light.translate.communicate.dto.FollowReadSentenceDTO;
import com.light.translate.communicate.services.FollowReadSentenceService;
import com.light.translate.communicate.utils.ListeningQuestionGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.function.Function;

@RestController
@RequestMapping("/api/dict/follow-read-sentences")
public class FollowReadSentenceController {

    @Autowired
    private FollowReadSentenceService followReadSentenceService;

    @Autowired
    @Qualifier("serialExecutor")
    private Executor serialExecutor;

    // 获取所有跟读句子
    @GetMapping("/{bookId}")
    public List<FollowReadSentence> getSentencesBybookId(@PathVariable String bookId) {
        return followReadSentenceService.getSentencesBybookId(bookId);
    }


    // 添加新句子
    @GetMapping("makeSentence")
    public String makeSentence() {

        followReadSentenceService.makeSentence();
        return "success";
    }

    @GetMapping("makeSentenceBatch")
    public String makeSentenceBatch() {
        for (int i = 0; i < 5; i++) {
            serialExecutor.execute(() -> followReadSentenceService.makeSentence());
        }

        return "success";
    }

    @GetMapping("/list")
    public Page<FollowReadSentence> list(
            @RequestParam String bookId,
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return followReadSentenceService.getFollowReadSentences(bookId, pageNum, pageSize);
    }

    @GetMapping("/random")
    public List<FollowReadSentenceDTO> random(String bookId) {
        List<FollowReadSentenceDTO> sentences = new ArrayList<>();

        // 创建方法引用的列表
        List<Function<String, ListeningQuestionGenerator.ListeningQuestion>> questionGenerators = Arrays.asList(
                ListeningQuestionGenerator::generateDetailQuestion,
                ListeningQuestionGenerator::generateOrderQuestion
        );

        Random random = new Random();

        followReadSentenceService.getRandomFiveSentences(bookId).forEach(sentence -> {
            // 随机选择一个生成器
            Function<String, ListeningQuestionGenerator.ListeningQuestion> randomGenerator = questionGenerators.get(
                    random.nextInt(questionGenerators.size())
            );

            // 使用选中的生成器创建问题
            FollowReadSentenceDTO followReadSentenceDTO = new FollowReadSentenceDTO(
                    sentence,
                    randomGenerator.apply(sentence.getSentence())
            );
            sentences.add(followReadSentenceDTO);
        });

        return sentences;
    }
}

