package com.shortvideo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video_info")
public class VideoInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String videoId;

    private String title;

    private LocalDateTime publishTime;

    private String platformType;

    private Long views;

    private Long likes;

    private Long comments;

    private Long shares;

    private String filePath;

    private Long fileSize;

    private String status;

    @TableField("user_id")
    private Long userId;

    @TableLogic
    private Integer deleted;

    @Version
    private Long version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}

