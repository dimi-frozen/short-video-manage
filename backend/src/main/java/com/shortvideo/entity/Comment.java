package com.shortvideo.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @TableField("video_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long videoId;

    @TableField("parent_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;  // 父评论ID，NULL表示一级评论

    @TableField("user_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    @TableField("user_name")
    private String userName;  // 评论用户名称（冗余字段，避免JOIN查询）

    @TableField(exist = false)
    private String replyToUserName;  // 回复的目标用户名（仅用于显示）

    @TableField
    private String content;

    @TableField("is_deleted")
    private Boolean isDeleted = false;  // 软删除标记

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
