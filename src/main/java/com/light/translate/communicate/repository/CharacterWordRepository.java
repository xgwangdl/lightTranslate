package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.CharacterWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterWordRepository extends JpaRepository<CharacterWord, Integer> {
    Page<CharacterWord> findByWordStartingWith(String word, Pageable pageable);
}
