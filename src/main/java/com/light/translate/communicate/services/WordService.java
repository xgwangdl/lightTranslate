package com.light.translate.communicate.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.light.translate.communicate.data.Translation;
import com.light.translate.communicate.data.Word;
import com.light.translate.communicate.repository.ExampleRepository;
import com.light.translate.communicate.repository.TranslationRepository;
import com.light.translate.communicate.repository.WordRepository;
import com.light.translate.communicate.vo.WordDTO;
import com.light.translate.communicate.vo.WordProjectionDTO;
import com.light.translate.communicate.vo.WordsDetailDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
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

    public WordDTO getWord(String word) {
        if (wordRepository.findByHeadWord(word).isEmpty()) {
            return null;
        } else {
            WordDTO wordDTO = new WordDTO();
            Word wordOptional = wordRepository.findByHeadWord(word).stream().findFirst().get();
            List<Translation> translation = this.translationRepository.findByWordId(wordOptional.getWordId());
            wordDTO.setWordId(wordOptional.getWordId());
            wordDTO.setHeadWord(wordOptional.getHeadWord());
            wordDTO.setWordRank(wordOptional.getWordRank());
            wordDTO.setUkPhone(wordOptional.getUkPhone());
            wordDTO.setUsPhone(wordOptional.getUsPhone());
            wordDTO.setTranslations(translation);
            return wordDTO;
        }
    }

    public List<WordDTO> getRecommendWords(String word) throws IOException {
        List<WordDTO> wordDTOList = new ArrayList<>();
        if (wordRepository.findByHeadWord(word).isEmpty()) {
            return wordDTOList;
        } else {
            Word wordOptional = wordRepository.findByHeadWord(word).stream().findFirst().get();
            ObjectMapper mapper = new ObjectMapper();
            List<WordsDetailDTO.Syno> synoData = parseSynoData(wordOptional.getSynoData(), mapper);
            synoData.forEach(syno -> {
                syno.getHwds().forEach(hwd -> {
                    List<Word> byHeadWord = wordRepository.findByHeadWord(hwd.getW());
                    if (!byHeadWord.isEmpty()) {
                        Word word1 = byHeadWord.stream().findFirst().get();
                        List<Translation> translation = this.translationRepository.findByWordId(word1.getWordId());
                        WordDTO wordDTO = new WordDTO();
                        wordDTO.setWordId(word1.getWordId());
                        wordDTO.setHeadWord(word1.getHeadWord());
                        wordDTO.setWordRank(word1.getWordRank());
                        wordDTO.setUkPhone(word1.getUkPhone());
                        wordDTO.setUsPhone(word1.getUsPhone());
                        wordDTO.setTranslations(translation);
                        wordDTOList.add(wordDTO);
                    }
                });
            });
            return wordDTOList;
        }
    }

    public List<WordProjectionDTO> searchWords(String query) {
        Pageable topTen = PageRequest.of(0, 6); // 第一页，每页10条
        return wordRepository.findGroupedWordProjections(query, topTen).getContent();
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

