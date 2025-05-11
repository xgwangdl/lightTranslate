package com.light.translate.communicate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.light.translate.communicate.data.Example;
import com.light.translate.communicate.data.Translation;
import lombok.Data;

import java.util.List;

@Data
public class WordsDetailDTO {
    private String wordId;
    private Integer wordRank;
    private String headWord;
    private String usPhone;
    private String ukPhone;

    private List<Translation> translations;
    private List<Example> examples;

    private List<Exam> examData;
    private List<Phrase> phraseData;
    private List<Syno> synoData;
    private List<RelWord> relWordData;
    private List<Sentence> sentenceData;
    private List<Trans> transData;

    @Data
    public static class Exam {
        private String question;
        private Answer answer;
        private Integer examType;
        private List<Choice> choices;

        @Data
        public static class Answer {
            private String explain;
            private Integer rightIndex;
        }

        @Data
        public static class Choice {
            private Integer choiceIndex;
            private String choice;
        }
    }

    @Data
    public static class Phrase {
        @JsonProperty("pContent")
        private String pContent;
        @JsonProperty("pCn")
        private String pCn;
    }

    @Data
    public static class Syno {
        private String pos;
        private String tran;
        private List<Hwd> hwds;
    }

    @Data
    public static class Hwd {
        private String w;
    }

    @Data
    public static class RelWord {
        private String pos;
        private List<Rel> words;

        @Data
        public static class Rel {
            private String hwd;
            private String tran;
        }
    }

    @Data
    public static class Sentence {
        @JsonProperty("sContent")
        private String sContent;
        @JsonProperty("sCn")
        private String sCn;
    }

    @Data
    public static class Trans {
        private String pos;
        @JsonProperty("descCn")
        private String descCn;
        @JsonProperty("tranCn")
        private String tranCn;
        @JsonProperty("tranOther")
        private String tranOther;
        @JsonProperty("descOther")
        private String descOther;
    }
}

