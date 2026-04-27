package com.shortvideo.controller.platform;

import com.alibaba.fastjson.JSONObject;
import com.shortvideo.dto.platform.AuthUrlResp;
import com.shortvideo.service.platform.PlatformServiceRegistry;
import com.shortvideo.utils.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.shortvideo.enums.PlatformType.KUAISHOU;

@RestController
@RequestMapping("/api/platforms/kuaishou")
public class KuaishouAuthController {

    private final PlatformServiceRegistry registry;
    private final StringRedisTemplate stringRedisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${platform.kuaishou.mode:mock}")
    private String mode;

    @Value("${platform.kuaishou.token-ttl-seconds:7200}")
    private long tokenTtlSeconds;

    @Value("${platform.kuaishou.fixed-access-token:kuaishou_mock_access_token}")
    private String fixedAccessToken;

    @Value("${platform.kuaishou.oauth.token-url:}")
    private String tokenUrl;

    @Value("${platform.kuaishou.client-id:}")
    private String clientId;

    @Value("${platform.kuaishou.client-secret:}")
    private String clientSecret;

    @Value("${platform.kuaishou.redirect-uri:}")
    private String redirectUri;

    public KuaishouAuthController(PlatformServiceRegistry registry, StringRedisTemplate stringRedisTemplate) {
        this.registry = registry;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @GetMapping("/auth-url")
    public Result<AuthUrlResp> authUrl() {
        return Result.success(registry.get(KUAISHOU).getAuthUrl());
    }

    @GetMapping("/oauth/callback")
    public Result<AuthUrlResp> callback(@RequestParam(value = "code", required = false) String code,
                                        @RequestParam(value = "state", required = false) String state) {
        if (!isSandboxMode()) {
            AuthUrlResp resp = new AuthUrlResp();
            resp.setAuthUrl("");
            resp.setAccessToken(fixedAccessToken);
            resp.setExpiresInSeconds(tokenTtlSeconds);
            resp.setExpireAt(LocalDateTime.now().plusSeconds(tokenTtlSeconds));
            storeAccessToken(resp.getAccessToken(), tokenTtlSeconds);
            return Result.success(resp);
        }

        if (StringUtils.hasText(state)) {
            String stateKey = redisStateKey(state.trim());
            String ok = null;
            try {
                ok = stringRedisTemplate.opsForValue().get(stateKey);
            } catch (Exception ignored) {
            }
            if (!StringUtils.hasText(ok)) {
                throw new IllegalArgumentException("state无效或已过期，请重新发起授权");
            }
        }

        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("缺少code");
        }

        if (!StringUtils.hasText(tokenUrl) || !StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret) || !StringUtils.hasText(redirectUri)) {
            AuthUrlResp resp = new AuthUrlResp();
            resp.setAuthUrl("");
            resp.setAccessToken(fixedAccessToken);
            resp.setExpiresInSeconds(tokenTtlSeconds);
            resp.setExpireAt(LocalDateTime.now().plusSeconds(tokenTtlSeconds));
            storeAccessToken(resp.getAccessToken(), tokenTtlSeconds);
            return Result.success(resp);
        }

        TokenResult tr = exchangeToken(code.trim());
        storeAccessToken(tr.accessToken, tr.expiresInSeconds);

        AuthUrlResp resp = new AuthUrlResp();
        resp.setAuthUrl("");
        resp.setAccessToken(tr.accessToken);
        resp.setExpiresInSeconds(tr.expiresInSeconds);
        resp.setExpireAt(LocalDateTime.now().plusSeconds(tr.expiresInSeconds));
        return Result.success(resp);
    }

    private boolean isSandboxMode() {
        return "sandbox".equalsIgnoreCase(mode) || "real".equalsIgnoreCase(mode);
    }

    private String redisTokenKey() {
        return "platform:kuaishou:access_token";
    }

    private String redisStateKey(String state) {
        return "platform:kuaishou:oauth_state:" + state;
    }

    private void storeAccessToken(String token, long expiresInSeconds) {
        try {
            stringRedisTemplate.opsForValue().set(redisTokenKey(), token, expiresInSeconds, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    private TokenResult exchangeToken(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("code", code);
            body.add("grant_type", "authorization_code");
            body.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);
            String raw = resp.getBody();
            if (!StringUtils.hasText(raw)) {
                throw new RuntimeException("token响应为空");
            }

            JSONObject obj = JSONObject.parseObject(raw);
            String accessToken = obj.getString("access_token");
            Long expiresIn = obj.getLong("expires_in");
            if (!StringUtils.hasText(accessToken)) {
                JSONObject data = obj.getJSONObject("data");
                if (data != null) {
                    accessToken = data.getString("access_token");
                    if (expiresIn == null) {
                        expiresIn = data.getLong("expires_in");
                    }
                }
            }
            if (!StringUtils.hasText(accessToken)) {
                throw new RuntimeException("未获取到access_token");
            }
            long ttl = expiresIn == null || expiresIn <= 0 ? tokenTtlSeconds : expiresIn;
            return new TokenResult(accessToken.trim(), ttl);
        } catch (Exception e) {
            return new TokenResult(fixedAccessToken, tokenTtlSeconds);
        }
    }

    private static class TokenResult {
        private final String accessToken;
        private final long expiresInSeconds;

        private TokenResult(String accessToken, long expiresInSeconds) {
            this.accessToken = accessToken;
            this.expiresInSeconds = expiresInSeconds;
        }
    }
}

