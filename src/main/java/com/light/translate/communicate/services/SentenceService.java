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
            String imageUrl = this.imageSynthesis.makeSentenceImage(sentence.getCn());
            sentence.setImageUrl(imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 保存到数据库
        sentenceRepository.save(sentence);
    }

    private String makeSentence() throws JsonProcessingException {
        List<String> cetWords = Arrays.asList(
                "abandon", "abolish", "absorb", "abstract", "abundant", "accelerate", "access", "accommodate", "accompany", "accomplish",
                "account", "accumulate", "accurate", "accuse", "acknowledge", "acquire", "activate", "adapt", "addict", "address",
                "adequate", "adjust", "administer", "admire", "adopt", "adore", "advance", "advertise", "advocate", "afford", "agency",
                "aggressive", "aid", "aim", "aircraft", "allocate", "alter", "alternative", "amateur", "amaze", "ambiguous",
                "ambition", "amount", "amplify", "amuse", "analyse", "analyze", "ancestor", "annual", "anticipate", "anxiety",
                "apologize", "apparent", "appeal", "appetite", "apply", "appoint", "appreciate", "approach", "appropriate", "approve", "arbitrary",
                "architect", "arise", "arrange", "arrest", "artificial", "ashamed", "aspect", "assemble", "assert", "assess",
                "assign", "assist", "associate", "assume", "assure", "astonish", "attach", "attain", "attempt", "attend",
                "attract", "attribute", "author", "automatic", "aware", "awkward", "balance", "ban", "barrier", "behave",
                "belief", "belong", "beneficial", "benefit", "betray", "bias", "bind", "biography", "blame", "boost",
                "border", "borrow", "bother", "brand", "brief", "broadcast", "burden", "calculate", "capture", "capacity",
                "capable", "cease", "ceremony", "chain", "challenge", "champion", "characteristic", "charity", "cherish", "chief",
                "circumstance", "claim", "clarify", "classify", "client", "collapse", "colleague", "comfort", "command", "commence",
                "comment", "commercial", "commit", "communicate", "compare", "compete", "compile", "complement", "complicate", "component",
                "compose", "comprehend", "conceal", "concentrate", "concept", "concern", "conclude", "concrete", "condemn", "conduct",
                "conference", "confess", "confidence", "confirm", "conflict", "confront", "confuse", "congratulate", "connect", "conquer",
                "conscious", "consent", "consequence", "conservative", "considerable", "consist", "consistent", "constant", "construct", "consult",
                "consume", "contact", "contain", "contemporary", "contend", "content", "contest", "continue", "contract", "contribute",
                "controversy", "convenience", "convey", "convince", "cooperate", "coordinate", "core", "corporate", "correspond", "corrupt",
                "cost", "council", "counsel", "create", "creature", "credit", "crew", "crisis", "critic", "crucial",
                "cultivate", "cure", "current", "curve", "damage", "deal", "debate", "decade", "deceive", "declare",
                "decline", "decorate", "decrease", "dedicate", "defeat", "defend", "define", "delay", "deliberate", "deliver",
                "demand", "demonstrate", "deny", "depart", "depend", "depict", "deposit", "depress", "derive", "descend",
                "describe", "deserve", "desire", "despair", "desperate", "destroy", "detect", "determine", "develop", "devote",
                "differ", "digest", "diminish", "dine", "direct", "disappear", "disappoint", "disaster", "discipline", "disclose",
                "discount", "discover", "discriminate", "discuss", "dismiss", "display", "dispose", "dissolve", "distinct", "distinguish",
                "distract", "distribute", "disturb", "diverse", "divide", "document", "domestic", "dominate", "donate", "doubt",
                "draft", "dramatic", "drastic", "draw", "drill", "due", "duration", "dynamic", "eager", "earn",
                "ease", "elaborate", "elect", "eliminate", "emerge", "emphasis", "employ", "enable", "encounter", "encourage",
                "endanger", "engage", "enhance", "enjoy", "enlighten", "ensure", "entertain", "enthusiasm", "entire", "entitle"
        );

        Random random = new Random();
        String randomWord = cetWords.get(random.nextInt(cetWords.size()));

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
                1.必须使用给定的一个单词:""" + randomWord + """
                2.长度<30单词
                3.提供对应的中文翻译。
                4.解释句中涉及的一个语法点。
                5.""" + tip + """
                6.句子需贴近现实生活或励志主题，但富有画面感。
                7. 返回标准 JSON 字符串，键为：
                   - "en": 英文句子
                   - "cn": 中文翻译
                   - "tip": 语法点解释
                   - "word": "目标高频词 + 音标 + 词性 + 中文释义"
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
