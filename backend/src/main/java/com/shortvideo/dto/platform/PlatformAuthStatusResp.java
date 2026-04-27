package com.shortvideo.dto.platform;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlatformAuthStatusResp {
    private String platform;
    private String status;
    private Long expiresInSeconds;
    private LocalDateTime expireAt;
}

