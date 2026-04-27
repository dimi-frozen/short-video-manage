package com.shortvideo.dto.platform;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoListItemResp {
    private String videoId;
    private String title;
    private LocalDateTime publishTime;
    private Long views;
    private Long likes;
    private Long comments;
    private Long shares;
}

