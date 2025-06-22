package com.light.translate.communicate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodayCheckinDTO {
    private boolean checkedIn;
    private int wordCount;
    private int totalDays;
    private int streak;
    private int totalPoints;
}
