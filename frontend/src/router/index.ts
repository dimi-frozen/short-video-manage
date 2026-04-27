import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/views/admin/AdminView.vue'),
      meta: { title: '管理员后台' }
    },
    {
      path: '/admin-login',
      name: 'AdminLogin',
      component: () => import('@/views/admin/AdminLogin.vue')
    },
    {
      path: '/comment-detail',
      name: 'CommentDetail',
      component: () => import('@/views/admin/CommentDetail.vue')
    },
    {
      path: '/',
      redirect: '/login',
    },
    
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'),
      children: [
        {
          path: '',
          redirect: '/dashboard/local',
        },
        {
          path: 'local',
          name: 'localDashboard',
          component: () => import('../views/LocalDashboardView.vue'),
        },
        {
          path: 'platform-dashboard',
          name: 'platformDashboard',
          component: () => import('../views/PlatformDashboardView.vue'),
        },
        {
          path: 'home',
          name: 'home',
          component: () => import('../views/HomePageView.vue'),
        },
        {
          path: 'play/:id',
          name: 'videoPlay',
          component: () => import('../views/VideoPlayView.vue'),
        },
        {
          path: 'videos',
          name: 'videos',
          component: () => import('../views/VideosView.vue'),
        },
        {
          path: 'publish',
          name: 'publish',
          component: () => import('../views/PublishView.vue'),
        },
        {
          path: 'publish-sim',
          name: 'publishSim',
          component: () => import('../views/PublishSimView.vue'),
        },
        {
          path: 'publish-detail/:videoId',
          name: 'publishDetail',
          component: () => import('../views/PublishDetailView.vue'),
        },
        {
          path: 'platform-publish/:platform/:videoId',
          name: 'platformPublish',
          component: () => import('../views/PlatformPublishView.vue'),
        },
        {
          path: 'analysis',
          name: 'analysis',
          component: () => import('../views/AnalysisView.vue'),
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('../views/SettingsView.vue'),
        },
        {
          path: 'admin/audit',
          name: 'adminAudit',
          component: () => import('../views/admin/AdminAuditView.vue'),
        },
      ],
    },
  ],
})

export default router
