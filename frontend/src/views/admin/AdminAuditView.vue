<template>
  <div class="sv-page admin-audit-page">
    <el-card shadow="never" class="sv-card sv-toolbar">
      <div class="toolbar-row">
        <div class="left">
          <el-button type="primary" :loading="loading" @click="load">刷新</el-button>
        </div>
        <div class="right">
          <el-tag type="warning" effect="light">待审核</el-tag>
          <el-tag type="info" effect="light">{{ total }} 条</el-tag>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="sv-card sv-table">
      <el-table :data="list" style="width: 100%" v-loading="loading">
        <el-table-column label="用户ID" width="110">
          <template #default> - </template>
        </el-table-column>
        <el-table-column label="用户名" width="140">
          <template #default> - </template>
        </el-table-column>
        <el-table-column label="预览" width="220">
          <template #default="{ row }">
            <video v-if="row.fileUrl" :src="row.fileUrl" class="preview" controls playsinline />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="封面" width="160">
          <template #default="{ row }">
            <img v-if="row.coverUrl" :src="row.coverUrl" class="cover" alt="cover" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="240" show-overflow-tooltip />
        <el-table-column prop="createTime" label="上传时间" width="190" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" @click="approve(row.id)" :loading="row._loadingApprove">通过</el-button>
            <el-button link type="danger" @click="reject(row.id)" :loading="row._loadingReject">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && list.length === 0" description="暂无待审核视频" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApproveVideo, adminPendingVideos, adminRejectVideo, type PendingVideoItem } from '@/api/adminVideoAudit'

type Row = PendingVideoItem & { _loadingApprove?: boolean; _loadingReject?: boolean }

const loading = ref(false)
const list = ref<Row[]>([])
const total = computed(() => list.value.length)

const load = async () => {
  loading.value = true
  try {
    const res: any = await adminPendingVideos(200)
    list.value = (res.data || []).map((it: PendingVideoItem) => ({ ...it }))
  } finally {
    loading.value = false
  }
}

const approve = async (id: number) => {
  const row = list.value.find(r => r.id === id)
  if (!row) return
  await ElMessageBox.confirm('确认审核通过该视频吗？', '提示', { type: 'warning' })
  row._loadingApprove = true
  try {
    await adminApproveVideo(id)
    ElMessage.success('已通过')
    await load()
  } finally {
    row._loadingApprove = false
  }
}

const reject = async (id: number) => {
  const row = list.value.find(r => r.id === id)
  if (!row) return
  await ElMessageBox.confirm('确认拒绝该视频吗？', '提示', { type: 'warning' })
  row._loadingReject = true
  try {
    await adminRejectVideo(id)
    ElMessage.success('已拒绝')
    await load()
  } finally {
    row._loadingReject = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview {
  width: 200px;
  height: 112px;
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.92);
  border: 1px solid rgba(15, 23, 42, 0.10);
}

.cover {
  width: 140px;
  height: 80px;
  border-radius: 10px;
  object-fit: cover;
  border: 1px solid rgba(15, 23, 42, 0.10);
}
</style>

