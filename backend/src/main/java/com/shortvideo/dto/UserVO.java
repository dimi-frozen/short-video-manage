package com.shortvideo.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户信息展示VO
 */
@Data
public class UserVO {
    private Long id;
    private String email;
    private String name;
    private String role;
    private String avatarUrl;
    private LocalDateTime createdAt;
}
