package com.shortvideo.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shortvideo.entity.LocalVideoDailyStats;
import com.shortvideo.mapper.LocalVideoDailyStatsMapper;
import com.shortvideo.mapper.SvVideoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 本地视频每日统计快照任务
 * 每日0点执行，记录当天所有本地视频的真实统计数据
 */
@Slf4j
@Component
public class LocalVideoDailyStatsJob {

    private final SvVideoMapper svVideoMapper;
    private final LocalVideoDailyStatsMapper dailyStatsMapper;

    public LocalVideoDailyStatsJob(SvVideoMapper svVideoMapper, 
                                   LocalVideoDailyStatsMapper dailyStatsMapper) {
        this.svVideoMapper = svVideoMapper;
        this.dailyStatsMapper = dailyStatsMapper;
    }

    /**
     * 每日0点执行，生成前一天的统计快照
     * cron表达式：0 0 0 * * ? 表示每天0点执行
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void createDailySnapshot() {
        log.info("开始执行本地视频每日统计快照任务");

        // 统计日期为昨天
        LocalDate statsDate = LocalDate.now().minusDays(1);
        
        // 检查是否已经存在该日期的快照
        LambdaQueryWrapper<LocalVideoDailyStats> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LocalVideoDailyStats::getStatsDate, statsDate);
        Long count = dailyStatsMapper.selectCount(queryWrapper);
        
        if (count > 0) {
            log.warn("日期 {} 的统计快照已存在，跳过", statsDate);
            return;
        }

        try {
            // 查询截至昨天的累计数据（所有未删除视频的当前真实值）
            LocalDateTime endTime = statsDate.atTime(23, 59, 59);
            
            Long totalVideos = svVideoMapper.selectLocalVideoCount(null, endTime);
            Long totalViews = svVideoMapper.selectLocalTotalViews(null, endTime);
            Long totalLikes = svVideoMapper.selectLocalTotalLikes(null, endTime);
            Long totalComments = svVideoMapper.selectLocalTotalComments(null, endTime);

            // 创建快照记录
            LocalVideoDailyStats snapshot = new LocalVideoDailyStats();
            snapshot.setStatsDate(statsDate);
            snapshot.setTotalVideos(totalVideos != null ? totalVideos : 0L);
            snapshot.setTotalViews(totalViews != null ? totalViews : 0L);
            snapshot.setTotalLikes(totalLikes != null ? totalLikes : 0L);
            snapshot.setTotalComments(totalComments != null ? totalComments : 0L);
            snapshot.setSnapshotTime(LocalDateTime.now());
            snapshot.setCreatedAt(LocalDateTime.now());

            dailyStatsMapper.insert(snapshot);

            log.info("本地视频每日统计快照创建成功: date={}, videos={}, views={}, likes={}, comments={}",
                    statsDate, snapshot.getTotalVideos(), snapshot.getTotalViews(), 
                    snapshot.getTotalLikes(), snapshot.getTotalComments());
        } catch (Exception e) {
            log.error("创建本地视频每日统计快照失败: date={}, error={}", statsDate, e.getMessage(), e);
            throw e;
        }
    }
}
