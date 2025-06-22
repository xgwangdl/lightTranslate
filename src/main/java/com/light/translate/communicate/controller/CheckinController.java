package com.light.translate.communicate.controller;

import com.light.translate.communicate.services.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dict/checkin")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    /**
     * 获取用户今日打卡数据
     */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayCheckin(@RequestParam String openid,@RequestParam String bookid) {
        return ResponseEntity.ok(checkinService.getTodayCheckin(openid,bookid));
    }

    /**
     * 获取用户总览统计
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getUserOverview(@RequestParam String openid,@RequestParam String bookid) {
        return ResponseEntity.ok(checkinService.getUserOverview(openid,bookid));
    }

    /**
     * 获取最近 7 天的打卡历史
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam String openid, @RequestParam String bookid) {
        return ResponseEntity.ok(checkinService.getCheckinHistory(openid,bookid));
    }

    @GetMapping("/wordbook")
    public ResponseEntity<?> getWordbookStats(@RequestParam String openid, @RequestParam String bookid) {
        return ResponseEntity.ok(checkinService.getWordbookStats(openid, bookid));
    }

    @GetMapping("/task")
    public ResponseEntity<?> getDailyTask(
            @RequestParam String openid,
            @RequestParam String bookid
    ) {
        return ResponseEntity.ok(checkinService.getTodayTaskStats(openid, bookid));
    }

}

