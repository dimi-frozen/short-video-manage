package com.shortvideo.dto.analysis;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnalysisTopVideoVO {
    private String videoId;
    private String title;
    private String platformType;
    private LocalDateTime publishTime;
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares;
}

