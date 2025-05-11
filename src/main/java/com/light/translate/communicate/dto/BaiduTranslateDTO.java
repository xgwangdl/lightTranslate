package com.light.translate.communicate.dto;

import java.util.List;

public class BaiduTranslateDTO {
    private String from;
    private String to;
    private List<TranslationResult> trans_result;
    private Integer error_code;
    private String error_msg;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<TranslationResult> getTrans_result() {
        return trans_result;
    }

    public void setTrans_result(List<TranslationResult> trans_result) {
        this.trans_result = trans_result;
    }

    public Integer getError_code() {
        return error_code;
    }

    public void setError_code(Integer error_code) {
        this.error_code = error_code;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }
}
