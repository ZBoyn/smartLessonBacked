<template>
  <div class="overview-container">
    <el-row :gutter="24">
      <!-- Course Info Card -->
      <el-col :span="16">
        <el-card class="info-card" shadow="never">
            <div class="card-header-flex">
                <h3>课程概况</h3>
                <el-button type="primary" plain size="small" icon="Edit">编辑信息</el-button>
            </div>
            
            <div class="info-grid" v-if="course">
                <div class="info-group">
                    <label>课程名称</label>
                    <div class="value-text">{{ course.courseName }}</div>
                </div>
                <div class="info-group">
                    <label>创建时间</label>
                    <div class="value-text">2025-10-01</div>
                </div>
                <div class="info-group full-width">
                    <label>课程描述</label>
                    <div class="value-text desc-text">{{ course.description || '暂无描述' }}</div>
                </div>
            </div>
        </el-card>

        <el-row :gutter="24" style="margin-top: 24px">
            <el-col :span="12">
                <div class="action-card purple-gradient">
                    <div class="action-icon"><el-icon><Connection /></el-icon></div>
                    <div class="action-text">
                        <h4>知识图谱</h4>
                        <p>构建课程知识体系</p>
                    </div>
                </div>
            </el-col>
            <el-col :span="12">
                <div class="action-card blue-gradient">
                    <div class="action-icon"><el-icon><Reading /></el-icon></div>
                    <div class="action-text">
                        <h4>发布测评</h4>
                        <p>创建新的考试或作业</p>
                    </div>
                </div>
            </el-col>
        </el-row>
      </el-col>

      <!-- Stats Column -->
      <el-col :span="8">
        <el-card class="stats-card" shadow="never">
            <h3>数据概览</h3>
            <div class="stat-list">
                <div class="stat-item">
                    <div class="stat-icon bg-blue"><el-icon><User /></el-icon></div>
                    <div class="stat-info">
                        <div class="stat-value">45</div>
                        <div class="stat-label">学生总数</div>
                    </div>
                </div>
                <div class="stat-item">
                    <div class="stat-icon bg-green"><el-icon><Document /></el-icon></div>
                    <div class="stat-info">
                        <div class="stat-value">12</div>
                        <div class="stat-label">教学资源</div>
                    </div>
                </div>
                <div class="stat-item">
                    <div class="stat-icon bg-orange"><el-icon><Trophy /></el-icon></div>
                    <div class="stat-info">
                        <div class="stat-value">8</div>
                        <div class="stat-label">已发布测评</div>
                    </div>
                </div>
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
.overview-container {
    max-width: 1200px;
}

.info-card, .stats-card {
    border-radius: 12px;
    border: 1px solid #e2e8f0;
}

.card-header-flex {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.card-header-flex h3 {
    margin: 0;
    font-size: 18px;
    color: #1e293b;
}

.info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24px;
}

.info-group label {
    display: block;
    font-size: 12px;
    color: #64748b;
    margin-bottom: 6px;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.value-text {
    font-size: 15px;
    color: #0f172a;
    font-weight: 500;
}

.desc-text {
    line-height: 1.6;
    color: #334155;
}

.full-width {
    grid-column: span 2;
}

.action-card {
    height: 100px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    padding: 0 24px;
    color: #fff;
    cursor: pointer;
    transition: transform 0.2s;
}

.action-card:hover {
    transform: translateY(-3px);
}

.purple-gradient {
    background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);
}

.blue-gradient {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.action-icon {
    font-size: 32px;
    margin-right: 16px;
    opacity: 0.9;
}

.action-text h4 {
    margin: 0 0 4px;
    font-size: 16px;
}

.action-text p {
    margin: 0;
    font-size: 12px;
    opacity: 0.8;
}

.stat-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.stat-item {
    display: flex;
    align-items: center;
    padding: 16px;
    background: #f8fafc;
    border-radius: 8px;
}

.stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #fff;
    margin-right: 16px;
}

.bg-blue { background: #3b82f6; }
.bg-green { background: #10b981; }
.bg-orange { background: #f59e0b; }

.stat-value {
    font-size: 20px;
    font-weight: 700;
    color: #0f172a;
}

.stat-label {
    font-size: 13px;
    color: #64748b;
}
</style>
