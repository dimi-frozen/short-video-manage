package com.shortvideo.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shortvideo.entity.SvVideo;
import com.shortvideo.mapper.SvVideoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class VideoCleanupJob {

    private final SvVideoMapper svVideoMapper;

    @Value("${video.cleanup.keep-days:30}")
    private int keepDays;

    public VideoCleanupJob(SvVideoMapper svVideoMapper) {
        this.svVideoMapper = svVideoMapper;
    }

    /**
     * 每天 02:00 扫描软删除且超期的数据：
     * - 删除本地物理文件
     * - 清理数据库记录
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanup() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(keepDays);
        
        // 构建查询条件：deleted=true 且 updateTime < threshold
        QueryWrapper<SvVideo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", true)
                   .lt("update_time", threshold);
        
        List<SvVideo> candidates = svVideoMapper.selectList(queryWrapper);

        for (SvVideo v : candidates) {
            String path = v.getFilePath();
            if (path != null && !path.isEmpty() && !(path.startsWith("http://") || path.startsWith("https://"))) {
                try {
                    File f = new File(path);
                    if (f.exists() && !f.delete()) {
                        log.warn("delete file failed, id={}, path={}", v.getId(), path);
                    }
                } catch (Exception e) {
                    log.warn("delete file error, id={}, path={}, err={}", v.getId(), path, e.getMessage());
                }
            }
            // 数据库清理
            svVideoMapper.deleteById(v.getId());
            log.info("cleanup video, id={}", v.getId());
        }
    }
}
