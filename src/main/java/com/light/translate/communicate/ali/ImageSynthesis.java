package com.light.translate.communicate.ali;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.translate.communicate.utils.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
public class ImageSynthesis {
    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${spring.ai.dashscope.images.options.model}")
    private String model;

    @Autowired
    private OssUtil ossUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();
    public String makeSentenceImage(String cnText) throws ApiException, NoApiKeyException {
        String prompt = """
                根据""" + cnText + """
                意思生成海报图片，这个海报主要是用于微信小程序每日美句分享。
                要求：简约风格, 留白区域, 适合文字排版,图片上不要有文字
                """;
        String taskId = this.createAsyncTask(prompt,"720*1280");
        return this.waitAsyncTask(taskId);
    }

    public String makeStoryImage(String central) throws NoApiKeyException, ApiException {
        String taskId = this.createAsyncTask(central,"1280*720");
        return this.waitAsyncTask(taskId);
    }
    /**
     * 创建异步任务
     * @return taskId
     */
    public String createAsyncTask(String prompt, String size) {
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .apiKey(apiKey)
                        .model(model)
                        .prompt(prompt)
                        .n(1)
                        .size(size)
                        .build();

        com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis imageSynthesis = new com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            result = imageSynthesis.asyncCall(param);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
        System.out.println(JsonUtils.toJson(result));
        String taskId = result.getOutput().getTaskId();
        System.out.println("taskId=" + taskId);
        return taskId;
    }

    /**
     * 等待异步任务结束
     * @param taskId 任务id
     * */
    private String waitAsyncTask(String taskId) {
        com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis imageSynthesis = new com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            //如果已经在环境变量中设置了 DASHSCOPE_API_KEY，wait()方法可将apiKey设置为null
            result = imageSynthesis.wait(taskId, this.apiKey);
        } catch (ApiException | NoApiKeyException e){
            throw new RuntimeException(e.getMessage());
        }
        return processDashscopeJson(JsonUtils.toJson(result.getOutput()));
    }

    private String processDashscopeJson(String dashscopeJson) {
        try {
            JsonNode root = objectMapper.readTree(dashscopeJson);
            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                String imageUrl = results.get(0).path("url").asText();
                return transferImageToOss(imageUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String transferImageToOss(String imageUrl) {
        try (InputStream inputStream = new URL(imageUrl).openStream()) {
            String fileName = "daily-quote-" + System.currentTimeMillis() + ".png";
            return ossUtil.upload(inputStream, fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
