package com.light.translate.communicate.baidu;

import com.light.translate.communicate.dto.BaiduTranslateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class BaiduTranslateClient {

    private static final Logger logger = LoggerFactory.getLogger(BaiduTranslateClient.class);

    private static final String API_URL = "https://fanyi-api.baidu.com/api/trans/vip/translate";
    private static final RestTemplate restTemplate = new RestTemplate();

    public static BaiduTranslateDTO postTranslate(Map<String, String> params) {
        // 设置请求头 Content-Type 为 application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 设置请求体参数
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            body.add(entry.getKey(), entry.getValue());
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            // 发起 POST 请求
            ResponseEntity<BaiduTranslateDTO> response = restTemplate.postForEntity(API_URL, requestEntity, BaiduTranslateDTO.class);
            return response.getBody();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}
