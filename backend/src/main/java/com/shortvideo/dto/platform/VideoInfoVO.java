package com.shortvideo.dto.platform;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoInfoVO {
    private String videoId;
    private String title;
    private LocalDateTime publishTime;
    private String platformType;
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

