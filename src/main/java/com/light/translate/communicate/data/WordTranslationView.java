package com.light.translate.communicate.data;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.Immutable;

@Entity
@Immutable  // 表示只读
@Table(name = "word_translation_view_cache")
public class WordTranslationView {

    @Id
    private String wordId;  // word_Id 是唯一键

    private String headWord;

    private String tranCn;

    private String pos;

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public String getHeadWord() {
        return headWord;
    }

    public void setHeadWord(String headWord) {
        this.headWord = headWord;
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

