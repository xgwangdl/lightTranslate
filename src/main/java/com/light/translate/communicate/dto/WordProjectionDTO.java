package com.light.translate.communicate.dto;

public class WordProjectionDTO {
    private String wordId;
    private String word;
    private String book;
    private String tranCn;
    private String pos;

    public WordProjectionDTO(String wordId, String word, String book) {
        this.wordId = wordId;
        this.word = word;
        this.book = book;
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getTranCn() {
        return tranCn;
    }

    public void setTranCn(String tranCn) {
        this.tranCn = tranCn;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }
}
