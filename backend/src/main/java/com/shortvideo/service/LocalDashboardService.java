package com.shortvideo.service;

import com.shortvideo.dto.analysis.LocalDashboardVO;

import java.time.LocalDateTime;

public interface LocalDashboardService {
    /**
     * 获取本平台 Dashboard 数据
     */
    LocalDashboardVO dashboard(LocalDateTime startTime, LocalDateTime endTime);
}
