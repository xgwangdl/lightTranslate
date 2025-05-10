package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, String> {
    List<Word> findByHeadWordStartingWith(String headWord);
    List<Word> findByHeadWord(String headWord);
}
