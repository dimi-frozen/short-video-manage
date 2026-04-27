package com.shortvideo.dto.analysis;

import lombok.Data;

/**
 * 本平台趋势点 VO
 */
@Data
public class LocalTrendPointVO {
    /** 日期 yyyy-MM-dd */
    private String day;
    /** 上传数量 */
    private Long uploadCount;
    /** 播放量 */
    private Long views;
    /** 点赞数 */
    private Long likes;
    /** 评论数 */
    private Long comments;
}
