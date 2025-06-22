package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.UserCheckinLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserCheckinLogRepository extends JpaRepository<UserCheckinLog, Long> {

    Optional<UserCheckinLog> findByOpenidAndBookIdAndCheckinDate(String openid, String bookId, LocalDate checkinDate);

    long countByOpenid(String openid);

    List<UserCheckinLog> findByOpenidAndBookIdAndCheckinDateBetween(String openid, String bookId, LocalDate from, LocalDate to);

    Optional<UserCheckinLog> findByOpenidAndCheckinDateAndBookId(String openid, LocalDate date, String bookId);
}