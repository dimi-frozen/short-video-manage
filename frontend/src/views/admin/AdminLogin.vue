<template>
  <div style="width: 400px; margin: 150px auto;">
    <el-card header="管理员登录">
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input v-model="loginForm.email" placeholder="请输入管理员邮箱"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码：123456"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="login" block>登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loginForm = ref({
  email: '',
  password: ''
})

// 登录请求（完全适配你的后端接口）
const login = async () => {
  try {
    // 调用后端登录接口
    const res = await request.post('/admin/login', loginForm.value)

    // 保存后端返回的Token（关键！）
    localStorage.setItem('token', res.data.token)
    ElMessage.success('管理员登录成功！')

    // 跳转到管理后台
    router.push('/admin')
  } catch (err) {
    console.error(err)
  }
}
</script>