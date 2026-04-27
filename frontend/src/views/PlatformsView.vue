<template>
  <div class="sv-page platforms-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="抖音" name="douyin">
        <el-card shadow="never" class="sv-card mb-12">
          <div class="row">
            <div class="left">
              <el-button type="primary" @click="handleAuth" :loading="authLoading">获取模拟授权</el-button>
              <el-button @click="clearToken">清除 Token</el-button>
            </div>
            <div class="right">
              <el-tag v-if="token" type="success">已授权</el-tag>
              <el-tag v-else type="info">未授权</el-tag>
            </div>
          </div>
          <el-descriptions v-if="authInfo" :column="2" border class="mt-12">
            <el-descriptions-item label="Auth URL" :span="2">{{ authInfo.authUrl }}</el-descriptions-item>
            <el-descriptions-item label="Access Token" :span="2">{{ authInfo.accessToken }}</el-descriptions-item>
            <el-descriptions-item label="过期时间">{{ authInfo.expireAt }}</el-descriptions-item>
            <el-descriptions-item label="有效期(s)">{{ authInfo.expiresInSeconds }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="sv-card mb-12">
          <template #header>抖音视频发布（模拟）</template>
          <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-position="top">
            <el-form-item label="视频文件" prop="file">
              <el-upload :auto-upload="false" :limit="1" accept=".mp4,.mov,.avi" :on-change="onPublishFileChange" :on-remove="onPublishFileRemove">
                <el-button>选择文件</el-button>
              </el-upload>
            </el-form-item>
            <el-form-item label="标题" prop="title">
              <el-input v-model="publishForm.title" maxlength="128" show-word-limit placeholder="1-128 字符" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitPublish" :loading="publishLoading">发布</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="sv-card sv-table">
          <template #header>
            <div class="row">
              <div class="left">
                <el-date-picker
                  v-model="timeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
                <el-button type="primary" @click="loadList">查询</el-button>
                <el-button @click="resetList">重置</el-button>
              </div>
              <div class="right">
                <el-select v-model="sortBy" style="width: 160px">
                  <el-option label="按播放量" value="views" />
                  <el-option label="按发布时间" value="publishTime" />
                  <el-option label="按点赞数" value="likes" />
                  <el-option label="按评论数" value="comments" />
                  <el-option label="按分享数" value="shares" />
                </el-select>
                <el-select v-model="sortOrder" style="width: 120px">
                  <el-option label="降序" value="desc" />
                  <el-option label="升序" value="asc" />
                </el-select>
              </div>
            </div>
          </template>

          <el-table :data="list" style="width: 100%">
            <el-table-column prop="videoId" label="视频ID" width="260" />
            <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="publishTime" label="发布时间" width="190" />
            <el-table-column prop="views" label="播放量" width="120" />
            <el-table-column prop="likes" label="点赞" width="100" />
            <el-table-column prop="comments" label="评论" width="100" />
            <el-table-column prop="shares" label="分享" width="100" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="refreshStats(row.videoId)">刷新数据</el-button>
                <el-button link type="primary" @click="openEdit(row)">改标题</el-button>
                <el-button link type="danger" @click="remove(row.videoId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              :page-size="page.pageSize"
              :current-page="page.pageNum"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="onSizeChange"
              @current-change="onCurrentChange"
            />
          </div>
        </el-card>

        <el-dialog v-model="editVisible" title="修改标题" width="520px" :close-on-click-modal="false" class="sv-dialog">
          <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-position="top">
            <el-form-item label="标题" prop="title">
              <el-input v-model="editForm.title" maxlength="128" show-word-limit />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="editVisible = false">取消</el-button>
            <el-button type="primary" :loading="editLoading" @click="submitEdit">保存</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <el-tab-pane label="快手" name="kuaishou">
        <el-card shadow="never" class="sv-card mb-12">
          <div class="row">
            <div class="left">
              <el-button type="primary" @click="handleKsAuth" :loading="ksAuthLoading">获取授权（沙箱/模拟）</el-button>
              <el-button @click="clearKsToken">清除 Token</el-button>
            </div>
            <div class="right">
              <el-tag v-if="ksToken" type="success">已授权</el-tag>
              <el-tag v-else type="info">未授权</el-tag>
            </div>
          </div>
          <el-descriptions v-if="ksAuthInfo" :column="2" border class="mt-12">
            <el-descriptions-item label="Auth URL" :span="2">{{ ksAuthInfo.authUrl }}</el-descriptions-item>
            <el-descriptions-item label="Access Token" :span="2">{{ ksAuthInfo.accessToken }}</el-descriptions-item>
            <el-descriptions-item label="过期时间">{{ ksAuthInfo.expireAt }}</el-descriptions-item>
            <el-descriptions-item label="有效期(s)">{{ ksAuthInfo.expiresInSeconds }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="sv-card mb-12">
          <template #header>快手视频发布（沙箱/模拟）</template>
          <el-form :model="ksPublishForm" :rules="publishRules" ref="ksPublishFormRef" label-position="top">
            <el-form-item label="视频文件" prop="file">
              <el-upload :auto-upload="false" :limit="1" accept=".mp4,.mov,.avi" :on-change="onKsPublishFileChange" :on-remove="onKsPublishFileRemove">
                <el-button>选择文件</el-button>
              </el-upload>
            </el-form-item>
            <el-form-item label="标题" prop="title">
              <el-input v-model="ksPublishForm.title" maxlength="128" show-word-limit placeholder="1-128 字符" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitKsPublish" :loading="ksPublishLoading">发布</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="sv-card sv-table">
          <template #header>
            <div class="row">
              <div class="left">
                <el-date-picker
                  v-model="ksTimeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
                <el-button type="primary" @click="loadKsList">查询</el-button>
                <el-button @click="resetKsList">重置</el-button>
              </div>
              <div class="right">
                <el-select v-model="ksSortBy" style="width: 160px">
                  <el-option label="按播放量" value="views" />
                  <el-option label="按发布时间" value="publishTime" />
                  <el-option label="按点赞数" value="likes" />
                  <el-option label="按评论数" value="comments" />
                  <el-option label="按分享数" value="shares" />
                </el-select>
                <el-select v-model="ksSortOrder" style="width: 120px">
                  <el-option label="降序" value="desc" />
                  <el-option label="升序" value="asc" />
                </el-select>
              </div>
            </div>
          </template>

          <el-table :data="ksList" style="width: 100%">
            <el-table-column prop="videoId" label="视频ID" width="260" />
            <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="publishTime" label="发布时间" width="190" />
            <el-table-column prop="views" label="播放量" width="120" />
            <el-table-column prop="likes" label="点赞" width="100" />
            <el-table-column prop="comments" label="评论" width="100" />
            <el-table-column prop="shares" label="分享" width="100" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="refreshKsStats(row.videoId)">刷新数据</el-button>
                <el-button link type="primary" @click="openKsEdit(row)">改标题</el-button>
                <el-button link type="danger" @click="removeKs(row.videoId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="ksTotal"
              :page-size="ksPage.pageSize"
              :current-page="ksPage.pageNum"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="onKsSizeChange"
              @current-change="onKsCurrentChange"
            />
          </div>
        </el-card>

        <el-dialog v-model="ksEditVisible" title="修改标题" width="520px" :close-on-click-modal="false" class="sv-dialog">
          <el-form :model="ksEditForm" :rules="editRules" ref="ksEditFormRef" label-position="top">
            <el-form-item label="标题" prop="title">
              <el-input v-model="ksEditForm.title" maxlength="128" show-word-limit />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="ksEditVisible = false">取消</el-button>
            <el-button type="primary" :loading="ksEditLoading" @click="submitKsEdit">保存</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>
      <el-tab-pane label="B站" name="bilibili">
        <el-card shadow="never" class="sv-card mb-12">
          <div class="row">
            <div class="left">
              <el-button type="primary" @click="handleBiliAuth" :loading="biliAuthLoading">获取模拟授权</el-button>
              <el-button @click="clearBiliToken">清除 Token</el-button>
            </div>
            <div class="right">
              <el-tag v-if="biliToken" type="success">已授权</el-tag>
              <el-tag v-else type="info">未授权</el-tag>
            </div>
          </div>
          <el-descriptions v-if="biliAuthInfo" :column="2" border class="mt-12">
            <el-descriptions-item label="Auth URL" :span="2">{{ biliAuthInfo.authUrl }}</el-descriptions-item>
            <el-descriptions-item label="Access Token" :span="2">{{ biliAuthInfo.accessToken }}</el-descriptions-item>
            <el-descriptions-item label="过期时间">{{ biliAuthInfo.expireAt }}</el-descriptions-item>
            <el-descriptions-item label="有效期(s)">{{ biliAuthInfo.expiresInSeconds }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card shadow="never" class="sv-card mb-12">
          <template #header>B站视频发布（模拟）</template>
          <el-form :model="biliPublishForm" :rules="publishRules" ref="biliPublishFormRef" label-position="top">
            <el-form-item label="视频文件" prop="file">
              <el-upload
                :auto-upload="false"
                :limit="1"
                accept=".mp4,.mov,.avi"
                :on-change="onBiliPublishFileChange"
                :on-remove="onBiliPublishFileRemove"
              >
                <el-button>选择文件</el-button>
              </el-upload>
            </el-form-item>
            <el-form-item label="标题" prop="title">
              <el-input v-model="biliPublishForm.title" maxlength="128" show-word-limit placeholder="1-128 字符" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitBiliPublish" :loading="biliPublishLoading">发布</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="sv-card sv-table">
          <template #header>
            <div class="row">
              <div class="left">
                <el-date-picker
                  v-model="biliTimeRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
                <el-button type="primary" @click="loadBiliList">查询</el-button>
                <el-button @click="resetBiliList">重置</el-button>
              </div>
              <div class="right">
                <el-select v-model="biliSortBy" style="width: 160px">
                  <el-option label="按播放量" value="views" />
                  <el-option label="按发布时间" value="publishTime" />
                  <el-option label="按点赞数" value="likes" />
                  <el-option label="按评论数" value="comments" />
                  <el-option label="按分享数" value="shares" />
                </el-select>
                <el-select v-model="biliSortOrder" style="width: 120px">
                  <el-option label="降序" value="desc" />
                  <el-option label="升序" value="asc" />
                </el-select>
              </div>
            </div>
          </template>

          <el-table :data="biliList" style="width: 100%">
            <el-table-column prop="videoId" label="视频ID" width="260" />
            <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="publishTime" label="发布时间" width="190" />
            <el-table-column prop="views" label="播放量" width="120" />
            <el-table-column prop="likes" label="点赞" width="100" />
            <el-table-column prop="comments" label="评论" width="100" />
            <el-table-column prop="shares" label="分享" width="100" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="refreshBiliStats(row.videoId)">刷新数据</el-button>
                <el-button link type="primary" @click="openBiliEdit(row)">改标题</el-button>
                <el-button link type="danger" @click="removeBili(row.videoId)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="biliTotal"
              :page-size="biliPage.pageSize"
              :current-page="biliPage.pageNum"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="onBiliSizeChange"
              @current-change="onBiliCurrentChange"
            />
          </div>
        </el-card>

        <el-dialog v-model="biliEditVisible" title="修改标题" width="520px" :close-on-click-modal="false" class="sv-dialog">
          <el-form :model="biliEditForm" :rules="editRules" ref="biliEditFormRef" label-position="top">
            <el-form-item label="标题" prop="title">
              <el-input v-model="biliEditForm.title" maxlength="128" show-word-limit />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="biliEditVisible = false">取消</el-button>
            <el-button type="primary" :loading="biliEditLoading" @click="submitBiliEdit">保存</el-button>
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
import {
  douyinAuthUrl,
  douyinDeleteVideo,
  douyinPublishVideo,
  douyinUpdateTitle,
  douyinVideoPage,
  douyinVideoStats,
  type DouyinAuthResp,
  type DouyinVideoItem
} from '@/api/douyin'
import {
  kuaishouAuthUrl,
  kuaishouDeleteVideo,
  kuaishouPublishVideo,
  kuaishouUpdateTitle,
  kuaishouVideoPage,
  kuaishouVideoStats,
  type KuaishouAuthResp,
  type KuaishouVideoItem
} from '@/api/kuaishou'
import {
  bilibiliAuthUrl,
  bilibiliDeleteVideo,
  bilibiliPublishVideo,
  bilibiliUpdateTitle,
  bilibiliVideoPage,
  bilibiliVideoStats,
  type BilibiliAuthResp,
  type BilibiliVideoItem
} from '@/api/bilibili'

const activeTab = ref('douyin')

const authLoading = ref(false)
const token = ref<string>(localStorage.getItem('douyin_token') || '')
const authInfo = ref<DouyinAuthResp | null>(null)

const handleAuth = async () => {
  authLoading.value = true
  try {
    const res: any = await douyinAuthUrl()
    authInfo.value = res.data
    token.value = res.data.accessToken
    localStorage.setItem('douyin_token', token.value)
    ElMessage.success('授权成功')
  } finally {
    authLoading.value = false
  }
}

const clearToken = () => {
  token.value = ''
  authInfo.value = null
  localStorage.removeItem('douyin_token')
  ElMessage.success('已清除')
}

const publishVisible = ref(false)
const publishLoading = ref(false)
const publishFormRef = ref<FormInstance>()
const publishForm = reactive({
  file: null as File | null,
  title: ''
})
const publishRules: FormRules = {
  file: [{ required: true, message: '请选择视频文件', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }, { min: 1, max: 128, message: '标题长度需在 1-128 字符', trigger: 'blur' }]
}

const onPublishFileChange = (file: UploadFile) => {
  const raw = (file.raw as File) || null
  if (!raw) {
    publishForm.file = null
    return
  }
  const maxBytes = 100 * 1024 * 1024
  if (raw.size > maxBytes) {
    ElMessage.error('文件大小不能超过 100MB')
    publishForm.file = null
    return
  }
  const name = raw.name || ''
  const lower = name.toLowerCase()
  if (!(lower.endsWith('.mp4') || lower.endsWith('.mov') || lower.endsWith('.avi'))) {
    ElMessage.error('仅支持 mp4/mov/avi 格式')
    publishForm.file = null
    return
  }
  publishForm.file = raw
}

const onPublishFileRemove = () => {
  publishForm.file = null
}

const submitPublish = async () => {
  if (!publishFormRef.value) return
  await publishFormRef.value.validate(async valid => {
    if (!valid) return
    if (!token.value) {
      ElMessage.error('请先获取模拟授权')
      return
    }
    if (!publishForm.file) return
    publishLoading.value = true
    try {
      const form = new FormData()
      form.append('file', publishForm.file)
      form.append('title', publishForm.title)
      try {
        await douyinPublishVideo(form, token.value)
        ElMessage.success('发布成功')
        publishVisible.value = false
        publishForm.file = null
        publishForm.title = ''
        await loadList()
      } catch (e: any) {
        const msg = e?.message || '发布失败'
        if (msg.includes('timeout')) {
          ElMessage.error('发布超时：文件较大或网络较慢，请稍后重试')
        } else if (msg === 'Network Error') {
          ElMessage.error('发布失败：网络错误（请确认后端已启动、数据库 video_info 表已创建、并检查后端日志）')
        } else {
          ElMessage.error(`发布失败：${msg}`)
        }
      }
    } finally {
      publishLoading.value = false
    }
  })
}

const page = reactive({ pageNum: 1, pageSize: 10 })
const sortBy = ref('views')
const sortOrder = ref('desc')
const timeRange = ref<[string, string] | null>(null)
const list = ref<DouyinVideoItem[]>([])
const total = ref(0)

const loadList = async () => {
  const params: any = {
    pageNum: page.pageNum,
    pageSize: page.pageSize,
    sortBy: sortBy.value,
    sortOrder: sortOrder.value
  }
  if (timeRange.value) {
    params.startTime = timeRange.value[0]
    params.endTime = timeRange.value[1]
  }
  const res: any = await douyinVideoPage(params)
  const data = res.data
  list.value = data.records || []
  total.value = data.total || 0
}

const resetList = async () => {
  page.pageNum = 1
  page.pageSize = 10
  sortBy.value = 'views'
  sortOrder.value = 'desc'
  timeRange.value = null
  await loadList()
}

const onSizeChange = async (size: number) => {
  page.pageSize = size
  page.pageNum = 1
  await loadList()
}

const onCurrentChange = async (num: number) => {
  page.pageNum = num
  await loadList()
}

const refreshStats = async (videoId: string) => {
  const res: any = await douyinVideoStats(videoId)
  const s = res.data
  const idx = list.value.findIndex(v => v.videoId === videoId)
  if (idx >= 0) {
    list.value[idx] = { ...list.value[idx], views: s.views, likes: s.likes, comments: s.comments, shares: s.shares }
  }
  ElMessage.success('已刷新')
}

const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive({ videoId: '', title: '' })
const editRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }, { min: 1, max: 128, message: '标题长度需在 1-128 字符', trigger: 'blur' }]
}

const openEdit = (row: DouyinVideoItem) => {
  editForm.videoId = row.videoId
  editForm.title = row.title
  editVisible.value = true
}

const submitEdit = async () => {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async valid => {
    if (!valid) return
    editLoading.value = true
    try {
      await douyinUpdateTitle(editForm.videoId, editForm.title)
      ElMessage.success('更新成功')
      editVisible.value = false
      await loadList()
    } finally {
      editLoading.value = false
    }
  })
}

const remove = async (videoId: string) => {
  await ElMessageBox.confirm('确认删除该抖音视频吗？', '提示', { type: 'warning' })
  await douyinDeleteVideo(videoId)
  ElMessage.success('删除成功')
  await loadList()
}

const ksAuthLoading = ref(false)
const ksToken = ref<string>(localStorage.getItem('kuaishou_token') || '')
const ksAuthInfo = ref<KuaishouAuthResp | null>(null)

const handleKsAuth = async () => {
  ksAuthLoading.value = true
  try {
    const res: any = await kuaishouAuthUrl()
    ksAuthInfo.value = res.data
    if (res.data?.accessToken) {
      ksToken.value = res.data.accessToken
      localStorage.setItem('kuaishou_token', ksToken.value)
      ElMessage.success('授权成功')
    } else {
      ElMessage.info('已生成授权链接：请在浏览器打开并完成授权回调')
    }
  } finally {
    ksAuthLoading.value = false
  }
}

const clearKsToken = () => {
  ksToken.value = ''
  ksAuthInfo.value = null
  localStorage.removeItem('kuaishou_token')
  ElMessage.success('已清除')
}

const ksPublishLoading = ref(false)
const ksPublishFormRef = ref<FormInstance>()
const ksPublishForm = reactive({
  file: null as File | null,
  title: ''
})

const onKsPublishFileChange = (file: UploadFile) => {
  const raw = (file.raw as File) || null
  if (!raw) {
    ksPublishForm.file = null
    return
  }
  const maxBytes = 100 * 1024 * 1024
  if (raw.size > maxBytes) {
    ElMessage.error('文件大小不能超过 100MB')
    ksPublishForm.file = null
    return
  }
  const name = raw.name || ''
  const lower = name.toLowerCase()
  if (!(lower.endsWith('.mp4') || lower.endsWith('.mov') || lower.endsWith('.avi'))) {
    ElMessage.error('仅支持 mp4/mov/avi 格式')
    ksPublishForm.file = null
    return
  }
  ksPublishForm.file = raw
}

const onKsPublishFileRemove = () => {
  ksPublishForm.file = null
}

const submitKsPublish = async () => {
  if (!ksPublishFormRef.value) return
  await ksPublishFormRef.value.validate(async valid => {
    if (!valid) return
    if (!ksToken.value) {
      ElMessage.error('请先获取授权（沙箱/模拟）')
      return
    }
    if (!ksPublishForm.file) return
    ksPublishLoading.value = true
    try {
      const form = new FormData()
      form.append('file', ksPublishForm.file)
      form.append('title', ksPublishForm.title)
      try {
        await kuaishouPublishVideo(form, ksToken.value)
        ElMessage.success('发布成功')
        ksPublishForm.file = null
        ksPublishForm.title = ''
        await loadKsList()
      } catch (e: any) {
        const msg = e?.message || '发布失败'
        if (msg.includes('timeout')) {
          ElMessage.error('发布超时：文件较大或网络较慢，请稍后重试')
        } else if (msg === 'Network Error') {
          ElMessage.error('发布失败：网络错误（请确认后端已启动、数据库 video_info 表已创建、并检查后端日志）')
        } else {
          ElMessage.error(`发布失败：${msg}`)
        }
      }
    } finally {
      ksPublishLoading.value = false
    }
  })
}

const ksPage = reactive({ pageNum: 1, pageSize: 10 })
const ksSortBy = ref('views')
const ksSortOrder = ref('desc')
const ksTimeRange = ref<[string, string] | null>(null)
const ksList = ref<KuaishouVideoItem[]>([])
const ksTotal = ref(0)

const loadKsList = async () => {
  const params: any = {
    pageNum: ksPage.pageNum,
    pageSize: ksPage.pageSize,
    sortBy: ksSortBy.value,
    sortOrder: ksSortOrder.value
  }
  if (ksTimeRange.value) {
    params.startTime = ksTimeRange.value[0]
    params.endTime = ksTimeRange.value[1]
  }
  const res: any = await kuaishouVideoPage(params)
  const data = res.data
  ksList.value = data.records || []
  ksTotal.value = data.total || 0
}

const resetKsList = async () => {
  ksPage.pageNum = 1
  ksPage.pageSize = 10
  ksSortBy.value = 'views'
  ksSortOrder.value = 'desc'
  ksTimeRange.value = null
  await loadKsList()
}

const onKsSizeChange = async (size: number) => {
  ksPage.pageSize = size
  ksPage.pageNum = 1
  await loadKsList()
}

const onKsCurrentChange = async (num: number) => {
  ksPage.pageNum = num
  await loadKsList()
}

const refreshKsStats = async (videoId: string) => {
  const res: any = await kuaishouVideoStats(videoId)
  const s = res.data
  const idx = ksList.value.findIndex(v => v.videoId === videoId)
  if (idx >= 0) {
    ksList.value[idx] = { ...ksList.value[idx], views: s.views, likes: s.likes, comments: s.comments, shares: s.shares }
  }
  ElMessage.success('已刷新')
}

const ksEditVisible = ref(false)
const ksEditLoading = ref(false)
const ksEditFormRef = ref<FormInstance>()
const ksEditForm = reactive({ videoId: '', title: '' })

const openKsEdit = (row: KuaishouVideoItem) => {
  ksEditForm.videoId = row.videoId
  ksEditForm.title = row.title
  ksEditVisible.value = true
}

const submitKsEdit = async () => {
  if (!ksEditFormRef.value) return
  await ksEditFormRef.value.validate(async valid => {
    if (!valid) return
    ksEditLoading.value = true
    try {
      await kuaishouUpdateTitle(ksEditForm.videoId, ksEditForm.title)
      ElMessage.success('更新成功')
      ksEditVisible.value = false
      await loadKsList()
    } finally {
      ksEditLoading.value = false
    }
  })
}

const removeKs = async (videoId: string) => {
  await ElMessageBox.confirm('确认删除该快手视频吗？', '提示', { type: 'warning' })
  await kuaishouDeleteVideo(videoId)
  ElMessage.success('删除成功')
  await loadKsList()
}

const biliAuthLoading = ref(false)
const biliToken = ref<string>(localStorage.getItem('bilibili_token') || '')
const biliAuthInfo = ref<BilibiliAuthResp | null>(null)

const handleBiliAuth = async () => {
  biliAuthLoading.value = true
  try {
    const res: any = await bilibiliAuthUrl()
    biliAuthInfo.value = res.data
    biliToken.value = res.data.accessToken
    localStorage.setItem('bilibili_token', biliToken.value)
    ElMessage.success('授权成功')
  } finally {
    biliAuthLoading.value = false
  }
}

const clearBiliToken = () => {
  biliToken.value = ''
  biliAuthInfo.value = null
  localStorage.removeItem('bilibili_token')
  ElMessage.success('已清除')
}

const biliPublishLoading = ref(false)
const biliPublishFormRef = ref<FormInstance>()
const biliPublishForm = reactive({
  file: null as File | null,
  title: ''
})

const onBiliPublishFileChange = (file: UploadFile) => {
  const raw = (file.raw as File) || null
  if (!raw) {
    biliPublishForm.file = null
    return
  }
  const maxBytes = 100 * 1024 * 1024
  if (raw.size > maxBytes) {
    ElMessage.error('文件大小不能超过 100MB')
    biliPublishForm.file = null
    return
  }
  const name = raw.name || ''
  const lower = name.toLowerCase()
  if (!(lower.endsWith('.mp4') || lower.endsWith('.mov') || lower.endsWith('.avi'))) {
    ElMessage.error('仅支持 mp4/mov/avi 格式')
    biliPublishForm.file = null
    return
  }
  biliPublishForm.file = raw
}

const onBiliPublishFileRemove = () => {
  biliPublishForm.file = null
}

const submitBiliPublish = async () => {
  if (!biliPublishFormRef.value) return
  await biliPublishFormRef.value.validate(async valid => {
    if (!valid) return
    if (!biliToken.value) {
      ElMessage.error('请先获取模拟授权')
      return
    }
    if (!biliPublishForm.file) return
    biliPublishLoading.value = true
    try {
      const form = new FormData()
      form.append('file', biliPublishForm.file)
      form.append('title', biliPublishForm.title)
      try {
        await bilibiliPublishVideo(form, biliToken.value)
        ElMessage.success('发布成功')
        biliPublishForm.file = null
        biliPublishForm.title = ''
        await loadBiliList()
      } catch (e: any) {
        const msg = e?.message || '发布失败'
        if (msg.includes('timeout')) {
          ElMessage.error('发布超时：文件较大或网络较慢，请稍后重试')
        } else if (msg === 'Network Error') {
          ElMessage.error('发布失败：网络错误（请确认后端已启动、数据库 video_info 表已创建、并检查后端日志）')
        } else {
          ElMessage.error(`发布失败：${msg}`)
        }
      }
    } finally {
      biliPublishLoading.value = false
    }
  })
}

const biliPage = reactive({ pageNum: 1, pageSize: 10 })
const biliSortBy = ref('views')
const biliSortOrder = ref('desc')
const biliTimeRange = ref<[string, string] | null>(null)
const biliList = ref<BilibiliVideoItem[]>([])
const biliTotal = ref(0)

const loadBiliList = async () => {
  const params: any = {
    pageNum: biliPage.pageNum,
    pageSize: biliPage.pageSize,
    sortBy: biliSortBy.value,
    sortOrder: biliSortOrder.value
  }
  if (biliTimeRange.value) {
    params.startTime = biliTimeRange.value[0]
    params.endTime = biliTimeRange.value[1]
  }
  const res: any = await bilibiliVideoPage(params)
  const data = res.data
  biliList.value = data.records || []
  biliTotal.value = data.total || 0
}

const resetBiliList = async () => {
  biliPage.pageNum = 1
  biliPage.pageSize = 10
  biliSortBy.value = 'views'
  biliSortOrder.value = 'desc'
  biliTimeRange.value = null
  await loadBiliList()
}

const onBiliSizeChange = async (size: number) => {
  biliPage.pageSize = size
  biliPage.pageNum = 1
  await loadBiliList()
}

const onBiliCurrentChange = async (num: number) => {
  biliPage.pageNum = num
  await loadBiliList()
}

const refreshBiliStats = async (videoId: string) => {
  const res: any = await bilibiliVideoStats(videoId)
  const s = res.data
  const idx = biliList.value.findIndex(v => v.videoId === videoId)
  if (idx >= 0) {
    biliList.value[idx] = { ...biliList.value[idx], views: s.views, likes: s.likes, comments: s.comments, shares: s.shares }
  }
  ElMessage.success('已刷新')
}

const biliEditVisible = ref(false)
const biliEditLoading = ref(false)
const biliEditFormRef = ref<FormInstance>()
const biliEditForm = reactive({ videoId: '', title: '' })

const openBiliEdit = (row: BilibiliVideoItem) => {
  biliEditForm.videoId = row.videoId
  biliEditForm.title = row.title
  biliEditVisible.value = true
}

const submitBiliEdit = async () => {
  if (!biliEditFormRef.value) return
  await biliEditFormRef.value.validate(async valid => {
    if (!valid) return
    biliEditLoading.value = true
    try {
      await bilibiliUpdateTitle(biliEditForm.videoId, biliEditForm.title)
      ElMessage.success('更新成功')
      biliEditVisible.value = false
      await loadBiliList()
    } finally {
      biliEditLoading.value = false
    }
  })
}

const removeBili = async (videoId: string) => {
  await ElMessageBox.confirm('确认删除该B站视频吗？', '提示', { type: 'warning' })
  await bilibiliDeleteVideo(videoId)
  ElMessage.success('删除成功')
  await loadBiliList()
}

onMounted(() => {
  loadList()
  loadKsList()
  loadBiliList()
})
</script>

<style scoped>
.mb-12 {
  margin-bottom: 12px;
}

.mt-12 {
  margin-top: 12px;
}

.row {
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

.right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
