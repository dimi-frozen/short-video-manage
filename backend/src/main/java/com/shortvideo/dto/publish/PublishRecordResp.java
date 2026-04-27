package com.shortvideo.dto.publish;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PublishRecordResp {
    private Long recordId;
    private Long videoId;
    private String platform;
    private String status;
    private String platformVideoUrl;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

