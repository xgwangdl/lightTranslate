package com.light.translate.communicate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckinResult {
    private boolean success;
    private String message;
    private int streak;
    private int rewardPoints;
    private int totalPoints;
}