<template>
  <div class="sv-page platform-dashboard">
    <div class="kpi-grid">
      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-top">
          <div class="kpi-left">
            <div class="kpi-title">总播放量</div>
            <div class="kpi-value">{{ formatCompact(platformStats.totalViews) }}</div>
          </div>
          <div class="kpi-icon blue">
            <el-icon><View /></el-icon>
          </div>
        </div>
        <div class="kpi-sub">
          <span class="muted">全平台累计</span>
        </div>
      </el-card>

      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-top">
          <div class="kpi-left">
            <div class="kpi-title">总点赞</div>
            <div class="kpi-value">{{ formatCompact(platformStats.totalLikes) }}</div>
          </div>
          <div class="kpi-icon green">
            <el-icon><Star /></el-icon>
          </div>
        </div>
        <div class="kpi-sub">
          <span class="muted">互动数据</span>
        </div>
      </el-card>

      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-top">
          <div class="kpi-left">
            <div class="kpi-title">总评论</div>
            <div class="kpi-value">{{ formatCompact(platformStats.totalComments) }}</div>
          </div>
          <div class="kpi-icon amber">
            <el-icon><ChatDotRound /></el-icon>
          </div>
        </div>
        <div class="kpi-sub">
          <span class="muted">用户反馈</span>
        </div>
      </el-card>

      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-top">
          <div class="kpi-left">
            <div class="kpi-title">总分享</div>
            <div class="kpi-value">{{ formatCompact(platformStats.totalShares) }}</div>
          </div>
          <div class="kpi-icon red">
            <el-icon><Share /></el-icon>
          </div>
        </div>
        <div class="kpi-sub">
          <span class="muted">传播效果</span>
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
            <div class="card-sub">播放量 / 点赞 / 评论</div>
          </div>
        </template>
        <div ref="compareEl" class="chart"></div>
      </el-card>
    </div>

    <el-card shadow="never" class="sv-card sv-table">
      <template #header>
        <div class="card-header">
          <div class="card-title">视频排行榜</div>
          <div class="card-sub">Top 10（按播放量）</div>
        </div>
      </template>
      <el-table :data="topVideos" style="width: 100%">
        <el-table-column prop="platformType" label="平台" width="110">
          <template #default="{ row }">{{ platformLabel(row.platformType) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="views" label="播放量" width="120" />
        <el-table-column prop="likes" label="点赞" width="100" />
        <el-table-column prop="comments" label="评论" width="100" />
        <el-table-column prop="shares" label="分享" width="100" />
        <el-table-column prop="publishTime" label="发布时间" width="190" />
      </el-table>
    </el-card>

    <el-card shadow="never" class="sv-card sv-table">
      <template #header>
        <div class="card-header">
          <div class="card-title">平台发布详情</div>
          <div class="card-sub">各平台数据统计</div>
        </div>
      </template>
      <el-table :data="platformDetails" style="width: 100%">
        <el-table-column prop="platform" label="平台" width="120">
          <template #default="{ row }">{{ platformLabel(row.platform) }}</template>
        </el-table-column>
        <el-table-column prop="videoCount" label="视频数" width="100" />
        <el-table-column prop="totalViews" label="总播放量" width="120" />
        <el-table-column prop="totalLikes" label="总点赞" width="100" />
        <el-table-column prop="totalComments" label="总评论" width="100" />
        <el-table-column prop="totalShares" label="总分享" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewPlatformDetail(row.platform)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { View, Star, ChatDotRound, Share } from '@element-plus/icons-vue'
import { getAnalysisDashboard, type AnalysisDashboard } from '@/api/analysis'
import { pageVideoInfo, type VideoInfoItem } from '@/api/videoInfo'

const router = useRouter()

const trendEl = ref<HTMLElement | null>(null)
const compareEl = ref<HTMLElement | null>(null)
let trendChart: echarts.ECharts | null = null
let compareChart: echarts.ECharts | null = null

const platformStats = ref({
  totalViews: 0,
  totalLikes: 0,
  totalComments: 0,
  totalShares: 0
})

const topVideos = ref<any[]>([])
const platformDetails = ref<any[]>([])

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

const viewPlatformDetail = (platform: string) => {
  router.push({ path: '/dashboard/videos', query: { tab: 'platform', platform } })
}

const buildTrendChart = (dashboard: AnalysisDashboard) => {
  if (!trendEl.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendEl.value)
  }
  const points = dashboard.trend || []
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

const buildCompareChart = (dashboard: AnalysisDashboard) => {
  if (!compareEl.value) return
  if (!compareChart) {
    compareChart = echarts.init(compareEl.value)
  }
  const agg = dashboard.platformAgg || []
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
  const dashboard = res.data as AnalysisDashboard
  
  // 更新统计数据
  platformStats.value = {
    totalViews: dashboard.overview?.totalViews || 0,
    totalLikes: dashboard.overview?.totalLikes || 0,
    totalComments: dashboard.overview?.totalComments || 0,
    totalShares: dashboard.overview?.totalShares || 0
  }
  
  // 更新排行榜
  topVideos.value = (dashboard.topVideos || []).slice(0, 10)
  
  // 更新平台详情
  platformDetails.value = (dashboard.platformAgg || []).map(item => ({
    platform: item.platformType,
    videoCount: item.videoCount,
    totalViews: item.views,
    totalLikes: item.likes,
    totalComments: item.comments,
    totalShares: item.shares || 0
  }))
  
  await nextTick()
  buildTrendChart(dashboard)
  buildCompareChart(dashboard)
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

@media (max-width: 1200px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
