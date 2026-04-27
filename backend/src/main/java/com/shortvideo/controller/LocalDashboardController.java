package com.shortvideo.controller;

import com.shortvideo.dto.analysis.LocalDashboardVO;
import com.shortvideo.service.LocalDashboardService;
import com.shortvideo.utils.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 本平台 Dashboard API
 */
@RestController
@RequestMapping("/api/local-dashboard")
public class LocalDashboardController {

    private final LocalDashboardService localDashboardService;

    public LocalDashboardController(LocalDashboardService localDashboardService) {
        this.localDashboardService = localDashboardService;
    }

    /**
     * 获取本平台 Dashboard 数据
     */
    @GetMapping("/dashboard")
    public Result<LocalDashboardVO> dashboard(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(localDashboardService.dashboard(startTime, endTime));
    }
}
