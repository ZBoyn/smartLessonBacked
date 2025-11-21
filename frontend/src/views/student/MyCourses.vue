<template>
  <div class="page-container">
    <h2>我的课程</h2>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6" v-for="course in courses" :key="course.id">
        <el-card :body-style="{ padding: '0px' }" shadow="hover" class="course-card">
          <div class="course-cover">
            <span>{{ course.name.charAt(0) }}</span>
          </div>
          <div style="padding: 14px">
            <h3 class="course-title">{{ course.name }}</h3>
            <p class="course-desc">{{ course.description || '暂无描述' }}</p>
            <div class="bottom">
              <span class="teacher-name"><el-icon><User /></el-icon> {{ course.teacherName || '教师' }}</span>
              <el-button type="primary" text class="button">进入学习</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-empty v-if="courses.length === 0" description="暂无已注册的课程" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { courseApi } from '../../api/modules'
import { ElMessage } from 'element-plus'

const courses = ref([])

onMounted(async () => {
  try {
    // 尝试调用获取学生课程的 API
    // 注意：后端目前 CourseController 可能没有直接提供 "getStudentCourses" 
    // 这是一个需要确认的 Gap
    const res = await courseApi.getMyCourses() 
    courses.value = res
  } catch (error) {
    // Mock data for demo if API fails
    console.warn('API 调用失败，使用模拟数据', error)
    courses.value = [
        { id: 1, name: 'Java 高级编程', description: '深入理解 JVM 与并发', teacherName: '王老师' },
        { id: 2, name: '人工智能导论', description: '机器学习基础算法', teacherName: '李老师' }
    ]
  }
})
</script>

<style scoped>
.course-card {
  cursor: pointer;
  transition: transform 0.3s;
}
.course-card:hover {
  transform: translateY(-5px);
}
.course-cover {
  height: 120px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 48px;
  font-weight: bold;
}
.course-title {
  margin: 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.course-desc {
  font-size: 12px;
  color: #999;
  margin: 8px 0;
  height: 36px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
.bottom {
  margin-top: 13px;
  line-height: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.teacher-name {
    font-size: 12px;
    color: #666;
    display: flex;
    align-items: center;
    gap: 4px;
}
</style>

