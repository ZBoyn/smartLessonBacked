<template>
  <el-container class="layout-container">
    <el-aside width="200px" class="student-aside">
      <div class="logo">
        <el-icon><Reading /></el-icon>
        <span>Smart Study</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        router
        background-color="#ffffff"
        text-color="#606266"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/student/dashboard">
          <el-icon><House /></el-icon>
          <span>学习中心</span>
        </el-menu-item>
        <el-menu-item index="/student/my-courses">
          <el-icon><Notebook /></el-icon>
          <span>我的课程</span>
        </el-menu-item>
        <el-menu-item index="/student/assessments">
          <el-icon><Edit /></el-icon>
          <span>在线考试</span>
        </el-menu-item>
        <el-menu-item index="/student/analysis">
          <el-icon><TrendCharts /></el-icon>
          <span>成长档案</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="student-header">
        <div class="header-left">
          <h2>你好, {{ username }} 同学 👋</h2>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="avatar-wrapper">
              <el-avatar :size="36" style="background-color: #409EFF">{{ username.charAt(0) }}</el-avatar>
              <span class="user-name">{{ username }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="student-main">
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const username = computed(() => userStore.username)

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  background-color: #f5f7fa;
}

.student-aside {
  background-color: #fff;
  box-shadow: 2px 0 8px rgba(0,0,0,0.05);
  z-index: 10;
  display: flex;
  flex-direction: column;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
  border-bottom: 1px solid #f0f0f0;
}

.el-menu-vertical {
  border-right: none;
  padding-top: 10px;
}

.student-header {
  background-color: #fff;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}

.header-left h2 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-name {
  font-size: 14px;
  color: #606266;
}

.student-main {
  padding: 24px;
}
</style>

