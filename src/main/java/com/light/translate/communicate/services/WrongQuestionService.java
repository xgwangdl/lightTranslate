package com.light.translate.communicate.services;

import com.light.translate.communicate.data.WrongQuestion;
import com.light.translate.communicate.repository.WrongQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WrongQuestionService {

    @Autowired
    private WrongQuestionRepository repository;

    public WrongQuestion save(WrongQuestion question) {
        return repository.save(question);
    }

    public List<WrongQuestion> findByOpenid(String openid) {
        return repository.findByOpenid(openid);
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
}
