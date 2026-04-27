package com.shortvideo.service;

import com.shortvideo.dto.analysis.AnalysisDashboardVO;

import java.time.LocalDateTime;

public interface AnalysisService {
    AnalysisDashboardVO dashboard(String platformType, String orderBy, int topLimit, LocalDateTime startTime, LocalDateTime endTime);
}

