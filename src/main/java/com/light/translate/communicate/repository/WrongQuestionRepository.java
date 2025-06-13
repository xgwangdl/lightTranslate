package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.WrongQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WrongQuestionRepository extends JpaRepository<WrongQuestion, Integer> {
    List<WrongQuestion> findByOpenid(String openid);
    @Query("SELECT COUNT(w) FROM WrongQuestion w WHERE w.openid = :openid")
    long countByOpenid(@Param("openid") String openid);

    @Query("SELECT w FROM WrongQuestion w " +
            "WHERE w.openid = :openid " +
            "AND (:bookId IS NULL OR w.bookId = :bookId) " +
            "AND (:isMastered IS NULL OR w.isMastered = :isMastered)")
    List<WrongQuestion> findByOpenidAndOptionalFilters(
            @Param("openid") String openid,
            @Param("bookId") String bookId,
            @Param("isMastered") Boolean isMastered);

    @Query("SELECT DISTINCT w.bookId FROM WrongQuestion w WHERE w.openid = :openid")
    List<String> findDistinctBookIdByOpenid(@Param("openid") String openid);
}

