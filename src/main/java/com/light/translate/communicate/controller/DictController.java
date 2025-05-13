package com.light.translate.communicate.controller;

import com.alibaba.nacos.api.model.v2.Result;
import com.light.translate.communicate.data.*;
import com.light.translate.communicate.dto.UserWordCollectDTO;
import com.light.translate.communicate.services.*;
import com.light.translate.communicate.dto.WordDTO;
import com.light.translate.communicate.dto.WordsDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    private WordService wordService;
    @Autowired
    private ExampleService exampleService;
    @Autowired
    private TranslationService translationService;
    @Autowired
    private WordBookService bookListService;
    @Autowired
    private UserWordCollectService service;

    @GetMapping("/english/words/{wordId}")
    public ResponseEntity<Word> getWordById(@PathVariable String wordId) {
        return wordService.getWordById(wordId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/english/words")
    public ResponseEntity<WordDTO> getWord(@RequestParam("word") String word) {
        return ResponseEntity.ok(wordService.getWord(word));
    }

    @GetMapping("/english/words/search")
    public List<WordTranslationView> search(@RequestParam("word") String q) {
        return wordService.searchWords(q);
    }

    @GetMapping("/english/words/detail/{wordId}")
    public ResponseEntity<WordsDetailDTO> getWordDetailById(@PathVariable String wordId) {
        return ResponseEntity.ok(wordService.getWordDetail(wordId));
    }


    @GetMapping("/english/examples/{wordId}")
    public List<Example> getExamples(@PathVariable String wordId) {
        return exampleService.getExamplesByWordId(wordId);
    }

    @GetMapping("/english/translations/{wordId}")
    public List<Translation> getTranslations(@PathVariable String wordId) {
        return translationService.getTranslationsByWordId(wordId);
    }

    @GetMapping("/english/words/recommendWords")
    public List<WordDTO> getRecommendWords(@RequestParam("word") String word) throws IOException {
        return wordService.getRecommendWords(word);
    }

    @GetMapping("/english/books")
    public Page<WordBook> list(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size) {
        return bookListService.getAllBooks(PageRequest.of(page, size));
    }

    @GetMapping("/english/books/{id}")
    public WordBook getById(@PathVariable String id) {
        return bookListService.getBookById(id);
    }

    @GetMapping("/english/collect/check")
    public ResponseEntity<Boolean> checkCollect(@RequestParam String openid, @RequestParam String wordId) {
        return ResponseEntity.ok(service.checkCollected(openid, wordId));
    }

    @PostMapping("/english/collect/add")
    public ResponseEntity<Map<String, Object>> addCollect(@RequestBody Map<String, String> body) {
        service.addCollect(body.get("openid"), body.get("wordId"), body.get("category"));
        return ResponseEntity.ok(Map.of("code", 0, "msg", "收藏成功"));
    }

    @PostMapping("/english/collect/cancel")
    public ResponseEntity<Map<String, Object>> cancelCollect(@RequestBody Map<String, String> body) {
        service.cancelCollect(body.get("openid"), body.get("wordId"));
        return ResponseEntity.ok(Map.of("code", 0, "msg", "取消收藏成功"));
    }

    @GetMapping("/english/collect/list")
    public Page<UserWordCollectDTO> listCollect(@RequestParam String openid,
                                            @RequestParam(required = false) String category,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<UserCollection> pageResult;
        if (StringUtils.hasText(category)) {
            pageResult = service.listCollects(openid, category, pageable);
        } else {
            pageResult = service.listCollects(openid, pageable);
        }
        return pageResult.map(UserWordCollectDTO::new);
    }
}

