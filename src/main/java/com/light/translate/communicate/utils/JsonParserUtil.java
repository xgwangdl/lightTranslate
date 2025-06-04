package com.light.translate.communicate.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParserUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 解析 AI 返回的 JSON 字符串为指定的对象类型，自动清理 markdown 格式（```json ... ```）。
     *
     * @param rawJson 原始 JSON 字符串，可能被 markdown 包裹
     * @param clazz   要转换的目标类型
     * @param <T>     类型参数
     * @return 解析后的对象，失败时返回 null
     */
    public static <T> T parseJson(String rawJson, Class<T> clazz) {
        if (rawJson == null || rawJson.isBlank()) return null;

        try {
            // 清洗 markdown 代码块
            String cleaned = rawJson.replaceAll("(?s)^```(?:json)?\\s*|\\s*```$", "").trim();
            return objectMapper.readValue(cleaned, clazz);
        } catch (Exception e) {
            System.err.println("解析 JSON 出错，原始内容如下：\n" + rawJson);
            e.printStackTrace();
            return null;
        }
    }
}
