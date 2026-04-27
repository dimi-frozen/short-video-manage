<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索视频标题"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleSearch"
        style="width: 400px"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 视频卡片网格 -->
    <div class="video-grid">
      <div
        v-for="video in videoList"
        :key="video.id"
        class="video-card"
        @click="handleVideoClick(video)"
      >
        <!-- 视频封面区 -->
        <div class="video-cover">
          <img loading="lazy" :src="video.coverUrl || '/placeholder-cover.jpg'" :alt="video.title" />
          <!-- 视频时长 -->
          <div class="video-duration" v-if="video.duration">
            <el-icon><Clock /></el-icon>
            <span>{{ formatDuration(video.duration) }}</span>
          </div>
        </div>
        
        <!-- 视频信息区 -->
        <div class="video-info">
          <!-- 标题（最多两行） -->
          <h3 class="video-title" :title="video.title">{{ video.title }}</h3>
          
          <!-- 数据统计行 -->
          <div class="video-stats-row">
            <span class="stat-item">
              <el-icon><VideoPlay /></el-icon>
              {{ formatNumber(video.views || 0) }}
            </span>
            <span class="stat-item">
              <el-icon><ChatDotRound /></el-icon>
              {{ formatNumber(video.comments || 0) }}
            </span>
            <span class="stat-item">
              <el-icon><Clock /></el-icon>
              {{ video.duration ? formatDuration(video.duration) : '-' }}
            </span>
          </div>
          
          <!-- 作者和发布时间行 -->
          <div class="video-author-row" v-if="video.userName">
            <span class="author-name">
              <el-icon><User /></el-icon>
              {{ video.userName }}
            </span>
            <span class="publish-date">{{ formatPublishDate(video.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="currentPage"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-if="videoList.length === 0 && !loading">
      <el-empty description="暂无视频" />
    </div>

    <!-- 加载状态 -->
    <div class="loading" v-if="loading">
      <el-skeleton :rows="6" animated />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, VideoPlay, Star, User, Clock, ChatDotRound } from '@element-plus/icons-vue'
import { getPublicVideoList } from '@/api/video' // 替换为公开接口

interface VideoItem {
  id: number
  title: string
  description?: string
  coverUrl?: string
  views?: number
  likes?: number
  comments?: number
  duration?: number
  createTime?: string
  userName?: string
}

const router = useRouter()
const searchKeyword = ref('')
const videoList = ref<VideoItem[]>([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

// 获取视频列表
const fetchVideoList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined
    }
    
    const res: any = await getPublicVideoList(params) // 使用公开接口
    if (res.code === 0) {
      videoList.value = res.data.list || []
      total.value = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取视频列表失败')
    }
  } catch (error) {
    ElMessage.error('获取视频列表失败')
    console.error('Error fetching video list:', error)
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchVideoList()
}

// 分页切换
const handlePageChange = (page: number) => {
  currentPage.value = page
  fetchVideoList()
}

// 视频点击
const handleVideoClick = (video: VideoItem) => {
  router.push(`/dashboard/play/${video.id}`)
}

// 格式化数字
const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

// 格式化视频时长（秒转为 mm:ss）
const formatDuration = (seconds: number): string => {
  if (!seconds) return ''
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

// 格式化发布时间（月-日）
const formatPublishDate = (dateStr?: string): string => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

// 截断文本
const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text
  return text.slice(0, maxLength) + '...'
}

onMounted(() => {
  fetchVideoList()
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.video-card {
  background: white;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s ease;
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 视频封面区 */
.video-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
  background: #f3f4f6;
}

.video-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.video-card:hover .video-cover img {
  transform: scale(1.05);
}

/* 视频时长标签 */
.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-duration .el-icon {
  font-size: 12px;
}

.video-info {
  padding: 12px;
}

/* 视频标题 */
.video-title {
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 8px 0;
  color: #1f2937;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

/* 数据统计行 */
.video-stats-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #6b7280;
  font-size: 13px;
  margin-bottom: 8px;
}

.video-stats-row .stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.video-stats-row .el-icon {
  font-size: 14px;
}

/* 作者和发布时间行 */
.video-author-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #6b7280;
  font-size: 12px;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
}

.video-author-row .author-name {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #9ca3af;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.video-author-row .el-icon {
  font-size: 14px;
  flex-shrink: 0;
}

.video-author-row .publish-date {
  color: #9ca3af;
  flex-shrink: 0;
  margin-left: 8px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading {
  padding: 40px 0;
}

@media (max-width: 768px) {
  .video-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 16px;
  }
  
  .home-page {
    padding: 16px;
  }
  
  .search-bar {
    margin-bottom: 20px;
  }
}
</style>