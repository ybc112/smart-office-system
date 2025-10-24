<template>
  <el-container class="layout-container">
    <!-- 移动端遮罩层 -->
    <div 
      v-if="isMobile && !isCollapse" 
      class="mobile-overlay"
      @click="toggleSidebar"
    ></div>
    
    <!-- 侧边栏 -->
    <el-aside 
      :width="isCollapse ? '64px' : '200px'" 
      class="sidebar"
      :class="{ 'mobile-sidebar': isMobile, 'sidebar-hidden': isMobile && isCollapse }"
    >
      <div class="logo" :class="{ 'logo-collapsed': isCollapse }">
        <el-icon><OfficeBuilding /></el-icon>
        <span v-show="!isCollapse">智慧办公楼</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        :collapse="isCollapse"
      >
        <el-menu-item
          v-for="item in menuList"
          :key="item.path"
          :index="item.path"
          @click="handleMenuClick"
        >
          <el-icon><component :is="item.meta.icon" /></el-icon>
          <template #title>
            <span>{{ item.meta.title }}</span>
          </template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="left">
          <!-- 移动端菜单按钮 -->
          <el-button 
            v-if="isMobile"
            type="text" 
            class="mobile-menu-btn"
            @click="toggleSidebar"
          >
            <el-icon :size="20"><Menu /></el-icon>
          </el-button>
          <!-- 桌面端折叠按钮 -->
          <el-button 
            v-else
            type="text" 
            class="collapse-btn"
            @click="toggleSidebar"
          >
            <el-icon :size="20">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </el-button>
          <span class="title">{{ currentPageTitle }}</span>
        </div>
        <div class="right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span class="username" v-show="!isMobile || windowWidth > 480">
                {{ userStore.userInfo?.realName || userStore.userInfo?.username }}
              </span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>
                  角色：{{ userStore.userInfo?.role === 'ADMIN' ? '管理员' : '普通用户' }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式状态
const windowWidth = ref(window.innerWidth)
const isCollapse = ref(false)

// 计算属性
const isMobile = computed(() => windowWidth.value <= 768)

const menuList = computed(() => {
  const routes = router.options.routes.find(r => r.path === '/')?.children || []
  // 根据用户角色过滤菜单
  if (userStore.userInfo?.role !== 'ADMIN') {
    return routes.filter(r => !r.meta?.requiresAdmin)
  }
  return routes
})

const activeMenu = computed(() => route.path)
const currentPageTitle = computed(() => route.meta?.title || '首页')

// 窗口大小监听
const handleResize = () => {
  windowWidth.value = window.innerWidth
  // 移动端默认收起侧边栏
  if (isMobile.value) {
    isCollapse.value = true
  }
}

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 移动端菜单点击处理
const handleMenuClick = () => {
  if (isMobile.value) {
    isCollapse.value = true
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  handleResize() // 初始化
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  position: relative;
}

.sidebar {
  background-color: #304156;
  overflow-x: hidden;
  transition: width 0.3s ease;
  position: relative;
  z-index: 1001;
}

.mobile-sidebar {
  position: fixed !important;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 1002;
  transform: translateX(0);
  transition: transform 0.3s ease;
}

.sidebar-hidden {
  transform: translateX(-100%);
}

.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1001;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  background-color: #2a3c4f;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.3s ease;
}

.logo-collapsed {
  padding: 0;
}

.logo-collapsed span {
  display: none;
}

.el-menu-vertical {
  border-right: none;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}

.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: relative;
  z-index: 1000;
}

.left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.mobile-menu-btn,
.collapse-btn {
  padding: 8px;
  border: none;
  background: none;
  color: #333;
}

.mobile-menu-btn:hover,
.collapse-btn:hover {
  background-color: #f5f7fa;
}

.title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: #333;
  font-size: 14px;
}

.username {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
  margin-left: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .header {
    padding: 0 15px;
  }
  
  .main-content {
    padding: 15px;
  }
  
  .title {
    font-size: 16px;
  }
  
  .user-info {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .header {
    padding: 0 10px;
  }
  
  .main-content {
    padding: 10px;
  }
  
  .title {
    font-size: 14px;
    max-width: 150px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
