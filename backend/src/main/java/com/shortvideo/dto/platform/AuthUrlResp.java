package com.shortvideo.dto.platform;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthUrlResp {
    private String authUrl;
    private String accessToken;
    private Long expiresInSeconds;
    private LocalDateTime expireAt;
}

