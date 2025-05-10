package com.light.translate.communicate.controller;

import com.light.translate.communicate.data.Example;
import com.light.translate.communicate.data.Translation;
import com.light.translate.communicate.data.Word;
import com.light.translate.communicate.services.ExampleService;
import com.light.translate.communicate.services.TranslationService;
import com.light.translate.communicate.services.WordService;
import com.light.translate.communicate.vo.WordDTO;
import com.light.translate.communicate.vo.WordProjectionDTO;
import com.light.translate.communicate.vo.WordsDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    private WordService wordService;
    @Autowired
    private ExampleService exampleService;
    @Autowired
    private TranslationService translationService;

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
    public List<WordProjectionDTO> search(@RequestParam("word") String q) {
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
}

