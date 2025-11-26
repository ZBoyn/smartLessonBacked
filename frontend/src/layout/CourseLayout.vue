<template>
  <div class="layout-wrapper">
    <!-- Sidebar -->
    <div class="course-sidebar">
        <div class="sidebar-header" @click="$router.push('/courses')">
            <el-icon><Back /></el-icon>
            <span>返回列表</span>
        </div>
        
        <div class="course-profile" v-if="currentCourse">
            <div class="profile-icon">{{ currentCourse.courseName.charAt(0).toUpperCase() }}</div>
            <div class="profile-info">
                <h3 class="profile-title">{{ currentCourse.courseName }}</h3>
                <p class="profile-id">ID: {{ currentCourse.courseId }}</p>
            </div>
        </div>
        <div class="course-profile loading" v-else>
            <el-skeleton-item variant="circle" style="width: 40px; height: 40px" />
            <el-skeleton-item variant="text" style="width: 70%" />
        </div>

        <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            @select="handleSelect"
            background-color="#1e293b"
            text-color="#94a3b8"
            active-text-color="#fff"
        >
            <el-menu-item index="overview">
                <el-icon><Odometer /></el-icon>
                <span>课程概览</span>
            </el-menu-item>
            <el-menu-item index="classes">
                <el-icon><School /></el-icon>
                <span>班级管理</span>
            </el-menu-item>
            <el-menu-item index="resources">
                <el-icon><FolderOpened /></el-icon>
                <span>教学资源</span>
            </el-menu-item>
            <el-menu-item index="knowledge-graph">
                <el-icon><Connection /></el-icon>
                <span>知识图谱</span>
            </el-menu-item>
            <el-menu-item index="questions">
                <el-icon><Collection /></el-icon>
                <span>智能题库</span>
            </el-menu-item>
            <el-menu-item index="assessments">
                <el-icon><Reading /></el-icon>
                <span>考试测评</span>
            </el-menu-item>
        </el-menu>
    </div>

    <!-- Main Content -->
    <div class="main-container">
        <div class="content-header">
            <div class="breadcrumb">
                <span>课程管理</span>
                <span class="separator">/</span>
                <span class="current">{{ getPageTitle(activeMenu) }}</span>
            </div>
            <div class="user-actions">
                <el-avatar :size="32" icon="UserFilled" />
            </div>
        </div>

        <div class="content-body">
            <router-view v-if="currentCourse" :course-id="courseId" />
            <div v-else class="loading-view">
                <el-skeleton :rows="10" animated />
            </div>
        </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { courseApi } from '../api/modules'

const route = useRoute()
const router = useRouter()
const courseId = computed(() => route.params.courseId)
const currentCourse = ref(null)

const activeMenu = computed(() => {
    const path = route.path
    const parts = path.split('/')
    return parts[parts.length - 1] || 'overview'
})

const getPageTitle = (menu) => {
    const map = {
        'overview': '课程概览',
        'classes': '班级管理',
        'resources': '教学资源',
        'knowledge-graph': '知识图谱',
        'questions': '智能题库',
        'assessments': '考试测评'
    }
    return map[menu] || '未知页面'
}

const fetchCourseDetails = async () => {
    if (!courseId.value || courseId.value === 'undefined') return
    try {
        const res = await courseApi.getCourseById(courseId.value)
        currentCourse.value = res
    } catch (e) {
        console.error('Failed to load course details', e)
    }
}

const handleSelect = (index) => {
    router.push(`/teacher/course/${courseId.value}/${index}`)
}

onMounted(() => {
    fetchCourseDetails()
})

watch(courseId, () => {
    fetchCourseDetails()
})
</script>

<style scoped>
.layout-wrapper {
    display: flex;
    height: 100vh;
    background-color: #f8fafc;
}

.course-sidebar {
    width: 240px;
    background-color: #1e293b;
    color: #fff;
    display: flex;
    flex-direction: column;
    flex-shrink: 0;
}

.sidebar-header {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    cursor: pointer;
    color: #94a3b8;
    font-size: 14px;
    transition: color 0.2s;
    border-bottom: 1px solid #334155;
}

.sidebar-header:hover {
    color: #fff;
}

.sidebar-header .el-icon {
    margin-right: 8px;
    font-size: 16px;
}

.course-profile {
    padding: 24px 20px;
    display: flex;
    align-items: center;
    gap: 12px;
}

.profile-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 18px;
    color: #fff;
}

.profile-info {
    flex: 1;
    overflow: hidden;
}

.profile-title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    color: #f1f5f9;
}

.profile-id {
    margin: 2px 0 0;
    font-size: 12px;
    color: #64748b;
}

.sidebar-menu {
    border-right: none;
    flex: 1;
}

:deep(.el-menu-item) {
    height: 50px;
    line-height: 50px;
    margin: 4px 12px;
    border-radius: 6px;
}

:deep(.el-menu-item.is-active) {
    background-color: #3b82f6 !important;
}

:deep(.el-menu-item:hover) {
    background-color: #334155 !important;
}

.main-container {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.content-header {
    height: 60px;
    background: #fff;
    border-bottom: 1px solid #e2e8f0;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
}

.breadcrumb {
    font-size: 14px;
    color: #64748b;
    display: flex;
    align-items: center;
    gap: 8px;
}

.breadcrumb .current {
    color: #0f172a;
    font-weight: 500;
}

.content-body {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
}

.loading-view {
    padding: 20px;
    background: #fff;
    border-radius: 8px;
}
</style>
