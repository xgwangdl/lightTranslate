package com.light.translate.communicate.baidu;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;


@Service
public class BaiduOcrService {
    @Value("${spring.ai.baidu.ocr.api-key}")
    private String apiKey;
    @Value("${spring.ai.baidu.ocr.api-secret}")
    private String securityKey;
    @Value("${spring.ai.baidu.ocr.url}")
    private String url;

    private String getAccessToken() {
        String tokenUrl = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + securityKey;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Body 可为空
        HttpEntity<String> requestEntity = new HttpEntity<>("", headers);

        ResponseEntity<String> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        JsonObject jsonObject = JsonParser.parseString(response.getBody()).getAsJsonObject();
        return jsonObject.get("access_token").getAsString();

    }

    public String recognizeText(String base64,OcrRequest req) {
        String accessToken = getAccessToken();
        try {
            StringBuilder paramBuilder = new StringBuilder();
            paramBuilder.append("image=").append(base64);
            paramBuilder.append("&language_type=").append(req.getLanguageType());
            paramBuilder.append("&detect_direction=").append(req.isDetectDirection());
            paramBuilder.append("&detect_language=").append(req.isDetectLanguage());
            paramBuilder.append("&paragraph=").append(req.isParagraph());
            paramBuilder.append("&probability=").append(req.isProbability());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(MediaType.parseMediaTypes("application/json"));

            HttpEntity<String> entity = new HttpEntity<>(paramBuilder.toString(), headers);

            String urlWithToken = url + "?access_token=" + accessToken;

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(urlWithToken, entity, String.class);

            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
