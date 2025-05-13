package com.light.translate.communicate.controller;

import com.alibaba.nacos.api.model.v2.Result;
import com.jayway.jsonpath.JsonPath;
import com.light.translate.communicate.data.User;
import com.light.translate.communicate.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/dict/auth")
public class AuthController {

    @Value("${spring.ai.wechat.app-id}")
    private String appId;

    @Value("${spring.ai.wechat.api-secret}")
    private String appSecret;

    private static final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepo;
    private boolean isNewUser = false;

    // 静默登录接口
    @PostMapping("/silentLogin")
    public Result silentLogin(@RequestBody LoginRequest request) {
        this.isNewUser = false;
        // 1. 获取openid
        String openid = getOpenid(request.getCode());
        // 2. 查找或创建用户
        User user = userRepo.findByOpenid(openid).orElseGet(() -> {
            User newUser = new User();
            newUser.setOpenid(openid);
            if (request.getUserInfo() != null) {
                newUser.setNickname(request.getUserInfo().getNickName());
                newUser.setAvatarUrl(request.getUserInfo().getAvatarUrl());
                newUser.setGender(request.getUserInfo().getGender());
            }
            this.isNewUser = true;
            return userRepo.save(newUser);
        });

        return Result.success(Map.of(
                "isNew", isNewUser,
                "user", user
        ));
    }

    private String getOpenid(String code) {
        String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                appId, appSecret, code);

        String response = restTemplate.getForObject(url, String.class);
        return JsonPath.parse(response).read("$.openid");
    }
}

// DTO定义
@Data
class LoginRequest {
    private String code;
    private UserInfoDTO userInfo; // 非必传
}

@Data
class UserInfoDTO {
    private String nickName;
    private String avatarUrl;
    private Integer gender;
}
