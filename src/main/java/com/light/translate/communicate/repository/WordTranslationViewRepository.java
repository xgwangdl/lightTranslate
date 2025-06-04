package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.Word;
import com.light.translate.communicate.data.WordTranslationView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordTranslationViewRepository extends JpaRepository<WordTranslationView, Long> {

    Page<WordTranslationView> findByHeadWordStartingWith(String prefix, Pageable pageable);
    Page<WordTranslationView> findByTranCnContaining(String prefix, Pageable pageable);
    @Query(value = "SELECT * FROM word_translation_view_cache WHERE head_word != :exclude AND (:pos IS NULL OR pos = :pos) ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<WordTranslationView> findRandomWordsExclude(@Param("exclude") String exclude, @Param("pos") String pos, @Param("count") int count);
    @Query(value = "SELECT * FROM word_translation_view_cache WHERE head_word != :exclude AND pos = :pos AND tran_cn like :cn ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<WordTranslationView> findRandomWordsCn(@Param("exclude") String exclude, @Param("pos") String pos, @Param("cn") String cn, @Param("count") int count);
    @Query(value = "SELECT * FROM word_translation_view_cache WHERE tran_cn LIKE CONCAT(:prefix, '%') " +
            "UNION " +
            "SELECT * FROM word_translation_view_cache WHERE tran_cn LIKE CONCAT('%', :prefix, '%')",
            countQuery = "SELECT COUNT(*) FROM (" +
                    "SELECT * FROM word_translation_view_cache WHERE tran_cn LIKE CONCAT(:prefix, '%') " +
                    "UNION " +
                    "SELECT * FROM word_translation_view_cache WHERE tran_cn LIKE CONCAT('%', :prefix, '%')" +
                    ") AS total",
            nativeQuery = true)
    Page<WordTranslationView> findUnionLike(@Param("prefix") String prefix, Pageable pageable);

}

