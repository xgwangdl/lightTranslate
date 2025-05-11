package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Word;
import com.light.translate.communicate.dto.WordProjectionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, String> {
    List<Word> findByHeadWordStartingWith(String headWord);
    List<Word> findByHeadWord(String headWord);
}
