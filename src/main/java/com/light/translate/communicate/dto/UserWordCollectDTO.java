package com.light.translate.communicate.dto;

import com.light.translate.communicate.data.UserCollection;

import java.time.LocalDateTime;

public class UserWordCollectDTO {

    private Long id;
    private String openid;
    private String wordId;
    private String category;
    private LocalDateTime createTime;

    private String headWord;
    private String tranCn;
    private String pos;

    public UserWordCollectDTO(UserCollection entity) {
        this.id = entity.getId();
        this.openid = entity.getOpenid();
        this.wordId = entity.getWordId();
        this.category = entity.getCategory();
        this.createTime = entity.getCreateTime();

        if (entity.getWord() != null) {
            this.headWord = entity.getWord().getHeadWord();
            this.tranCn = entity.getWord().getTranCn();
            this.pos = entity.getWord().getPos();
        }
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
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

