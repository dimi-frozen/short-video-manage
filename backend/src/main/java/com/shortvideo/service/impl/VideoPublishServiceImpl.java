package com.shortvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shortvideo.dto.publish.PublishConfirmResp;
import com.shortvideo.dto.publish.PublishRecordResp;
import com.shortvideo.dto.publish.PublishResp;
import com.shortvideo.entity.SvVideo;
import com.shortvideo.entity.VideoInfo;
import com.shortvideo.entity.VideoPublishRecord;
import com.shortvideo.enums.PlatformType;
import com.shortvideo.mapper.SvVideoMapper;
import com.shortvideo.mapper.VideoPublishRecordMapper;
import com.shortvideo.service.VideoInfoService;
import com.shortvideo.service.VideoPublishService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class VideoPublishServiceImpl implements VideoPublishService {

    private final SvVideoMapper svVideoMapper;
    private final VideoPublishRecordMapper recordMapper;
    private final VideoInfoService videoInfoService;

    public VideoPublishServiceImpl(SvVideoMapper svVideoMapper,
                                   VideoPublishRecordMapper recordMapper,
                                   VideoInfoService videoInfoService) {
        this.svVideoMapper = svVideoMapper;
        this.recordMapper = recordMapper;
        this.videoInfoService = videoInfoService;
    }

    @Override
    @Transactional
    public PublishResp createPublish(Long videoId, String platform) {
        if (videoId == null) {
            throw new IllegalArgumentException("videoId不能为空");
        }
        PlatformType pt = parsePlatform(platform);

        SvVideo v = svVideoMapper.selectById(videoId);
        if (v == null) {
            throw new NoSuchElementException("视频不存在");
        }
        if (Boolean.TRUE.equals(v.getDeleted())) {
            throw new NoSuchElementException("视频不存在");
        }
        if (!StringUtils.hasText(v.getFilePath())) {
            throw new IllegalStateException("视频文件地址为空，请重新上传");
        }

        VideoPublishRecord r = new VideoPublishRecord();
        r.setVideoId(videoId);
        r.setPlatform(pt.name().toLowerCase(Locale.ROOT));
        r.setStatus("PENDING");
        recordMapper.insert(r);

        PublishResp resp = new PublishResp();
        resp.setRecordId(r.getId());
        resp.setRedirectUrl("/dashboard/publish-sim?recordId=" + r.getId());
        return resp;
    }

    @Override
    @Transactional
    public PublishConfirmResp confirmPublish(Long recordId) {
        if (recordId == null) {
            throw new IllegalArgumentException("recordId不能为空");
        }
        VideoPublishRecord r = recordMapper.selectById(recordId);
        if (r == null) {
            throw new NoSuchElementException("发布记录不存在");
        }
        if ("SUCCESS".equalsIgnoreCase(r.getStatus())) {
            PublishConfirmResp resp = new PublishConfirmResp();
            resp.setRecordId(r.getId());
            resp.setStatus(r.getStatus());
            resp.setPlatformVideoUrl(r.getPlatformVideoUrl());
            resp.setPlatformVideoId(extractPlatformVideoId(r.getPlatformVideoUrl()));
            return resp;
        }

        PlatformType pt = parsePlatform(r.getPlatform());
        SvVideo v = svVideoMapper.selectById(r.getVideoId());
        if (v == null) {
            throw new NoSuchElementException("视频不存在");
        }
        if (Boolean.TRUE.equals(v.getDeleted())) {
            throw new NoSuchElementException("视频不存在");
        }

        String platformVideoId = generatePlatformVideoId(pt);
        String platformVideoUrl = buildPlatformVideoUrl(pt, platformVideoId);

        r.setStatus("SUCCESS");
        r.setPlatformVideoUrl(platformVideoUrl);
        recordMapper.updateById(r);

        VideoInfo info = new VideoInfo();
        info.setVideoId(platformVideoId);
        info.setTitle(v.getTitle());
        info.setPublishTime(LocalDateTime.now());
        info.setPlatformType(pt.name());
        info.setViews(0L);
        info.setLikes(0L);
        info.setComments(0L);
        info.setShares(0L);
        info.setFilePath(v.getFilePath());
        info.setFileSize(v.getFileSize());
        info.setStatus("PUBLISHED");
        info.setUserId(v.getUserId()); // 绑定原视频发布者ID
        videoInfoService.save(info);

        PublishConfirmResp resp = new PublishConfirmResp();
        resp.setRecordId(r.getId());
        resp.setStatus(r.getStatus());
        resp.setPlatformVideoUrl(platformVideoUrl);
        resp.setPlatformVideoId(platformVideoId);
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public PublishRecordResp getRecord(Long recordId) {
        VideoPublishRecord r = recordMapper.selectById(recordId);
        if (r == null) {
            throw new NoSuchElementException("发布记录不存在");
        }
        PublishRecordResp resp = new PublishRecordResp();
        resp.setRecordId(r.getId());
        resp.setVideoId(r.getVideoId());
        resp.setPlatform(r.getPlatform());
        resp.setStatus(r.getStatus());
        resp.setPlatformVideoUrl(r.getPlatformVideoUrl());
        resp.setCreateTime(r.getCreateTime());
        resp.setUpdateTime(r.getUpdateTime());
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PublishRecordResp> listRecords(Long videoId, String platform) {
        if (videoId == null) {
            throw new IllegalArgumentException("videoId不能为空");
        }
        List<VideoPublishRecord> list;
        if (StringUtils.hasText(platform)) {
            String p = platform.trim().toLowerCase(Locale.ROOT);
            // Replace findTopByVideoIdAndPlatformOrderByIdDesc with MyBatis-Plus query
            QueryWrapper<VideoPublishRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("video_id", videoId)
                        .eq("platform", p)
                        .orderByDesc("id")
                        .last("LIMIT 1");
            VideoPublishRecord one = recordMapper.selectOne(queryWrapper);
            list = new ArrayList<>();
            if (one != null) {
                list.add(one);
            }
        } else {
            // Replace findByVideoIdOrderByIdDesc with MyBatis-Plus query
            QueryWrapper<VideoPublishRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("video_id", videoId)
                        .orderByDesc("id");
            list = recordMapper.selectList(queryWrapper);
        }
        list.sort(Comparator.comparing(VideoPublishRecord::getId).reversed());
        List<PublishRecordResp> out = new ArrayList<>();
        for (VideoPublishRecord r : list) {
            PublishRecordResp resp = new PublishRecordResp();
            resp.setRecordId(r.getId());
            resp.setVideoId(r.getVideoId());
            resp.setPlatform(r.getPlatform());
            resp.setStatus(r.getStatus());
            resp.setPlatformVideoUrl(r.getPlatformVideoUrl());
            resp.setCreateTime(r.getCreateTime());
            resp.setUpdateTime(r.getUpdateTime());
            out.add(resp);
        }
        return out;
    }

    private PlatformType parsePlatform(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new IllegalArgumentException("platform不能为空");
        }
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if ("DOUYIN".equals(s) || "DOU_YIN".equals(s) || "DOUYIN".equalsIgnoreCase(raw) || "douyin".equalsIgnoreCase(raw)) {
            return PlatformType.DOUYIN;
        }
        if ("KUAISHOU".equals(s) || "KUAI_SHOU".equals(s) || "kuaishou".equalsIgnoreCase(raw)) {
            return PlatformType.KUAISHOU;
        }
        if ("BILIBILI".equals(s) || "bilibili".equalsIgnoreCase(raw) || "B站".equals(raw.trim())) {
            return PlatformType.BILIBILI;
        }
        try {
            return PlatformType.valueOf(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("不支持的平台: " + raw);
        }
    }

    private String generatePlatformVideoId(PlatformType pt) {
        if (pt == PlatformType.DOUYIN) {
            return "dy_" + UUID.randomUUID().toString().replace("-", "");
        }
        if (pt == PlatformType.KUAISHOU) {
            return "ks_" + UUID.randomUUID().toString().replace("-", "");
        }
        String core = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        return "BV" + core.substring(0, 10);
    }

    private String buildPlatformVideoUrl(PlatformType pt, String platformVideoId) {
        if (pt == PlatformType.DOUYIN) {
            return "https://www.douyin.com/video/" + platformVideoId;
        }
        if (pt == PlatformType.KUAISHOU) {
            return "https://www.kuaishou.com/short-video/" + platformVideoId;
        }
        return "https://www.bilibili.com/video/" + platformVideoId;
    }

    private String extractPlatformVideoId(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        int idx = url.lastIndexOf('/');
        if (idx < 0 || idx == url.length() - 1) {
            return null;
        }
        return url.substring(idx + 1);
    }
}
