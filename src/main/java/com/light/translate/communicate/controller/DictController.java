package com.light.translate.communicate.controller;

import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.nacos.api.model.v2.Result;
import com.light.translate.communicate.data.*;
import com.light.translate.communicate.dto.*;
import com.light.translate.communicate.services.*;
import com.light.translate.communicate.utils.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private OssUtil ossUtil;
    @Autowired
    private QuestionService questionService;
    @Autowired
    private WrongQuestionService wrongQuestionService;
    @Autowired
    private CheckinService checkinService;

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
    @GetMapping("/english/words/study/{bookId}")
    public ResponseEntity<WordsDetailDTO> getStudyWord(@PathVariable String bookId) throws IOException {
        return ResponseEntity.ok(wordService.getStudyWord(bookId));
    }

    @GetMapping("/english/books")
    public List<WordBookDTO> list() {
        List<WordBook> books =  bookListService.getAllBooks();
        return books.stream().map(book -> {
            WordBookDTO dto = new WordBookDTO();
            dto.setBookId(book.getBookId());
            dto.setBookName(book.getBookName());
            dto.setWordCount(book.getWordCount());

            // 拼接 OSS 图片 URL
            String iconUrl = ossUtil.getUrl("book-png/" + book.getBookId() + ".png") ;
            dto.setIcon(iconUrl);
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/english/books/{id}")
    public WordBookDTO getById(@PathVariable String id) {
        WordBook book = bookListService.getBookById(id);
        WordBookDTO dto = new WordBookDTO();
        dto.setBookId(book.getBookId());
        dto.setBookName(book.getBookName());
        dto.setWordCount(book.getWordCount());

        // 拼接 OSS 图片 URL
        String iconUrl = ossUtil.getUrl("book-png/" + book.getBookId() + ".png") ;
        dto.setIcon(iconUrl);
        return dto;
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
    public Page<UserCollectionDTO> listCollect(@RequestParam String openid,
                                               @RequestParam(required = false) String category,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        if (StringUtils.hasText(category)) {
            return service.listCollects(openid, category, pageable);
        } else {
            return service.listCollects(openid, pageable);
        }
    }

    @PostMapping("/english/words/review/submit")
    public UserWordReview reviewWord(
            @RequestParam String openid,
            @RequestParam String wordId,
            @RequestParam String bookId,
            @RequestParam int quality
    ) {
        UserWordReview userWordReview = reviewService.reviewWord(openid, wordId, bookId, quality);
        this.checkinService.autoCheckinIfNeeded(openid, bookId);
        return userWordReview;
    }

    @GetMapping("/english/words/review/today")
    public List<UserWordReview> getTodayReviewList(@RequestParam String openid,@RequestParam String bookId) {
        return reviewService.getTodayReviewList(openid,bookId);
    }

    @PostMapping("/english/words/review/learn")
    public UserWordReview learnNewWord(
            @RequestParam String openid,
            @RequestParam String wordId,
            @RequestParam String bookId
    ) {
        UserWordReview userWordReview = reviewService.learnNewWord(openid, wordId, bookId);
        this.checkinService.autoCheckinIfNeeded(openid, bookId);
        return userWordReview;
    }

    @GetMapping("/english/words/review/distractors")
    public ResponseEntity<List<String>> getDistractors(
            @RequestParam String wordId,
            @RequestParam(defaultValue = "3") int count
    ) {
        List<String> result = wordService.getDistractorMeanings(wordId, count);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/english/words/question/test")
    public QuestionDTO testWord(
            @RequestParam String bookId
    ) throws IOException {
        WordsDetailDTO studyWord = wordService.getStudyWord(bookId);
        WordBook book = bookListService.getBookById(bookId);
        return this.questionService.ask(studyWord.getHeadWord(), book.getBookName());
    }
    // 1. 记录错题
    @PostMapping("/english/words/wrong-questions")
    public ResponseEntity<WrongQuestion> create(@RequestBody WrongQuestion question) {
        return ResponseEntity.ok(wrongQuestionService.save(question));
    }

    // 2. 获取某用户的所有错题（可加 bookId 筛选）
    @GetMapping("/english/words/wrong-questions")
    public List<WrongQuestionDTO> list(@RequestParam String openid,@RequestParam(required = false) String bookId,@RequestParam(required = false) String filter) {
        Boolean isMastered = null;
        if (!"null".equals(filter)) {
            isMastered = filter.equals("0")?false:true;
        }
        bookId = "null".equals(bookId)  ? null : bookId;
        return wrongQuestionService.findByOpenid(openid,bookId,isMastered);
    }

    // 3. 标记为已掌握
    @PutMapping("/english/words/wrong-questions/{id}/master")
    public String markAsMastered(@PathVariable Integer id) {
        boolean result = wrongQuestionService.markAsMastered(id);
        return result ? "Marked as mastered" : "Not found";
    }

    // 4. 删除错题
    @DeleteMapping("/english/words/wrong-questions/{id}")
    public String delete(@PathVariable Integer id) {
        wrongQuestionService.deleteById(id);
        return "Deleted";
    }

    // 5. 查看错题详情
    @GetMapping("/english/words/wrong-questions/{id}")
    public WrongQuestion getDetail(@PathVariable Integer id) {
        return wrongQuestionService.findById(id).orElse(null);
    }

    @GetMapping("/english/words/wrong-questions/count")
    public ResponseEntity<Long> countByOpenid(@RequestParam String openid) {
        long count = wrongQuestionService.countByOpenid(openid);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/english/words/wrong-questions/books")
    public ResponseEntity<List<WordBook>> listUserWrongBooks(@RequestParam String openid) {
        List<WordBook> books = wrongQuestionService.findUserWrongBooks(openid);
        return ResponseEntity.ok(books);
    }
}

