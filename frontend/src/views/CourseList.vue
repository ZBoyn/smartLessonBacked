<template>
  <div class="page-container">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" icon="Plus" @click="handleCreate">创建新课程</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="6" v-for="course in tableData" :key="course.id">
        <el-card class="course-card" :body-style="{ padding: '0px' }" shadow="hover" @click="enterCourse(course.id)">
           <div class="course-cover">
             {{ course.name ? course.name.charAt(0) : 'C' }}
           </div>
           <div style="padding: 14px">
             <h3 class="course-title">{{ course.name }}</h3>
             <p class="course-desc">{{ course.description || '暂无描述' }}</p>
             <div class="course-footer">
                <el-tag size="small">{{ course.status }}</el-tag>
                <el-button link type="primary">进入管理</el-button>
             </div>
           </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { courseApi } from '../api/modules'
import { ElMessage } from 'element-plus'

const router = useRouter()
const tableData = ref([])
const loading = ref(false)

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await courseApi.getAllCourses()
    tableData.value = res
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const enterCourse = (id) => {
    // 跳转到重构后的课程详情页
    router.push(`/teacher/course/${id}/overview`)
}

const handleCreate = () => {
    ElMessage.info('创建课程功能暂未实现弹窗')
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.course-card {
    cursor: pointer;
    margin-bottom: 20px;
    transition: transform 0.2s;
}
.course-card:hover {
    transform: translateY(-5px);
}
.course-cover {
    height: 100px;
    background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 36px;
    color: #fff;
    font-weight: bold;
}
.course-title {
    margin: 0 0 10px;
    font-size: 16px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
.course-desc {
    font-size: 12px;
    color: #909399;
    height: 36px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    margin-bottom: 10px;
}
.course-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>

