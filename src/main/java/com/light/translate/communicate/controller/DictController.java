package com.light.translate.communicate.controller;

import com.light.translate.communicate.data.Example;
import com.light.translate.communicate.data.Translation;
import com.light.translate.communicate.data.Word;
import com.light.translate.communicate.services.ExampleService;
import com.light.translate.communicate.services.TranslationService;
import com.light.translate.communicate.services.WordService;
import com.light.translate.communicate.vo.WordsDetailDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Word> getWord(@RequestParam("word") String word) {
        return wordService.getWord(word)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/english/words/search")
    public List<Word> search(@RequestParam("word") String q) {
        return wordService.searchWords(q);
    }

    @GetMapping("/english/words/detail/{wordId}")
    public ResponseEntity<WordsDetailDTO> getWordDetailById(@PathVariable String wordId) {
        return ResponseEntity.ok(wordService.getWordDetail(wordId));
    }

    @GetMapping("/english/words/detail")
    public ResponseEntity<WordsDetailDTO> getWordDetail(@RequestParam("word") String word) {
        Word w = wordService.getWord(word)
                .orElseThrow(() -> new RuntimeException("Word not found"));
        return ResponseEntity.ok(wordService.getWordDetail(w.getWordId()));
    }

    @GetMapping("/english/examples/{wordId}")
    public List<Example> getExamples(@PathVariable String wordId) {
        return exampleService.getExamplesByWordId(wordId);
    }

    @GetMapping("/english/translations/{wordId}")
    public List<Translation> getTranslations(@PathVariable String wordId) {
        return translationService.getTranslationsByWordId(wordId);
    }
}

