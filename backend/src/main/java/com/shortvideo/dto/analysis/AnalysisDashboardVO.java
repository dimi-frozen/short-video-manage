package com.shortvideo.dto.analysis;

import lombok.Data;

import java.util.List;

@Data
public class AnalysisDashboardVO {
    private AnalysisOverviewVO overview;
    private List<AnalysisPlatformAggVO> platformAgg;
    private List<AnalysisTopVideoVO> topVideos;
    private List<AnalysisTrendPointVO> trend;
}

