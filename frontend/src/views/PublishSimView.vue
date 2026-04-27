<template>
  <div class="sv-page publish-sim-page">
    <el-card shadow="never" class="sv-card">
      <template #header>
        <div class="header">
          <div class="title">平台发布（模拟）</div>
          <div class="sub">选择的平台：{{ platformLabel(record?.platform) }}</div>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="6" animated />
      </div>

      <div v-else>
        <div class="grid">
          <div class="media">
            <div class="media-card">
              <img v-if="video?.coverUrl" :src="video.coverUrl" class="cover" alt="cover" />
              <video v-if="video?.filePath" :src="video.filePath" class="player" controls playsinline />
            </div>
          </div>
          <div class="info">
            <div class="kv">
              <div class="k">视频ID</div>
              <div class="v">{{ record?.videoId }}</div>
            </div>
            <div class="kv">
              <div class="k">标题</div>
              <div class="v">{{ video?.title }}</div>
            </div>
            <div class="kv">
              <div class="k">状态</div>
              <div class="v">
                <el-tag v-if="record?.status === 'SUCCESS'" type="success">SUCCESS</el-tag>
                <el-tag v-else type="info">PENDING</el-tag>
              </div>
            </div>
            <div class="kv">
              <div class="k">平台链接</div>
              <div class="v">
                <el-link v-if="record?.platformVideoUrl" :href="record.platformVideoUrl" target="_blank" type="primary">
                  {{ record.platformVideoUrl }}
                </el-link>
                <span v-else class="muted">发布确认后生成</span>
              </div>
            </div>

            <div class="actions">
              <el-button @click="back">返回系统</el-button>
              <el-button type="primary" :loading="confirming" :disabled="record?.status === 'SUCCESS'" @click="confirm">
                模拟完成发布并确认
              </el-button>
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
import { confirmPublishTask, getPublishRecord, type PublishRecordResp } from '@/api/publish'
import { getVideo, type VideoDetail } from '@/api/video'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const confirming = ref(false)
const record = ref<PublishRecordResp | null>(null)
const video = ref<VideoDetail | null>(null)

const platformLabel = (p?: string) => {
  const s = (p || '').toLowerCase()
  if (s.includes('douyin')) return '抖音'
  if (s.includes('kuaishou')) return '快手'
  if (s.includes('bilibili') || s.includes('b站')) return 'B站'
  return p || '-'
}

const load = async () => {
  const rid = Number(route.query.recordId || 0)
  if (!rid) {
    ElMessage.error('recordId 缺失')
    router.push('/dashboard/videos')
    return
  }
  loading.value = true
  try {
    const r: any = await getPublishRecord(rid)
    record.value = r.data
    const v: any = await getVideo(r.data.videoId)
    video.value = v.data
  } finally {
    loading.value = false
  }
}

const confirm = async () => {
  if (!record.value) return
  if (record.value.status === 'SUCCESS') return
  confirming.value = true
  try {
    const res: any = await confirmPublishTask({ recordId: record.value.recordId })
    record.value = { ...record.value, status: res.data.status, platformVideoUrl: res.data.platformVideoUrl }
    ElMessage.success('发布确认成功')
  } finally {
    confirming.value = false
  }
}

const back = () => {
  if (record.value?.videoId) {
    router.push(`/dashboard/publish-detail/${record.value.videoId}`)
    return
  }
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

.info {
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
  font-weight: 600;
  color: rgba(15, 23, 42, 0.86);
  word-break: break-all;
}

.muted {
  color: rgba(15, 23, 42, 0.55);
  font-weight: 500;
}

.actions {
  margin-top: 6px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
