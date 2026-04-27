package com.shortvideo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VideoDetailResp {
    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private String filePath;
    private String coverUrl;
    private Long fileSize;
    private Integer duration;
    private String status;
    private String auditStatus;
    private Long views;
    private Long likes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean deleted;
    private Long version;
    private String userName;
}
