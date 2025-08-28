package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.FollowReadSentence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowReadSentenceRepository extends JpaRepository<FollowReadSentence, Long> {
    List<FollowReadSentence> findTop200ByAudioUrlIsNull();

    List<FollowReadSentence> findBybookId(String bookId);

    Page<FollowReadSentence> findByBookIdAndAudioUrlIsNotNull(String bookId, Pageable pageable);

    @Query(value = "SELECT * FROM follow_read_sentence " +
            "WHERE book_id = :bookId " +
            "AND audio_url IS NOT NULL " +
            "AND audio_url <> '' " +
            "ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<FollowReadSentence> findRandomFiveByBookId(@Param("bookId") String bookId);
}

