import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'House' }
      },
      {
        path: 'devices',
        name: 'Devices',
        component: () => import('@/views/Devices.vue'),
        meta: { title: '设备监控', icon: 'Monitor' }
      },
      {
        path: 'alarms',
        name: 'Alarms',
        component: () => import('@/views/Alarms.vue'),
        meta: { title: '告警管理', icon: 'Bell' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/Config.vue'),
        meta: { title: '系统配置', icon: 'Setting', requiresAdmin: true }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/Users.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAdmin: true }
      },
      {
        path: 'device-management',
        name: 'DeviceManagement',
        component: () => import('@/views/DeviceManagement.vue'),
        meta: { title: '设备管理', icon: 'Tools' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth !== false) {
    if (!userStore.token) {
      next('/login')
    } else {
      // 检查是否需要管理员权限
      if (to.meta.requiresAdmin && userStore.userInfo?.role !== 'ADMIN') {
        next('/')
      } else {
        next()
      }
    }
  } else {
    next()
  }
})

export default router
