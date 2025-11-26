<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '260px'" class="aside">
      <div class="logo-container" :class="{ 'collapsed': isCollapse }">
        <div class="logo-icon">
          <el-icon><Reading /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-text">Smart Lesson</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        :collapse="isCollapse"
        router
        background-color="#1e293b"
        text-color="#94a3b8"
        active-text-color="#fff"
        :collapse-transition="false"
      >
        <div class="menu-category" v-show="!isCollapse">主要</div>
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>
        
        <div class="menu-category" v-show="!isCollapse">教学管理</div>
        <el-menu-item index="/courses">
          <el-icon><Notebook /></el-icon>
          <template #title>课程中心</template>
        </el-menu-item>
        <el-menu-item index="/analysis">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>全局学情分析</template>
        </el-menu-item>
        <el-menu-item index="/knowledge-graph">
          <el-icon><Connection /></el-icon>
          <template #title>知识图谱</template>
        </el-menu-item>
        <el-menu-item index="/grading">
          <el-icon><EditPen /></el-icon>
          <template #title>智能批改</template>
        </el-menu-item>
        <el-menu-item index="/questions">
          <el-icon><FolderChecked /></el-icon>
          <template #title>题库管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-tooltip content="系统通知" placement="bottom">
            <div class="icon-btn">
              <el-badge is-dot class="item">
                <el-icon><Bell /></el-icon>
              </el-badge>
            </div>
          </el-tooltip>
          
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="user-profile">
              <el-avatar :size="32" class="user-avatar">
                {{ username.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ username }}</span>
              <el-icon><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item divided command="logout" style="color: #f56c6c;">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主要内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const isCollapse = ref(false)
const activeMenu = computed(() => route.path)
const currentRouteName = computed(() => route.meta.title || '当前页面')
const username = ref(localStorage.getItem('username') || 'Teacher')

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('user')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #1e293b;
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 10;
}

.logo-container {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  background-color: #0f172a;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  transition: all 0.3s;
}

.logo-container.collapsed {
  padding: 0;
  justify-content: center;
}

.logo-icon {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  margin-right: 12px;
  flex-shrink: 0;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(to right, #fff, #cbd5e1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.el-menu-vertical {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.menu-category {
  font-size: 12px;
  color: #64748b;
  padding: 16px 20px 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #3a7bd5;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.icon-btn {
  cursor: pointer;
  color: #64748b;
  font-size: 20px;
  transition: color 0.3s;
}

.icon-btn:hover {
  color: #3a7bd5;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 20px;
  transition: background 0.3s;
}

.user-profile:hover {
  background: #f8fafc;
}

.user-avatar {
  background: #3a7bd5;
  font-weight: 600;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #334155;
}

.main-content {
  background-color: #f8fafc;
  padding: 24px;
}

/* 过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

/* Element Plus Menu Override */
:deep(.el-menu-item) {
  margin: 4px 10px;
  border-radius: 6px;
  height: 46px;
}

:deep(.el-menu-item.is-active) {
  background: #2563eb !important;
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

:deep(.el-menu-item:hover) {
  background-color: #334155 !important;
}
</style>
