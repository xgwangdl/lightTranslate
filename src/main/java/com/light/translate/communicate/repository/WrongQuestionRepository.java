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
}

