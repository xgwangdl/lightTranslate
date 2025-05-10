package com.light.translate.communicate.data;

import jakarta.persistence.*;

@Entity
@Table(name = "translations")
public class Translation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String wordId;
    private String pos;
    private String tranCn;
    private String tranOther;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getTranCn() {
        return tranCn;
    }

    public void setTranCn(String tranCn) {
        this.tranCn = tranCn;
    }

    public String getTranOther() {
        return tranOther;
    }

    public void setTranOther(String tranOther) {
        this.tranOther = tranOther;
    }
}

