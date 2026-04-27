package com.shortvideo.dto.analysis;

import lombok.Data;

import java.util.List;

/**
 * 本平台（本地视频）Dashboard VO
 */
@Data
public class LocalDashboardVO {
    /** 视频总数 */
    private Long totalVideos;
    /** 总播放量 */
    private Long totalViews;
    /** 总点赞量 */
    private Long totalLikes;
    /** 总评论数 */
    private Long totalComments;
    /** 趋势数据（按天） */
    private List<LocalTrendPointVO> trend;
    
    /** 昨日统计快照（用于计算日增量） */
    private YesterdaySnapshotVO yesterdaySnapshot;
    
    /**
     * 昨日统计快照 VO
     */
    @Data
    public static class YesterdaySnapshotVO {
        /** 昨日的视频总数 */
        private Long totalVideos;
        /** 昨日的总播放量 */
        private Long totalViews;
        /** 昨日的总点赞量 */
        private Long totalLikes;
        /** 昨日的总评论数 */
        private Long totalComments;
    }
}
