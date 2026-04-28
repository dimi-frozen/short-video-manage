<template>
  <div class="sv-page publish-page">
    <div class="grid">
      <div class="left">
        <el-card shadow="never" class="sv-card media-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">上传视频</div>
              <div class="card-sub">拖拽或选择文件，发布前可预览</div>
            </div>
          </template>

          <el-upload
            class="upload"
            drag
            :auto-upload="false"
            :limit="1"
            accept=".mp4,.mov,.avi"
            :on-change="onVideoChange"
            :on-remove="onVideoRemove"
          >
            <el-icon class="upload-icon"><UploadFilled /></el-icon>
            <div class="upload-text">
              <div class="primary">将视频拖拽到此处</div>
              <div class="secondary">或点击选择文件（mp4/mov/avi，≤ 100MB）</div>
            </div>
          </el-upload>

          <div v-if="uploading || uploadPercent > 0" class="progress">
            <div class="progress-meta">
              <div class="name">{{ videoFile?.name || '上传中' }}</div>
              <div class="percent">{{ uploadPercent }}%</div>
            </div>
            <el-progress :percentage="uploadPercent" :stroke-width="10" :show-text="false" />
          </div>

          <div v-if="videoUrl" class="preview">
            <video :src="videoUrl" controls playsinline class="player" />
          </div>
        </el-card>

        <el-card shadow="never" class="sv-card cover-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">封面设置</div>
              <div class="card-sub">上传封面图片，优先展示你的内容亮点</div>
            </div>
          </template>

          <div class="cover-grid">
            <div class="cover-preview">
              <div class="cover-frame" :class="{ empty: !coverUrl }">
                <img v-if="coverUrl" :src="coverUrl" alt="cover" />
                <div v-else class="cover-empty">
                  <el-icon><PictureFilled /></el-icon>
                  <div class="tip">未选择封面</div>
                </div>
              </div>
            </div>
            <div class="cover-actions">
              <el-upload
                :auto-upload="false"
                :limit="1"
                accept="image/*"
                :show-file-list="false"
                :on-change="onCoverChange"
                :on-remove="onCoverRemove"
              >
                <el-button type="primary">上传封面</el-button>
              </el-upload>
              <el-button @click="clearCover" :disabled="!coverUrl">移除封面</el-button>
              <div class="cover-hint">建议比例 16:9，JPG/PNG</div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="right">
        <el-card shadow="never" class="sv-card form-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">发布信息</div>
              <div class="card-sub">完善内容信息，提升曝光与互动</div>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="标题" prop="title">
              <el-input
                v-model="form.title"
                maxlength="30"
                show-word-limit
                placeholder="请输入标题（最多 30 字）"
              />
              <div class="field-sub">
                <div class="muted">建议用清晰的描述 + 核心关键词</div>
                <div class="counter">剩余 {{ titleLeft }} 字</div>
              </div>
            </el-form-item>

            <el-form-item label="视频简介" prop="description">
              <div class="description-wrapper">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="10"
                  placeholder="写点有趣的简介吧，支持 #标签"
                  @input="syncHashtags"
                />
                <div class="ai-actions">
                  <div class="muted">在简介中输入 #旅行 #美食，会自动生成标签</div>
                  <el-button 
                    type="primary" 
                    plain 
                    size="small" 
                    :loading="aiOptimizing"
                    @click="handleAIOptimize"
                    class="ai-btn"
                  >
                    <el-icon><MagicStick /></el-icon>
                    AI 优化
                  </el-button>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="标签" prop="tags">
              <div class="tags">
                <el-tag v-for="t in form.tags" :key="t" closable @close="removeTag(t)" class="tag">
                  {{ t }}
                </el-tag>
                <el-input
                  v-model="tagInput"
                  class="tag-input"
                  placeholder="#输入后回车"
                  @keydown.enter.prevent="addTagByInput"
                />
              </div>
            </el-form-item>

            <el-form-item label="发布设置" prop="visibility">
              <div class="visibility">
                <el-radio-group v-model="form.visibility">
                  <el-radio-button label="PUBLIC">公开</el-radio-button>
                  <el-radio-button label="PRIVATE">私密</el-radio-button>
                </el-radio-group>
                <div class="visibility-tip">
                  <div v-if="form.visibility === 'PUBLIC'" class="muted">公开：所有用户可见，可被搜索与推荐</div>
                  <div v-else class="muted">私密：仅自己可见，不进入推荐</div>
                </div>
              </div>
            </el-form-item>

            <div class="actions">
              <el-button class="ghost" @click="resetAll" :disabled="uploading">重置</el-button>
              <el-button type="primary" :loading="uploading" @click="publish">发布</el-button>
            </div>
          </el-form>
        </el-card>

        <el-card v-if="lastPublished" shadow="never" class="sv-card result-card">
          <template #header>
            <div class="card-header">
              <div class="card-title">发布成功</div>
              <div class="card-sub">视频已进入管理列表</div>
            </div>
          </template>
          <div class="result">
            <div class="kpi">
              <div class="k">{{ lastPublished.videoId }}</div>
              <div class="v">视频ID</div>
            </div>
            <div class="kpi">
              <div class="k">{{ lastPublished.uploadTime }}</div>
              <div class="v">发布时间</div>
            </div>
          </div>
          <div class="result-actions">
            <el-button type="primary" plain @click="goVideos">去视频管理</el-button>
            <el-button @click="newPublish">继续发布</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules, UploadFile } from 'element-plus'
import { UploadFilled, PictureFilled, MagicStick } from '@element-plus/icons-vue'
import { uploadVideoWithProgress } from '@/api/video'
import { optimizeVideoDescription } from '@/api/ai'

const router = useRouter()

const videoFile = ref<File | null>(null)
const videoUrl = ref('')
const coverFile = ref<File | null>(null)
const coverUrl = ref('')

const uploading = ref(false)
const uploadPercent = ref(0)

const formRef = ref<FormInstance>()
const tagInput = ref('')
const aiOptimizing = ref(false)  // AI 优化加载状态

const form = reactive({
  title: '',
  description: '',
  tags: [] as string[],
  visibility: 'PUBLIC' as 'PUBLIC' | 'PRIVATE'
})

const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 1, max: 30, message: '标题长度需在 1-30 字符', trigger: 'blur' }
  ],
  description: [{ max: 2000, message: '简介过长', trigger: 'blur' }]
}

const titleLeft = computed(() => Math.max(0, 30 - (form.title?.length || 0)))

const revokeVideoUrl = () => {
  if (videoUrl.value) {
    try {
      URL.revokeObjectURL(videoUrl.value)
    } catch (e) {
    }
  }
}

const revokeCoverUrl = () => {
  if (coverUrl.value) {
    try {
      URL.revokeObjectURL(coverUrl.value)
    } catch (e) {
    }
  }
}

onBeforeUnmount(() => {
  revokeVideoUrl()
  revokeCoverUrl()
})

const normalizeTag = (raw: string) => {
  const t = (raw || '').trim()
  if (!t) return ''
  return t.startsWith('#') ? t.slice(1).trim() : t
}

const addTag = (raw: string) => {
  const t = normalizeTag(raw)
  if (!t) return
  if (t.length > 20) return
  if (!form.tags.includes(t)) {
    form.tags.push(t)
  }
}

const removeTag = (tag: string) => {
  const idx = form.tags.indexOf(tag)
  if (idx >= 0) form.tags.splice(idx, 1)
}

const addTagByInput = () => {
  if (!tagInput.value) return
  addTag(tagInput.value)
  tagInput.value = ''
}

const syncHashtags = () => {
  const text = form.description || ''
  const regex = /#([^\s#]{1,20})/g
  let m: RegExpExecArray | null
  while ((m = regex.exec(text)) !== null) {
    addTag(m[1])
  }
}

/**
 * AI 优化视频简介
 */
const handleAIOptimize = async () => {
  if (!form.description || form.description.trim().length < 5) {
    ElMessage.warning('请先输入至少5个字的简介内容')
    return
  }

  try {
    await ElMessageBox.confirm(
      'AI 将根据您的简介内容生成更吸引人的版本，是否继续？',
      'AI 优化',
      {
        confirmButtonText: '开始优化',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    aiOptimizing.value = true
    const res: any = await optimizeVideoDescription(form.description)
    
    if (res.code === 0 && res.data.optimized) {
      form.description = res.data.optimized
      // 同步更新标签
      syncHashtags()
      ElMessage.success('AI 优化成功！')
    } else {
      ElMessage.error(res.message || 'AI 优化失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || 'AI 优化失败，请稍后重试')
    }
  } finally {
    aiOptimizing.value = false
  }
}

const onVideoChange = (file: UploadFile) => {
  const raw = (file.raw as File) || null
  if (!raw) return
  const maxBytes = 100 * 1024 * 1024
  if (raw.size > maxBytes) {
    ElMessage.error('文件大小不能超过 100MB')
    return
  }
  const name = (raw.name || '').toLowerCase()
  if (!(name.endsWith('.mp4') || name.endsWith('.mov') || name.endsWith('.avi'))) {
    ElMessage.error('仅支持 mp4/mov/avi 格式')
    return
  }

  revokeVideoUrl()
  videoFile.value = raw
  videoUrl.value = URL.createObjectURL(raw)
  uploadPercent.value = 0
}

const onVideoRemove = () => {
  revokeVideoUrl()
  videoFile.value = null
  videoUrl.value = ''
  uploadPercent.value = 0
}

const onCoverChange = (file: UploadFile) => {
  const raw = (file.raw as File) || null
  if (!raw) return
  revokeCoverUrl()
  coverFile.value = raw
  coverUrl.value = URL.createObjectURL(raw)
}

const onCoverRemove = () => {
  revokeCoverUrl()
  coverFile.value = null
  coverUrl.value = ''
}

const clearCover = () => {
  onCoverRemove()
}

type Published = { videoId: number; title: string; uploadTime: string }
const lastPublished = ref<Published | null>(null)

const publish = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async valid => {
    if (!valid) return
    if (!videoFile.value) {
      ElMessage.error('请先选择视频文件')
      return
    }

    uploading.value = true
    uploadPercent.value = 0
    try {
      const tags = [...form.tags]
      if (form.visibility === 'PRIVATE' && !tags.includes('私密')) {
        tags.unshift('私密')
      }

      const fd = new FormData()
      fd.append('file', videoFile.value)
      if (coverFile.value) fd.append('cover', coverFile.value)
      fd.append('title', form.title.trim())
      if (form.description) fd.append('description', form.description)
      tags.forEach(t => fd.append('tags', t))

      const res: any = await uploadVideoWithProgress(fd, p => {
        uploadPercent.value = p
      })

      lastPublished.value = {
        videoId: res.data.videoId,
        title: res.data.title,
        uploadTime: res.data.uploadTime
      }

      ElMessage.success('发布成功')
    } finally {
      uploading.value = false
    }
  })
}

const resetAll = () => {
  onVideoRemove()
  clearCover()
  form.title = ''
  form.description = ''
  form.tags.splice(0, form.tags.length)
  form.visibility = 'PUBLIC'
  tagInput.value = ''
  lastPublished.value = null
}

const newPublish = () => {
  resetAll()
}

const goVideos = () => {
  router.push('/dashboard/videos')
}
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: minmax(520px, 1.2fr) minmax(420px, 0.8fr);
  gap: 14px;
  align-items: start;
}

.left,
.right {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.card-header {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.card-title {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.card-sub {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.upload {
  width: 100%;
}

:deep(.upload .el-upload-dragger) {
  border-radius: 16px;
  border: 1px dashed rgba(15, 23, 42, 0.18);
  background: rgba(255, 255, 255, 0.55);
  padding: 22px 16px;
  transition: 0.2s ease;
}

:deep(.upload .el-upload-dragger:hover) {
  border-color: rgba(37, 99, 235, 0.5);
  background: rgba(255, 255, 255, 0.75);
}

.upload-icon {
  font-size: 34px;
  color: rgba(37, 99, 235, 0.85);
}

.upload-text .primary {
  font-size: 14px;
  font-weight: 700;
  margin-top: 6px;
}

.upload-text .secondary {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.58);
  margin-top: 4px;
}

.progress {
  margin-top: 14px;
  padding: 12px 12px 10px;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.62);
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.progress-meta .name {
  font-size: 12px;
  font-weight: 600;
  color: rgba(15, 23, 42, 0.74);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-meta .percent {
  font-size: 12px;
  font-weight: 700;
  color: rgba(37, 99, 235, 0.92);
}

.preview {
  margin-top: 14px;
}

.player {
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: 16px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.10);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.12);
}

.cover-grid {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 14px;
  align-items: start;
}

.cover-frame {
  width: 100%;
  aspect-ratio: 16 / 9;
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(255, 255, 255, 0.6);
}

.cover-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-frame.empty {
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-empty {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: center;
  color: rgba(15, 23, 42, 0.55);
}

.cover-empty .tip {
  font-size: 12px;
}

.cover-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cover-hint {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.field-sub {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.description-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.ai-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.ai-btn {
  flex-shrink: 0;
}

.muted {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.counter {
  font-size: 12px;
  font-weight: 700;
  color: rgba(37, 99, 235, 0.92);
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.tag {
  border-radius: 999px;
  padding: 0 10px;
}

.tag-input {
  width: 160px;
  min-width: 120px;
}

:deep(.tag-input .el-input__wrapper) {
  border-radius: 999px;
}

.visibility {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.visibility-tip {
  display: flex;
  justify-content: flex-start;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 6px;
}

.ghost {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(15, 23, 42, 0.12);
}

.result {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.kpi {
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.10);
  background: rgba(255, 255, 255, 0.62);
  padding: 14px 14px 12px;
}

.kpi .k {
  font-size: 16px;
  font-weight: 800;
  color: rgba(15, 23, 42, 0.92);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kpi .v {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.result-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }
  .cover-grid {
    grid-template-columns: 1fr;
  }
}
</style>
