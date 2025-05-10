package com.light.translate.communicate.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.translate.communicate.data.Word;
import com.light.translate.communicate.repository.ExampleRepository;
import com.light.translate.communicate.repository.TranslationRepository;
import com.light.translate.communicate.repository.WordRepository;
import com.light.translate.communicate.vo.WordsDetailDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class WordService {

    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private ExampleRepository exampleRepository;

    public Optional<Word> getWordById(String wordId) {
        return wordRepository.findById(wordId);
    }

    public Optional<Word> getWord(String word) {
        if (wordRepository.findByHeadWord(word).isEmpty()) {
            return Optional.empty();
        } else {
            return wordRepository.findByHeadWord(word).stream().findFirst();
        }
    }

    public List<Word> searchWords(String query) {
        return wordRepository.findByHeadWordStartingWith(query);
    }


    public WordsDetailDTO getWordDetail(String wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new RuntimeException("Word not found"));

        WordsDetailDTO dto = new WordsDetailDTO();
        BeanUtils.copyProperties(word, dto);

        dto.setTranslations(translationRepository.findByWordId(wordId));
        dto.setExamples(exampleRepository.findByWordId(wordId));

        ObjectMapper mapper = new ObjectMapper();
        try {
            dto.setExamData(parseList(word.getExamData(), new TypeReference<List<WordsDetailDTO.Exam>>() {}, mapper));
            dto.setPhraseData(parseData(word.getPhraseData(), mapper));
            dto.setSynoData(parseSynoData(word.getSynoData(), mapper));
            dto.setRelWordData(parseRelWordData(word.getRelWordData(), mapper));
            dto.setSentenceData(parseSentenceData(word.getSentenceData(), mapper));
            dto.setTransData(parseList(word.getTransData(), new TypeReference<List<WordsDetailDTO.Trans>>() {}, mapper));
        } catch (IOException e) {
            throw new RuntimeException("JSON parse error", e);
        }

        return dto;
    }

    private <T> List<T> parseList(String json, TypeReference<List<T>> type, ObjectMapper mapper) throws IOException {
        return StringUtils.hasText(json) ? mapper.readValue(json, type) : Collections.emptyList();
    }

    private List<WordsDetailDTO.Phrase> parseData(String json, ObjectMapper mapper) throws IOException {
        if (!StringUtils.hasText(json)) return Collections.emptyList();
        JsonNode root = mapper.readTree(json).get("phrases");
        return mapper.convertValue(root, new TypeReference<List<WordsDetailDTO.Phrase>>() {});
    }

    private List<WordsDetailDTO.Syno> parseSynoData(String json, ObjectMapper mapper) throws IOException {
        if (!StringUtils.hasText(json)) return Collections.emptyList();
        JsonNode root = mapper.readTree(json).get("synos");
        return mapper.convertValue(root, new TypeReference<List<WordsDetailDTO.Syno>>() {});
    }

    private List<WordsDetailDTO.RelWord> parseRelWordData(String json, ObjectMapper mapper) throws IOException {
        if (!StringUtils.hasText(json)) return Collections.emptyList();
        JsonNode root = mapper.readTree(json).get("rels");
        return mapper.convertValue(root, new TypeReference<List<WordsDetailDTO.RelWord>>() {});
    }

    private List<WordsDetailDTO.Sentence> parseSentenceData(String json, ObjectMapper mapper) throws IOException {
        if (!StringUtils.hasText(json)) return Collections.emptyList();
        JsonNode root = mapper.readTree(json).get("sentences");
        return mapper.convertValue(root, new TypeReference<List<WordsDetailDTO.Sentence>>() {});
    }

}

