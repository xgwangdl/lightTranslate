package com.light.translate.communicate.vo;

public class TranslateVO {
    String audioData;
    String audioUrl;
    String originText;
    String translateText;

    public String getAudioData() {
        return audioData;
    }

    public void setAudioData(String audioData) {
        this.audioData = audioData;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getOriginText() {
        return originText;
    }

    public void setOriginText(String originText) {
        this.originText = originText;
    }

    public String getTranslateText() {
        return translateText;
    }

    public void setTranslateText(String translateText) {
        this.translateText = translateText;
    }
}
