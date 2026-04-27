<template>
  <div style="padding: 20px">
    <h2>管理员后台</h2>
    <el-tabs v-model="activeTab" type="card">
      <!-- 待审核视频 -->
      <el-tab-pane label="待审核视频" name="audit">
        <el-table :data="auditList" border style="width: 100%">
          <el-table-column prop="id" label="ID" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="auditStatus" label="审核状态" />
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button type="success" size="small" @click="approveVideo(scope.row.id)">通过</el-button>
              <el-button type="danger" size="small" @click="rejectVideo(scope.row.id)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 所有视频 -->
      <el-tab-pane label="所有视频" name="video">
        <el-table :data="videoList" border style="width: 100%">
          <el-table-column prop="id" label="ID" />
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="auditStatus" label="审核状态" />
          <el-table-column prop="createTime" label="时间" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="danger" size="small" @click="deleteVideo(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 评论管理 -->
      <el-tab-pane label="评论管理" name="comment">
        <el-table :data="videoListForComment" border style="width: 100%">
          <el-table-column prop="id" label="视频ID" />
          <el-table-column prop="title" label="视频标题" />
          <el-table-column prop="auditStatus" label="审核状态" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button type="primary" size="small" @click="goToCommentDetail(scope.row.id)">
                查看评论
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="user">
        <el-table :data="userList" border style="width: 100%">
          <el-table-column prop="id" label="ID" />
          <el-table-column prop="name" label="昵称" />
          <el-table-column prop="email" label="邮箱" />
          <el-table-column prop="role" label="角色" />
          <el-table-column prop="createdAt" label="创建时间" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button type="danger" size="small" @click="deleteUser(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 登录校验
const token = localStorage.getItem('token')
if (!token) {
  ElMessage.error('请先登录管理员账号')
  router.push('/admin-login')
}

// 初始化数据
const activeTab = ref('audit')
const auditList = ref([])
const videoList = ref([])
const commentList = ref([])
const userList = ref([])

// 页面加载时获取数据
onMounted(() => {
  loadAuditVideos()
  loadAllVideos()
  loadComments()
  loadUsers()
})

// ====================== 视频审核 ======================
const loadAuditVideos = async () => {
  try {
    const res = await request.get('/admin/video/pending')
    auditList.value = res.data || []
  } catch (err) {
    console.error('加载待审核视频失败:', err)
    auditList.value = []
  }
}

const approveVideo = async (id) => {
  try {
    await request.post('/admin/video/approve', null, { params: { id } })
    ElMessage.success('审核通过')
    loadAuditVideos()
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const rejectVideo = async (id) => {
  try {
    await request.post('/admin/video/reject', null, { params: { id } })
    ElMessage.success('已拒绝')
    loadAuditVideos()
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

// ====================== 所有视频 ======================
const loadAllVideos = async () => {
  try {
    const res = await request.get('/admin/video/all')
    videoList.value = res.data || []
  } catch (err) {
    console.error('加载所有视频失败:', err)
    videoList.value = []
  }
}

const deleteVideo = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该视频？')
    await request.delete(`/admin/video/${id}`)
    ElMessage.success('删除成功')
    loadAllVideos()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

// ====================== 评论 ======================
const loadComments = async () => {
  try {
    const res = await request.get('/admin/comment/all')
    commentList.value = res.data || []
  } catch (err) {
    console.error('加载评论失败:', err)
    commentList.value = []
  }
}
// 1. 新增变量
const videoListForComment = ref([])

// 2. 在 onMounted 里加加载方法（和其他load方法放一起）
onMounted(() => {
  if (route.query.tab === 'comment') {
    activeTab.value = 'comment'
  }
  loadAuditVideos()
  loadAllVideos()
  loadComments()
  loadUsers()
  loadVideoListForComment() // 新增这一行
})

// 3. 新增加载视频列表的方法（和其他load方法放一起）
const loadVideoListForComment = async () => {
  try {
    const res = await request.get('/admin/video/all')
    videoListForComment.value = res.data || []
  } catch (err) {
    console.error('加载视频列表失败:', err)
    videoListForComment.value = []
  }
}

// 4. 新增跳转到评论详情页的方法
const goToCommentDetail = (videoId) => {
  // 用路由参数传递视频ID
  router.push({
    path: '/comment-detail',
    query: { videoId: videoId }
  })
}

const deleteComment = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该评论？')
    await request.delete(`/admin/comment/${id}`)
    ElMessage.success('删除成功')
    loadComments()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

// ====================== 用户 ======================
const loadUsers = async () => {
  try {
    const res = await request.get('/admin/user/all')
    userList.value = res.data || []
  } catch (err) {
    console.error('加载用户失败:', err)
    userList.value = []
  }
}

const deleteUser = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该用户？')
    await request.delete(`/admin/user/${id}`)
    ElMessage.success('删除成功')
    loadUsers()
  } catch (err) {
    ElMessage.error('删除失败')
  }
}
</script>