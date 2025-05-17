package com.light.translate.communicate.dto;

public class UserCollectionDTO {
    private Long id;
    private String openid;
    private String wordId;
    private String category;
    private String headWord;
    private String tranCn;
    private String pos;

    public UserCollectionDTO(Long id, String openid, String wordId, String category, String headWord, String tranCn, String pos) {
        this.id = id;
        this.openid = openid;
        this.wordId = wordId;
        this.category = category;
        this.headWord = headWord;
        this.tranCn = tranCn;
        this.pos = pos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
