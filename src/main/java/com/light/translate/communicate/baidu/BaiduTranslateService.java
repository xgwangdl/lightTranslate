package com.light.translate.communicate.baidu;

import com.light.translate.communicate.utils.MD5;
import com.light.translate.communicate.dto.BaiduTranslateDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.light.translate.communicate.utils.MD5.md5;

@Service
public class BaiduTranslateService {
    @Value("${spring.ai.baidu.app-id}")
    private String appid;
    @Value("${spring.ai.baidu.api-key}")
    private String securityKey;

    public BaiduTranslateDTO translate(String query, String from, String to) {
        BaiduTranslateDTO baiduTranslateVO = BaiduTranslateClient.postTranslate(this.buildParams(query, from, to));
        return baiduTranslateVO;
    }

    private Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);

        params.put("appid", appid);

        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);

        // 签名
        String src = appid + query + salt + securityKey; // 加密前的原文
        params.put("sign", MD5.md5(src));

        return params;
    }


}
