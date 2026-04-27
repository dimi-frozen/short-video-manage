package com.shortvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shortvideo.dto.PageResp;
import com.shortvideo.dto.VideoDetailResp;
import com.shortvideo.dto.VideoListItemResp;
import com.shortvideo.dto.VideoUpdateReq;
import com.shortvideo.dto.VideoUploadResp;
import com.shortvideo.entity.SvVideo;
import com.shortvideo.entity.User;
import com.shortvideo.mapper.CommentMapper;
import com.shortvideo.mapper.SvVideoMapper;
import com.shortvideo.mapper.UserMapper;
import com.shortvideo.service.SvVideoService;
import com.shortvideo.service.CommentService;
import com.shortvideo.service.storage.OssStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SvVideoServiceImpl implements SvVideoService {

    private final SvVideoMapper svVideoMapper;
    private final OssStorageService ossStorageService;
    private final UserMapper userMapper;
    private final CommentService commentService;
    private final CommentMapper commentMapper;

    @Value("${video.upload.allowed-ext:mp4,mov,avi}")
    private String allowedExt;

    @Value("${video.upload.max-bytes:104857600}")
    private long maxBytes;

    @Value("${video.upload.cover-max-bytes:5242880}")
    private long coverMaxBytes;

    public SvVideoServiceImpl(SvVideoMapper svVideoMapper, OssStorageService ossStorageService, UserMapper userMapper, CommentService commentService, CommentMapper commentMapper) {
        this.svVideoMapper = svVideoMapper;
        this.ossStorageService = ossStorageService;
        this.userMapper = userMapper;
        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }

    /**
     * 上传视频文件并入库元数据。
     * <p>
     * 存储规则：
     * - 目录：${user.home}/short-video/upload/{yyyyMMdd}/
     * - 文件名：UUID + 原扩展名
     * <p>
     * 状态流转：
     * - 先落库 status=TRANSCODING
     * - 文件写入成功后 status=NORMAL
     * - 文件写入失败则 status=FAILED
     *
     * @param file        视频文件（multipart/form-data 的 file 字段）
     * @param title       标题（1-128 字符）
     * @param description 描述（可选）
     * @param tags        标签（可选；支持 tags=旅行&tags=美食 或 tags=旅行,美食）
     * @return 上传结果（videoId、title、uploadTime）
     */
    @Override
    @Transactional
    public VideoUploadResp upload(MultipartFile file, MultipartFile cover, String title, String description, List<String> tags) {
        // 获取当前登录用户ID
        Long currentUserId = getCurrentUserId();

        // 1) 参数校验：文件非空、大小限制、格式白名单、标题长度、标签数量与长度
        validateUpload(file, title, tags);
        validateCover(cover);

        // 2) 标签规范化与编码（用于 like 精确匹配）
        List<String> tagList = normalizeTags(tags);
        String storedTags = encodeTags(tagList);

        // 3) 先落库（状态：转码中），再上传 OSS；成功后更新状态为 NORMAL，失败则 FAILED
        SvVideo video = new SvVideo();
        video.setUserId(currentUserId);
        video.setTitle(title.trim());
        video.setDescription(description);
        video.setTags(storedTags);
        video.setFilePath("PENDING");
        video.setFileSize(file.getSize());
        video.setDuration(0);
        video.setStatus("TRANSCODING");
        video.setAuditStatus("PENDING");
        video.setDeleted(false);
        video.setViews(0L);
        video.setLikes(0L);
        video.setVersion(0L);

        // 显式赋值时间，防止自动填充失效导致插入报错回滚
        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());

        svVideoMapper.insert(video);

        try {
            OssStorageService.UploadResult ur = ossStorageService.uploadPublic("videos", file);
            video.setFilePath(ur.getUrl());
            if (cover != null && !cover.isEmpty()) {
                OssStorageService.UploadResult cr = ossStorageService.uploadPublic("covers", cover);
                video.setCoverUrl(cr.getUrl());
            }
            video.setStatus("NORMAL");
            svVideoMapper.updateById(video);
        } catch (Exception e) {
            video.setStatus("FAILED");
            svVideoMapper.updateById(video);
            throw new RuntimeException("上传失败: " + e.getMessage());
        }

        VideoUploadResp resp = new VideoUploadResp();
        resp.setVideoId(video.getId());
        resp.setTitle(video.getTitle());
        resp.setUploadTime(video.getCreateTime());
        resp.setFileUrl(video.getFilePath());
        resp.setCoverUrl(video.getCoverUrl());
        return resp;
    }

    /**
     * 幂等更新视频信息（标题/描述/标签）。
     * <p>
     * 约束：
     * - deleted=true 的记录对外表现为 404（当作不存在）
     * - 标题若更新则校验 1-128 字符
     * - tags 字段出现则视为全量覆盖（并重新编码保存）
     * - 并发写入由 @Version 乐观锁控制，冲突返回 409
     */
    @Override
    @Transactional
    public VideoDetailResp update(Long id, VideoUpdateReq req) {
        Long currentUserId = getCurrentUserId();
        // deleted=true 的记录对外表现为 404
        SvVideo video = svVideoMapper.selectById(id);
        if (video == null || Boolean.TRUE.equals(video.getDeleted())) {
            throw new NoSuchElementException("视频不存在");
        }
        // 权限校验：只能编辑自己的视频
        if (!currentUserId.equals(video.getUserId())) {
            throw new SecurityException("无权操作该视频");
        }

        if (req != null) {
            if (req.getTitle() != null) {
                String t = req.getTitle().trim();
                if (t.length() < 1 || t.length() > 128) {
                    throw new IllegalArgumentException("标题长度需在1-128字符之间");
                }
                video.setTitle(t);
            }
            if (req.getDescription() != null) {
                video.setDescription(req.getDescription());
            }
            if (req.getTags() != null) {
                // 只要 tags 字段出现，就视为全量覆盖
                List<String> tagList = normalizeTags(req.getTags());
                video.setTags(encodeTags(tagList));
            }
        }

        int rows = svVideoMapper.updateById(video);
        if (rows == 0) {
            throw new IllegalStateException("数据已被其他请求更新，请刷新后重试");
        }

        // 重新查询获取最新数据
        SvVideo updatedVideo = svVideoMapper.selectById(id);
        return toDetailResp(updatedVideo);
    }

    /**
     * 查询视频详情。
     * <p>
     * 约束：
     * - deleted=true 的记录对外表现为 404
     */
    @Override
    @Transactional(readOnly = true)
    public VideoDetailResp get(Long id, boolean includeUnapproved) {
        Long currentUserId = getCurrentUserId();

        SvVideo video = svVideoMapper.selectById(id);
        if (video == null || Boolean.TRUE.equals(video.getDeleted())) {
            throw new NoSuchElementException("视频不存在");
        }
        // 权限校验：只能查看自己的视频
        if (!currentUserId.equals(video.getUserId())) {
            throw new SecurityException("无权查看该视频");
        }
        if (!includeUnapproved && !"APPROVED".equalsIgnoreCase(video.getAuditStatus())) {
            throw new NoSuchElementException("视频不存在");
        }
        return toDetailResp(video);
    }

    /**
     * 分页查询视频列表（不返回 file_path）。
     * <p>
     * 查询能力：
     * - keyword：标题/描述模糊搜索
     * - tag：标签精确过滤
     * - sort：多字段排序（字段映射白名单，避免任意字段排序风险）
     * <p>
     * 分页参数：
     * - pageNum >= 1
     * - 1 <= pageSize <= 100
     */
    @Override
    @Transactional(readOnly = true)
    public PageResp<VideoListItemResp> page(int pageNum, int pageSize, String keyword, String tag, List<String> sort, boolean includeUnapproved) {
        Long currentUserId = getCurrentUserId();
        // 分页参数上限约束：pageNum>=1，pageSize<=100
        if (pageNum < 1) {
            throw new IllegalArgumentException("pageNum必须>=1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("pageSize必须在1-100之间");
        }

        // 构建查询条件
        QueryWrapper<SvVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId)
                .eq("deleted", false);

        if (!includeUnapproved) {
            queryWrapper.eq("audit_status", "APPROVED");
        }
        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            queryWrapper.and(wrapper -> wrapper
                    .like("title", like)
                    .or()
                    .like("description", like)
            );
        }
        if (StringUtils.hasText(tag)) {
            String t = tag.trim();
            queryWrapper.like("tags", "%," + t + ",%");
        }

        // 应用排序
        applySorting(queryWrapper, sort);

        // 执行分页查询
        Page<SvVideo> mpPage = new Page<>(pageNum, pageSize);
        Page<SvVideo> page = svVideoMapper.selectPage(mpPage, queryWrapper);

        List<VideoListItemResp> list = convertToListItemRespList(page.getRecords());

        PageResp<VideoListItemResp> resp = new PageResp<>();
        resp.setTotal(page.getTotal());
        resp.setList(list);
        return resp;
    }

    /**
     * 批量转换视频列表项，优化 N+1 查询问题
     */
    private List<VideoListItemResp> convertToListItemRespList(List<SvVideo> videos) {
        if (videos == null || videos.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 收集所有 videoId 和 userId
        List<Long> videoIds = videos.stream().map(SvVideo::getId).collect(Collectors.toList());
        List<Long> userIds = videos.stream().map(SvVideo::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());

        // 2. 批量查询评论数
        Map<Long, Long> commentCountMap = new HashMap<>();
        if (!videoIds.isEmpty()) {
            List<Map<String, Object>> counts = commentMapper.batchCountCommentsByVideoIds(videoIds);
            for (Map<String, Object> row : counts) {
                Long vid = ((Number) row.get("video_id")).longValue();
                Long cnt = ((Number) row.get("count")).longValue();
                commentCountMap.put(vid, cnt);
            }
        }

        // 3. 批量查询用户名
        Map<Long, String> userNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<Map<String, Object>> users = userMapper.batchSelectUsersByIds(userIds);
            for (Map<String, Object> row : users) {
                Long uid = ((Number) row.get("id")).longValue();
                String name = (String) row.get("name");
                userNameMap.put(uid, name);
            }
        }

        // 4. 内存组装
        Map<Long, Long> finalCommentCountMap = commentCountMap;
        Map<Long, String> finalUserNameMap = userNameMap;
        return videos.stream().map(v -> {
            VideoListItemResp resp = new VideoListItemResp();
            resp.setId(v.getId());
            resp.setTitle(v.getTitle());
            resp.setDescription(v.getDescription());
            resp.setTags(decodeTags(v.getTags()));
            resp.setCoverUrl(v.getCoverUrl());
            resp.setFileSize(v.getFileSize());
            resp.setDuration(v.getDuration());
            resp.setStatus(v.getStatus());
            resp.setAuditStatus(v.getAuditStatus());
            resp.setViews(v.getViews());
            resp.setLikes(v.getLikes());
            resp.setComments(finalCommentCountMap.getOrDefault(v.getId(), 0L));
            resp.setCreateTime(v.getCreateTime());
            resp.setUpdateTime(v.getUpdateTime());
            if (v.getUserId() != null) {
                resp.setUserName(finalUserNameMap.get(v.getUserId()));
            }
            return resp;
        }).collect(Collectors.toList());
    }

    /**
     * 软删除：仅将 deleted=true 并更新时间（updateTime 由 @PreUpdate 自动维护）。
     * <p>
     * 约束：
     * - 若记录不存在或已删除，对外表现为 404
     */
    @Override
    @Transactional
    public void softDelete(Long id) {
        Long currentUserId = getCurrentUserId();
        // 软删除只更新 deleted 与 updateTime（updateTime 由 @PreUpdate 自动维护）
        SvVideo video = svVideoMapper.selectById(id);
        if (video == null || Boolean.TRUE.equals(video.getDeleted())) {
            throw new NoSuchElementException("视频不存在");
        }
        // 权限校验：只能删除自己的视频
        if (!currentUserId.equals(video.getUserId())) {
            throw new SecurityException("无权删除该视频");
        }
        video.setDeleted(true);
        svVideoMapper.updateById(video);
    }

    @Override
    @Transactional
    public void incrementViews(Long id) {
        UpdateWrapper<SvVideo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("views = views + 1")
                .eq("id", id)
                .eq("deleted", false);
        svVideoMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional
    public void incrementLikes(Long id) {
        UpdateWrapper<SvVideo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("likes = likes + 1")
                .eq("id", id)
                .eq("deleted", false);
        svVideoMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional
    public Long likeAndReturnCount(Long id) {
        // 先增加点赞数
        incrementLikes(id);

        // 查询最新的点赞数
        SvVideo video = svVideoMapper.selectById(id);
        if (video == null) {
            throw new java.util.NoSuchElementException("视频不存在");
        }
        return video.getLikes();
    }

    private void applySorting(QueryWrapper<SvVideo> queryWrapper, List<String> sort) {
        // sort 参数支持多字段：sort=createTime,desc&sort=title,asc
        if (sort == null || sort.isEmpty()) {
            queryWrapper.orderByDesc("create_time");
            return;
        }

        for (String s : sort) {
            if (!StringUtils.hasText(s)) {
                continue;
            }
            String[] parts = s.split(",");
            if (parts.length < 1) {
                continue;
            }
            String field = parts[0].trim();
            String dir = parts.length >= 2 ? parts[1].trim() : "asc";
            String mapped = mapSortableField(field);
            if (mapped == null) {
                continue;
            }
            if ("desc".equalsIgnoreCase(dir)) {
                queryWrapper.orderByDesc(mapped);
            } else {
                queryWrapper.orderByAsc(mapped);
            }
        }
    }

    private void validateUpload(MultipartFile file, String title, List<String> tags) {
        // 文件校验：非空、大小限制（默认 100MB，可配置）
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("文件大小超过限制");
        }
        // 标题校验：1-128 字符
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("标题不能为空");
        }
        String t = title.trim();
        if (t.length() < 1 || t.length() > 128) {
            throw new IllegalArgumentException("标题长度需在1-128字符之间");
        }

        // 格式白名单：mp4/mov/avi（可配置）
        String ext = getExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        Set<String> white = Arrays.stream(allowedExt.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(s -> s.startsWith(".") ? s : "." + s)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (!white.contains(ext)) {
            throw new IllegalArgumentException("文件格式不支持");
        }

        // 标签校验：最多 5 个，每个不超过 16 字符
        List<String> tagList = normalizeTags(tags);
        if (tagList.size() > 5) {
            throw new IllegalArgumentException("标签最多5个");
        }
        for (String tag : tagList) {
            if (tag.length() > 16) {
                throw new IllegalArgumentException("单个标签不超过16字符");
            }
        }
    }

    private void validateCover(MultipartFile cover) {
        if (cover == null || cover.isEmpty()) {
            return;
        }
        if (cover.getSize() > coverMaxBytes) {
            throw new IllegalArgumentException("封面大小超过限制");
        }
        String ext = getExtension(cover.getOriginalFilename()).toLowerCase(Locale.ROOT);
        Set<String> white = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".png", ".webp"));
        if (white.contains(ext)) {
            return;
        }
        String ct = cover.getContentType();
        if (ct != null) {
            String c = ct.toLowerCase(Locale.ROOT);
            if (c.startsWith("image/")) {
                return;
            }
        }
        throw new IllegalArgumentException("封面格式不支持");
    }

    private List<String> normalizeTags(List<String> raw) {
        // 兼容 tags=旅行&tags=美食 与 tags=旅行,美食 两种传法，并去重保持顺序
        if (raw == null) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String item : raw) {
            if (!StringUtils.hasText(item)) {
                continue;
            }
            String[] parts = item.split(",");
            for (String p : parts) {
                String s = p == null ? null : p.trim();
                if (StringUtils.hasText(s)) {
                    set.add(s);
                }
            }
        }
        return new ArrayList<>(set);
    }

    private String encodeTags(List<String> tags) {
        // 以逗号包裹，便于用 "%,tag,%" 做精确匹配
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return "," + String.join(",", tags) + ",";
    }

    private List<String> decodeTags(String tags) {
        // 将 ",旅行,美食," 还原为 ["旅行","美食"]
        if (!StringUtils.hasText(tags)) {
            return new ArrayList<>();
        }
        String s = tags.trim();
        if (s.startsWith(",")) {
            s = s.substring(1);
        }
        if (s.endsWith(",")) {
            s = s.substring(0, s.length() - 1);
        }
        if (!StringUtils.hasText(s)) {
            return new ArrayList<>();
        }
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
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

    private String mapSortableField(String field) {
        // 对外字段映射到数据库字段，避免任意字段排序带来的 SQL 注入风险
        if (!StringUtils.hasText(field)) {
            return null;
        }
        switch (field) {
            case "createTime":
                return "create_time";
            case "updateTime":
                return "update_time";
            case "title":
                return "title";
            case "fileSize":
                return "file_size";
            case "duration":
                return "duration";
            case "status":
                return "status";
            default:
                return null;
        }
    }

    private VideoDetailResp toDetailResp(SvVideo v) {
        VideoDetailResp resp = new VideoDetailResp();
        resp.setId(v.getId());
        resp.setTitle(v.getTitle());
        resp.setDescription(v.getDescription());
        resp.setTags(decodeTags(v.getTags()));
        resp.setFilePath(v.getFilePath());
        resp.setCoverUrl(v.getCoverUrl());
        resp.setFileSize(v.getFileSize());
        resp.setDuration(v.getDuration());
        resp.setStatus(v.getStatus());
        resp.setAuditStatus(v.getAuditStatus());
        resp.setViews(v.getViews());
        resp.setLikes(v.getLikes());
        resp.setCreateTime(v.getCreateTime());
        resp.setUpdateTime(v.getUpdateTime());
        resp.setDeleted(v.getDeleted());
        resp.setVersion(v.getVersion());
        
        // 查询发布用户的用户名
        if (v.getUserId() != null) {
            User user = userMapper.selectById(v.getUserId());
            if (user != null) {
                resp.setUserName(user.getName());
            }
        }
        
        return resp;
    }

    private VideoListItemResp toListItemResp(SvVideo v) {
        VideoListItemResp resp = new VideoListItemResp();
        resp.setId(v.getId());
        resp.setTitle(v.getTitle());
        resp.setDescription(v.getDescription());
        resp.setTags(decodeTags(v.getTags()));
        resp.setCoverUrl(v.getCoverUrl());
        resp.setFileSize(v.getFileSize());
        resp.setDuration(v.getDuration());
        resp.setStatus(v.getStatus());
        resp.setAuditStatus(v.getAuditStatus());
        resp.setViews(v.getViews());
        resp.setLikes(v.getLikes());
        resp.setComments(commentService.getCommentCountByVideoId(v.getId()));
        resp.setCreateTime(v.getCreateTime());
        resp.setUpdateTime(v.getUpdateTime());
        
        // 查询发布用户的用户名
        if (v.getUserId() != null) {
            User user = userMapper.selectById(v.getUserId());
            if (user != null) {
                resp.setUserName(user.getName());
            }
        }
        
        return resp;
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new SecurityException("用户未登录");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResp<VideoListItemResp> pagePublic(int pageNum, int pageSize, String keyword, String tag, List<String> sort) {
        if (pageNum < 1) throw new IllegalArgumentException("pageNum必须>=1");
        if (pageSize < 1 || pageSize > 100) throw new IllegalArgumentException("pageSize必须在1-100之间");

        QueryWrapper<SvVideo> queryWrapper = new QueryWrapper<>();
        // 公开列表：只看已审核且未删除的视频，不限制 user_id
        queryWrapper.eq("audit_status", "APPROVED")
                .eq("deleted", false);

        if (StringUtils.hasText(keyword)) {
            String like = "%" + keyword.trim() + "%";
            queryWrapper.and(wrapper -> wrapper.like("title", like).or().like("description", like));
        }
        if (StringUtils.hasText(tag)) {
            queryWrapper.like("tags", "%," + tag.trim() + ",%");
        }

        applySorting(queryWrapper, sort);

        Page<SvVideo> mpPage = new Page<>(pageNum, pageSize);
        Page<SvVideo> page = svVideoMapper.selectPage(mpPage, queryWrapper);

        List<VideoListItemResp> list = convertToListItemRespList(page.getRecords());

        PageResp<VideoListItemResp> resp = new PageResp<>();
        resp.setTotal(page.getTotal());
        resp.setList(list);
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public VideoDetailResp getPublic(Long id) {
        SvVideo video = svVideoMapper.selectById(id);
        if (video == null || Boolean.TRUE.equals(video.getDeleted())) {
            throw new NoSuchElementException("视频不存在");
        }
        // 公开接口只允许查看已审核通过的视频
        if (!"APPROVED".equalsIgnoreCase(video.getAuditStatus())) {
            throw new NoSuchElementException("视频不存在");
        }
        return toDetailResp(video);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoListItemResp> getRelatedVideos(Long currentVideoId, int limit) {
        SvVideo current = svVideoMapper.selectById(currentVideoId);
        if (current == null) {
            return Collections.emptyList();
        }

        QueryWrapper<SvVideo> qw = new QueryWrapper<>();
        qw.eq("audit_status", "APPROVED")
          .eq("deleted", false)
          .ne("id", currentVideoId);

        // 如果有标签，优先匹配包含相同标签的视频
        if (StringUtils.hasText(current.getTags())) {
            // tags 格式为 ,tag1,tag2,
            String[] tags = current.getTags().split(",");
            if (tags.length > 0 && StringUtils.hasText(tags[1])) { // tags[0] is empty due to leading comma
                // 使用 OR 匹配任意一个标签
                qw.and(wrapper -> {
                    for (int i = 1; i < tags.length; i++) {
                        if (StringUtils.hasText(tags[i])) {
                            wrapper.or().like("tags", "," + tags[i] + ",");
                        }
                    }
                });
            }
        }

        // 按热度（播放量）和发布时间排序
        qw.orderByDesc("views")
          .orderByDesc("create_time")
          .last("LIMIT " + limit);

        List<SvVideo> related = svVideoMapper.selectList(qw);
        return convertToListItemRespList(related);
    }

}
