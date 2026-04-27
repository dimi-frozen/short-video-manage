package com.shortvideo.dto.platform;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublishResp {
    private String videoId;
    private String title;
    private LocalDateTime publishTime;
    private String status;
    private String platformVideoUrl;
}
