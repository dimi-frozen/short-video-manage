<template>
  <div class="login-shell">
    <div class="login-bg"></div>
    <div class="login-panel">
      <div class="login-brand">
        <div class="brand-mark">SV</div>
        <div class="brand-text">
          <div class="brand-title">短视频管理</div>
          <div class="brand-sub">Multi-Platform Console</div>
        </div>
      </div>
      <div class="login-card sv-card sv-dialog">
        <div class="login-header">
          <div class="headline">欢迎回来</div>
          <div class="hint">登录或注册以开始管理多平台视频内容</div>
        </div>
        <el-tabs v-model="activeName" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-position="top">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="loginForm.email" placeholder="请输入邮箱" prefix-icon="User"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="full-width" @click="handleLogin" :loading="loading" round>登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-position="top">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" prefix-icon="User"></el-input>
            </el-form-item>
            <el-form-item label="昵称" prop="name">
              <el-input v-model="registerForm.name" placeholder="请输入昵称" prefix-icon="Edit"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="full-width" @click="handleRegister" :loading="loading" round>注册</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      </div>
      <div class="login-footer">
        <span>© {{ new Date().getFullYear() }} Short Video Manage</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login, register } from '@/api/auth'
import { User, Lock, Edit } from '@element-plus/icons-vue'

const router = useRouter()
const activeName = ref('login')
const loading = ref(false)

// 登录表单
const loginFormRef = ref<FormInstance>()
const loginForm = reactive({
  email: '',
  password: ''
})
const loginRules = reactive<FormRules>({
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度至少为6位', trigger: 'blur' }]
})

// 注册表单
const registerFormRef = ref<FormInstance>()
const registerForm = reactive({
  email: '',
  name: '',
  password: ''
})
const registerRules = reactive<FormRules>({
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }],
  name: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度至少为6位', trigger: 'blur' }]
})

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res: any = await login(loginForm)
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data.user))
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register(registerForm)
        ElMessage.success('注册成功，请登录')
        activeName.value = 'login'
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-shell {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: -20%;
  background: radial-gradient(800px 500px at 20% 20%, rgba(37, 99, 235, 0.20), transparent 60%),
    radial-gradient(700px 520px at 85% 25%, rgba(16, 185, 129, 0.16), transparent 55%),
    radial-gradient(900px 600px at 50% 105%, rgba(245, 158, 11, 0.12), transparent 55%);
  filter: blur(10px);
}

.login-panel {
  width: min(980px, calc(100vw - 32px));
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 18px;
  align-items: stretch;
  position: relative;
  z-index: 1;
}

.login-brand {
  padding: 28px 24px;
  border-radius: 18px;
  background: rgba(15, 23, 42, 0.88);
  border: 1px solid rgba(255, 255, 255, 0.10);
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.22);
  color: rgba(255, 255, 255, 0.92);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.brand-mark {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  letter-spacing: 0.8px;
  background: radial-gradient(60% 60% at 30% 20%, rgba(37, 99, 235, 0.95), rgba(59, 130, 246, 0.40));
  border: 1px solid rgba(255, 255, 255, 0.16);
  margin-bottom: 14px;
}

.brand-title {
  font-size: 20px;
  font-weight: 800;
  margin-top: 6px;
}

.brand-sub {
  font-size: 13px;
  opacity: 0.7;
  margin-top: 6px;
}

.login-header {
  margin-bottom: 14px;
}

.headline {
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 0.2px;
}

.hint {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.62);
  margin-top: 6px;
}

.full-width {
  width: 100%;
}

.login-card {
  padding: 18px;
  border-radius: 18px;
}

.login-tabs :deep(.el-tabs__header) {
  margin: 0 0 12px;
}

.login-footer {
  grid-column: 1 / -1;
  text-align: center;
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
  margin-top: 8px;
}

@media (max-width: 900px) {
  .login-panel {
    grid-template-columns: 1fr;
  }
  .login-brand {
    display: none;
  }
}
</style>
