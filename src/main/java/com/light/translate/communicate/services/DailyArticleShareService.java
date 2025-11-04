package com.light.translate.communicate.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.light.translate.communicate.ali.ImageSynthesis;
import com.light.translate.communicate.data.DailyArticleShare;
import com.light.translate.communicate.data.Sentence;
import com.light.translate.communicate.dto.WordsDetailDTO;
import com.light.translate.communicate.repository.DailyArticleShareRepository;
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
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.DayOfWeek;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DailyArticleShareService {

    @Value("${spring.ai.deepseek.api-key}")
    private String token;

    @Autowired
    private DailyArticleShareRepository repository;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private ImageSynthesis imageSynthesis;

    @Autowired
    private WordService wordService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void executeDailyTask() throws IOException {
        System.out.println("Executing daily sentence save task...");
        saveSentence();
    }

    public void saveSentence() throws IOException {
        String json = makeArticle();

        // 清理掉 `json` 和多余的反引号
        String cleanedJson = cleanJson(json);

        // 解析 cleanedJson
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(cleanedJson);

        DailyArticleShare entity = new DailyArticleShare();
        entity.setTitle(node.get("title").asText());
        entity.setContentEn(node.get("en").asText());
        entity.setContentZh(node.get("cn").asText());
        entity.setCentral(objectMapper.writeValueAsString(node.get("central")));

        entity.setQuestion1(node.get("question1").asText());
        entity.setOptions1(objectMapper.writeValueAsString(node.get("options1"))); // 转成 JSON 字符串
        entity.setAnswer1(node.get("answer1").asText());

        entity.setQuestion2(node.get("question2").asText());
        entity.setOptions2(objectMapper.writeValueAsString(node.get("options2")));
        entity.setAnswer2(node.get("answer2").asText());

        entity.setCreateTime(LocalDateTime.now());

        try {
            String imageUrl = this.imageSynthesis.makeStoryImage(entity.getContentZh());
            entity.setImageUrl(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取当前日期
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        // 根据星期几替换tip
        String voice = this.getVoiceForDay(dayOfWeek);
        byte[] tts = textToSpeechService.tts(entity.getContentEn(), voice);
        InputStream is = new ByteArrayInputStream(tts);
        String url = ossUtil.upload(is, "article.mp3");
        entity.setAudioUrl(url); // 可选：音频

        repository.save(entity);
    }

    public String makeArticle() throws IOException {
        String url = "https://api.deepseek.com/chat/completions";

        // 1. 构造请求体
        Map<String, Object> message1 = new HashMap<>();
        message1.put("role", "system");
        message1.put("content", """
                你是一位兼具英语文学家、语言学家、教育家和作家身份的专业助手。你在英美文学领域拥有深厚的造诣，同时对英语语言的演变、结构和教学法有专业见解。
                你的核心任务是创作优美、地道、富有文学性的英文文本，并以深入浅出的方式解析和教授语言知识。
                【核心要求】
                1.  **语言质量**：你输出的英文应纯正、优雅，符合母语者的用语习惯，并可根据要求体现不同的文体风格（如学术、文学、日常等）。
                2.  **教育洞察**：在解释语言现象或文学作品时，应力求清晰、准确、有启发性，善于类比和举例，避免晦涩难懂的术语堆砌。
                3.  **创作能力**：能够进行文学创作，包括但不限于故事、诗歌、散文等，作品应结构精巧，富有感染力和想象力。
                """);

        // 获取当前日期
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        String word = this.getWordForDay(dayOfWeek);
        // 根据星期几替换tip
        String difficulty = getTipForDay(dayOfWeek);

        Map<String, Object> message2 = new HashMap<>();
        message2.put("role", "user");
        String content = """
                给我写一篇英文小故事
                要求：
                1.一个结构完整、细节丰富的小故事（300~500字）。请在短小的篇幅内，通过具体的场景描绘、简短对话或人物心理活动，让故事更生动。包含该词""" + word +
                """
                2.目标受众是大学生，风格轻松有趣、帮助记忆。
                3.返回英文以及翻译。并出两道测试题包含答案。还有个属性central是重点单词解释。
                4.难度""" + difficulty + """
                6. 返回json字符串
                生成的例句：
                {
                  "title": "The "Brilliant" Thesis"
                  "en": "Persistence is the key to success.",
                  "cn": "坚持是成功的关键。",
                  "central": "Persistence:坚持不懈",
                  "question1": "What is the main message of the quote?",
                  "options1": ["A. Success lasts forever", "B. Failure is the end", "C. Courage matters most", "D. Final results count"],
                  "answer1": "C",
                  "question2": "Who is the quote often attributed to?",
                  "options2": ["A. Einstein", "B. Churchill", "C. Lincoln", "D. Roosevelt"],
                  "answer2": "B",
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
                return "大学英语四级";
            case TUESDAY:
                return "大学英语六级";
            case WEDNESDAY:
                return "大学商务英语";
            case THURSDAY:
                return "雅思阅读";
            case FRIDAY:
                return "GRE阅读";
            case SATURDAY:
                return "托福阅读";
            case SUNDAY:
                return "考研阅读";
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

    private String getWordForDay(DayOfWeek dayOfWeek) throws IOException {
        String word;
        switch (dayOfWeek) {
            case MONDAY:
                word = this.wordService.getWordByPos("CET4_2");
                break;
            case TUESDAY:
                word = this.wordService.getWordByPos("CET6_2");
                break;
            case WEDNESDAY:
                word = this.wordService.getWordByPos("BEC_2");
                break;
            case THURSDAY:
                word = this.wordService.getWordByPos("IELTS_3");
                break;
            case FRIDAY:
                word = this.wordService.getWordByPos("GRE_3");
                break;
            case SATURDAY:
                word = this.wordService.getWordByPos("TOEFL_2");
                break;
            case SUNDAY:
                word = this.wordService.getWordByPos("KaoYan_2");
                break;
            default:
                word = this.wordService.getWordByPos("KaoYan_2");
                break;
        }
        return word;
    }

    private String cleanJson(String rawJson) {
        // 1. 去掉开始的 ` ```json `
        // 2. 去掉结束的 ` ``` `
        rawJson = rawJson.replaceAll("^```json", "").replaceAll("```$", "").trim();
        return rawJson;
    }

    public List<DailyArticleShare> findByCreateTimeBetween(LocalDateTime start, LocalDateTime end) {
        return this.repository.findByCreateTimeBetween(start, end);
    }

    public DailyArticleShare findTopByOrderByCreateTimeDesc() {
        return repository.findTopByOrderByCreateTimeDesc();
    }
}
