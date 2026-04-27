package com.shortvideo.controller.platform;

import com.shortvideo.dto.platform.AuthUrlResp;
import com.shortvideo.service.platform.PlatformServiceRegistry;
import com.shortvideo.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.shortvideo.enums.PlatformType.DOUYIN;

@RestController
@RequestMapping("/api/platforms/douyin")
public class DouyinAuthController {

    private final PlatformServiceRegistry registry;

    public DouyinAuthController(PlatformServiceRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/auth-url")
    public Result<AuthUrlResp> authUrl() {
        return Result.success(registry.get(DOUYIN).getAuthUrl());
    }
}

