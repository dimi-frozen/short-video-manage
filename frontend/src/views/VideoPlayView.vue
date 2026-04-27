<template>
  <div class="video-play-page">
    <div class="main-container" v-loading="loading">
      <div class="left-col">
        <!-- 视频播放区 -->
        <div class="player-wrapper">
          <video
            v-if="videoDetail?.filePath"
            ref="videoPlayer"
            class="video-player"
            :src="videoDetail.filePath"
            :poster="videoDetail.coverUrl"
            controls
            controlsList="nodownload"
            autoplay
          ></video>
          <div v-else class="player-placeholder">
            <el-empty description="视频加载中..." />
          </div>
        </div>

        <!-- 视频信息区 -->
        <div class="video-info-box">
          <h1 class="video-title">{{ videoDetail?.title || '加载中...' }}</h1>
          
          <div class="video-meta">
            <span class="meta-item">
              <el-icon><Clock /></el-icon> {{ formatDate(videoDetail?.createTime) }}
            </span>
            <span class="meta-item">
              <el-icon><VideoPlay /></el-icon> {{ formatNumber(videoDetail?.views || 0) }} 播放
            </span>
            <span class="meta-item like-btn" :class="{ 'has-liked': hasLiked }" @click="handleLike">
              <el-icon><Star /></el-icon> {{ formatNumber(videoDetail?.likes || 0) }} 点赞
            </span>
          </div>

          <div class="video-desc" :class="{ 'expanded': isDescExpanded }">
            {{ videoDetail?.description || '暂无简介' }}
            <div class="tags-container" v-if="videoDetail?.tags?.length">
              <el-tag v-for="tag in videoDetail.tags" :key="tag" size="small" class="tag-item">
                {{ tag }}
              </el-tag>
            </div>
          </div>
          <div class="desc-toggle" v-if="videoDetail?.description && videoDetail.description.length > 100" @click="isDescExpanded = !isDescExpanded">
            {{ isDescExpanded ? '收起' : '展开更多' }}
          </div>
        </div>

        <!-- 评论区 (纯前端模拟) -->
        <div class="comments-section">
          <div class="comments-header">
            <h3>评论 <span class="comment-count">{{ comments.length }}</span></h3>
          </div>
          
          <div class="comment-input-box">
            <div class="avatar-placeholder">
              <el-icon><User /></el-icon>
            </div>
            <div class="input-wrapper">
              <el-input
                v-model="newComment"
                type="textarea"
                :rows="3"
                placeholder="发一条友善的评论"
                resize="none"
              />
              <div class="submit-btn-wrapper">
                <el-button type="primary" @click="submitComment" :disabled="!newComment.trim()">
                  发表评论
                </el-button>
              </div>
            </div>
          </div>

          <div class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-avatar">
                <el-icon><User /></el-icon>
              </div>
              <div class="comment-content-box">
                <div class="comment-user">{{ comment.userNickname || '匿名用户' }}</div>
                <div class="comment-text">{{ comment.content }}</div>
                <div class="comment-meta">
                  <span class="comment-time">{{ formatCommentTime(comment.createTime) }}</span>
                </div>
              </div>
            </div>
            
            <div v-if="comments.length === 0 && !commentLoading" class="no-comments">
              <el-empty description="还没有评论，快来抢沙发吧~" :image-size="100" />
            </div>
            
            <div v-if="commentLoading" class="comment-loading">
              <el-skeleton :rows="2" animated />
            </div>
          </div>
        </div>
      </div>

      <div class="right-col">
        <!-- 作者信息卡片（移到右侧） -->
        <div class="author-card">
          <div class="author-avatar">
            <el-icon><User /></el-icon>
          </div>
          <div class="author-info">
            <div class="author-name">{{ videoDetail?.userName || '未知用户' }}</div>
            <div class="author-meta">{{ formatDate(videoDetail?.createTime) }} 发布</div>
          </div>
        </div>

        <div class="recommend-box">
          <div class="recommend-title">相关推荐</div>
          <div class="recommend-list">
            <div v-for="video in relatedVideos" :key="video.id" class="recommend-item" @click="goToVideo(video.id)">
              <div class="rec-cover">
                <img :src="video.coverUrl || '/placeholder-cover.jpg'" :alt="video.title" />
                <span class="rec-duration" v-if="video.duration">{{ formatDuration(video.duration) }}</span>
              </div>
              <div class="rec-info">
                <h4 class="rec-title" :title="video.title">{{ video.title }}</h4>
                <div class="rec-author">{{ video.userName }}</div>
                <div class="rec-stats">
                  <span>{{ formatNumber(video.views || 0) }}播放</span>
                </div>
              </div>
            </div>
            <div v-if="relatedVideos.length === 0 && !loading" class="no-recommend">
              <el-empty description="暂无相关推荐" :image-size="80" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { VideoPlay, Clock, Star, User } from '@element-plus/icons-vue'
import { getPublicVideo, incrementVideoViews, incrementVideoLikes, getRelatedVideos, type VideoDetail } from '@/api/video'
import { getCommentList, addComment, type Comment, type AddCommentReq } from '@/api/comment'

const route = useRoute()
const videoId = Number(route.params.id)

const loading = ref(false)
const videoDetail = ref<VideoDetail | null>(null)
const isDescExpanded = ref(false)
const hasLiked = ref(false)
const relatedVideos = ref<any[]>([])

// 评论相关
const newComment = ref('')
const comments = ref<Comment[]>([])
const commentLoading = ref(false)

// 获取评论列表
const fetchComments = async () => {
  if (!videoId) return
  commentLoading.value = true
  try {
    const res: any = await getCommentList(videoId)
    if (res.code === 0) {
      // 只改这一行！去掉 .list
      comments.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取评论列表失败')
    }
  } catch (e) {
    ElMessage.error('获取评论列表失败')
  } finally {
    commentLoading.value = false
  }
}

const fetchVideoDetail = async () => {
  if (!videoId) return
  loading.value = true
  try {
    const res: any = await getPublicVideo(videoId)
    if (res.code === 0) {
      videoDetail.value = res.data
      // 获取视频详情后获取评论和推荐
      fetchComments()
      fetchRelatedVideos()
    } else {
      ElMessage.error(res.message || '获取视频详情失败')
    }
  } catch (e) {
    ElMessage.error('获取视频详情失败')
  } finally {
    loading.value = false
  }
}

const fetchRelatedVideos = async () => {
  if (!videoId) return
  try {
    const res: any = await getRelatedVideos(videoId, 10)
    if (res.code === 0) {
      relatedVideos.value = res.data || []
    }
  } catch (e) {
    console.error('获取相关推荐失败', e)
  }
}

const goToVideo = (id: number) => {
  window.location.href = `/dashboard/play/${id}`
}

const handleLike = async () => {
  if (!videoDetail.value || hasLiked.value) return
  
  try {
    const res: any = await incrementVideoLikes(videoId)
    if (res.code === 0 || res.code === 200) {
      // 成功后，更新最新点赞数（接口返回最新的点赞数）
      videoDetail.value.likes = res.data !== undefined ? res.data : (videoDetail.value.likes || 0) + 1
      hasLiked.value = true
      ElMessage.success('点赞成功')
      
      // 触发控制台数据刷新
      window.dispatchEvent(new CustomEvent('video-data-updated'))
    } else {
      ElMessage.error(res.message || '点赞失败')
    }
  } catch (e) {
    ElMessage.error('点赞失败')
  }
}

const submitComment = async () => {
  if (!newComment.value.trim() || !videoId) return
  
  try {
    const res: any = await addComment({
      videoId,
      content: newComment.value.trim()
    })
    
    if (res.code === 0) {
      newComment.value = ''
      ElMessage.success('评论发表成功')
      // 发表成功后重新获取评论列表
      fetchComments()
      
      // 触发控制台数据刷新
      window.dispatchEvent(new CustomEvent('video-data-updated'))
    } else {
      ElMessage.error(res.message || '评论发表失败')
    }
  } catch (e) {
    ElMessage.error('评论发表失败')
  }
}

const formatNumber = (num: number): string => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const formatDate = (dateStr?: string) => {
  if (!dateStr) return ''
  // 简单格式化：去掉T，保留到分钟
  return dateStr.replace('T', ' ').substring(0, 16)
}

// 格式化评论时间
const formatCommentTime = (dateStr?: string) => {
  if (!dateStr) return ''
  
  const now = new Date()
  const commentTime = new Date(dateStr)
  const diffInSeconds = Math.floor((now.getTime() - commentTime.getTime()) / 1000)
  
  if (diffInSeconds < 60) {
    return '刚刚'
  } else if (diffInSeconds < 3600) {
    return `${Math.floor(diffInSeconds / 60)}分钟前`
  } else if (diffInSeconds < 86400) {
    return `${Math.floor(diffInSeconds / 3600)}小时前`
  } else if (diffInSeconds < 2592000) {
    return `${Math.floor(diffInSeconds / 86400)}天前`
  } else {
    return formatDate(dateStr)
  }
}

// 格式化视频时长（秒转为 mm:ss）
const formatDuration = (seconds: number): string => {
  if (!seconds) return ''
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
}

onMounted(() => {
  fetchVideoDetail()
  
  // 增加播放量
  const viewedKey = `viewed_${videoId}`
  console.log('检查是否已观看:', viewedKey, sessionStorage.getItem(viewedKey))
  
  if (!sessionStorage.getItem(viewedKey)) {
    console.log('调用播放量增加接口')
    incrementVideoViews(videoId).then(() => {
      sessionStorage.setItem(viewedKey, '1')
      console.log('播放量增加成功')
    }).catch((err) => {
      console.error('播放量增加失败', err)
    })
  } else {
    console.log('今日已观看，不重复增加播放量')
  }
})
</script>

<style scoped>
.video-play-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.main-container {
  display: flex;
  gap: 30px;
}

.left-col {
  flex: 1;
  min-width: 0;
}

.right-col {
  width: 350px;
  flex-shrink: 0;
}

/* 播放器区域 */
.player-wrapper {
  width: 100%;
  aspect-ratio: 16 / 9;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player {
  width: 100%;
  height: 100%;
  outline: none;
}

.player-placeholder {
  color: #fff;
}

/* 视频信息区 */
.video-info-box {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.video-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.video-meta {
  display: flex;
  align-items: center;
  gap: 20px;
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.like-btn {
  cursor: pointer;
  transition: color 0.3s;
}

.like-btn:hover {
  color: #3b82f6;
}

.like-btn.has-liked {
  color: #3b82f6;
  font-weight: 500;
}

/* B站风格作者卡片 */
.author-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
  border-radius: 8px;
  gap: 12px;
  margin-bottom: 4px;
}

.author-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.author-info {
  flex: 1;
  min-width: 0;
}

.author-name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.author-meta {
  font-size: 13px;
  color: #6b7280;
}

.author-stats {
  display: flex;
  gap: 16px;
  flex-shrink: 0;
}

.author-stats .stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 6px;
  min-width: 60px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.author-stats .stat-item .el-icon {
  font-size: 16px;
  color: #3b82f6;
}

.author-stats .stat-item span {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.video-desc {
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
  max-height: 66px;
  overflow: hidden;
  transition: max-height 0.3s;
  white-space: pre-wrap;
}

.video-desc.expanded {
  max-height: 500px;
}

.tags-container {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.desc-toggle {
  color: #3b82f6;
  font-size: 13px;
  cursor: pointer;
  margin-top: 8px;
  display: inline-block;
}

/* 评论区 */
.comments-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.comments-header {
  margin-bottom: 20px;
}

.comments-header h3 {
  font-size: 18px;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-count {
  font-size: 14px;
  color: #6b7280;
  font-weight: normal;
}

.comment-input-box {
  display: flex;
  gap: 16px;
  margin-bottom: 30px;
}

.avatar-placeholder {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #9ca3af;
  flex-shrink: 0;
}

.input-wrapper {
  flex: 1;
}

.submit-btn-wrapper {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f3f4f6;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #9ca3af;
  flex-shrink: 0;
}

.comment-content-box {
  flex: 1;
}

.comment-user {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 6px;
  font-weight: 500;
}

.comment-text {
  font-size: 14px;
  color: #1f2937;
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #9ca3af;
}

.comment-action {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.comment-action:hover {
  color: #3b82f6;
}

.no-comments {
  padding: 20px 0;
}

/* 右侧推荐 */
.recommend-box {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.recommend-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 16px;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-item {
  display: flex;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.recommend-item:hover .rec-title {
  color: #3b82f6;
}

.rec-cover {
  width: 140px;
  height: 79px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  background: #f3f4f6;
  flex-shrink: 0;
}

.rec-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.rec-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-size: 10px;
  padding: 2px 4px;
  border-radius: 2px;
}

.rec-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.rec-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.2s;
}

.rec-author {
  font-size: 12px;
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rec-stats {
  font-size: 12px;
  color: #9ca3af;
}

@media (max-width: 1024px) {
  .main-container {
    flex-direction: column;
  }
  .right-col {
    width: 100%;
  }
}
</style>