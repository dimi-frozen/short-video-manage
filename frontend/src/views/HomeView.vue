<template>
  <div class="sv-page home-page">
    <div class="kpi-grid">
      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-top">
          <div class="kpi-left">
            <div class="kpi-title">视频总数</div>
            <div class="kpi-value">{{ formatCompact(dashboard?.overview?.totalVideos) }}</div>
          </div>
          <div class="kpi-icon blue">
            <el-icon><VideoCamera /></el-icon>
          </div>
        </div>
        <div class="kpi-sub">
          <span class="muted">相较前一日</span>
          <span :class="trendClass(trendDelta.videos)">
            {{ trendText(trendDelta.videos) }}
          </span>
        </div>
      </el-card>

      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-top">
          <div class="kpi-left">
            <div class="kpi-title">总播放量</div>
            <div class="kpi-value">{{ formatCompact(dashboard?.overview?.totalViews) }}</div>
          </div>
          <div class="kpi-icon green">
            <el-icon><View /></el-icon>
          </div>
        </div>
        <div class="kpi-sub">
          <span class="muted">相较前一日</span>
          <span :class="trendClass(trendDelta.views)">{{ trendText(trendDelta.views) }}</span>
        </div>
      </el-card>

      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-top">
          <div class="kpi-left">
            <div class="kpi-title">总点赞</div>
            <div class="kpi-value">{{ formatCompact(dashboard?.overview?.totalLikes) }}</div>
          </div>
          <div class="kpi-icon amber">
            <el-icon><Star /></el-icon>
          </div>
        </div>
        <div class="kpi-sub">
          <span class="muted">相较前一日</span>
          <span :class="trendClass(trendDelta.likes)">{{ trendText(trendDelta.likes) }}</span>
        </div>
      </el-card>

      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-top">
          <div class="kpi-left">
            <div class="kpi-title">总评论</div>
            <div class="kpi-value">{{ formatCompact(dashboard?.overview?.totalComments) }}</div>
          </div>
          <div class="kpi-icon red">
            <el-icon><ChatDotRound /></el-icon>
          </div>
        </div>
        <div class="kpi-sub">
          <span class="muted">相较前一日</span>
          <span :class="trendClass(trendDelta.comments)">{{ trendText(trendDelta.comments) }}</span>
        </div>
      </el-card>
    </div>

    <div class="grid-2">
      <el-card shadow="never" class="sv-card chart-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">最近 7 天趋势</div>
            <div class="card-sub">播放量（按天汇总）</div>
          </div>
        </template>
        <div ref="trendEl" class="chart"></div>
      </el-card>

      <el-card shadow="never" class="sv-card chart-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">平台对比</div>
            <div class="card-sub">播放量 / 点赞 / 评论（全平台）</div>
          </div>
        </template>
        <div ref="compareEl" class="chart"></div>
      </el-card>
    </div>

    <div class="grid-2">
      <el-card shadow="never" class="sv-card sv-table">
        <template #header>
          <div class="card-header">
            <div class="card-title">视频排行榜</div>
            <div class="card-sub">Top 5（按播放量）</div>
          </div>
        </template>
        <el-table :data="top5" style="width: 100%">
          <el-table-column prop="platformType" label="平台" width="110">
            <template #default="{ row }">{{ platformLabel(row.platformType) }}</template>
          </el-table-column>
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="views" label="播放量" width="120" />
          <el-table-column prop="likes" label="点赞" width="100" />
          <el-table-column prop="comments" label="评论" width="100" />
          <el-table-column prop="publishTime" label="发布时间" width="190" />
        </el-table>
      </el-card>

      <el-card shadow="never" class="sv-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">最近上传</div>
            <div class="card-sub">本地视频（可查看详情）</div>
          </div>
        </template>

        <div class="recent-list">
          <div v-for="item in recent" :key="item.id" class="recent-item">
            <div class="thumb" :class="{ empty: !item.coverUrl }">
              <img v-if="item.coverUrl" :src="item.coverUrl" alt="cover" />
              <div v-else class="thumb-empty">
                <el-icon><PictureFilled /></el-icon>
              </div>
            </div>
            <div class="info">
              <div class="title-row">
                <div class="t" :title="item.title">{{ item.title }}</div>
                <el-tag size="small" type="info" effect="light">本地</el-tag>
              </div>
              <div class="tags">
                <el-tag v-for="p in item.publishedPlatforms" :key="p" size="small" type="success" effect="light">
                  {{ p }}
                </el-tag>
                <el-tag v-if="item.publishedPlatforms.length === 0" size="small" type="warning" effect="light">
                  未发布
                </el-tag>
              </div>
              <div class="meta">
                <span class="muted">{{ item.createTime }}</span>
                <span class="dot">·</span>
                <span class="muted">{{ formatSize(item.fileSize) }}</span>
              </div>
            </div>
            <div class="actions">
              <el-button type="primary" plain size="small" @click="goDetail(item.id)">查看详情</el-button>
            </div>
          </div>
          <el-empty v-if="recent.length === 0" description="暂无数据" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { VideoCamera, View, Star, ChatDotRound, PictureFilled } from '@element-plus/icons-vue'
import { getAnalysisDashboard, type AnalysisDashboard } from '@/api/analysis'
import { pageVideos, getVideo, type VideoDetail, type VideoListItem } from '@/api/video'
import { listPublishRecords, type PublishRecordResp } from '@/api/publish'

const router = useRouter()

const dashboard = ref<AnalysisDashboard | null>(null)

const trendEl = ref<HTMLElement | null>(null)
const compareEl = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let compareChart: echarts.ECharts | null = null

type RecentItem = {
  id: number
  title: string
  coverUrl: string
  createTime: string
  fileSize: number
  publishedPlatforms: string[]
}

const recent = ref<RecentItem[]>([])

const top5 = computed(() => {
  const list = dashboard.value?.topVideos || []
  return list.slice(0, 5)
})

const trendDelta = computed(() => {
  const t = dashboard.value?.trend || []
  if (t.length < 2) {
    return { videos: 0, views: 0, likes: 0, comments: 0 }
  }
  const a = t[t.length - 2]
  const b = t[t.length - 1]
  return {
    videos: 0,
    views: (b.views || 0) - (a.views || 0),
    likes: (b.likes || 0) - (a.likes || 0),
    comments: (b.comments || 0) - (a.comments || 0)
  }
})

const trendClass = (delta: number) => {
  if (delta > 0) return 'up'
  if (delta < 0) return 'down'
  return 'flat'
}

const trendText = (delta: number) => {
  if (delta > 0) return `↑ ${formatCompact(delta)}`
  if (delta < 0) return `↓ ${formatCompact(Math.abs(delta))}`
  return '—'
}

const platformLabel = (t: string) => {
  if (t === 'DOUYIN') return '抖音'
  if (t === 'KUAISHOU') return '快手'
  if (t === 'BILIBILI') return 'B站'
  return t || '-'
}

const formatCompact = (n?: number | null) => {
  const v = Number(n || 0)
  if (v < 1000) return String(v)
  if (v < 10000) return `${(v / 1000).toFixed(1)}k`
  if (v < 100000000) return `${(v / 10000).toFixed(1)}w`
  return `${(v / 100000000).toFixed(1)}亿`
}

const formatSize = (bytes: number) => {
  if (bytes == null) return '-'
  const n = Number(bytes)
  if (Number.isNaN(n)) return '-'
  if (n < 1024) return `${n} B`
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`
  if (n < 1024 * 1024 * 1024) return `${(n / 1024 / 1024).toFixed(1)} MB`
  return `${(n / 1024 / 1024 / 1024).toFixed(1)} GB`
}

const goDetail = (id: number) => {
  router.push({ path: '/dashboard/videos', query: { tab: 'local', openId: String(id) } })
}

const buildTrendChart = () => {
  if (!trendEl.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendEl.value)
  }
  const points = dashboard.value?.trend || []
  trendChart.setOption({
    grid: { left: 42, right: 18, top: 24, bottom: 34 },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: points.map(p => p.day),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: 'rgba(15,23,42,0.18)' } },
      axisLabel: { color: 'rgba(15,23,42,0.65)' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(15,23,42,0.08)' } },
      axisLabel: { color: 'rgba(15,23,42,0.55)' }
    },
    series: [
      {
        name: '播放量',
        type: 'line',
        smooth: true,
        showSymbol: false,
        lineStyle: { width: 3, color: '#2563eb' },
        areaStyle: {
          color: new (echarts as any).graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(37,99,235,0.28)' },
            { offset: 1, color: 'rgba(37,99,235,0.02)' }
          ])
        },
        data: points.map(p => p.views || 0)
      }
    ]
  })
}

const buildCompareChart = () => {
  if (!compareEl.value) return
  if (!compareChart) {
    compareChart = echarts.init(compareEl.value)
  }
  const agg = dashboard.value?.platformAgg || []
  const platforms = ['DOUYIN', 'KUAISHOU', 'BILIBILI']
  const pick = (pt: string) => agg.find(a => a.platformType === pt) || { views: 0, likes: 0, comments: 0 }
  const rows = platforms.map(p => pick(p))
  const x = platforms.map(p => platformLabel(p))

  const series: any[] = [
    {
      name: '播放量',
      type: 'bar',
      barWidth: 18,
      itemStyle: { color: '#22c55e', borderRadius: [8, 8, 0, 0] },
      data: rows.map(r => r.views || 0)
    },
    {
      name: '点赞',
      type: 'bar',
      barWidth: 18,
      itemStyle: { color: '#f59e0b', borderRadius: [8, 8, 0, 0] },
      data: rows.map(r => r.likes || 0)
    },
    {
      name: '评论',
      type: 'bar',
      barWidth: 18,
      itemStyle: { color: '#2563eb', borderRadius: [8, 8, 0, 0] },
      data: rows.map(r => r.comments || 0)
    }
  ]

  compareChart.setOption({
    grid: { left: 46, right: 18, top: 38, bottom: 34 },
    tooltip: { trigger: 'axis' },
    legend: { top: 6, textStyle: { color: 'rgba(15,23,42,0.65)' } },
    xAxis: {
      type: 'category',
      data: x,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: 'rgba(15,23,42,0.18)' } },
      axisLabel: { color: 'rgba(15,23,42,0.65)' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(15,23,42,0.08)' } },
      axisLabel: { color: 'rgba(15,23,42,0.55)' }
    },
    series
  })
}

const loadDashboard = async () => {
  const end = new Date()
  const start = new Date(end.getTime() - 7 * 24 * 60 * 60 * 1000)
  const fmt = (d: Date) => {
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  }
  const res: any = await getAnalysisDashboard({
    startTime: fmt(start),
    endTime: fmt(end),
    orderBy: 'views'
  })
  dashboard.value = res.data
  await nextTick()
  buildTrendChart()
  buildCompareChart()
}

const loadRecent = async () => {
  const res: any = await pageVideos({ pageNum: 1, pageSize: 5, sort: ['createTime,desc'] })
  const list: VideoListItem[] = res.data.list || []
  const out: RecentItem[] = []
  for (const it of list) {
    let coverUrl = ''
    try {
      const d: any = await getVideo(it.id)
      const detail = d.data as VideoDetail
      coverUrl = (detail as any).coverUrl || ''
    } catch (e) {
    }

    let platforms: string[] = []
    try {
      const pr: any = await listPublishRecords({ videoId: it.id } as any)
      const records = (pr.data || []) as PublishRecordResp[]
      const ok = records.filter(r => String(r.status).toUpperCase() === 'SUCCESS').map(r => String(r.platform || '').toLowerCase())
      const set = new Set(ok)
      platforms = Array.from(set).map(p => {
        if (p.includes('douyin')) return '抖音'
        if (p.includes('kuaishou')) return '快手'
        if (p.includes('bilibili')) return 'B站'
        return p
      })
    } catch (e) {
    }

    out.push({
      id: it.id,
      title: it.title,
      coverUrl,
      createTime: String(it.createTime || ''),
      fileSize: Number(it.fileSize || 0),
      publishedPlatforms: platforms
    })
  }
  recent.value = out
}

const handleResize = () => {
  try {
    trendChart?.resize()
    compareChart?.resize()
  } catch (e) {
  }
}

onMounted(async () => {
  await loadDashboard()
  await loadRecent()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  try {
    trendChart?.dispose()
    compareChart?.dispose()
  } catch (e) {
  }
  trendChart = null
  compareChart = null
})
</script>

<style scoped>
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.kpi {
  padding: 2px;
}

.kpi-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.kpi-title {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.kpi-value {
  margin-top: 6px;
  font-size: 28px;
  font-weight: 900;
  letter-spacing: 0.2px;
  color: rgba(15, 23, 42, 0.92);
}

.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.12);
}

.kpi-icon.blue {
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.95), rgba(59, 130, 246, 0.75));
}

.kpi-icon.green {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.95), rgba(16, 185, 129, 0.75));
}

.kpi-icon.amber {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.95), rgba(251, 191, 36, 0.75));
}

.kpi-icon.red {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.95), rgba(244, 63, 94, 0.75));
}

.kpi-sub {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  font-size: 12px;
}

.muted {
  color: rgba(15, 23, 42, 0.55);
}

.up {
  color: rgba(16, 185, 129, 0.95);
  font-weight: 800;
}

.down {
  color: rgba(239, 68, 68, 0.95);
  font-weight: 800;
}

.flat {
  color: rgba(15, 23, 42, 0.55);
  font-weight: 700;
}

.grid-2 {
  margin-top: 12px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
}

.chart-card :deep(.el-card__header) {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-title {
  font-size: 14px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.92);
}

.card-sub {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.chart {
  height: 320px;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.recent-item {
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(255, 255, 255, 0.62);
  padding: 12px;
  display: grid;
  grid-template-columns: 120px 1fr auto;
  gap: 12px;
  align-items: center;
}

.thumb {
  width: 120px;
  aspect-ratio: 16 / 9;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: radial-gradient(120px 80px at 30% 30%, rgba(37, 99, 235, 0.28), transparent 60%),
    radial-gradient(120px 80px at 70% 70%, rgba(16, 185, 129, 0.24), transparent 60%),
    rgba(15, 23, 42, 0.82);
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.thumb-empty {
  color: rgba(255, 255, 255, 0.75);
  font-size: 20px;
}

.info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.t {
  font-size: 13px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.90);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.meta {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  color: rgba(15, 23, 42, 0.32);
}

.actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
