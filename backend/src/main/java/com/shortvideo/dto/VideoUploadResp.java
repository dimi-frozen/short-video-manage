package com.shortvideo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoUploadResp {
    private Long videoId;
    private String title;
    private LocalDateTime uploadTime;
    private String fileUrl;
    private String coverUrl;
}
