package com.light.translate.communicate.controller;

import com.light.translate.communicate.dto.ShadowReadingResponse;
import com.light.translate.communicate.services.ShadowReadingService;
import com.light.translate.communicate.translate.AudioConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/dict/shadow")
@RequiredArgsConstructor
public class ShadowReadingController {

    private final ShadowReadingService shadowReadingService;

    @PostMapping("/evaluate")
    public ResponseEntity<ShadowReadingResponse> evaluateShadowReading(
            @RequestParam("date") String date,
            @RequestPart("audio") MultipartFile audioFile) throws IOException, InterruptedException {

        // 将字符串日期转换为 LocalDate
        LocalDate localDate = LocalDate.parse(date);

        // 获取当天的开始时间（00:00:00）
        LocalDateTime startOfDay = localDate.atStartOfDay();

        // 获取当天的结束时间（23:59:59.999）
        LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);
        // 保存临时文件
        File tempFile = File.createTempFile("upload", ".wav");
        audioFile.transferTo(tempFile);
        File convertFile = AudioConverter.convertToWav(tempFile);

        ShadowReadingResponse response = shadowReadingService.evaluateShadowReading(startOfDay, endOfDay, convertFile);
        return ResponseEntity.ok(response);
    }

}
