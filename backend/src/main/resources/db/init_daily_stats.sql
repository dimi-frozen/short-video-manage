-- 初始化本地视频每日统计快照表
-- 此脚本用于手动创建昨日的统计快照，方便测试

USE short_video_db;

-- 插入昨日的统计快照（基于当前数据库的真实数据）
INSERT INTO local_video_daily_stats (stats_date, total_videos, total_views, total_likes, total_comments, snapshot_time, created_at)
SELECT 
    CURDATE() - INTERVAL 1 DAY AS stats_date,
    COUNT(*) AS total_videos,
    COALESCE(SUM(views), 0) AS total_views,
    COALESCE(SUM(likes), 0) AS total_likes,
    (SELECT COUNT(c.id) FROM comment c INNER JOIN sv_video v ON c.video_id = v.id WHERE v.deleted = 0) AS total_comments,
    NOW() AS snapshot_time,
    NOW() AS created_at
FROM sv_video
WHERE deleted = 0;

-- 查询验证
SELECT * FROM local_video_daily_stats ORDER BY stats_date DESC LIMIT 5;
