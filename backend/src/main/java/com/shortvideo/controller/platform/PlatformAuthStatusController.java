package com.shortvideo.controller.platform;

import com.shortvideo.dto.platform.PlatformAuthStatusResp;
import com.shortvideo.service.platform.PlatformTokenStore;
import com.shortvideo.utils.Result;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/platforms")
public class PlatformAuthStatusController {

    private final StringRedisTemplate stringRedisTemplate;
    private final PlatformTokenStore platformTokenStore;

    public PlatformAuthStatusController(StringRedisTemplate stringRedisTemplate, PlatformTokenStore platformTokenStore) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.platformTokenStore = platformTokenStore;
    }

    @GetMapping("/{platform}/auth-status")
    public Result<PlatformAuthStatusResp> authStatus(@PathVariable("platform") String platform) {
        String p = platform == null ? "" : platform.trim().toLowerCase(Locale.ROOT);
        String key = redisTokenKey(p);
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("不支持的平台: " + platform);
        }

        String token = null;
        Long ttl = null;
        try {
            token = stringRedisTemplate.opsForValue().get(key);
            ttl = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
        if (!StringUtils.hasText(token)) {
            PlatformTokenStore.TokenInfo info = null;
            try {
                info = platformTokenStore.get(p);
            } catch (Exception ignored) {
            }
            if (info != null) {
                token = info.getToken();
                ttl = info.getExpireAt() == null ? 0L : info.getTtlSeconds();
            }
        }

        PlatformAuthStatusResp resp = new PlatformAuthStatusResp();
        resp.setPlatform(p);
        if (StringUtils.hasText(token)) {
            resp.setStatus("AUTHORIZED");
            long ttlSec = ttl == null || ttl < 0 ? 0L : ttl;
            resp.setExpiresInSeconds(ttlSec);
            resp.setExpireAt(ttlSec > 0 ? LocalDateTime.now().plusSeconds(ttlSec) : null);
        } else {
            resp.setStatus("UNAUTHORIZED");
            resp.setExpiresInSeconds(0L);
            resp.setExpireAt(null);
        }
        return Result.success(resp);
    }

    private String redisTokenKey(String platform) {
        if ("douyin".equals(platform)) {
            return "platform:douyin:access_token";
        }
        if ("kuaishou".equals(platform)) {
            return "platform:kuaishou:access_token";
        }
        if ("bilibili".equals(platform) || "b站".equals(platform)) {
            return "platform:bilibili:access_token";
        }
        return "";
    }
}
