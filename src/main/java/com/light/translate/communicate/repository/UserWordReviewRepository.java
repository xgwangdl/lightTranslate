package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.UserWordReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserWordReviewRepository extends JpaRepository<UserWordReview, Long> {

    Optional<UserWordReview> findByOpenidAndWordIdAndBookId(String openid, String wordId,  String bookId);

    List<UserWordReview> findByOpenidAndBookIdAndNextReviewDateLessThanEqual(String openid,  String bookId, LocalDate today);

    List<UserWordReview> findByOpenidAndBookId(String openid, String bookId);
}
