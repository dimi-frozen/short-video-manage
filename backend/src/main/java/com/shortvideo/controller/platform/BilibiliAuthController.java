package com.shortvideo.controller.platform;

import com.shortvideo.dto.platform.AuthUrlResp;
import com.shortvideo.service.platform.PlatformServiceRegistry;
import com.shortvideo.utils.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.shortvideo.enums.PlatformType.BILIBILI;

@RestController
@RequestMapping("/api/platforms/bilibili")
public class BilibiliAuthController {

    private final PlatformServiceRegistry registry;

    public BilibiliAuthController(PlatformServiceRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/auth-url")
    public Result<AuthUrlResp> authUrl() {
        return Result.success(registry.get(BILIBILI).getAuthUrl());
    }
}

