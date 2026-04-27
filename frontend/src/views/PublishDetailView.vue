<template>
  <div class="sv-page publish-detail-page">
    <div class="grid">
      <div class="left">
        <el-card shadow="never" class="sv-card">
          <template #header>
            <div class="header">
              <div class="title">发布详情</div>
              <div class="sub">在这里完成授权与多平台发布</div>
            </div>
          </template>

          <div v-if="loading" class="loading">
            <el-skeleton :rows="8" animated />
          </div>

          <div v-else>
            <div class="media">
              <div class="media-card">
                <img v-if="video?.coverUrl" :src="video.coverUrl" class="cover" alt="cover" />
                <video v-if="video?.filePath" :src="video.filePath" class="player" controls playsinline />
              </div>
            </div>

            <div class="meta">
              <div class="kv">
                <div class="k">标题</div>
                <div class="v">{{ video?.title }}</div>
              </div>
              <div class="kv">
                <div class="k">简介</div>
                <div class="v muted">{{ video?.description || '-' }}</div>
              </div>
              <div class="kv">
                <div class="k">标签</div>
                <div class="v">
                  <el-space wrap>
                    <el-tag v-for="t in video?.tags || []" :key="t" size="small">{{ t }}</el-tag>
                  </el-space>
                </div>
              </div>
              <div class="kv">
                <div class="k">文件地址</div>
                <div class="v">
                  <el-link v-if="video?.filePath" :href="video.filePath" target="_blank" type="primary">
                    {{ video.filePath }}
                  </el-link>
                  <span v-else class="muted">-</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="right">
        <el-card shadow="never" class="sv-card">
          <template #header>
            <div class="header">
              <div class="title">平台发布</div>
              <div class="sub">抖音 / 快手</div>
            </div>
          </template>

          <div class="platforms">
            <div v-for="p in platforms" :key="p.key" class="platform">
              <div class="p-left">
                <div class="p-name">{{ p.name }}</div>
                <div class="p-tags">
                  <el-tag v-if="p.auth.status === 'AUTHORIZED'" type="success">已授权</el-tag>
                  <el-tag v-else type="danger">未授权</el-tag>
                  <el-tag v-if="p.record?.status === 'SUCCESS'" type="success">发布成功</el-tag>
                  <el-tag v-else-if="p.record?.status === 'PENDING'" type="warning">待确认</el-tag>
                  <el-tag v-else type="info">未发布</el-tag>
                </div>
              </div>

              <div class="p-right">
                <el-button
                  v-if="p.auth.status !== 'AUTHORIZED'"
                  type="primary"
                  :loading="p.loadingAuth"
                  @click="authorize(p.key)"
                >
                  获取授权
                </el-button>

                <el-button
                  v-else
                  type="primary"
                  :loading="p.loadingPublish"
                  @click="publish(p.key)"
                >
                  发布视频
                </el-button>

                <el-button
                  v-if="p.record?.status === 'PENDING'"
                  :loading="p.loadingConfirm"
                  @click="confirm(p.key)"
                >
                  确认已发布
                </el-button>
              </div>

              <div v-if="p.record?.platformVideoUrl" class="p-link">
                <el-link :href="p.record.platformVideoUrl" target="_blank" type="primary">
                  {{ p.record.platformVideoUrl }}
                </el-link>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getVideo, type VideoDetail } from '@/api/video'
import { getPlatformAuthStatus, type PlatformKey, type PlatformAuthStatusResp } from '@/api/platformAuth'
import { createPublishTask, confirmPublishTask, listPublishRecords, type PublishPlatform, type PublishRecordResp } from '@/api/publish'
import { douyinAuthUrl } from '@/api/douyin'
import { kuaishouAuthUrl } from '@/api/kuaishou'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const video = ref<VideoDetail | null>(null)
const videoId = ref<number>(0)

type PlatformCard = {
  key: PlatformKey
  name: string
  auth: PlatformAuthStatusResp
  record: PublishRecordResp | null
  loadingAuth: boolean
  loadingPublish: boolean
  loadingConfirm: boolean
}

const platforms = reactive<PlatformCard[]>([
  {
    key: 'douyin',
    name: '抖音',
    auth: { platform: 'douyin', status: 'UNAUTHORIZED', expiresInSeconds: 0, expireAt: null },
    record: null,
    loadingAuth: false,
    loadingPublish: false,
    loadingConfirm: false
  },
  {
    key: 'kuaishou',
    name: '快手',
    auth: { platform: 'kuaishou', status: 'UNAUTHORIZED', expiresInSeconds: 0, expireAt: null },
    record: null,
    loadingAuth: false,
    loadingPublish: false,
    loadingConfirm: false
  }
])

const fetchAuth = async (p: PlatformKey) => {
  const card = platforms.find(x => x.key === p)
  if (!card) return
  const res: any = await getPlatformAuthStatus(p)
  card.auth = res.data
}

const fetchRecord = async (p: PublishPlatform) => {
  const card = platforms.find(x => x.key === p)
  if (!card) return
  const res: any = await listPublishRecords({ videoId: videoId.value, platform: p })
  card.record = (res.data && res.data.length > 0) ? res.data[0] : null
}

const loadAll = async () => {
  loading.value = true
  try {
    const id = Number(route.params.videoId || 0)
    if (!id) {
      ElMessage.error('videoId 缺失')
      router.push('/dashboard/videos')
      return
    }
    videoId.value = id
    const v: any = await getVideo(id)
    video.value = v.data
    await Promise.all([fetchAuth('douyin'), fetchAuth('kuaishou'), fetchRecord('douyin'), fetchRecord('kuaishou')])
  } finally {
    loading.value = false
  }
}

const authorize = async (p: PlatformKey) => {
  const card = platforms.find(x => x.key === p)
  if (!card) return
  card.loadingAuth = true
  try {
    if (p === 'douyin') {
      const res: any = await douyinAuthUrl()
      localStorage.setItem('douyin_token', res.data.accessToken)
    } else if (p === 'kuaishou') {
      const res: any = await kuaishouAuthUrl()
      localStorage.setItem('kuaishou_token', res.data.accessToken)
    }
    await fetchAuth(p)
    ElMessage.success('授权成功')
  } finally {
    card.loadingAuth = false
  }
}

const publish = async (p: PlatformKey) => {
  const card = platforms.find(x => x.key === p)
  if (!card) return
  card.loadingPublish = true
  try {
    const res: any = await createPublishTask({ videoId: videoId.value, platform: p as PublishPlatform })
    await fetchRecord(p as PublishPlatform)
    const url = res.data.redirectUrl
    if (url) router.push(url)
  } finally {
    card.loadingPublish = false
  }
}

const confirm = async (p: PlatformKey) => {
  const card = platforms.find(x => x.key === p)
  if (!card?.record?.recordId) return
  card.loadingConfirm = true
  try {
    await confirmPublishTask({ recordId: card.record.recordId })
    await fetchRecord(p as PublishPlatform)
    ElMessage.success('已确认发布')
  } finally {
    card.loadingConfirm = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: minmax(560px, 1.1fr) minmax(420px, 0.9fr);
  gap: 14px;
  align-items: start;
}

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

.meta {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr;
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

.muted {
  color: rgba(15, 23, 42, 0.55);
  font-weight: 500;
}

.platforms {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.platform {
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(255, 255, 255, 0.62);
  padding: 14px;
}

.p-left {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.p-name {
  font-size: 14px;
  font-weight: 800;
}

.p-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.p-right {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.p-link {
  margin-top: 10px;
  font-size: 12px;
}

@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>

