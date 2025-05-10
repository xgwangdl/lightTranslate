package com.light.translate.communicate.data;

import jakarta.persistence.*;

@Entity
@Table(name = "examples")
public class Example {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word_id")
    private String wordId;

    @Column(name = "s_content")
    private String sContent;

    @Column(name = "s_cn")
    private String sCn;

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

    public String getsContent() {
        return sContent;
    }

    public void setsContent(String sContent) {
        this.sContent = sContent;
    }

    public String getsCn() {
        return sCn;
    }

    public void setsCn(String sCn) {
        this.sCn = sCn;
    }
}

