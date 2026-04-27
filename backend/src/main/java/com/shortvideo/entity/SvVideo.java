package com.shortvideo.entity;

import com.baomidou.mybatisplus.annotation.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 短视频管理模块视频实体（MyBatis-Plus）
 *
 * tags 字段采用字符串编码：
 * - 存储形态：",旅行,美食,"（首尾逗号方便用 like 做精确标签过滤）
 * - 读取时解码为标签列表
 *
 * deleted=true 表示软删除；定时任务会对软删且超期的数据做物理清理。
 */
@Data
@TableName("sv_video")
public class SvVideo {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("tags")
    private String tags;

    @TableField("file_path")
    private String filePath;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("file_size")
    private Long fileSize;

    @TableField("duration")
    private Integer duration;

    @TableField("status")
    private String status;

    @TableField("audit_status")
    private String auditStatus = "PENDING";

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("views")
    private Long views = 0L;

    @TableField("likes")
    private Long likes = 0L;

    @TableField("deleted")
    private Boolean deleted = false;

    @Version
    @TableField("version")
    private Long version = 0L;
}
