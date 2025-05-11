package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.WordTranslationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordTranslationViewRepository extends JpaRepository<WordTranslationView, Long> {

    Page<WordTranslationView> findByHeadWordStartingWith(String prefix, Pageable pageable);
    Page<WordTranslationView> findByTranCnStartingWith(String prefix, Pageable pageable);
}

