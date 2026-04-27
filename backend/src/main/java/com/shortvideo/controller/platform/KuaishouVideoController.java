package com.shortvideo.controller.platform;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shortvideo.dto.platform.PublishResp;
import com.shortvideo.dto.platform.UpdateTitleReq;
import com.shortvideo.dto.platform.VideoListItemResp;
import com.shortvideo.dto.platform.VideoStatsResp;
import com.shortvideo.service.platform.PlatformServiceRegistry;
import com.shortvideo.utils.Result;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static com.shortvideo.enums.PlatformType.KUAISHOU;

@RestController
@RequestMapping("/api/platforms/kuaishou")
public class KuaishouVideoController {

    private final PlatformServiceRegistry registry;

    public KuaishouVideoController(PlatformServiceRegistry registry) {
        this.registry = registry;
    }

    @PostMapping("/videos/publish")
    public Result<PublishResp> publish(@RequestParam("file") MultipartFile file,
                                       @RequestParam("title") String title,
                                       @RequestHeader(value = "Authorization", required = false) String authorization,
                                       @RequestParam(value = "access_token", required = false) String accessToken) {
        String token = accessToken != null ? accessToken : authorization;
        return Result.success(registry.get(KUAISHOU).publish(file, title, token));
    }

    @GetMapping("/videos/{videoId}/stats")
    public Result<VideoStatsResp> stats(@PathVariable("videoId") String videoId) {
        return Result.success(registry.get(KUAISHOU).stats(videoId));
    }

    @GetMapping("/videos")
    public Result<IPage<VideoListItemResp>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                 @RequestParam(defaultValue = "10") int pageSize,
                                                 @RequestParam(required = false) String sortBy,
                                                 @RequestParam(required = false) String sortOrder,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                                 @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(registry.get(KUAISHOU).page(pageNum, pageSize, sortBy, sortOrder, startTime, endTime));
    }

    @PutMapping("/videos/{videoId}")
    public Result<Void> updateTitle(@PathVariable("videoId") String videoId, @Validated @RequestBody UpdateTitleReq req) {
        registry.get(KUAISHOU).updateTitle(videoId, req.getTitle());
        return Result.success("更新成功");
    }

    @DeleteMapping("/videos/{videoId}")
    public Result<Void> delete(@PathVariable("videoId") String videoId) {
        registry.get(KUAISHOU).delete(videoId);
        return Result.success("删除成功");
    }
}

