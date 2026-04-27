package com.shortvideo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortvideo.dto.analysis.LocalDashboardVO;
import com.shortvideo.dto.analysis.LocalTrendPointVO;
import com.shortvideo.entity.LocalVideoDailyStats;
import com.shortvideo.mapper.LocalVideoDailyStatsMapper;
import com.shortvideo.mapper.SvVideoMapper;
import com.shortvideo.service.LocalDashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LocalDashboardServiceImpl implements LocalDashboardService {

    private final SvVideoMapper svVideoMapper;
    private final LocalVideoDailyStatsMapper dailyStatsMapper;

    public LocalDashboardServiceImpl(SvVideoMapper svVideoMapper,
                                     LocalVideoDailyStatsMapper dailyStatsMapper) {
        this.svVideoMapper = svVideoMapper;
        this.dailyStatsMapper = dailyStatsMapper;
    }

    @Override
    public LocalDashboardVO dashboard(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDashboardVO vo = new LocalDashboardVO();

        // 获取视频趋势数据（播放量、点赞数）
        List<LocalTrendPointVO> videoTrend = svVideoMapper.selectLocalTrend(startTime, endTime);
        
        // 获取评论趋势数据
        List<LocalTrendPointVO> commentTrend = svVideoMapper.selectLocalCommentTrend(startTime, endTime);
        
        // 将评论数据合并到视频趋势中
        if (videoTrend != null && commentTrend != null) {
            // 创建一个以日期为key的Map，方便快速查找
            java.util.Map<String, Long> commentMap = new java.util.HashMap<>();
            for (LocalTrendPointVO point : commentTrend) {
                commentMap.put(point.getDay(), point.getComments() != null ? point.getComments() : 0L);
            }
            
            // 将评论数填充到视频趋势中
            for (LocalTrendPointVO point : videoTrend) {
                point.setComments(commentMap.getOrDefault(point.getDay(), 0L));
            }
        }
        
        vo.setTrend(videoTrend);

        // 直接从数据库查询总量（不依赖趋势数据累加）
        Long totalVideosCount = svVideoMapper.selectLocalVideoCount(startTime, endTime);
        vo.setTotalVideos(totalVideosCount != null ? totalVideosCount : 0L);

        // 查询总播放量、总点赞量、总评论数
        if (videoTrend != null && !videoTrend.isEmpty()) {
            long totalViews = 0;
            long totalLikes = 0;
            long totalComments = 0;
            
            for (LocalTrendPointVO point : videoTrend) {
                totalViews += (point.getViews() != null ? point.getViews() : 0);
                totalLikes += (point.getLikes() != null ? point.getLikes() : 0);
                totalComments += (point.getComments() != null ? point.getComments() : 0);
            }
            
            vo.setTotalViews(totalViews);
            vo.setTotalLikes(totalLikes);
            vo.setTotalComments(totalComments);
        } else {
            vo.setTotalViews(0L);
            vo.setTotalLikes(0L);
            vo.setTotalComments(0L);
        }
        
        // 查询昨日的统计快照（用于计算日增量）
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LambdaQueryWrapper<LocalVideoDailyStats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LocalVideoDailyStats::getStatsDate, yesterday);
        LocalVideoDailyStats yesterdayStats = dailyStatsMapper.selectOne(queryWrapper);
        
        if (yesterdayStats != null) {
            LocalDashboardVO.YesterdaySnapshotVO snapshot = new LocalDashboardVO.YesterdaySnapshotVO();
            snapshot.setTotalVideos(yesterdayStats.getTotalVideos());
            snapshot.setTotalViews(yesterdayStats.getTotalViews());
            snapshot.setTotalLikes(yesterdayStats.getTotalLikes());
            snapshot.setTotalComments(yesterdayStats.getTotalComments());
            vo.setYesterdaySnapshot(snapshot);
        }

        return vo;
    }
}
