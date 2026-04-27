package com.shortvideo.dto.analysis;

import lombok.Data;

@Data
public class AnalysisOverviewVO {
    private Long totalVideos;
    private Long totalViews;
    private Long totalLikes;
    private Long totalComments;
    private Long totalShares;
}

