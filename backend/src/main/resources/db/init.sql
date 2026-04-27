-- 创建数据库
CREATE DATABASE IF NOT EXISTS short_video_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE short_video_db;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL COMMENT '用户邮箱',
  password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希',
  name VARCHAR(100) NOT NULL COMMENT '用户昵称',
  role VARCHAR(20) DEFAULT 'user' COMMENT '角色: user, admin',
  avatar_url VARCHAR(500) COMMENT '头像URL',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='用户表';

-- 平台表
CREATE TABLE IF NOT EXISTS platforms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) UNIQUE NOT NULL COMMENT '平台名称',
  api_key VARCHAR(255) COMMENT 'API Key',
  api_secret VARCHAR(255) COMMENT 'API Secret',
  webhook_url VARCHAR(500) COMMENT '回调地址',
  status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active, inactive',
  config_json TEXT COMMENT '平台特定配置',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB COMMENT='平台表';

-- 视频表
CREATE TABLE IF NOT EXISTS videos (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '所属用户ID',
  platform_id BIGINT NOT NULL COMMENT '所属平台ID',
  title VARCHAR(255) NOT NULL COMMENT '视频标题',
  description TEXT COMMENT '视频描述',
  video_url VARCHAR(500) NOT NULL COMMENT '视频URL',
  cover_url VARCHAR(500) COMMENT '封面URL',
  category VARCHAR(50) COMMENT '视频分类',
  status VARCHAR(20) DEFAULT 'active' COMMENT '状态: active, deleted, private',
  upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '系统创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '系统更新时间',
  INDEX idx_user_id (user_id),
  INDEX idx_platform_id (platform_id),
  INDEX idx_category (category),
  INDEX idx_status (status)
) ENGINE=InnoDB COMMENT='视频表';

-- 视频统计表
CREATE TABLE IF NOT EXISTS video_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  video_id BIGINT NOT NULL COMMENT '视频ID',
  views BIGINT DEFAULT 0 COMMENT '播放量',
  likes BIGINT DEFAULT 0 COMMENT '点赞数',
  comments BIGINT DEFAULT 0 COMMENT '评论数',
  shares BIGINT DEFAULT 0 COMMENT '分享数',
  stats_date DATE NOT NULL COMMENT '统计日期',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY unique_video_date (video_id, stats_date),
  INDEX idx_video_id (video_id),
  INDEX idx_stats_date (stats_date)
) ENGINE=InnoDB COMMENT='视频统计表';

-- 标签表
CREATE TABLE IF NOT EXISTS tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) UNIQUE NOT NULL COMMENT '标签名称',
  color VARCHAR(7) DEFAULT '#1890ff' COMMENT '标签颜色',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB COMMENT='标签表';

-- 视频标签关联表
CREATE TABLE IF NOT EXISTS video_tags (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  video_id BIGINT NOT NULL COMMENT '视频ID',
  tag_id BIGINT NOT NULL COMMENT '标签ID',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY unique_video_tag (video_id, tag_id),
  INDEX idx_video_id (video_id),
  INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB COMMENT='视频标签关联表';

-- 初始数据
INSERT INTO platforms (name, api_key, api_secret) VALUES
('抖音', 'douyin_api_key_123', 'douyin_secret_456'),
('快手', 'kuaishou_api_key_789', 'kuaishou_secret_012'),
('B站', 'bilibili_api_key_345', 'bilibili_secret_678');

INSERT INTO tags (name, color) VALUES
('搞笑', '#ff4d4f'),
('美食', '#fa8c16'),
('旅行', '#52c41a'),
('科技', '#1890ff'),
('音乐', '#722ed1');

CREATE TABLE IF NOT EXISTS sv_video (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(128) NOT NULL,
  description TEXT,
  tags VARCHAR(512),
  file_path VARCHAR(1024) NOT NULL,
  cover_url VARCHAR(1024),
  file_size BIGINT NOT NULL,
  duration INT DEFAULT 0,
  status VARCHAR(20) NOT NULL,
  audit_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  views BIGINT NOT NULL DEFAULT 0,
  likes BIGINT NOT NULL DEFAULT 0,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  version BIGINT NOT NULL DEFAULT 0,
  INDEX idx_sv_video_title (title),
  INDEX idx_sv_video_tags (tags(32)),
  INDEX idx_sv_video_create_time (create_time),
  INDEX idx_sv_video_title_tag_time (title, tags(32), create_time)
) ENGINE=InnoDB COMMENT='短视频管理模块视频表';

CREATE TABLE IF NOT EXISTS video_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  video_id VARCHAR(64) NOT NULL COMMENT '平台视频ID（模拟）',
  title VARCHAR(128) NOT NULL COMMENT '标题',
  publish_time DATETIME NOT NULL COMMENT '发布时间',
  platform_type VARCHAR(20) NOT NULL COMMENT '平台类型：DOUYIN/KUAISHOU/BILIBILI',
  views BIGINT NOT NULL DEFAULT 0 COMMENT '播放量',
  likes BIGINT NOT NULL DEFAULT 0 COMMENT '点赞数',
  comments BIGINT NOT NULL DEFAULT 0 COMMENT '评论数',
  shares BIGINT NOT NULL DEFAULT 0 COMMENT '分享数',
  file_path VARCHAR(1024) NOT NULL COMMENT '文件路径',
  file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
  status VARCHAR(20) NOT NULL COMMENT '状态：PUBLISHED/FAILED',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除',
  version BIGINT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_video_info_video_id (video_id),
  INDEX idx_video_info_title (title),
  INDEX idx_video_info_platform_time (platform_type, publish_time),
  INDEX idx_video_info_platform_views (platform_type, views)
) ENGINE=InnoDB COMMENT='多平台视频信息表';

CREATE TABLE IF NOT EXISTS video_publish_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  video_id BIGINT NOT NULL,
  platform VARCHAR(32) NOT NULL COMMENT 'douyin/kuaishou/bilibili',
  status VARCHAR(20) NOT NULL COMMENT 'PENDING/SUCCESS',
  platform_video_url VARCHAR(1024),
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  INDEX idx_vpr_video (video_id),
  INDEX idx_vpr_platform_status (platform, status)
) ENGINE=InnoDB COMMENT='视频发布任务记录';

-- 本地视频每日统计快照表
CREATE TABLE IF NOT EXISTS local_video_daily_stats (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stats_date DATE NOT NULL COMMENT '统计日期',
  total_videos BIGINT NOT NULL DEFAULT 0 COMMENT '视频总数',
  total_views BIGINT NOT NULL DEFAULT 0 COMMENT '总播放量',
  total_likes BIGINT NOT NULL DEFAULT 0 COMMENT '总点赞数',
  total_comments BIGINT NOT NULL DEFAULT 0 COMMENT '总评论数',
  snapshot_time DATETIME NOT NULL COMMENT '快照时间',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY uk_stats_date (stats_date),
  INDEX idx_stats_date (stats_date)
) ENGINE=InnoDB COMMENT='本地视频每日统计快照表';
