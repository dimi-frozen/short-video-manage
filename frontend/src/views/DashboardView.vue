<template>
  <el-container class="layout-container">
    <el-aside width="240px" class="sv-aside">
      <div class="brand">
        <div class="brand-mark">SV</div>
        <div class="brand-text">
          <div class="brand-title">短视频管理</div>
          <div class="brand-sub">Multi-Platform Console</div>
        </div>
      </div>
      <el-menu :default-active="activeMenu" class="sv-menu" router>
        <el-sub-menu index="/dashboard">
          <template #title>
            <el-icon><DataLine /></el-icon>
            <span>控制台</span>
          </template>
          <el-menu-item index="/dashboard/local">
            <span>本平台</span>
          </el-menu-item>
          <el-menu-item index="/dashboard/platform-dashboard">
            <span>其他平台</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/dashboard/home">
          <el-icon><House /></el-icon>
          <span>视频首页</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/videos">
          <el-icon><VideoCamera /></el-icon>
          <span>视频管理</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/publish">
          <el-icon><UploadFilled /></el-icon>
          <span>视频发布</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/analysis">
          <el-icon><PieChart /></el-icon>
          <span>数据分析</span>
        </el-menu-item>
        <el-menu-item index="/dashboard/settings">
          <el-icon><Setting /></el-icon>
          <span>个人设置</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <el-container class="sv-main">
      <el-header class="sv-header">
        <div class="header-content">
          <div class="title-area">
            <div class="title">{{ pageTitle }}</div>
            <div class="subtitle">实时掌控跨平台短视频数据与内容</div>
          </div>
          <div class="user-area">
            <el-dropdown @command="handleCommand">
              <div class="user-pill">
                <div class="avatar">{{ userName.slice(0, 1).toUpperCase() }}</div>
                <div class="name">{{ userName }}</div>
                <el-icon class="caret"><arrow-down /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>

      <el-main class="sv-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  DataLine, 
  VideoCamera, 
  UploadFilled,
  PieChart, 
  Platform, 
  Setting,
  ArrowDown,
  House
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)
const userName = ref('管理员')

const pageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/dashboard': '控制台',
    '/dashboard/local': '本平台',
    '/dashboard/platform-dashboard': '其他平台',
    '/dashboard/home': '视频首页',
    '/dashboard/videos': '视频管理',
    '/dashboard/publish': '视频发布',
    '/dashboard/analysis': '数据分析',
    '/dashboard/settings': '个人设置',
  }
  return titles[route.path] || '系统'
})

const syncUser = () => {
  const userStr = localStorage.getItem('user')
  if (userStr) {
    const user = JSON.parse(userStr)
    userName.value = user.name || '用户'
  }
}

onMounted(() => {
  syncUser()
  window.addEventListener('user-updated', syncUser as any)
})

// 监听路由变化，当从视频播放页返回时触发数据刷新
watch(
  () => route.path,
  (newPath, oldPath) => {
    // 如果之前是在视频播放页，现在返回到本平台控制台，触发刷新
    if (oldPath?.includes('/dashboard/play/') && newPath === '/dashboard/local') {
      console.log('从视频播放页返回，触发数据刷新')
      window.dispatchEvent(new CustomEvent('video-data-updated'))
    }
  }
)

onBeforeUnmount(() => {
  window.removeEventListener('user-updated', syncUser as any)
})

const handleCommand = (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/')
  } else if (command === 'profile') {
    router.push('/dashboard/settings')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sv-aside {
  padding: 14px 12px;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.92), rgba(15, 23, 42, 0.86));
  color: rgba(255, 255, 255, 0.92);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 10px 12px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.10);
  margin-bottom: 10px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  letter-spacing: 0.5px;
  background: radial-gradient(60% 60% at 30% 20%, rgba(37, 99, 235, 0.9), rgba(59, 130, 246, 0.45));
  border: 1px solid rgba(255, 255, 255, 0.16);
}

.brand-title {
  font-size: 14px;
  font-weight: 700;
  line-height: 1.1;
}

.brand-sub {
  font-size: 12px;
  opacity: 0.75;
  margin-top: 2px;
}

.sv-menu {
  border-right: none;
  background: transparent;
}

:deep(.sv-menu .el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.94);
  height: 44px;
  margin: 6px 4px;
  border-radius: 12px;
}

:deep(.sv-menu .el-sub-menu__title:hover) {
  background: rgba(255, 255, 255, 0.08);
}

:deep(.sv-menu .el-menu-item) {
  height: 44px;
  margin: 6px 4px;
  border-radius: 12px;
  color: rgba(255, 255, 255, 0.78);
  background: transparent;
}

:deep(.sv-menu .el-menu-item.is-active) {
  color: rgba(255, 255, 255, 0.94);
  background: rgba(37, 99, 235, 0.22);
  border: 1px solid rgba(37, 99, 235, 0.28);
}

:deep(.sv-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
}

:deep(.sv-menu .el-sub-menu .el-menu-item) {
  background: transparent;
}

:deep(.sv-menu .el-sub-menu .el-menu-item.is-active) {
  background: rgba(37, 99, 235, 0.22);
}

:deep(.sv-menu .el-sub-menu .el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08);
}

:deep(.sv-menu .el-sub-menu .el-menu) {
  background: rgba(37, 99, 235, 0.08);
  border-radius: 12px;
  padding: 4px;
}

.sv-main {
  background: transparent;
}

.sv-header {
  height: 74px;
  background: rgba(255, 255, 255, 0.65);
  border-bottom: 1px solid rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(10px);
}

.header-content {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 18px;
}

.title-area {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.subtitle {
  font-size: 12px;
  color: rgba(15, 23, 42, 0.55);
}

.user-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid rgba(15, 23, 42, 0.10);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
  cursor: pointer;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.96);
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.9), rgba(16, 185, 129, 0.65));
}

.name {
  font-size: 13px;
  font-weight: 600;
}

.caret {
  color: rgba(15, 23, 42, 0.55);
}

.sv-content {
  padding: 0;
}

.sv-content :deep(.sv-page) {
  padding: 18px;
}
</style>
