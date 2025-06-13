package com.light.translate.communicate.services;

import com.light.translate.communicate.data.WordBook;
import com.light.translate.communicate.data.WrongQuestion;
import com.light.translate.communicate.dto.WrongQuestionDTO;
import com.light.translate.communicate.repository.WordBookRepository;
import com.light.translate.communicate.repository.WrongQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WrongQuestionService {

    @Autowired
    private WrongQuestionRepository repository;
    @Autowired
    private WordBookRepository wordBookRepository;

    public WrongQuestion save(WrongQuestion question) {
        return repository.save(question);
    }

    public List<WrongQuestionDTO> findByOpenid(String openid,String bookId, Boolean filter) {
        List<WrongQuestion> questions = repository.findByOpenidAndOptionalFilters(openid,bookId,filter);

        // 1. 获取所有涉及的 bookId
        Set<String> bookIds = questions.stream()
                .map(WrongQuestion::getBookId)
                .collect(Collectors.toSet());

        // 2. 一次性查询所有 bookId 对应的 WordBook
        List<WordBook> books = wordBookRepository.findAllById(bookIds);

        // 3. 构建 Map<String, String> 映射 bookId -> bookName
        Map<String, String> bookNameMap = books.stream()
                .collect(Collectors.toMap(WordBook::getBookId, WordBook::getBookName));

        // 4. 填充 DTO
        return questions.stream().map(q -> {
            WrongQuestionDTO dto = new WrongQuestionDTO();
            dto.setId(q.getId());
            dto.setOpenid(q.getOpenid());
            dto.setBookId(q.getBookId());
            dto.setQuestion(q.getQuestion());
            dto.setCorrectAnswer(q.getCorrectAnswer());
            dto.setUserAnswer(q.getUserAnswer());
            dto.setExplanation(q.getExplanation());
            dto.setQuestionData(q.getQuestionData());
            dto.setIsMastered(q.getIsMastered());
            dto.setReviewCount(q.getReviewCount());
            dto.setCreatedAt(q.getCreatedAt());
            dto.setUpdatedAt(q.getUpdatedAt());
            dto.setLastReviewTime(q.getLastReviewTime());

            // 用 Map 获取 bookTitle
            dto.setBookTitle(bookNameMap.getOrDefault(q.getBookId(), null));

            return dto;
        }).collect(Collectors.toList());
    }

    public Optional<WrongQuestion> findById(Integer id) {
        return repository.findById(id);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean markAsMastered(Integer id) {
        Optional<WrongQuestion> optional = repository.findById(id);
        if (optional.isPresent()) {
            WrongQuestion question = optional.get();
            question.setIsMastered(true);
            repository.save(question);
            return true;
        }
        return false;
    }

    public long countByOpenid(String openid) {
        return repository.countByOpenid(openid);
    }

    public List<WordBook> findUserWrongBooks(String openid) {
        List<String> bookIds = repository.findDistinctBookIdByOpenid(openid);
        return wordBookRepository.findAllById(bookIds);
    }
}
