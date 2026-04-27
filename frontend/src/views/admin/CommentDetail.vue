<template>
  <div style="padding: 20px">
    <el-button @click="goBack" style="margin-bottom: 20px">← 返回视频列表</el-button>
    <h2>视频 {{ videoId }} 的评论管理</h2>
    <el-table :data="commentList" border style="width: 100%; margin-top: 20px">
      <el-table-column prop="id" label="评论ID" />
      <el-table-column prop="content" label="评论内容" />
      <el-table-column prop="createTime" label="评论时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="scope">
          <el-button type="danger" size="small" @click="deleteComment(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

// 从路由参数获取视频ID
const videoId = ref(route.query.videoId)
const commentList = ref([])

// 页面加载时获取评论列表
onMounted(() => {
  if (!videoId.value) {
    ElMessage.error('视频ID不存在')
    router.push('/admin')
    return
  }
  loadComments()
})

// 加载该视频的所有评论
const loadComments = async () => {
  try {
    const res = await request.get(`/admin/comment/byVideo/${videoId.value}`)
    commentList.value = res.data || []
  } catch (err) {
    console.error('加载评论失败:', err)
    commentList.value = []
    ElMessage.error('加载评论失败')
  }
}

// 删除评论
const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定删除这条评论？')
    await request.delete(`/admin/comment/${commentId}`)
    ElMessage.success('删除成功')
    loadComments() // 刷新列表
  } catch (err) {
    ElMessage.error('删除失败')
  }
}

// 返回按钮
const goBack = () => {
  router.push('/admin?tab=comment')
}
</script>