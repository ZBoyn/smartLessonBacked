<template>
  <div class="course-layout">
    <!-- 课程顶部头部：展示当前课程信息 -->
    <div class="course-header">
      <div class="header-content">
        <el-button link icon="ArrowLeft" @click="$router.push('/courses')" style="color: #fff; margin-right: 10px">
          返回课程列表
        </el-button>
        <h2 class="course-title">{{ currentCourse?.name || '加载中...' }}</h2>
        <span class="course-subtitle">{{ currentCourse?.description }}</span>
      </div>
      
      <!-- 课程上下文菜单 -->
      <el-tabs v-model="activeTab" class="course-tabs" @tab-click="handleTabClick">
        <el-tab-pane label="概览" name="overview" />
        <el-tab-pane label="班级管理" name="classes" />
        <el-tab-pane label="教学资源" name="resources" />
        <el-tab-pane label="知识图谱" name="knowledge-graph" />
        <el-tab-pane label="智能题库" name="questions" />
        <el-tab-pane label="考试测评" name="assessments" />
      </el-tabs>
    </div>

    <!-- 子路由内容区 -->
    <div class="course-content">
      <router-view v-if="currentCourse" :course-id="courseId" />
      <el-skeleton v-else :rows="5" animated />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { courseApi } from '../api/modules'

const route = useRoute()
const router = useRouter()
const courseId = computed(() => route.params.courseId)
const currentCourse = ref(null)

// 根据当前路由路径通过 regex 或 split 确定 activeTab
const activeTab = ref('overview')

onMounted(async () => {
  if (courseId.value) {
    // 1. 获取课程详情，确保上下文正确
    try {
      // 假设后端有 getCourseById 接口，如果没有需从列表筛选或补充接口
      // 这里使用 mock 或 list 接口模拟
      const res = await courseApi.getCourseById(courseId.value)
      currentCourse.value = res
    } catch (e) {
      console.error('获取课程详情失败', e)
    }
    
    // 2. 设置当前 Tab
    const path = route.path
    if (path.includes('/classes')) activeTab.value = 'classes'
    else if (path.includes('/resources')) activeTab.value = 'resources'
    else if (path.includes('/knowledge-graph')) activeTab.value = 'knowledge-graph'
    else if (path.includes('/questions')) activeTab.value = 'questions'
    else if (path.includes('/assessments')) activeTab.value = 'assessments'
    else activeTab.value = 'overview'
  }
})

const handleTabClick = (tab) => {
  const base = `/teacher/course/${courseId.value}`
  if (tab.paneName === 'overview') router.push(`${base}/overview`)
  else router.push(`${base}/${tab.paneName}`)
}
</script>

<style scoped>
.course-layout {
  min-height: 100%;
}
.course-header {
  background-color: #1e293b; /* 与侧边栏一致或配套 */
  color: #fff;
  padding: 20px 20px 0 20px;
}
.header-content {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
.course-title {
  margin: 0 15px 0 0;
  font-size: 20px;
}
.course-subtitle {
  color: #94a3b8;
  font-size: 13px;
}
/* 覆盖 Element Tabs 样式以适应深色背景 */
:deep(.el-tabs__item) {
  color: #94a3b8;
  font-size: 15px;
}
:deep(.el-tabs__item.is-active) {
  color: #fff;
  font-weight: 600;
}
:deep(.el-tabs__nav-wrap::after) {
  background-color: transparent;
}
:deep(.el-tabs__active-bar) {
  background-color: #3a7bd5;
}
.course-content {
  padding: 20px;
  background-color: #f8fafc;
  min-height: calc(100vh - 150px);
}
</style>

