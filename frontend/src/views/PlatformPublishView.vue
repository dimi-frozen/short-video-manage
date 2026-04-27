<template>
  <div class="sv-page platform-publish-page">
    <el-card shadow="never" class="sv-card">
      <template #header>
        <div class="header">
          <div class="title">发布到 {{ platformLabel(platform) }}</div>
          <div class="sub">先授权，再一键发布（使用已上传的 OSS 视频地址）</div>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="8" animated />
      </div>

      <div v-else class="grid">
        <div class="left">
          <div class="media-card">
            <img v-if="video?.coverUrl" :src="video.coverUrl" class="cover" alt="cover" />
            <video v-if="video?.filePath" :src="video.filePath" class="player" controls playsinline />
          </div>
        </div>

        <div class="right">
          <div class="kv">
            <div class="k">标题</div>
            <div class="v">{{ video?.title }}</div>
          </div>
          <div class="kv">
            <div class="k">授权状态</div>
            <div class="v">
              <el-tag v-if="auth.status === 'AUTHORIZED'" type="success">已授权</el-tag>
              <el-tag v-else type="danger">未授权</el-tag>
            </div>
          </div>

          <div class="actions">
            <el-button @click="back">返回</el-button>
            <el-button
              v-if="auth.status !== 'AUTHORIZED'"
              type="primary"
              :loading="authLoading"
              @click="doAuth"
            >
              获取授权
            </el-button>
            <el-button
              v-else
              type="primary"
              :loading="publishing"
              @click="doPublish"
            >
              发布视频
            </el-button>
          </div>

          <div v-if="result" class="result">
            <el-tag type="success">发布成功</el-tag>
            <div class="result-row">
              <div class="rk">平台链接</div>
              <el-link :href="result.platformVideoUrl" target="_blank" type="primary">{{ result.platformVideoUrl }}</el-link>
            </div>
            <div class="result-row">
              <div class="rk">平台视频ID</div>
              <div class="rv">{{ result.videoId }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVideo, type VideoDetail } from '@/api/video'
import { getPlatformAuthStatus, type PlatformAuthStatusResp, type PlatformKey } from '@/api/platformAuth'
import { publishLocalVideoToPlatform, type PlatformPublishResp } from '@/api/platformPublish'
import { douyinAuthUrl } from '@/api/douyin'
import { kuaishouAuthUrl } from '@/api/kuaishou'
import { bilibiliAuthUrl } from '@/api/bilibili'

const route = useRoute()
const router = useRouter()

const platform = ref<PlatformKey>('douyin')
const videoId = ref<number>(0)

const loading = ref(true)
const authLoading = ref(false)
const publishing = ref(false)

const video = ref<VideoDetail | null>(null)
const auth = ref<PlatformAuthStatusResp>({ platform: 'douyin', status: 'UNAUTHORIZED', expiresInSeconds: 0, expireAt: null })
const result = ref<PlatformPublishResp | null>(null)

const platformLabel = (p: PlatformKey) => {
  if (p === 'douyin') return '抖音'
  if (p === 'kuaishou') return '快手'
  return 'B站'
}

const tokenKey = (p: PlatformKey) => {
  if (p === 'douyin') return 'douyin_token'
  if (p === 'kuaishou') return 'kuaishou_token'
  return 'bilibili_token'
}

const load = async () => {
  loading.value = true
  try {
    const p = String(route.params.platform || '').toLowerCase()
    if (p !== 'douyin' && p !== 'kuaishou' && p !== 'bilibili') {
      ElMessage.error('平台参数错误')
      router.push('/dashboard/videos')
      return
    }
    platform.value = p as PlatformKey
    videoId.value = Number(route.params.videoId || 0)
    if (!videoId.value) {
      ElMessage.error('videoId 缺失')
      router.push('/dashboard/videos')
      return
    }
    const v: any = await getVideo(videoId.value)
    video.value = v.data
    const a: any = await getPlatformAuthStatus(platform.value)
    auth.value = a.data
    const localToken = localStorage.getItem(tokenKey(platform.value)) || ''
    if (auth.value.status !== 'AUTHORIZED' && localToken) {
      auth.value = { ...auth.value, status: 'AUTHORIZED' }
    }
  } finally {
    loading.value = false
  }
}

const doAuth = async () => {
  authLoading.value = true
  try {
    if (platform.value === 'douyin') {
      const res: any = await douyinAuthUrl()
      localStorage.setItem(tokenKey(platform.value), res.data.accessToken)
    } else if (platform.value === 'kuaishou') {
      const res: any = await kuaishouAuthUrl()
      localStorage.setItem(tokenKey(platform.value), res.data.accessToken)
    } else {
      const res: any = await bilibiliAuthUrl()
      localStorage.setItem(tokenKey(platform.value), res.data.accessToken)
    }
    const a: any = await getPlatformAuthStatus(platform.value)
    auth.value = a.data
    const localToken = localStorage.getItem(tokenKey(platform.value)) || ''
    if (auth.value.status !== 'AUTHORIZED' && localToken) {
      auth.value = { ...auth.value, status: 'AUTHORIZED' }
    }
    if (auth.value.status === 'AUTHORIZED') {
      ElMessage.success('授权成功')
    } else {
      ElMessage.warning('已获取 Token，但授权状态未更新（可直接尝试发布）')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '授权失败')
  } finally {
    authLoading.value = false
  }
}

const doPublish = async () => {
  if (!videoId.value) return
  publishing.value = true
  try {
    const t = localStorage.getItem(tokenKey(platform.value)) || ''
    const res: any = await publishLocalVideoToPlatform(platform.value, videoId.value, t)
    result.value = res.data
    ElMessage.success('发布成功')
  } finally {
    publishing.value = false
  }
}

const back = () => {
  router.push('/dashboard/videos')
}

onMounted(load)
</script>

<style scoped>
.header {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title {
  font-size: 14px;
  font-weight: 800;
}

.sub {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.grid {
  display: grid;
  grid-template-columns: minmax(520px, 1fr) minmax(360px, 420px);
  gap: 14px;
  align-items: start;
}

.media-card {
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(255, 255, 255, 0.62);
  overflow: hidden;
}

.cover {
  width: 100%;
  display: block;
  object-fit: cover;
  aspect-ratio: 16 / 9;
}

.player {
  width: 100%;
  display: block;
  background: rgba(15, 23, 42, 0.92);
  aspect-ratio: 16 / 9;
}

.right {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.kv {
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(255, 255, 255, 0.62);
  padding: 12px 12px 10px;
}

.k {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.v {
  margin-top: 4px;
  font-size: 13px;
  font-weight: 650;
  color: rgba(15, 23, 42, 0.88);
  word-break: break-all;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 4px;
}

.result {
  margin-top: 6px;
  border-radius: 16px;
  border: 1px solid rgba(16, 185, 129, 0.22);
  background: rgba(16, 185, 129, 0.06);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.result-row {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: 10px;
  align-items: center;
}

.rk {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.rv {
  font-size: 13px;
  font-weight: 650;
  color: rgba(15, 23, 42, 0.88);
  word-break: break-all;
}

@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
