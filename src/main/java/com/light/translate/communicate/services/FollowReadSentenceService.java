package com.light.translate.communicate.services;

import com.light.translate.communicate.data.FollowReadSentence;
import com.light.translate.communicate.repository.FollowReadSentenceRepository;
import com.light.translate.communicate.translate.TextToSpeechService;
import com.light.translate.communicate.utils.OssUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class FollowReadSentenceService {

    @Autowired
    private OssUtil ossUtil;

    @Autowired
    private FollowReadSentenceRepository followReadSentenceRepository;

    @Autowired
    private TextToSpeechService textToSpeechService;

    private List<String> voices = List.of("en-US-JennyNeural", "en-US-GuyNeural", "en-CA-ClaraNeural", "en-GB-LibbyNeural", "en-GB-RyanNeural", "en-GB-SoniaNeural", "en-US-AriaNeural");
    public void makeSentence() {
        Random random = new Random();

        List<FollowReadSentence> sentences = followReadSentenceRepository.findTop200ByAudioUrlIsNull();
        for (FollowReadSentence sentence : sentences) {
            String voice = voices.get(random.nextInt(voices.size()));
            byte[] tts = textToSpeechService.tts(sentence.getSentence(), voice);
            InputStream is = new ByteArrayInputStream(tts);
            String url = ossUtil.upload2(is, "word.mp3");
            sentence.setAudioUrl(url);
        }
        followReadSentenceRepository.saveAll(sentences);
    }

    public List<FollowReadSentence> getSentencesBybookId(String bookId) {
        List<FollowReadSentence> sentences = followReadSentenceRepository.findBybookId(bookId);
        return sentences;
    }

    public Page<FollowReadSentence> getFollowReadSentences(String bookId, int pageNum, int pageSize) {
        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("id").descending());
        Page<FollowReadSentence> sentences = followReadSentenceRepository.findByBookIdAndAudioUrlIsNotNull(bookId, pageable);
        sentences.getContent().forEach(sentence -> {
            sentence.setAudioUrl(ossUtil.getUrl(sentence.getAudioUrl()));
        });
        return sentences;
    }

    public List<FollowReadSentence> getRandomFiveSentences(String bookId) {
        List<FollowReadSentence> sentences = followReadSentenceRepository.findRandomFiveByBookId(bookId);
        sentences.forEach(sentence -> {
            sentence.setAudioUrl(ossUtil.getUrl(sentence.getAudioUrl()));
        });
        return sentences;
    }
}
