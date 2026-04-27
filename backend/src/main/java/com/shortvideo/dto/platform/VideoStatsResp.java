package com.shortvideo.dto.platform;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoStatsResp {
    private String videoId;
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares;
    private LocalDateTime statTime;
}

