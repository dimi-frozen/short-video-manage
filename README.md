# 多平台短视频管理与数据分析系统

<div align="center">

![Vue](https://img.shields.io/badge/Vue-3.2-4FC08D?logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7-6DB33F?logo=spring-boot)
![TypeScript](https://img.shields.io/badge/TypeScript-4.9-3178C6?logo=typescript)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)

一个功能完善的短视频管理平台，支持视频上传、管理、多平台发布和数据分析。

</div>

## 📖 项目简介

本项目是一个前后端分离的短视频管理系统，旨在为短视频创作者提供统一的内容管理和数据分析平台。系统支持本地视频管理、多平台（抖音、快手、B站）模拟发布、数据统计分析和可视化展示。

### ✨ 核心特性

- 🔐 **用户认证与权限管理**：基于 JWT 的用户登录注册和角色权限控制
- 📹 **视频管理**：视频上传、编辑、删除、标签分类和审核流程
- 🌐 **多平台发布**：支持抖音、快手、B站等多平台模拟发布
- 📊 **数据分析**：播放量、点赞数、评论数等数据统计和趋势分析
- 📈 **数据可视化**：使用 ECharts 实现丰富的图表展示
- 🔍 **智能搜索**：支持标题、描述、标签的多维度搜索
- 👥 **社交互动**：评论系统和用户互动功能
- 🎯 **热门排行**：基于播放量和点赞数的视频排行榜

## 🛠️ 技术栈

### 后端技术
- **框架**: Spring Boot 2.7.12
- **ORM**: MyBatis Plus 3.5.3.1
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **安全**: Spring Security + JWT
- **存储**: 阿里云 OSS
- **其他**: Lombok, FastJSON, Commons Lang3

### 前端技术
- **框架**: Vue 3.2 + TypeScript 4.9
- **构建工具**: Vite 4.3
- **UI 组件库**: Element Plus 2.3
- **状态管理**: Pinia 2.0
- **路由**: Vue Router 4.1
- **图表**: ECharts 6.0
- **样式**: Tailwind CSS 3.3
- **图标**: Lucide Icons
- **HTTP 客户端**: Axios 1.3

## 📁 项目结构

```
short_video_manage/
├── backend/                    # 后端 Spring Boot 项目
│   ├── src/main/java/
│   │   └── com/shortvideo/
│   │       ├── config/        # 配置类（Security, JWT, MyBatis等）
│   │       ├── controller/    # 控制器层
│   │       ├── dto/           # 数据传输对象
│   │       ├── entity/        # 实体类
│   │       ├── enums/         # 枚举类
│   │       ├── job/           # 定时任务
│   │       ├── mapper/        # MyBatis Mapper
│   │       ├── service/       # 业务逻辑层
│   │       └── utils/         # 工具类
│   ├── src/main/resources/
│   │   ├── db/                # 数据库初始化脚本
│   │   └── application.yml    # 应用配置文件
│   └── pom.xml                # Maven 依赖配置
├── frontend/                   # 前端 Vue 项目
│   ├── src/
│   │   ├── api/               # API 接口封装
│   │   ├── components/        # 公共组件
│   │   ├── router/            # 路由配置
│   │   ├── store/             # Pinia 状态管理
│   │   ├── utils/             # 工具函数
│   │   └── views/             # 页面组件
│   ├── package.json           # npm 依赖配置
│   └── vite.config.ts         # Vite 配置
└── README.md                   # 项目说明文档
```

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Node.js 16+ 
- MySQL 8.0+
- Redis 6.0+
- Maven 3.6+

### 1. 数据库准备

创建 MySQL 数据库并执行初始化脚本：

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE short_video_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 使用数据库
USE short_video_db;

# 执行初始化脚本
source backend/src/main/resources/db/init.sql;
source backend/src/main/resources/db/init_daily_stats.sql;
```

### 2. 后端启动

#### 配置数据库连接

编辑 `backend/src/main/resources/application.yml`，修改以下配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/short_video_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    password: your_redis_password  # 如果没有密码则留空

# 阿里云 OSS 配置（可选）
oss:
  endpoint: your_endpoint
  accessKeyId: your_access_key_id
  accessKeySecret: your_access_key_secret
  bucketName: your_bucket_name
```

#### 启动后端服务

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

或者直接运行主类：`com.shortvideo.ShortVideoApplication`

后端服务将在 `http://localhost:8080` 启动

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端应用将在 `http://localhost:5173` 启动

## 📝 API 文档

### 主要接口

#### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/profile` - 获取用户信息

#### 视频管理
- `POST /api/videos/upload` - 上传视频
- `GET /api/videos/public` - 获取公开视频列表
- `GET /api/videos/{id}` - 获取视频详情
- `PUT /api/videos/{id}` - 更新视频信息
- `DELETE /api/videos/{id}` - 删除视频

#### 数据分析
- `GET /api/analysis/overview` - 获取数据概览
- `GET /api/analysis/trend` - 获取趋势数据
- `GET /api/analysis/ranking` - 获取排行榜

#### 平台发布
- `POST /api/platform/publish` - 发布到指定平台
- `GET /api/video-info/page` - 查询平台发布记录

## 💡 使用说明

### 用户操作流程

1. **注册/登录**：访问系统首页，完成用户注册或登录
2. **上传视频**：在"我的视频"页面上传视频文件，填写标题、描述和标签
3. **查看视频**：在首页浏览公开视频，支持搜索和筛选
4. **数据分析**：在"数据分析"页面查看统计图表和趋势
5. **平台发布**：选择视频后，可模拟发布到抖音、快手、B站等平台
6. **查看评论**：在视频详情页查看和管理评论

### 管理员功能

- 视频审核：审核用户上传的视频内容
- 用户管理：查看和管理系统用户
- 数据统计：查看全平台的数据统计信息

## 🔧 开发指南

### 后端开发

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn package

# 运行 JAR 包
java -jar target/short-video-manage-0.0.1-SNAPSHOT.jar
```

### 前端开发

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 📊 数据库设计

### 主要数据表

- `users` - 用户表
- `sv_video` - 本地视频表
- `video_info` - 平台发布信息表
- `comment` - 评论表
- `local_video_daily_stats` - 本地视频每日统计表
- `video_publish_record` - 视频发布记录表

详细表结构请参考 `backend/src/main/resources/db/init.sql`

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 👥 作者

- 开发者团队

## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis Plus](https://baomidou.com/)
- [ECharts](https://echarts.apache.org/)

---

<div align="center">

如果这个项目对你有帮助，请给一个 ⭐ Star！

</div>
