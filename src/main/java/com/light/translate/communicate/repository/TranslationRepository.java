package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Translation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TranslationRepository extends JpaRepository<Translation, Long> {
    List<Translation> findByWordId(String wordId);
}

