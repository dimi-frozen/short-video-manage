package com.shortvideo.controller;

import com.shortvideo.dto.PageResp;
import com.shortvideo.dto.VideoDetailResp;
import com.shortvideo.dto.VideoListItemResp;
import com.shortvideo.dto.VideoUpdateReq;
import com.shortvideo.dto.VideoUploadResp;
import com.shortvideo.service.SvVideoService;
import com.shortvideo.utils.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 短视频管理模块 REST API
 * <p>
 * - 上传：POST /api/videos/upload（multipart/form-data）
 * - 更新：PUT /api/videos/{id}
 * - 详情：GET /api/videos/{id}
 * - 分页：GET /api/videos
 * - 删除：DELETE /api/videos/{id}（软删除，返回 204）
 */
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final SvVideoService svVideoService;

    public VideoController(SvVideoService svVideoService) {
        this.svVideoService = svVideoService;
    }

    /**
     * 上传视频文件并入库元数据。
     * <p>
     * tags 支持两种传法：
     * - tags=旅行&tags=美食
     * - tags=旅行,美食
     */
    @PostMapping("/upload")
    public Result<VideoUploadResp> upload(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "cover", required = false) MultipartFile cover,
                                          @RequestParam("title") String title,
                                          @RequestParam(value = "description", required = false) String description,
                                          @RequestParam(value = "tags", required = false) List<String> tags) {
        VideoUploadResp resp = svVideoService.upload(file, cover, title, description, tags);
        return Result.success(resp);
    }

    /**
     * 幂等更新视频信息（标题/描述/标签）。
     * - 乐观锁由 JPA @Version 控制
     * - 更新成功返回更新后的完整数据
     */
    @PutMapping("/{id}")
    public Result<VideoDetailResp> update(@PathVariable("id") Long id, @Validated @RequestBody VideoUpdateReq req) {
        return Result.success(svVideoService.update(id, req));
    }

    /**
     * 查询视频详情。
     * - 若 deleted=true，返回 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Result<VideoDetailResp>> detail(@PathVariable("id") Long id,
                                                          @RequestParam(value = "includeUnapproved", defaultValue = "false") boolean includeUnapproved) {
        try {
            return ResponseEntity.ok(Result.success(svVideoService.get(id, includeUnapproved)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(404).body(Result.error(404, "视频不存在"));
        }
    }

    /**
     * 查询视频详情（通过参数）
     */
    @GetMapping("/detail")
    public Result<VideoDetailResp> detailByParam(@RequestParam("id") Long id,
                                                 @RequestParam(value = "includeUnapproved", defaultValue = "false") boolean includeUnapproved) {
        try {
            return Result.success(svVideoService.get(id, includeUnapproved));
        } catch (java.util.NoSuchElementException e) {
            return Result.error(404, "视频不存在");
        }
    }

    /**
     * 分页查询。
     * <p>
     * 示例：
     * GET /api/videos?pageNum=1&pageSize=10&keyword=xxx&tag=旅行&sort=createTime,desc&sort=title,asc
     * <p>
     * - keyword：标题/描述模糊搜索
     * - tag：精确过滤（内部通过 tags 字符串编码匹配）
     * - sort：支持多字段排序，格式 field,asc|desc
     */
    @GetMapping
    public Result<PageResp<VideoListItemResp>> page(@RequestParam(defaultValue = "1") int pageNum,
                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                    @RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) String tag,
                                                    @RequestParam(required = false) String auditStatus,
                                                    @RequestParam(required = false) List<String> sort,
                                                    @RequestParam(value = "includeUnapproved", defaultValue = "false") boolean includeUnapproved) {
        return Result.success(svVideoService.page(pageNum, pageSize, keyword, tag, auditStatus, sort, includeUnapproved));
    }

    /**
     * 软删除：仅设置 deleted=true，并更新时间。
     * 成功返回 204 No Content。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        try {
            svVideoService.softDelete(id);
            return ResponseEntity.noContent().build();
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(404).build();
        }
    }

    /**
     * 播放量+1
     */
    @PostMapping("/{id}/view")
    public Result<Void> incrementViews(@PathVariable("id") Long id) {
        svVideoService.incrementViews(id);
        return Result.success(null);
    }

    /**
     * 点赞+1
     */
    @PostMapping("/{id}/like")
    public Result<Void> incrementLikes(@PathVariable("id") Long id) {
        svVideoService.incrementLikes(id);
        return Result.success(null);
    }

    /**
     * 点赞+1并返回最新点赞数
     */
    @PostMapping("/like")
    public Result<Long> likeVideo(@RequestParam("id") Long id) {
        Long latestLikes = svVideoService.likeAndReturnCount(id);
        return Result.success(latestLikes);
    }

    @GetMapping("/public")
    public Result<PageResp<VideoListItemResp>> pagePublic(@RequestParam(defaultValue = "1") int pageNum,
                                                          @RequestParam(defaultValue = "10") int pageSize,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) String tag,
                                                          @RequestParam(required = false) List<String> sort) {
        return Result.success(svVideoService.pagePublic(pageNum, pageSize, keyword, tag, sort));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<Result<VideoDetailResp>> detailPublic(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(Result.success(svVideoService.getPublic(id)));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(404).body(Result.error(404, "视频不存在"));
        }
    }

    /**
     * 获取相关推荐视频
     */
    @GetMapping("/{id}/related")
    public Result<List<VideoListItemResp>> getRelatedVideos(@PathVariable("id") Long id,
                                                            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(svVideoService.getRelatedVideos(id, limit));
    }

}
