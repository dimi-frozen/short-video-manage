package com.shortvideo.controller.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortvideo.dto.platform.VideoInfoVO;
import com.shortvideo.dto.platform.VideoStatsResp;
import com.shortvideo.entity.VideoInfo;
import com.shortvideo.enums.PlatformType;
import com.shortvideo.service.VideoInfoService;
import com.shortvideo.service.platform.PlatformServiceRegistry;
import com.shortvideo.utils.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/video-info")
public class VideoInfoController {

    private final VideoInfoService videoInfoService;
    private final PlatformServiceRegistry registry;

    public VideoInfoController(VideoInfoService videoInfoService, PlatformServiceRegistry registry) {
        this.videoInfoService = videoInfoService;
        this.registry = registry;
    }

    @GetMapping("/page")
    public Result<IPage<VideoInfoVO>> page(@RequestParam(defaultValue = "1") @Min(1) int pageNum,
                                          @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String platformType,
                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                          @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                                          @RequestParam(required = false) String sortBy,
                                          @RequestParam(required = false) String sortOrder) {
        Long currentUserId = getCurrentUserId();
        LambdaQueryWrapper<VideoInfo> qw = new LambdaQueryWrapper<>();
        // 过滤当前用户的平台发布记录
        qw.eq(VideoInfo::getUserId, currentUserId);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like(VideoInfo::getTitle, keyword.trim());
        }
        if (platformType != null && !platformType.trim().isEmpty()) {
            qw.eq(VideoInfo::getPlatformType, platformType.trim().toUpperCase());
        }
        if (startTime != null) {
            qw.ge(VideoInfo::getPublishTime, startTime);
        }
        if (endTime != null) {
            qw.le(VideoInfo::getPublishTime, endTime);
        }

        boolean desc = !"asc".equalsIgnoreCase(sortOrder);
        String field = sortBy == null ? "" : sortBy.trim();
        if ("publishTime".equalsIgnoreCase(field) || "publish_time".equalsIgnoreCase(field)) {
            qw.orderBy(true, !desc, VideoInfo::getPublishTime);
        } else if ("views".equalsIgnoreCase(field)) {
            qw.orderBy(true, !desc, VideoInfo::getViews);
        } else if ("likes".equalsIgnoreCase(field)) {
            qw.orderBy(true, !desc, VideoInfo::getLikes);
        } else if ("comments".equalsIgnoreCase(field)) {
            qw.orderBy(true, !desc, VideoInfo::getComments);
        } else if ("shares".equalsIgnoreCase(field)) {
            qw.orderBy(true, !desc, VideoInfo::getShares);
        } else {
            qw.orderByDesc(VideoInfo::getPublishTime);
        }

        IPage<VideoInfo> page = videoInfoService.page(new Page<>(pageNum, pageSize), qw);
        Page<VideoInfoVO> out = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        out.setRecords(page.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return Result.success(out);
    }

    @GetMapping("/{videoId}")
    public Result<VideoInfoVO> get(@PathVariable("videoId") String videoId) {
        VideoInfo info = requireVideo(videoId);
        return Result.success(toVO(info));
    }

    @PostMapping("/{videoId}/refresh-stats")
    public Result<VideoStatsResp> refreshStats(@PathVariable("videoId") String videoId) {
        VideoInfo info = requireVideo(videoId);
        PlatformType type = PlatformType.valueOf(info.getPlatformType());
        VideoStatsResp resp = registry.get(type).stats(videoId);
        return Result.success(resp);
    }

    @PutMapping("/{videoId}/title")
    public Result<Void> updateTitle(@PathVariable("videoId") String videoId, @RequestParam("title") String title) {
        VideoInfo info = requireVideo(videoId);
        PlatformType type = PlatformType.valueOf(info.getPlatformType());
        registry.get(type).updateTitle(videoId, title);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{videoId}")
    public Result<Void> delete(@PathVariable("videoId") String videoId) {
        VideoInfo info = requireVideo(videoId);
        PlatformType type = PlatformType.valueOf(info.getPlatformType());
        registry.get(type).delete(videoId);
        return Result.success("删除成功");
    }

    private VideoInfo requireVideo(String videoId) {
        Long currentUserId = getCurrentUserId();
        // 查询时同时校验 user_id，防止越权操作
        VideoInfo info = videoInfoService.getOne(new LambdaQueryWrapper<VideoInfo>()
                .eq(VideoInfo::getVideoId, videoId)
                .eq(VideoInfo::getUserId, currentUserId));
        if (info == null) {
            throw new IllegalArgumentException("视频不存在");
        }
        return info;
    }

    private VideoInfoVO toVO(VideoInfo info) {
        VideoInfoVO vo = new VideoInfoVO();
        BeanUtils.copyProperties(info, vo);
        return vo;
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new SecurityException("用户未登录");
    }
}

