package com.light.translate.communicate.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Entity
@Table(name = "words")
public class Word {
    @Id
    @Column(name = "word_id")
    private String wordId;

    @Column(name = "word_rank")
    private Integer wordRank;

    @Column(name = "head_word")
    private String headWord;

    @Column(name = "us_phone")
    private String usPhone;

    @Column(name = "uk_phone")
    private String ukPhone;

    @Column(name = "book_id")
    private String bookId;

    @Column(name = "exam_data", columnDefinition = "json")
    private String examData;

    @Column(name = "syno_data", columnDefinition = "json")
    private String synoData;

    @Column(name = "phrase_data", columnDefinition = "json")
    private String phraseData;

    @Column(name = "rel_word_data", columnDefinition = "json")
    private String relWordData;

    @Column(name = "sentence_data", columnDefinition = "json")
    private String sentenceData;

    @Column(name = "trans_data", columnDefinition = "json")
    private String transData;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;


    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public Integer getWordRank() {
        return wordRank;
    }

    public void setWordRank(Integer wordRank) {
        this.wordRank = wordRank;
    }

    public String getHeadWord() {
        return headWord;
    }

    public void setHeadWord(String headWord) {
        this.headWord = headWord;
    }

    public String getUsPhone() {
        return usPhone;
    }

    public void setUsPhone(String usPhone) {
        this.usPhone = usPhone;
    }

    public String getUkPhone() {
        return ukPhone;
    }

    public void setUkPhone(String ukPhone) {
        this.ukPhone = ukPhone;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getExamData() {
        return examData;
    }

    public void setExamData(String examData) {
        this.examData = examData;
    }

    public String getSynoData() {
        return synoData;
    }

    public void setSynoData(String synoData) {
        this.synoData = synoData;
    }

    public String getPhraseData() {
        return phraseData;
    }

    public void setPhraseData(String phraseData) {
        this.phraseData = phraseData;
    }

    public String getRelWordData() {
        return relWordData;
    }

    public void setRelWordData(String relWordData) {
        this.relWordData = relWordData;
    }

    public String getSentenceData() {
        return sentenceData;
    }

    public void setSentenceData(String sentenceData) {
        this.sentenceData = sentenceData;
    }

    public String getTransData() {
        return transData;
    }

    public void setTransData(String transData) {
        this.transData = transData;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

