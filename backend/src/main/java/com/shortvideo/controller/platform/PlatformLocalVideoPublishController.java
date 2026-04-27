package com.shortvideo.controller.platform;

import com.shortvideo.dto.platform.PublishResp;
import com.shortvideo.entity.VideoPublishRecord;
import com.shortvideo.enums.PlatformType;
import com.shortvideo.mapper.SvVideoMapper;
import com.shortvideo.mapper.VideoPublishRecordMapper;
import com.shortvideo.service.platform.PlatformServiceRegistry;
import com.shortvideo.utils.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/platforms")
public class PlatformLocalVideoPublishController {

    private final SvVideoMapper svVideoMapper;
    private final VideoPublishRecordMapper recordMapper;
    private final PlatformServiceRegistry registry;

    public PlatformLocalVideoPublishController(SvVideoMapper svVideoMapper,
                                               VideoPublishRecordMapper recordMapper,
                                               PlatformServiceRegistry registry) {
        this.svVideoMapper = svVideoMapper;
        this.recordMapper = recordMapper;
        this.registry = registry;
    }

    @PostMapping("/{platform}/videos/publish-local")
    public Result<PublishResp> publishLocal(@PathVariable("platform") String platform,
                                           @RequestParam("videoId") Long videoId,
                                           @RequestHeader(value = "Authorization", required = false) String authorization,
                                           @RequestParam(value = "access_token", required = false) String accessToken) {
        PlatformType pt = parsePlatform(platform);
        String token = accessToken != null ? accessToken : authorization;

        com.shortvideo.entity.SvVideo v = svVideoMapper.selectById(videoId);
        if (v == null) {
            throw new NoSuchElementException("视频不存在");
        }
        if (Boolean.TRUE.equals(v.getDeleted())) {
            throw new NoSuchElementException("视频不存在");
        }
        if (!StringUtils.hasText(v.getFilePath())) {
            throw new IllegalStateException("视频地址为空，请重新上传");
        }

        VideoPublishRecord r = new VideoPublishRecord();
        r.setVideoId(videoId);
        r.setPlatform(pt.name().toLowerCase(Locale.ROOT));
        r.setStatus("PENDING");
        recordMapper.insert(r);

        try {
            PublishResp resp = registry.get(pt).publishByUrl(v.getFilePath(), v.getFileSize(), v.getTitle(), token);
            r.setStatus("SUCCESS");
            r.setPlatformVideoUrl(resp.getPlatformVideoUrl());
            recordMapper.updateById(r);
            return Result.success(resp);
        } catch (Exception e) {
            recordMapper.updateById(r);
            throw e;
        }
    }

    private PlatformType parsePlatform(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new IllegalArgumentException("platform不能为空");
        }
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if ("DOUYIN".equals(s) || "DOU_YIN".equals(s)) return PlatformType.DOUYIN;
        if ("KUAISHOU".equals(s) || "KUAI_SHOU".equals(s)) return PlatformType.KUAISHOU;
        if ("BILIBILI".equals(s)) return PlatformType.BILIBILI;
        try {
            return PlatformType.valueOf(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("不支持的平台: " + raw);
        }
    }
}

