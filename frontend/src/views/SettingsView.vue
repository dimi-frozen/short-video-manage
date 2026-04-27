<template>
  <div class="sv-page settings-page">
    <div class="grid">
      <el-card shadow="never" class="sv-card profile-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">个人信息</div>
            <div class="card-sub">更新昵称与头像，将同步到顶部用户信息</div>
          </div>
        </template>

        <div class="profile">
          <div class="avatar-wrap">
            <el-avatar :size="68" :src="form.avatarUrl" class="avatar">
              {{ (form.name || 'U').slice(0, 1).toUpperCase() }}
            </el-avatar>
            <div class="meta">
              <div class="name">{{ profile?.name || '-' }}</div>
              <div class="email">{{ profile?.email || '-' }}</div>
            </div>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" label-position="top" class="form">
            <el-form-item label="昵称" prop="name">
              <el-input v-model="form.name" maxlength="32" show-word-limit placeholder="输入你的昵称" />
            </el-form-item>
            <el-form-item label="头像 URL" prop="avatarUrl">
              <el-input v-model="form.avatarUrl" maxlength="512" placeholder="https://..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <el-card shadow="never" class="sv-card security-card">
        <template #header>
          <div class="card-header">
            <div class="card-title">安全设置</div>
            <div class="card-sub">修改登录密码</div>
          </div>
        </template>

        <el-form :model="pwd" :rules="pwdRules" ref="pwdRef" label-position="top">
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="pwd.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwd.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input v-model="pwd.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="pwdSaving" @click="savePassword">修改密码</el-button>
            <el-button @click="logout">退出登录</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { changePassword, getProfile, updateProfile } from '@/api/auth'

const router = useRouter()

const profile = ref<any>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  name: '',
  avatarUrl: ''
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入昵称', trigger: 'blur' }, { min: 1, max: 32, message: '昵称长度需在 1-32 字符', trigger: 'blur' }],
  avatarUrl: [{ max: 512, message: '头像地址过长', trigger: 'blur' }]
}

const pwdSaving = ref(false)
const pwdRef = ref<FormInstance>()
const pwd = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '新密码长度需在 6-20 字符', trigger: 'blur' }],
  confirmPassword: [
    {
      validator: (_rule, value, callback) => {
        if (!value) return callback(new Error('请确认新密码'))
        if (value !== pwd.newPassword) return callback(new Error('两次输入的新密码不一致'))
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const load = async () => {
  const res: any = await getProfile()
  profile.value = res.data
  form.name = res.data?.name || ''
  form.avatarUrl = res.data?.avatarUrl || ''
}

const saveProfile = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async valid => {
    if (!valid) return
    saving.value = true
    try {
      const res: any = await updateProfile({ name: form.name, avatarUrl: form.avatarUrl })
      profile.value = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      window.dispatchEvent(new CustomEvent('user-updated', { detail: res.data }))
      ElMessage.success('保存成功')
    } finally {
      saving.value = false
    }
  })
}

const savePassword = async () => {
  if (!pwdRef.value) return
  await pwdRef.value.validate(async valid => {
    if (!valid) return
    pwdSaving.value = true
    try {
      await changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword })
      pwd.oldPassword = ''
      pwd.newPassword = ''
      pwd.confirmPassword = ''
      ElMessage.success('修改成功')
    } finally {
      pwdSaving.value = false
    }
  })
}

const logout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/')
}

onMounted(() => {
  load()
})
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
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
}

.card-sub {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.profile {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.avatar-wrap {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.55);
}

.meta .name {
  font-size: 16px;
  font-weight: 800;
}

.meta .email {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.6);
  margin-top: 4px;
}

@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
