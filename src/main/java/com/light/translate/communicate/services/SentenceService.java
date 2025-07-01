package com.light.translate.communicate.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.light.translate.communicate.ali.ImageSynthesis;
import com.light.translate.communicate.data.Sentence;
import com.light.translate.communicate.repository.SentenceRepository;
import com.light.translate.communicate.translate.TextToSpeechService;
import com.light.translate.communicate.utils.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.DayOfWeek;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SentenceService {

    @Value("${spring.ai.deepseek.api-key}")
    private String token;

    @Autowired
    private SentenceRepository sentenceRepository;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private ImageSynthesis imageSynthesis;

    @Scheduled(cron = "0 0 1 * * ?")
    public void executeDailyTask() throws JsonProcessingException {
        System.out.println("Executing daily sentence save task...");
        saveSentence();
    }

    public void saveSentence() throws JsonProcessingException {
        String json = makeSentence();

        // 清理掉 `json` 和多余的反引号
        String cleanedJson = cleanJson(json);

        // 解析 cleanedJson
        ObjectMapper objectMapper = new ObjectMapper();
        Sentence sentence = objectMapper.readValue(cleanedJson, Sentence.class);

        // 设置生成日期和URL
        sentence.setCreateTime(LocalDateTime.now());

        // 获取当前日期
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        // 根据星期几替换tip
        String voice = this.getVoiceForDay(dayOfWeek);
        byte[] tts = textToSpeechService.tts(sentence.getEn(), voice);
        InputStream is = new ByteArrayInputStream(tts);
        String url = ossUtil.upload(is, "sentence.mp3");
        sentence.setUrl(url);

        try {
            String imageUrl = this.imageSynthesis.basicCall(sentence.getCn());
            sentence.setImageUrl(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 保存到数据库
        sentenceRepository.save(sentence);
    }

    private String makeSentence() throws JsonProcessingException {
        String url = "https://api.deepseek.com/chat/completions";

        // 1. 构造请求体
        Map<String, Object> message1 = new HashMap<>();
        message1.put("role", "system");
        message1.put("content", """
                你是一个优秀的英语文学家，可以写出很优美的英语句子
                """);

        // 获取当前日期
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        // 根据星期几替换tip
        String tip = getTipForDay(dayOfWeek);

        Map<String, Object> message2 = new HashMap<>();
        message2.put("role", "user");
        String content = """
                生成一句英文学习句子，要求：
                1. 包含1个四六级高频词
                2. 长度<30单词
                3. 附带中文翻译和语法点、
                4. """ + tip + """
                5. 句子要优美富有文学色彩
                6. 返回json字符串
                生成的例句：
                {
                  "en": "Persistence is the key to success.",
                  "cn": "坚持是成功的关键。",
                  "tip": "key在这里是名词，意为'关键'",
                  "word": "persistence [ˌpɜːrˈsɪstəns] n.坚持"
                }
                """;
        message2.put("content", content);

        List<Map<String, Object>> messages = Arrays.asList(message1,message2);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", messages);
        requestBody.put("model", "deepseek-chat");
        requestBody.put("frequency_penalty", 0);
        requestBody.put("max_tokens", 2048);
        requestBody.put("presence_penalty", 0);
        requestBody.put("response_format", Collections.singletonMap("type", "text"));
        requestBody.put("stop", null);
        requestBody.put("stream", false);
        requestBody.put("stream_options", null);
        requestBody.put("temperature", 1);
        requestBody.put("top_p", 1);
        requestBody.put("tools", null);
        requestBody.put("tool_choice", "none");
        requestBody.put("logprobs", false);
        requestBody.put("top_logprobs", null);

        // 2. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);
        System.out.println(headers);

        // 3. 创建 HTTP 实体对象
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        // 4. 发送请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        // 5. 输出响应内容
        String json =  response.getBody();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        // 提取 content 字段
        String result = root
                .path("choices")
                .get(0)
                .path("message")
                .path("content")
                .asText();
        return result;
    }

    private String getTipForDay(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "日常英语，要求像诗一样优美";
            case TUESDAY:
                return "使用大学英语四级语法";
            case WEDNESDAY:
                return "使用大学英语六级语法";
            case THURSDAY:
                return "使用雅思英语句式";
            case FRIDAY:
                return "使用GRE英语句式";
            case SATURDAY:
                return "使用托福英语句式";
            case SUNDAY:
                return "使用考研英语语法";
            default:
                return "电影英语";
        }
    }

    private String getVoiceForDay(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "en-US-JennyNeural";
            case TUESDAY:
                return "en-US-GuyNeural";
            case WEDNESDAY:
                return "en-CA-ClaraNeural";
            case THURSDAY:
                return "en-GB-LibbyNeural";
            case FRIDAY:
                return "en-GB-RyanNeural";
            case SATURDAY:
                return "en-GB-SoniaNeural";
            case SUNDAY:
                return "en-US-AriaNeural";
            default:
                return "en-US-JennyNeural";
        }
    }

    private String cleanJson(String rawJson) {
        // 1. 去掉开始的 ` ```json `
        // 2. 去掉结束的 ` ``` `
        rawJson = rawJson.replaceAll("^```json", "").replaceAll("```$", "").trim();
        return rawJson;
    }

    public List<Sentence> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end) {
        return sentenceRepository.findByCreateTimeBetween(start, end);
    }

    public Sentence findTopByOrderByCreateTimeDesc() {
        return sentenceRepository.findTopByOrderByCreateTimeDesc();
    }
}
