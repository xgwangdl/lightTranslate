package com.light.translate.communicate.services;

import com.light.translate.communicate.data.Translation;
import com.light.translate.communicate.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TranslationService {

    @Autowired
    private TranslationRepository translationRepository;

    public List<Translation> getTranslationsByWordId(String wordId) {
        return translationRepository.findByWordId(wordId);
    }
}

