package com.light.translate.communicate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckinStatsDTO {
    private String bookId;
    private int streak;
    private int totalReviewed;
    private int dailyTarget;
}
