package com.shortvideo.service.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortvideo.dto.platform.AuthUrlResp;
import com.shortvideo.dto.platform.PublishResp;
import com.shortvideo.dto.platform.VideoListItemResp;
import com.shortvideo.dto.platform.VideoStatsResp;
import com.shortvideo.entity.VideoInfo;
import com.shortvideo.enums.PlatformType;
import com.shortvideo.service.VideoInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class KuaishouVideoServiceImpl implements VideoPlatformService {

    private final VideoInfoService videoInfoService;
    private final StringRedisTemplate stringRedisTemplate;
    private final PlatformTokenStore platformTokenStore;

    @Value("${platform.kuaishou.mode:mock}")
    private String mode;

    @Value("${platform.kuaishou.token-ttl-seconds:7200}")
    private long tokenTtlSeconds;

    @Value("${platform.kuaishou.fixed-access-token:kuaishou_mock_access_token}")
    private String fixedAccessToken;

    @Value("${platform.kuaishou.upload-base-dir:${user.home}/short-video/upload/kuaishou}")
    private String uploadBaseDir;

    @Value("${platform.kuaishou.oauth.authorize-url:}")
    private String authorizeUrl;

    @Value("${platform.kuaishou.client-id:}")
    private String clientId;

    @Value("${platform.kuaishou.redirect-uri:}")
    private String redirectUri;

    @Value("${platform.kuaishou.scope:}")
    private String scope;

    public KuaishouVideoServiceImpl(VideoInfoService videoInfoService, StringRedisTemplate stringRedisTemplate, PlatformTokenStore platformTokenStore) {
        this.videoInfoService = videoInfoService;
        this.stringRedisTemplate = stringRedisTemplate;
        this.platformTokenStore = platformTokenStore;
    }

    @Override
    public PlatformType platformType() {
        return PlatformType.KUAISHOU;
    }

    @Override
    public AuthUrlResp getAuthUrl() {
        if (isSandboxMode() && StringUtils.hasText(authorizeUrl) && StringUtils.hasText(clientId) && StringUtils.hasText(redirectUri)) {
            String state = UUID.randomUUID().toString().replace("-", "");
            try {
                stringRedisTemplate.opsForValue().set(redisStateKey(state), "1", 10, TimeUnit.MINUTES);
            } catch (Exception ignored) {
            }

            String url = authorizeUrl;
            String sep = url.contains("?") ? "&" : "?";
            url = url + sep +
                    "client_id=" + urlEncode(clientId) +
                    "&response_type=code" +
                    "&redirect_uri=" + urlEncode(redirectUri) +
                    (StringUtils.hasText(scope) ? "&scope=" + urlEncode(scope) : "") +
                    "&state=" + urlEncode(state);

            AuthUrlResp resp = new AuthUrlResp();
            resp.setAuthUrl(url);

            String cached = null;
            try {
                cached = stringRedisTemplate.opsForValue().get(redisTokenKey());
            } catch (Exception ignored) {
            }
            if (StringUtils.hasText(cached)) {
                try {
                    platformTokenStore.put("kuaishou", cached, tokenTtlSeconds);
                } catch (Exception ignored) {
                }
                Long ttl = null;
                try {
                    ttl = stringRedisTemplate.getExpire(redisTokenKey(), TimeUnit.SECONDS);
                } catch (Exception ignored) {
                }
                long ttlSec = ttl == null || ttl < 0 ? tokenTtlSeconds : ttl;
                resp.setAccessToken(cached);
                resp.setExpiresInSeconds(ttlSec);
                resp.setExpireAt(LocalDateTime.now().plusSeconds(ttlSec));
            } else {
                resp.setAccessToken("");
                resp.setExpiresInSeconds(0L);
                resp.setExpireAt(null);
            }
            return resp;
        }

        String token = fixedAccessToken;
        LocalDateTime expireAt = LocalDateTime.now().plusSeconds(tokenTtlSeconds);
        try {
            stringRedisTemplate.opsForValue().set(redisTokenKey(), token, tokenTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
        try {
            platformTokenStore.put("kuaishou", token, tokenTtlSeconds);
        } catch (Exception ignored) {
        }

        AuthUrlResp resp = new AuthUrlResp();
        resp.setAuthUrl("https://open.kuaishou.com/oauth2/authorize?client_id=mock&response_type=code&scope=user_info&redirect_uri=http://localhost/mock");
        resp.setAccessToken(token);
        resp.setExpiresInSeconds(tokenTtlSeconds);
        resp.setExpireAt(expireAt);
        return resp;
    }

    @Override
    public PublishResp publish(MultipartFile file, String title, String accessToken) {
        if (isSandboxMode()) {
            String t = normalizeToken(accessToken);
            if (!StringUtils.hasText(t)) {
                try {
                    t = stringRedisTemplate.opsForValue().get(redisTokenKey());
                } catch (Exception ignored) {
                }
            }
            if (!StringUtils.hasText(t)) {
                validateToken(accessToken);
            }
        } else {
            validateToken(accessToken);
        }

        validateUpload(file, title);

        String ext = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

        String dateDir = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path dir = Paths.get(uploadBaseDir, dateDir).toAbsolutePath().normalize();
        File folder = dir.toFile();
        if (!folder.exists() && !folder.mkdirs()) {
            throw new RuntimeException("创建上传目录失败");
        }

        String fullPath = dir.resolve(fileName).toString();
        try {
            file.transferTo(new File(fullPath));
        } catch (Exception e) {
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }

        String videoId = "ks_" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        VideoInfo info = new VideoInfo();
        info.setVideoId(videoId);
        info.setTitle(title.trim());
        info.setPublishTime(now);
        info.setPlatformType(PlatformType.KUAISHOU.name());
        info.setViews(0L);
        info.setLikes(0L);
        info.setComments(0L);
        info.setShares(0L);
        info.setFilePath(fullPath);
        info.setFileSize(file.getSize());
        info.setStatus(isSandboxMode() ? "PUBLISHED_SANDBOX" : "PUBLISHED");
        videoInfoService.save(info);

        PublishResp resp = new PublishResp();
        resp.setVideoId(videoId);
        resp.setTitle(info.getTitle());
        resp.setPublishTime(now);
        resp.setStatus(info.getStatus());
        resp.setPlatformVideoUrl(buildPlatformVideoUrl(videoId));
        return resp;
    }

    @Override
    public PublishResp publishByUrl(String fileUrl, Long fileSize, String title, String accessToken) {
        validateToken(accessToken);
        if (!StringUtils.hasText(fileUrl)) {
            throw new IllegalArgumentException("fileUrl不能为空");
        }
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("标题不能为空");
        }
        String t = title.trim();
        if (t.length() < 1 || t.length() > 128) {
            throw new IllegalArgumentException("标题长度需在1-128字符之间");
        }

        String videoId = "ks_" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();

        VideoInfo info = new VideoInfo();
        info.setVideoId(videoId);
        info.setTitle(t);
        info.setPublishTime(now);
        info.setPlatformType(PlatformType.KUAISHOU.name());
        info.setViews(0L);
        info.setLikes(0L);
        info.setComments(0L);
        info.setShares(0L);
        info.setFilePath(fileUrl);
        info.setFileSize(fileSize == null ? 0L : fileSize);
        info.setStatus("PUBLISHED");
        videoInfoService.save(info);

        PublishResp resp = new PublishResp();
        resp.setVideoId(videoId);
        resp.setTitle(info.getTitle());
        resp.setPublishTime(now);
        resp.setStatus(info.getStatus());
        resp.setPlatformVideoUrl(buildPlatformVideoUrl(videoId));
        return resp;
    }

    @Override
    public VideoStatsResp stats(String videoId) {
        VideoInfo info = requireVideo(videoId);

        Random r = new Random((System.nanoTime() ^ videoId.hashCode()) & 0xffffffffL);
        long incViews = 30 + r.nextInt(1800);
        long incLikes = r.nextInt(260);
        long incComments = r.nextInt(70);
        long incShares = r.nextInt(25);

        VideoInfo upd = new VideoInfo();
        upd.setId(info.getId());
        upd.setViews(safeAdd(info.getViews(), incViews));
        upd.setLikes(safeAdd(info.getLikes(), incLikes));
        upd.setComments(safeAdd(info.getComments(), incComments));
        upd.setShares(safeAdd(info.getShares(), incShares));
        videoInfoService.updateById(upd);

        VideoStatsResp resp = new VideoStatsResp();
        resp.setVideoId(videoId);
        resp.setViews(upd.getViews());
        resp.setLikes(upd.getLikes());
        resp.setComments(upd.getComments());
        resp.setShares(upd.getShares());
        resp.setStatTime(LocalDateTime.now());
        return resp;
    }

    @Override
    public IPage<VideoListItemResp> page(int pageNum, int pageSize, String sortBy, String sortOrder, LocalDateTime startTime, LocalDateTime endTime) {
        if (pageNum < 1) {
            throw new IllegalArgumentException("pageNum必须>=1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize必须在1-100之间");
        }

        LambdaQueryWrapper<VideoInfo> qw = new LambdaQueryWrapper<>();
        qw.eq(VideoInfo::getPlatformType, PlatformType.KUAISHOU.name());
        if (startTime != null) {
            qw.ge(VideoInfo::getPublishTime, startTime);
        }
        if (endTime != null) {
            qw.le(VideoInfo::getPublishTime, endTime);
        }

        String sortField = mapSortField(sortBy);
        boolean desc = !"asc".equalsIgnoreCase(sortOrder);
        if ("publish_time".equals(sortField)) {
            qw.orderBy(true, !desc, VideoInfo::getPublishTime);
        } else if ("views".equals(sortField)) {
            qw.orderBy(true, !desc, VideoInfo::getViews);
        } else if ("likes".equals(sortField)) {
            qw.orderBy(true, !desc, VideoInfo::getLikes);
        } else if ("comments".equals(sortField)) {
            qw.orderBy(true, !desc, VideoInfo::getComments);
        } else if ("shares".equals(sortField)) {
            qw.orderBy(true, !desc, VideoInfo::getShares);
        } else {
            qw.orderByDesc(VideoInfo::getPublishTime);
        }

        Page<VideoInfo> p = new Page<>(pageNum, pageSize);
        IPage<VideoInfo> raw = videoInfoService.page(p, qw);

        Page<VideoListItemResp> out = new Page<>(raw.getCurrent(), raw.getSize(), raw.getTotal());
        out.setRecords(raw.getRecords().stream().map(v -> {
            VideoListItemResp item = new VideoListItemResp();
            item.setVideoId(v.getVideoId());
            item.setTitle(v.getTitle());
            item.setPublishTime(v.getPublishTime());
            item.setViews(v.getViews());
            item.setLikes(v.getLikes());
            item.setComments(v.getComments());
            item.setShares(v.getShares());
            return item;
        }).collect(java.util.stream.Collectors.toList()));
        return out;
    }

    @Override
    public void updateTitle(String videoId, String title) {
        if (!StringUtils.hasText(title) || title.trim().length() > 128) {
            throw new IllegalArgumentException("标题长度需在1-128字符之间");
        }
        VideoInfo info = requireVideo(videoId);
        VideoInfo upd = new VideoInfo();
        upd.setId(info.getId());
        upd.setTitle(title.trim());
        videoInfoService.updateById(upd);
    }

    @Override
    public void delete(String videoId) {
        VideoInfo info = requireVideo(videoId);
        videoInfoService.removeById(info.getId());
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

    private void validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("access_token不能为空");
        }
        String t = normalizeToken(token);

        String cached = null;
        try {
            cached = stringRedisTemplate.opsForValue().get(redisTokenKey());
        } catch (Exception ignored) {
        }

        if (cached == null) {
            PlatformTokenStore.TokenInfo info = null;
            try {
                info = platformTokenStore.get("kuaishou");
            } catch (Exception ignored) {
            }
            cached = info != null ? info.getToken() : fixedAccessToken;
        }

        if (!cached.equals(t)) {
            throw new IllegalArgumentException("access_token无效，请先授权");
        }
    }

    private String normalizeToken(String token) {
        if (!StringUtils.hasText(token)) {
            return "";
        }
        String t = token.trim();
        if (t.toLowerCase(Locale.ROOT).startsWith("bearer ")) {
            t = t.substring(7);
        }
        return t;
    }

    private void validateUpload(MultipartFile file, String title) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("标题不能为空");
        }
        String t = title.trim();
        if (t.length() < 1 || t.length() > 128) {
            throw new IllegalArgumentException("标题长度需在1-128字符之间");
        }
        String ext = getExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (".mp4".equals(ext) || ".mov".equals(ext) || ".avi".equals(ext)) {
            return;
        }

        String contentType = file.getContentType();
        if (contentType != null) {
            String ct = contentType.toLowerCase(Locale.ROOT);
            if (ct.contains("video/mp4") || ct.contains("video/quicktime") || ct.contains("x-msvideo") || ct.contains("avi")) {
                return;
            }
        }
        throw new IllegalArgumentException("文件格式不支持");
    }

    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int idx = filename.lastIndexOf('.');
        if (idx < 0) {
            return "";
        }
        return filename.substring(idx);
    }

    private VideoInfo requireVideo(String videoId) {
        VideoInfo info = videoInfoService.getOne(new LambdaQueryWrapper<VideoInfo>()
                .eq(VideoInfo::getVideoId, videoId)
                .eq(VideoInfo::getPlatformType, PlatformType.KUAISHOU.name()));
        if (info == null) {
            throw new IllegalArgumentException("视频不存在");
        }
        return info;
    }

    private long safeAdd(Long base, long inc) {
        long b = base == null ? 0L : base;
        long r = b + inc;
        return r < 0 ? Long.MAX_VALUE : r;
    }

    private String mapSortField(String sortBy) {
        if (!StringUtils.hasText(sortBy)) {
            return "";
        }
        String s = sortBy.trim();
        if ("publishTime".equals(s) || "publish_time".equals(s)) return "publish_time";
        if ("views".equals(s)) return "views";
        if ("likes".equals(s)) return "likes";
        if ("comments".equals(s)) return "comments";
        if ("shares".equals(s)) return "shares";
        return "";
    }

    private String urlEncode(String raw) {
        try {
            return java.net.URLEncoder.encode(raw, "UTF-8");
        } catch (Exception e) {
            return raw;
        }
    }

    private String buildPlatformVideoUrl(String videoId) {
        return "https://www.kuaishou.com/short-video/" + videoId;
    }
}
