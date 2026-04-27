package com.shortvideo.dto.analysis;

import lombok.Data;

@Data
public class AnalysisTrendPointVO {
    private String day;
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares;
}

