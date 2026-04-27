package com.shortvideo.service.platform;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlatformTokenStore {
    private final Map<String, TokenInfo> map = new ConcurrentHashMap<>();

    public void put(String platform, String token, long ttlSeconds) {
        if (!StringUtils.hasText(platform) || !StringUtils.hasText(token)) {
            return;
        }
        String p = platform.trim().toLowerCase(Locale.ROOT);
        LocalDateTime expireAt = ttlSeconds > 0 ? LocalDateTime.now().plusSeconds(ttlSeconds) : null;
        map.put(p, new TokenInfo(token.trim(), ttlSeconds, expireAt));
    }

    public TokenInfo get(String platform) {
        if (!StringUtils.hasText(platform)) {
            return null;
        }
        String p = platform.trim().toLowerCase(Locale.ROOT);
        TokenInfo info = map.get(p);
        if (info == null) {
            return null;
        }
        if (info.expireAt != null && info.expireAt.isBefore(LocalDateTime.now())) {
            map.remove(p);
            return null;
        }
        return info;
    }

    public static class TokenInfo {
        private final String token;
        private final long ttlSeconds;
        private final LocalDateTime expireAt;

        public TokenInfo(String token, long ttlSeconds, LocalDateTime expireAt) {
            this.token = token;
            this.ttlSeconds = ttlSeconds;
            this.expireAt = expireAt;
        }

        public String getToken() {
            return token;
        }

        public long getTtlSeconds() {
            return ttlSeconds;
        }

        public LocalDateTime getExpireAt() {
            return expireAt;
        }
    }
}

