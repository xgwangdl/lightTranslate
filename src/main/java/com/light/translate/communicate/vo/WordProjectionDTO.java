package com.light.translate.communicate.vo;

public class WordProjectionDTO {
    private String wordId;
    private String word;
    private String book;

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
}
