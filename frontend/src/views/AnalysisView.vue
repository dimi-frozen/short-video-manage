<template>
  <div class="sv-page analysis-page">
    <el-card shadow="never" class="sv-card sv-toolbar">
      <div class="toolbar-row">
        <div class="left">
          <el-select v-model="filters.platformType" clearable placeholder="全平台" style="width: 160px">
            <el-option label="全平台" value="" />
            <el-option label="抖音" value="DOUYIN" />
            <el-option label="快手" value="KUAISHOU" />
            <el-option label="B站" value="BILIBILI" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
          <el-select v-model="filters.orderBy" style="width: 160px">
            <el-option label="Top 按播放量" value="views" />
            <el-option label="Top 按点赞数" value="likes" />
            <el-option label="Top 按评论数" value="comments" />
            <el-option label="Top 按分享数" value="shares" />
          </el-select>
          <el-button type="primary" :loading="loading" @click="load">刷新</el-button>
        </div>
        <div class="right">
          <el-tag type="info" effect="light">数据源：平台发布视频（video_info）</el-tag>
        </div>
      </div>
    </el-card>

    <div class="kpi-grid">
      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-title">视频总数</div>
        <div class="kpi-value">{{ formatCompact(dashboard?.overview?.totalVideos) }}</div>
        <div class="kpi-sub">统计范围内发布视频</div>
      </el-card>
      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-title">总播放量</div>
        <div class="kpi-value">{{ formatCompact(dashboard?.overview?.totalViews) }}</div>
        <div class="kpi-sub">views 累计</div>
      </el-card>
      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-title">总点赞</div>
        <div class="kpi-value">{{ formatCompact(dashboard?.overview?.totalLikes) }}</div>
        <div class="kpi-sub">likes 累计</div>
      </el-card>
      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-title">总评论</div>
        <div class="kpi-value">{{ formatCompact(dashboard?.overview?.totalComments) }}</div>
        <div class="kpi-sub">comments 累计</div>
      </el-card>
      <el-card shadow="never" class="sv-card kpi">
        <div class="kpi-title">总分享</div>
        <div class="kpi-value">{{ formatCompact(dashboard?.overview?.totalShares) }}</div>
        <div class="kpi-sub">shares 累计</div>
      </el-card>
    </div>

    <div class="grid-2">
      <el-card shadow="never" class="sv-card chart-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">趋势</div>
            <div class="card-sub">按天汇总（发布量带来的数据累计）</div>
          </div>
        </template>
        <div ref="trendEl" class="chart"></div>
      </el-card>

      <el-card shadow="never" class="sv-card chart-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">平台分布</div>
            <div class="card-sub">按平台聚合（播放量/视频数）</div>
          </div>
        </template>
        <div ref="platformEl" class="chart"></div>
      </el-card>
    </div>

    <el-card shadow="never" class="sv-card sv-table">
      <template #header>
        <div class="card-header">
          <div class="card-title">Top 视频</div>
          <div class="card-sub">按 {{ orderLabel(filters.orderBy) }} 排序</div>
        </div>
      </template>
      <el-table :data="dashboard?.topVideos || []" style="width: 100%">
        <el-table-column prop="platformType" label="平台" width="110">
          <template #default="{ row }">{{ platformLabel(row.platformType) }}</template>
        </el-table-column>
        <el-table-column prop="videoId" label="视频ID" min-width="240" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="publishTime" label="发布时间" width="190" />
        <el-table-column prop="views" label="播放量" width="120" />
        <el-table-column prop="likes" label="点赞" width="100" />
        <el-table-column prop="comments" label="评论" width="100" />
        <el-table-column prop="shares" label="分享" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import { getAnalysisDashboard, type AnalysisDashboard } from '@/api/analysis'

const loading = ref(false)
const dashboard = ref<AnalysisDashboard | null>(null)

const filters = reactive({
  platformType: '',
  orderBy: 'views'
})

const dateRange = ref<[string, string] | null>(null)

const trendEl = ref<HTMLDivElement | null>(null)
const platformEl = ref<HTMLDivElement | null>(null)
let trendChart: echarts.ECharts | null = null
let platformChart: echarts.ECharts | null = null

const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`)
const formatDateTime = (d: Date) => {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const initDefaultRange = () => {
  const end = new Date()
  const start = new Date(end.getTime() - 6 * 24 * 60 * 60 * 1000)
  start.setHours(0, 0, 0, 0)
  end.setHours(23, 59, 59, 0)
  dateRange.value = [formatDateTime(start), formatDateTime(end)]
}

const load = async () => {
  loading.value = true
  try {
    const params: any = {
      platformType: filters.platformType || undefined,
      orderBy: filters.orderBy || undefined,
      topLimit: 10
    }
    if (dateRange.value) {
      params.startTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res: any = await getAnalysisDashboard(params)
    dashboard.value = res.data
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

const renderCharts = () => {
  if (trendEl.value) {
    if (!trendChart) trendChart = echarts.init(trendEl.value)
    const points = dashboard.value?.trend || []
    trendChart.setOption({
      grid: { left: 44, right: 18, top: 26, bottom: 30 },
      tooltip: { trigger: 'axis' },
      legend: { top: 0, left: 0 },
      xAxis: { type: 'category', data: points.map(p => p.day), axisLabel: { color: 'rgba(15,23,42,0.65)' } },
      yAxis: { type: 'value', axisLabel: { color: 'rgba(15,23,42,0.65)' }, splitLine: { lineStyle: { color: 'rgba(15,23,42,0.08)' } } },
      series: [
        { name: '播放量', type: 'line', smooth: true, showSymbol: false, data: points.map(p => p.views), lineStyle: { width: 2 } },
        { name: '点赞', type: 'line', smooth: true, showSymbol: false, data: points.map(p => p.likes), lineStyle: { width: 2 } },
        { name: '评论', type: 'line', smooth: true, showSymbol: false, data: points.map(p => p.comments), lineStyle: { width: 2 } },
        { name: '分享', type: 'line', smooth: true, showSymbol: false, data: points.map(p => p.shares), lineStyle: { width: 2 } }
      ]
    })
  }

  if (platformEl.value) {
    if (!platformChart) platformChart = echarts.init(platformEl.value)
    const rows = dashboard.value?.platformAgg || []
    platformChart.setOption({
      grid: { left: 44, right: 18, top: 26, bottom: 30 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      legend: { top: 0, left: 0 },
      xAxis: { type: 'category', data: rows.map(r => platformLabel(r.platformType)), axisLabel: { color: 'rgba(15,23,42,0.65)' } },
      yAxis: [
        { type: 'value', name: '播放量', axisLabel: { color: 'rgba(15,23,42,0.65)' }, splitLine: { lineStyle: { color: 'rgba(15,23,42,0.08)' } } },
        { type: 'value', name: '视频数', axisLabel: { color: 'rgba(15,23,42,0.65)' }, splitLine: { show: false } }
      ],
      series: [
        { name: '播放量', type: 'bar', data: rows.map(r => r.views), barWidth: 18, itemStyle: { borderRadius: [8, 8, 0, 0] } },
        { name: '视频数', type: 'line', yAxisIndex: 1, smooth: true, showSymbol: false, data: rows.map(r => r.videoCount), lineStyle: { width: 2 } }
      ]
    })
  }
}

const platformLabel = (t: string) => {
  if (t === 'DOUYIN') return '抖音'
  if (t === 'KUAISHOU') return '快手'
  if (t === 'BILIBILI') return 'B站'
  return t || '-'
}

const orderLabel = (o: string) => {
  if (o === 'likes') return '点赞数'
  if (o === 'comments') return '评论数'
  if (o === 'shares') return '分享数'
  return '播放量'
}

const formatCompact = (v: any) => {
  const n = Number(v || 0)
  if (n >= 1e8) return `${(n / 1e8).toFixed(2)}亿`
  if (n >= 1e4) return `${(n / 1e4).toFixed(1)}万`
  return `${n}`
}

const handleResize = () => {
  trendChart?.resize()
  platformChart?.resize()
}

watch(() => [filters.platformType, filters.orderBy], () => load())

onMounted(() => {
  initDefaultRange()
  load()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  platformChart?.dispose()
  trendChart = null
  platformChart = null
})
</script>

<style scoped>
.toolbar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.kpi-grid {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.kpi {
  padding: 14px;
}

.kpi-title {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.62);
}

.kpi-value {
  margin-top: 8px;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.2px;
}

.kpi-sub {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.grid-2 {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 12px;
}

.chart-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-title {
  font-size: 14px;
  font-weight: 800;
}

.card-sub {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.chart {
  height: 340px;
}

@media (max-width: 1100px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .grid-2 {
    grid-template-columns: 1fr;
  }
  .chart {
    height: 300px;
  }
}
</style>
