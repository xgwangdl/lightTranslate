package com.light.translate.communicate.services;

import com.light.translate.communicate.data.DailyArticleShare;
import com.light.translate.communicate.dto.SentenceMatchResult;
import com.light.translate.communicate.dto.ShadowReadingResponse;
import com.light.translate.communicate.repository.DailyArticleShareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShadowReadingService {

    private final DailyArticleShareRepository articleRepository;
    private final AliSpeechRecognizer aliSpeechRecognizer;

    public ShadowReadingResponse evaluateShadowReading(LocalDateTime start, LocalDateTime end, File audioFile) throws IOException {
        // 获取原文
        List<DailyArticleShare> articles = articleRepository.findByCreateTimeBetween(start, end);
        String originalText = articles.get(0).getContentEn();

        // Step 1: 调用阿里模型解析音频
        List<String> sentencesFromAudio = splitIntoSentences(aliSpeechRecognizer.recognizeSentences(audioFile));

        // Step 2: 拆分原文为句子
        List<String> originalSentences = splitIntoSentences(originalText);

        // Step 3: 比对
        List<SentenceMatchResult> results = compareSentences(originalSentences, sentencesFromAudio);

        return new ShadowReadingResponse(originalText, sentencesFromAudio, results);
    }

    private List<String> splitIntoSentences(String text) {
        return Arrays.stream(text.split("(?<=[.!?]['”’\"]?)\\s+"))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private List<SentenceMatchResult> compareSentences(List<String> original, List<String> spoken) {
        List<SentenceMatchResult> results = new ArrayList<>();

        for (int i = 0; i < original.size(); i++) {
            String orig = original.get(i);
            String spokenLine = i < spoken.size() ? spoken.get(i) : "";

            double similarity = calculateSimilarity(orig, spokenLine);
            List<String> diffs = calculateDiffWords(orig, spokenLine);

            results.add(new SentenceMatchResult(i + 1, orig, spokenLine, similarity, diffs));
        }

        return results;
    }

    private double calculateSimilarity(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        int maxLength = Math.max(a.length(), b.length());
        if (maxLength == 0) return 1.0;
        return (maxLength - levenshtein(a, b)) / (double) maxLength;
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                                dp[i - 1][j] + 1,
                                dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }

        return dp[a.length()][b.length()];
    }

    private List<String> calculateDiffWords(String a, String b) {
        Set<String> originalWords = new HashSet<>(Arrays.asList(a.toLowerCase().split("\\W+")));
        Set<String> spokenWords = new HashSet<>(Arrays.asList(b.toLowerCase().split("\\W+")));
        originalWords.removeAll(spokenWords);
        return new ArrayList<>(originalWords);
    }
}
