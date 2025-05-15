package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Word;
import com.light.translate.communicate.dto.WordProjectionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, String> {
    List<Word> findByHeadWordStartingWith(String headWord);
    List<Word> findByHeadWord(String headWord);
    @Query("SELECT COUNT(w) FROM Word w")
    long countAllWords();

    @Query(value = "SELECT * FROM words LIMIT 1 OFFSET :offset", nativeQuery = true)
    Word findWordByOffset(@Param("offset") int offset);

    @Query("SELECT COUNT(w) FROM Word w WHERE w.bookId = :bookId")
    long countAllWordsByBook(@Param("bookId") String bookI);

    @Query(value = "SELECT * FROM words WHERE book_id = :bookId LIMIT 1 OFFSET :offset", nativeQuery = true)
    Word findWordByOffsetByBook(@Param("bookId") String bookId, @Param("offset") int offset);

}
