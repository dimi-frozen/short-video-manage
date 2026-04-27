package com.shortvideo.controller;

import com.shortvideo.dto.analysis.AnalysisDashboardVO;
import com.shortvideo.service.AnalysisService;
import com.shortvideo.utils.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/dashboard")
    public Result<AnalysisDashboardVO> dashboard(@RequestParam(required = false) String platformType,
                                                @RequestParam(required = false) String orderBy,
                                                @RequestParam(defaultValue = "10") @Min(1) @Max(50) int topLimit,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(analysisService.dashboard(platformType, orderBy, topLimit, startTime, endTime));
    }
}

