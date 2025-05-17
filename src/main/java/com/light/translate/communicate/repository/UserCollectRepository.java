package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.UserCollection;
import com.light.translate.communicate.dto.UserCollectionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserCollectRepository extends JpaRepository<UserCollection, Long> {
    boolean existsByOpenidAndWordId(String openid, String wordId);
    UserCollection findByOpenidAndWordId(String openid, String wordId);
    @Query("""
        SELECT new com.light.translate.communicate.dto.UserCollectionDTO(
            uc.id, uc.openid, uc.wordId, uc.category,
            w.headWord, w.tranCn, w.pos
        )
        FROM UserCollection uc
        JOIN WordTranslationAllView w ON uc.wordId = w.wordId
        WHERE uc.openid = :openid
    """)
    Page<UserCollectionDTO> findAllByOpenid(String openid, Pageable pageable);
    @Query("""
        SELECT new com.light.translate.communicate.dto.UserCollectionDTO(
            uc.id, uc.openid, uc.wordId, uc.category,
            w.headWord, w.tranCn, w.pos
        )
        FROM UserCollection uc
        JOIN WordTranslationAllView w ON uc.wordId = w.wordId
        WHERE uc.openid = :openid AND uc.category = :category
    """)
    Page<UserCollectionDTO> findByOpenidAndCategory(String openid, String category, Pageable pageable);
}
