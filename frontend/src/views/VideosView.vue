<template>
  <div class="sv-page videos-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="本地视频" name="local">
        <el-card shadow="never" class="sv-card sv-toolbar">
          <div class="toolbar-row">
            <div class="left">
              <el-input v-model="localQuery.keyword" placeholder="搜索标题/描述" clearable style="width: 260px" @keyup.enter="loadLocal" />
              <el-input v-model="localQuery.tag" placeholder="按标签过滤" clearable style="width: 180px" @keyup.enter="loadLocal" />
              <el-button type="primary" @click="loadLocal">查询</el-button>
              <el-button @click="resetLocal">重置</el-button>
            </div>
            <div class="right">
              <el-button type="primary" @click="openUpload">上传视频</el-button>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="sv-card sv-table">
          <el-table :data="localTableData" style="width: 100%" @sort-change="onLocalSortChange">
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="description" label="描述" min-width="240" show-overflow-tooltip />
            <el-table-column label="标签" min-width="180">
              <template #default="{ row }">
                <el-space wrap>
                  <el-tag v-for="t in row.tags" :key="t" size="small">{{ t }}</el-tag>
                </el-space>
              </template>
            </el-table-column>
            <el-table-column prop="fileSize" label="大小" width="120">
              <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
            </el-table-column>
            <el-table-column prop="duration" label="时长(s)" width="110" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column prop="createTime" label="创建时间" sortable="custom" width="190" />
            <el-table-column prop="updateTime" label="更新时间" sortable="custom" width="190" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
                <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
                <el-button link type="primary" @click="openPublishSelect(row.id)">发布</el-button>
                <el-button link type="danger" @click="confirmDelete(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="localTotal"
              :page-size="localQuery.pageSize"
              :current-page="localQuery.pageNum"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="onLocalSizeChange"
              @current-change="onLocalCurrentChange"
            />
          </div>
        </el-card>

        <el-dialog v-model="uploadVisible" title="上传视频" width="560px" :close-on-click-modal="false" class="sv-dialog">
          <el-form :model="uploadForm" :rules="uploadRules" ref="uploadFormRef" label-position="top">
            <el-form-item label="视频文件" prop="file">
              <el-upload
                :auto-upload="false"
                :limit="1"
                :on-change="onFileChange"
                :on-remove="onFileRemove"
                accept=".mp4,.mov,.avi"
              >
                <el-button>选择视频文件</el-button>
                <template #tip>
                  <div class="el-upload__tip">仅支持 mp4/mov/avi，最大 100MB（以服务端配置为准）</div>
                </template>
              </el-upload>
            </el-form-item>
            <el-form-item label="封面图片">
              <el-upload
                v-model:file-list="coverFileList"
                :auto-upload="false"
                :limit="1"
                :on-change="onCoverChange"
                :on-remove="onCoverRemove"
                accept=".jpg,.jpeg,.png,.gif"
                list-type="picture-card"
              >
                <el-icon><Plus /></el-icon>
                <template #tip>
                  <div class="el-upload__tip">支持 jpg/png/gif 格式，可选上传</div>
                </template>
              </el-upload>
            </el-form-item>
            <el-form-item label="标题" prop="title">
              <el-input v-model="uploadForm.title" maxlength="128" show-word-limit placeholder="1-128 字符" />
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="uploadForm.description" type="textarea" :rows="3" placeholder="可选" />
            </el-form-item>
            <el-form-item label="标签（最多 5 个，每个 ≤ 16 字符）" prop="tags">
              <el-select v-model="uploadForm.tags" multiple filterable allow-create default-first-option style="width: 100%" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="uploadVisible = false">取消</el-button>
            <el-button type="primary" :loading="uploading" @click="submitUpload">上传</el-button>
          </template>
        </el-dialog>

        <el-dialog v-model="detailVisible" title="视频详情" width="640px" class="sv-dialog">
          <el-descriptions v-if="detail" :column="2" border>
            <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
            <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
            <el-descriptions-item label="标题" :span="2">{{ detail.title }}</el-descriptions-item>
            <el-descriptions-item label="描述" :span="2">{{ detail.description }}</el-descriptions-item>
            <el-descriptions-item label="标签" :span="2">
              <el-space wrap>
                <el-tag v-for="t in detail.tags" :key="t" size="small">{{ t }}</el-tag>
              </el-space>
            </el-descriptions-item>
            <el-descriptions-item label="大小">{{ formatSize(detail.fileSize) }}</el-descriptions-item>
            <el-descriptions-item label="时长(s)">{{ detail.duration }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ detail.createTime }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ detail.updateTime }}</el-descriptions-item>
            <el-descriptions-item label="文件地址" :span="2">{{ detail.filePath }}</el-descriptions-item>
          </el-descriptions>
          <div v-if="detail?.coverUrl || (detail?.filePath && (detail.filePath.startsWith('http://') || detail.filePath.startsWith('https://')))" class="media-preview">
            <img v-if="detail?.coverUrl" :src="detail.coverUrl" class="cover" alt="cover" />
            <video
              v-if="detail?.filePath && (detail.filePath.startsWith('http://') || detail.filePath.startsWith('https://'))"
              :src="detail.filePath"
              class="player"
              controls
            ></video>
          </div>
          <template #footer>
            <el-button @click="detailVisible = false">关闭</el-button>
          </template>
        </el-dialog>

        <el-dialog v-model="editVisible" title="编辑视频" width="560px" :close-on-click-modal="false" class="sv-dialog">
          <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-position="top">
            <el-form-item label="标题" prop="title">
              <el-input v-model="editForm.title" maxlength="128" show-word-limit />
            </el-form-item>
            <el-form-item label="描述">
              <el-input v-model="editForm.description" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="标签" prop="tags">
              <el-select v-model="editForm.tags" multiple filterable allow-create default-first-option style="width: 100%" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="editVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
          </template>
        </el-dialog>

        <el-dialog v-model="publishSelectVisible" title="选择发布平台" width="520px" :close-on-click-modal="false" class="sv-dialog">
          <el-form :model="publishSelectForm" label-position="top">
            <el-form-item label="发布平台">
              <el-radio-group v-model="publishSelectForm.platform">
                <el-radio-button label="douyin">抖音</el-radio-button>
                <el-radio-button label="kuaishou">快手</el-radio-button>
                <el-radio-button label="bilibili">B站</el-radio-button>
              </el-radio-group>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="publishSelectVisible = false">取消</el-button>
            <el-button type="primary" @click="goPlatformPublish">进入发布页</el-button>
          </template>
        </el-dialog>

      </el-tab-pane>
      <el-tab-pane label="平台发布" name="platform">
        <el-card shadow="never" class="sv-card sv-toolbar">
          <div class="toolbar-row">
            <div class="left">
              <el-input v-model="platformQuery.keyword" placeholder="搜索标题" clearable style="width: 260px" @keyup.enter="loadPlatform" />
              <el-select v-model="platformQuery.platformType" clearable placeholder="平台" style="width: 160px">
                <el-option label="抖音" value="DOUYIN" />
                <el-option label="快手" value="KUAISHOU" />
                <el-option label="B站" value="BILIBILI" />
              </el-select>
              <el-date-picker
                v-model="platformTimeRange"
                type="datetimerange"
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
              <el-select v-model="platformQuery.sortBy" style="width: 140px">
                <el-option label="播放量" value="views" />
                <el-option label="发布时间" value="publishTime" />
                <el-option label="点赞" value="likes" />
                <el-option label="评论" value="comments" />
                <el-option label="分享" value="shares" />
              </el-select>
              <el-select v-model="platformQuery.sortOrder" style="width: 110px">
                <el-option label="降序" value="desc" />
                <el-option label="升序" value="asc" />
              </el-select>
              <el-button type="primary" @click="loadPlatform">查询</el-button>
              <el-button @click="resetPlatform">重置</el-button>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="sv-card sv-table">
          <el-table :data="platformTableData" style="width: 100%">
            <el-table-column prop="platformType" label="平台" width="110">
              <template #default="{ row }">{{ platformLabel(row.platformType) }}</template>
            </el-table-column>
            <el-table-column prop="videoId" label="视频ID" min-width="260" show-overflow-tooltip />
            <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="publishTime" label="发布时间" width="190" />
            <el-table-column prop="views" label="播放量" width="120" />
            <el-table-column prop="likes" label="点赞" width="100" />
            <el-table-column prop="comments" label="评论" width="100" />
            <el-table-column prop="shares" label="分享" width="100" />
            <el-table-column prop="status" label="状态" width="120" />
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="refreshStats(row.videoId)">刷新数据</el-button>
                <el-button link type="primary" @click="openPlatformEdit(row)">改标题</el-button>
                <el-button link type="danger" @click="confirmPlatformDelete(row.videoId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="platformTotal"
              :page-size="platformQuery.pageSize"
              :current-page="platformQuery.pageNum"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="onPlatformSizeChange"
              @current-change="onPlatformCurrentChange"
            />
          </div>
        </el-card>

        <el-dialog v-model="platformEditVisible" title="修改标题" width="520px" :close-on-click-modal="false" class="sv-dialog">
          <el-form :model="platformEditForm" :rules="platformEditRules" ref="platformEditFormRef" label-position="top">
            <el-form-item label="标题" prop="title">
              <el-input v-model="platformEditForm.title" maxlength="128" show-word-limit />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="platformEditVisible = false">取消</el-button>
            <el-button type="primary" :loading="platformSaving" @click="submitPlatformEdit">保存</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { deleteVideo, getVideo, pageVideos, updateVideo, uploadVideo, type VideoDetail, type VideoListItem } from '@/api/video'
import { deleteVideoInfo, pageVideoInfo, refreshVideoInfoStats, updateVideoInfoTitle, type VideoInfoItem } from '@/api/videoInfo'
import { useRoute, useRouter } from 'vue-router'

const activeTab = ref<'local' | 'platform'>('local')
const route = useRoute()
const router = useRouter()

const localQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  tag: '',
  sort: [] as string[]
})

const localTableData = ref<VideoListItem[]>([])
const localTotal = ref(0)

const platformQuery = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  platformType: '' as string | '',
  sortBy: 'views',
  sortOrder: 'desc'
})
const platformTimeRange = ref<[string, string] | null>(null)
const platformTableData = ref<VideoInfoItem[]>([])
const platformTotal = ref(0)

const uploadVisible = ref(false)
const uploading = ref(false)
const uploadFormRef = ref<FormInstance>()
const coverFileList = ref<any[]>([])
const uploadForm = reactive({
  file: null as File | null,
  cover: null as File | null,
  title: '',
  description: '',
  tags: [] as string[]
})
const uploadRules: FormRules = {
  file: [{ required: true, message: '请选择视频文件', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }, { min: 1, max: 128, message: '标题长度需在 1-128 字符', trigger: 'blur' }],
  tags: [
    {
      validator: (_rule, value, callback) => {
        const tags = (value || []) as string[]
        if (tags.length > 5) return callback(new Error('标签最多 5 个'))
        for (const t of tags) {
          if ((t || '').length > 16) return callback(new Error('单个标签不超过 16 字符'))
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

const detailVisible = ref(false)
const editVisible = ref(false)
const saving = ref(false)
const detail = ref<VideoDetail | null>(null)
const currentEditId = ref<number | null>(null)
const editFormRef = ref<FormInstance>()
const editForm = reactive({
  title: '',
  description: '',
  tags: [] as string[]
})
const editRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }, { min: 1, max: 128, message: '标题长度需在 1-128 字符', trigger: 'blur' }],
  tags: [
    {
      validator: (_rule, value, callback) => {
        const tags = (value || []) as string[]
        if (tags.length > 5) return callback(new Error('标签最多 5 个'))
        for (const t of tags) {
          if ((t || '').length > 16) return callback(new Error('单个标签不超过 16 字符'))
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

const loadLocal = async () => {
  const res: any = await pageVideos({
    pageNum: localQuery.pageNum,
    pageSize: localQuery.pageSize,
    keyword: localQuery.keyword || undefined,
    tag: localQuery.tag || undefined,
    sort: localQuery.sort.length ? localQuery.sort : undefined,
    includeUnapproved: false
  })
  localTotal.value = res.data.total || 0
  localTableData.value = res.data.list || []
}

const resetLocal = async () => {
  localQuery.pageNum = 1
  localQuery.pageSize = 10
  localQuery.keyword = ''
  localQuery.tag = ''
  localQuery.sort = []
  await loadLocal()
}

const onLocalSizeChange = async (size: number) => {
  localQuery.pageSize = size
  localQuery.pageNum = 1
  await loadLocal()
}

const onLocalCurrentChange = async (pageNum: number) => {
  localQuery.pageNum = pageNum
  await loadLocal()
}

const onLocalSortChange = async ({ prop, order }: any) => {
  if (!prop || !order) {
    localQuery.sort = []
  } else {
    const dir = order === 'descending' ? 'desc' : 'asc'
    localQuery.sort = [`${prop},${dir}`]
  }
  await loadLocal()
}

const openUpload = () => {
  uploadForm.file = null
  uploadForm.cover = null
  coverFileList.value = []
  uploadForm.title = ''
  uploadForm.description = ''
  uploadForm.tags = []
  uploadVisible.value = true
}

const onFileChange = (file: UploadFile) => {
  const raw = (file.raw as File) || null
  if (!raw) {
    uploadForm.file = null
    return
  }
  const maxBytes = 100 * 1024 * 1024
  if (raw.size > maxBytes) {
    ElMessage.error('文件大小不能超过 100MB')
    uploadForm.file = null
    return
  }
  const name = raw.name || ''
  const lower = name.toLowerCase()
  if (!(lower.endsWith('.mp4') || lower.endsWith('.mov') || lower.endsWith('.avi'))) {
    ElMessage.error('仅支持 mp4/mov/avi 格式')
    uploadForm.file = null
    return
  }
  uploadForm.file = raw
}

const onFileRemove = () => {
  uploadForm.file = null
}

const onCoverChange = (file: UploadFile) => {
  const raw = (file.raw as File) || null
  if (!raw) {
    uploadForm.cover = null
    return
  }
  const maxBytes = 5 * 1024 * 1024 // 5MB
  if (raw.size > maxBytes) {
    ElMessage.error('封面文件大小不能超过 5MB')
    uploadForm.cover = null
    return
  }
  const name = raw.name || ''
  const lower = name.toLowerCase()
  if (!(lower.endsWith('.jpg') || lower.endsWith('.jpeg') || lower.endsWith('.png') || lower.endsWith('.gif'))) {
    ElMessage.error('仅支持 jpg/png/gif 格式')
    uploadForm.cover = null
    return
  }
  uploadForm.cover = raw
}

const onCoverRemove = () => {
  uploadForm.cover = null
  coverFileList.value = []
}

const submitUpload = async () => {
  if (!uploadFormRef.value) return
  await uploadFormRef.value.validate(async valid => {
    if (!valid) return
    if (!uploadForm.file) return
    uploading.value = true
    try {
      const form = new FormData()
      form.append('file', uploadForm.file)
      if (uploadForm.cover) {
        form.append('cover', uploadForm.cover)
      }
      form.append('title', uploadForm.title)
      if (uploadForm.description) form.append('description', uploadForm.description)
      for (const t of uploadForm.tags) {
        form.append('tags', t)
      }
      try {
        await uploadVideo(form)
        ElMessage.success('上传成功')
        uploadVisible.value = false
        await loadLocal()
      } catch (e: any) {
        const msg = e?.message || '上传失败'
        if (msg === 'Network Error') {
          ElMessage.error('上传失败：后端接口不可用或上传被中断（请确认后端已启动且数据库表已初始化）')
        } else {
          ElMessage.error(`上传失败：${msg}`)
        }
      }
    } finally {
      uploading.value = false
    }
  })
}

const openDetail = async (id: number) => {
  const res: any = await getVideo(id, { includeUnapproved: true })
  detail.value = res.data
  detailVisible.value = true
}

const openEdit = async (id: number) => {
  const res: any = await getVideo(id, { includeUnapproved: true })
  const d = res.data as VideoDetail
  currentEditId.value = id
  editForm.title = d.title
  editForm.description = d.description
  editForm.tags = (d.tags || []).slice()
  editVisible.value = true
}

const submitEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async valid => {
    if (!valid) return
    if (!currentEditId.value) return
    saving.value = true
    try {
      await updateVideo(currentEditId.value, {
        title: editForm.title,
        description: editForm.description,
        tags: editForm.tags
      })
      ElMessage.success('保存成功')
      editVisible.value = false
      await loadLocal()
    } finally {
      saving.value = false
    }
  })
}

const confirmDelete = async (id: number) => {
  await ElMessageBox.confirm('确认删除该视频吗？', '提示', { type: 'warning' })
  await deleteVideo(id)
  ElMessage.success('删除成功')
  await loadLocal()
}

const publishSelectVisible = ref(false)
const publishSelectForm = reactive({
  videoId: 0,
  platform: 'douyin' as 'douyin' | 'kuaishou' | 'bilibili'
})

const openPublishSelect = (videoId: number) => {
  publishSelectForm.videoId = videoId
  publishSelectForm.platform = 'douyin'
  publishSelectVisible.value = true
}

const goPlatformPublish = () => {
  if (!publishSelectForm.videoId) return
  const p = publishSelectForm.platform
  publishSelectVisible.value = false
  router.push(`/dashboard/platform-publish/${p}/${publishSelectForm.videoId}`)
}

const loadPlatform = async () => {
  const params: any = {
    pageNum: platformQuery.pageNum,
    pageSize: platformQuery.pageSize,
    keyword: platformQuery.keyword || undefined,
    platformType: platformQuery.platformType || undefined,
    sortBy: platformQuery.sortBy || undefined,
    sortOrder: platformQuery.sortOrder || undefined
  }
  if (platformTimeRange.value) {
    params.startTime = platformTimeRange.value[0]
    params.endTime = platformTimeRange.value[1]
  }
  const res: any = await pageVideoInfo(params)
  platformTotal.value = res.data.total || 0
  platformTableData.value = res.data.records || []
}

const resetPlatform = async () => {
  platformQuery.pageNum = 1
  platformQuery.pageSize = 10
  platformQuery.keyword = ''
  platformQuery.platformType = ''
  platformQuery.sortBy = 'views'
  platformQuery.sortOrder = 'desc'
  platformTimeRange.value = null
  await loadPlatform()
}

const onPlatformSizeChange = async (size: number) => {
  platformQuery.pageSize = size
  platformQuery.pageNum = 1
  await loadPlatform()
}

const onPlatformCurrentChange = async (pageNum: number) => {
  platformQuery.pageNum = pageNum
  await loadPlatform()
}

const refreshStats = async (videoId: string) => {
  const res: any = await refreshVideoInfoStats(videoId)
  const s = res.data
  const idx = platformTableData.value.findIndex(v => v.videoId === videoId)
  if (idx >= 0) {
    platformTableData.value[idx] = {
      ...platformTableData.value[idx],
      views: s.views,
      likes: s.likes,
      comments: s.comments,
      shares: s.shares
    }
  }
  ElMessage.success('已刷新')
}

const platformEditVisible = ref(false)
const platformSaving = ref(false)
const platformEditFormRef = ref<FormInstance>()
const platformEditForm = reactive({ videoId: '', title: '' })
const platformEditRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }, { min: 1, max: 128, message: '标题长度需在 1-128 字符', trigger: 'blur' }]
}

const openPlatformEdit = (row: VideoInfoItem) => {
  platformEditForm.videoId = row.videoId
  platformEditForm.title = row.title
  platformEditVisible.value = true
}

const submitPlatformEdit = async () => {
  if (!platformEditFormRef.value) return
  await platformEditFormRef.value.validate(async valid => {
    if (!valid) return
    platformSaving.value = true
    try {
      await updateVideoInfoTitle(platformEditForm.videoId, platformEditForm.title)
      ElMessage.success('保存成功')
      platformEditVisible.value = false
      await loadPlatform()
    } finally {
      platformSaving.value = false
    }
  })
}

const confirmPlatformDelete = async (videoId: string) => {
  await ElMessageBox.confirm('确认删除该平台视频吗？', '提示', { type: 'warning' })
  await deleteVideoInfo(videoId)
  ElMessage.success('删除成功')
  await loadPlatform()
}

const platformLabel = (t: string) => {
  if (t === 'DOUYIN') return '抖音'
  if (t === 'KUAISHOU') return '快手'
  if (t === 'BILIBILI') return 'B站'
  return t || '-'
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

onMounted(() => {
  const tab = String(route.query.tab || '')
  if (tab === 'platform') {
    activeTab.value = 'platform'
  } else {
    activeTab.value = 'local'
  }

  const openId = Number(route.query.openId || 0)
  loadLocal().then(async () => {
    if (activeTab.value === 'local' && openId) {
      try {
        await openDetail(openId)
      } catch (e) {
      }
    }
  })
  loadPlatform()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}

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
  flex-wrap: wrap;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.media-preview {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.cover {
  width: 100%;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  object-fit: cover;
  aspect-ratio: 16 / 9;
}

.player {
  width: 100%;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(15, 23, 42, 0.92);
  aspect-ratio: 16 / 9;
}
</style>
