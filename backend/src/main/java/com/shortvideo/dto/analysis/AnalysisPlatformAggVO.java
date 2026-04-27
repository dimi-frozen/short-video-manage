package com.shortvideo.dto.analysis;

import lombok.Data;

@Data
public class AnalysisPlatformAggVO {
    private String platformType;
    private Long videoCount;
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares;
}

