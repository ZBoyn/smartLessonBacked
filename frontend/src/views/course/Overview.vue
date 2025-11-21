<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="overview-card">
            <template #header>
                <div class="card-header">
                    <span>课程概况</span>
                    <el-button type="primary" link>编辑信息</el-button>
                </div>
            </template>
            <div class="course-info" v-if="course">
                <div class="info-item">
                    <label>课程名称：</label>
                    <span>{{ course.name }}</span>
                </div>
                <div class="info-item">
                    <label>课程描述：</label>
                    <p>{{ course.description || '暂无描述' }}</p>
                </div>
                <div class="info-item">
                    <label>课程状态：</label>
                    <el-tag>{{ course.status || 'Active' }}</el-tag>
                </div>
            </div>
        </el-card>
      </el-col>
      <el-col :span="8">
          <el-card header="快速统计">
              <div class="stat-box">
                  <div class="stat-num">3</div>
                  <div class="stat-label">班级数</div>
              </div>
              <el-divider />
              <div class="stat-box">
                  <div class="stat-num">12</div>
                  <div class="stat-label">资源数</div>
              </div>
          </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { courseApi } from '../../api/modules'

const route = useRoute()
const courseId = route.params.courseId
const course = ref(null)

onMounted(async () => {
    if(courseId) {
        try {
            const res = await courseApi.getCourseById(courseId)
            course.value = res
        } catch (e) {
            console.error(e)
        }
    }
})
</script>

<style scoped>
.info-item {
    margin-bottom: 15px;
}
.info-item label {
    font-weight: bold;
    color: #606266;
    margin-right: 10px;
}
.stat-box {
    text-align: center;
    padding: 10px;
}
.stat-num {
    font-size: 24px;
    font-weight: bold;
    color: #409EFF;
}
.stat-label {
    font-size: 12px;
    color: #909399;
}
</style>

