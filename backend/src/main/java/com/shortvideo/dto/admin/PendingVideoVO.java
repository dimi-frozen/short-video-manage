package com.shortvideo.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PendingVideoVO {
    private Long id;
    private String title;
    private String description;
    private String tags;
    private String fileUrl;
    private String coverUrl;
    private Long fileSize;
    private String transcodeStatus;
    private String auditStatus;
    private LocalDateTime createTime;
}

