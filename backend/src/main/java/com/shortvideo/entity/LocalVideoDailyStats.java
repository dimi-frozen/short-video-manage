package com.shortvideo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 本地视频每日统计快照实体
 */
@Data
@TableName("local_video_daily_stats")
public class LocalVideoDailyStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 统计日期 */
    private LocalDate statsDate;

    /** 视频总数 */
    private Long totalVideos;

    /** 总播放量 */
    private Long totalViews;

    /** 总点赞数 */
    private Long totalLikes;

    /** 总评论数 */
    private Long totalComments;

    /** 快照时间 */
    private LocalDateTime snapshotTime;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
