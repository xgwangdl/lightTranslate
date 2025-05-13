package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.UserCollection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCollectRepository extends JpaRepository<UserCollection, Long> {
    boolean existsByOpenidAndWordId(String openid, String wordId);
    UserCollection findByOpenidAndWordId(String openid, String wordId);
    Page<UserCollection> findAllByOpenid(String openid, Pageable pageable);
    Page<UserCollection> findByOpenidAndCategory(String openid, String category, Pageable pageable);
}
