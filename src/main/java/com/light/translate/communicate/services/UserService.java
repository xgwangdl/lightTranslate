package com.light.translate.communicate.services;

import com.light.translate.communicate.data.User;
import com.light.translate.communicate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User updateBookId(String openid, String bookId) {
        Optional<User> userOpt = userRepository.findByOpenid(openid);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setBookId(bookId);
            return userRepository.save(user);
        } else {
            // 若找不到用户可选择抛异常，或创建新用户（根据业务需求）
            throw new IllegalArgumentException("用户不存在，无法更新bookId");
        }
    }
}

