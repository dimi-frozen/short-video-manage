package com.shortvideo.service;

import com.shortvideo.dto.PageResp;
import com.shortvideo.dto.VideoDetailResp;
import com.shortvideo.dto.VideoListItemResp;
import com.shortvideo.dto.VideoUpdateReq;
import com.shortvideo.dto.VideoUploadResp;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 短视频管理模块业务接口
 */
public interface SvVideoService {
    /**
     * 上传视频文件并持久化元数据。
     *
     * @param file 视频文件（必填）
     * @param title 标题（1-128）
     * @param description 描述（可选）
     * @param tags 标签（可选；支持 tags=旅行&tags=美食 或 tags=旅行,美食）
     */
    VideoUploadResp upload(MultipartFile file, MultipartFile cover, String title, String description, List<String> tags);

    /**
     * 幂等更新标题/描述/标签（乐观锁控制并发写）。
     */
    VideoDetailResp update(Long id, VideoUpdateReq req);

    /**
     * 查询详情；若 deleted=true 则当作不存在（404）。
     */
    default VideoDetailResp get(Long id) {
        return get(id, false);
    }

    /**
     * 查询详情；默认仅返回审核通过（APPROVED）。
     *
     * @param includeUnapproved true 时返回 PENDING/REJECTED 等未通过审核的视频（仍会过滤 deleted=true）
     */
    VideoDetailResp get(Long id, boolean includeUnapproved);

    /**
     * 分页查询：keyword 模糊搜索（title/description），tag 精确过滤，sort 支持多字段排序。
     */
    default PageResp<VideoListItemResp> page(int pageNum, int pageSize, String keyword, String tag, List<String> sort) {
        return page(pageNum, pageSize, keyword, tag, sort, false);
    }

    /**
     * 分页查询：默认仅返回审核通过（APPROVED）。
     *
     * @param includeUnapproved true 时返回 PENDING/REJECTED 等未通过审核的视频（仍会过滤 deleted=true）
     */
    PageResp<VideoListItemResp> page(int pageNum, int pageSize, String keyword, String tag, List<String> sort, boolean includeUnapproved);

    /**
     * 播放量+1
     */
    void incrementViews(Long id);

    /**
     * 点赞+1
     */
    void incrementLikes(Long id);

    /**
     * 点赞+1并返回最新点赞数
     */
    Long likeAndReturnCount(Long id);

    /**
     * 软删除：设置 deleted=true 并更新时间。
     */
    void softDelete(Long id);

    PageResp<VideoListItemResp> pagePublic(int pageNum, int pageSize, String keyword, String tag, List<String> sort);

    VideoDetailResp getPublic(Long id);

    /**
     * 获取相关推荐视频（基于标签匹配和热度）
     */
    List<VideoListItemResp> getRelatedVideos(Long currentVideoId, int limit);

}
