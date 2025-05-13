package com.light.translate.communicate.repository;

import com.light.translate.communicate.data.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 根据openid查找用户
    Optional<User> findByOpenid(String openid);

    // 检查openid是否存在
    boolean existsByOpenid(String openid);

    // 更新用户信息（昵称/头像等）
    @Modifying
    @Query("UPDATE User u SET u.nickname = :nickname, u.avatarUrl = :avatarUrl, u.gender = :gender WHERE u.openid = :openid")
    int updateUserInfo(
            @Param("openid") String openid,
            @Param("nickname") String nickname,
            @Param("avatarUrl") String avatarUrl,
            @Param("gender") Integer gender);

    // 批量查询用户（可选）
    @Query("SELECT u FROM User u WHERE u.openid IN :openids")
    List<User> findByOpenids(@Param("openids") List<String> openids);
}
