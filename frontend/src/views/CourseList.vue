<template>
  <div class="page-container">
    <div class="page-header">
      <div class="header-left">
        <h2>课程管理</h2>
        <p class="subtitle">管理您的所有教学课程</p>
      </div>
      <el-button type="primary" size="large" icon="Plus" @click="dialogVisible = true">
        创建新课程
      </el-button>
    </div>

    <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="tableData.length === 0" class="empty-state">
        <el-empty description="暂无课程，快去创建吧！" />
    </div>

    <el-row :gutter="24" v-else>
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="course in tableData" :key="course.courseId">
        <div class="course-card" @click="enterCourse(course.courseId)">
           <div class="course-cover" :class="getGradientClass(course.courseId)">
             <span class="cover-text">{{ course.courseName ? course.courseName.charAt(0).toUpperCase() : 'C' }}</span>
           </div>
           <div class="course-content">
             <div class="course-main">
                 <h3 class="course-title" :title="course.courseName">{{ course.courseName }}</h3>
                 <p class="course-desc">{{ course.description || '暂无描述' }}</p>
             </div>
             <div class="course-footer">
                <el-tag size="small" effect="light" round>进行中</el-tag>
                <span class="enter-text">进入管理 <el-icon><ArrowRight /></el-icon></span>
             </div>
           </div>
        </div>
      </el-col>
    </el-row>

    <!-- Create Course Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="创建新课程"
      width="500px"
      destroy-on-close
      class="custom-dialog"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" placeholder="请输入课程名称" size="large" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入课程简介..." 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            立即创建
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { courseApi } from '../api/modules'
import { ElMessage } from 'element-plus'

const router = useRouter()
const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
    courseName: '',
    description: ''
})

const rules = {
    courseName: [
        { required: true, message: '请输入课程名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ]
}

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await courseApi.getAllCourses()
    tableData.value = res
  } catch (error) {
    console.error(error)
    // ElMessage.error('获取课程列表失败') 
    // Global interceptor might handle this, but safe to log
  } finally {
    loading.value = false
  }
}

const enterCourse = (id) => {
    router.push(`/teacher/course/${id}/overview`)
}

const handleSubmit = async () => {
    if (!formRef.value) return
    
    await formRef.value.validate(async (valid) => {
        if (valid) {
            submitting.value = true
            try {
                await courseApi.createCourse(form)
                ElMessage.success('课程创建成功')
                dialogVisible.value = false
                // Reset form
                form.courseName = ''
                form.description = ''
                // Refresh list
                fetchCourses()
            } catch (error) {
                console.error(error)
                // Error handled by interceptor usually
            } finally {
                submitting.value = false
            }
        }
    })
}

const getGradientClass = (id) => {
    const gradients = ['grad-1', 'grad-2', 'grad-3', 'grad-4']
    return gradients[id % gradients.length]
}

onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.page-container {
    padding: 20px;
    max-width: 1400px;
    margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h2 {
    font-size: 28px;
    color: #1f2937;
    margin: 0;
    font-weight: 700;
}

.subtitle {
    color: #6b7280;
    margin-top: 5px;
    font-size: 14px;
}

.loading-container {
    padding: 40px;
}

.empty-state {
    padding: 60px 0;
    text-align: center;
}

.course-card {
    background: #fff;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    cursor: pointer;
    margin-bottom: 24px;
    border: 1px solid #f3f4f6;
    height: 280px;
    display: flex;
    flex-direction: column;
}

.course-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.course-cover {
    height: 140px;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
}

.grad-1 { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.grad-2 { background: linear-gradient(135deg, #2af598 0%, #009efd 100%); }
.grad-3 { background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 99%, #fecfef 100%); }
.grad-4 { background: linear-gradient(135deg, #f6d365 0%, #fda085 100%); }

.cover-text {
    font-size: 48px;
    font-weight: 800;
    color: rgba(255, 255, 255, 0.9);
    text-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.course-content {
    padding: 20px;
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}

.course-title {
    margin: 0 0 8px;
    font-size: 18px;
    font-weight: 600;
    color: #111827;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.course-desc {
    font-size: 13px;
    color: #6b7280;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    margin: 0;
}

.course-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #f3f4f6;
}

.enter-text {
    font-size: 13px;
    color: #3b82f6;
    display: flex;
    align-items: center;
    gap: 4px;
    font-weight: 500;
}

.custom-dialog :deep(.el-dialog__body) {
    padding-top: 10px;
}
</style>
