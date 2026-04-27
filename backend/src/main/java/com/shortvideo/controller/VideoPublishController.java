package com.shortvideo.controller;

import com.shortvideo.dto.publish.PublishConfirmReq;
import com.shortvideo.dto.publish.PublishConfirmResp;
import com.shortvideo.dto.publish.PublishRecordResp;
import com.shortvideo.dto.publish.PublishReq;
import com.shortvideo.dto.publish.PublishResp;
import com.shortvideo.service.VideoPublishService;
import com.shortvideo.utils.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publish")
public class VideoPublishController {

    private final VideoPublishService videoPublishService;

    public VideoPublishController(VideoPublishService videoPublishService) {
        this.videoPublishService = videoPublishService;
    }

    @PostMapping
    public Result<PublishResp> publish(@Validated @RequestBody PublishReq req) {
        return Result.success(videoPublishService.createPublish(req.getVideoId(), req.getPlatform()));
    }

    @PostMapping("/confirm")
    public Result<PublishConfirmResp> confirm(@Validated @RequestBody PublishConfirmReq req) {
        return Result.success(videoPublishService.confirmPublish(req.getRecordId()));
    }

    @GetMapping("/records/{recordId}")
    public Result<PublishRecordResp> record(@PathVariable("recordId") Long recordId) {
        return Result.success(videoPublishService.getRecord(recordId));
    }

    @GetMapping("/records")
    public Result<List<PublishRecordResp>> records(@RequestParam("videoId") Long videoId,
                                                   @RequestParam(value = "platform", required = false) String platform) {
        return Result.success(videoPublishService.listRecords(videoId, platform));
    }
}
