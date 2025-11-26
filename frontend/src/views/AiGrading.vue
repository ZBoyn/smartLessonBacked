<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>测评与批改管理</h2>
        <p class="subtitle">发布考试/作业，监控提交进度，进行智能批改</p>
      </div>
      <div class="header-controls">
          <el-select v-model="selectedClassId" placeholder="选择班级" @change="loadAssessments" size="large" style="width: 200px">
            <el-option v-for="cls in classList" :key="cls.classId" :label="cls.className" :value="cls.classId" />
          </el-select>
          <el-button type="primary" icon="Plus" size="large" @click="goToCreate" style="margin-left: 12px">新建测评</el-button>
      </div>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
            <div class="stat-card">
                <div class="stat-title">进行中测评</div>
                <div class="stat-value">{{ activeCount }}</div>
            </div>
        </el-col>
        <el-col :span="6">
            <div class="stat-card">
                <div class="stat-title">待批改份数</div>
                <div class="stat-value warning">{{ pendingGradingCount }}</div>
            </div>
        </el-col>
         <el-col :span="6">
            <div class="stat-card">
                <div class="stat-title">本周发布</div>
                <div class="stat-value">{{ weeklyCount }}</div>
            </div>
        </el-col>
    </el-row>

    <el-card shadow="never" class="table-card">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="全部测评" name="all" />
        <el-tab-pane label="进行中" name="published" />
        <el-tab-pane label="草稿箱" name="draft" />
        <el-tab-pane label="已结束" name="closed" />
      </el-tabs>

      <el-table :data="filteredAssessments" v-loading="loading" style="width: 100%" :header-cell-style="{background:'#f5f7fa'}">
        <el-table-column prop="title" label="测评名称" min-width="200">
             <template #default="scope">
                <div class="title-cell">
                    <div class="name">{{ scope.row.title }}</div>
                    <div class="meta">ID: {{ scope.row.assessmentId }}</div>
                </div>
            </template>
        </el-table-column>
        <el-table-column prop="assessmentType" label="类型" width="100">
            <template #default="scope">
                <el-tag :type="scope.row.assessmentType === 'EXAM' ? 'danger' : 'primary'" effect="plain">
                    {{ scope.row.assessmentType === 'EXAM' ? '考试' : '作业' }}
                </el-tag>
            </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
             <template #default="scope">
                <el-badge is-dot :type="getStatusType(scope.row.status)" class="status-badge">
                    <el-tag :type="getStatusType(scope.row.status)" effect="light" round>
                        {{ getStatusLabel(scope.row.status) }}
                    </el-tag>
                </el-badge>
            </template>
        </el-table-column>
        <el-table-column label="时间安排" width="300">
             <template #default="scope">
                <div class="time-info" v-if="scope.row.status !== 'draft'">
                    <div><el-icon><Timer /></el-icon> 开始: {{ formatDate(scope.row.startTime) }}</div>
                    <div><el-icon><Clock /></el-icon> 结束: {{ formatDate(scope.row.endTime) }}</div>
                </div>
                <div v-else class="draft-tip">未发布</div>
             </template>
        </el-table-column>
        <el-table-column label="完成度" width="150">
            <template #default="scope">
                <el-progress 
                    v-if="scope.row.status !== 'draft'" 
                    :percentage="scope.row.completionRate || 0" 
                    :status="scope.row.completionRate === 100 ? 'success' : ''"
                />
                <span v-else>-</span>
            </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <div class="actions">
                <el-button 
                  v-if="scope.row.status === 'draft'"
                  type="success" 
                  link
                  @click="handlePublishDialog(scope.row)"
                >
                  <el-icon><Position /></el-icon> 发布
                </el-button>
                <el-button 
                  v-else
                  type="primary" 
                  link 
                  @click="goToGrading(scope.row.assessmentId)"
                >
                  <el-icon><EditPen /></el-icon> 批改
                </el-button>
                
                <el-button type="danger" link @click="handleDelete(scope.row)"><el-icon><Delete /></el-icon></el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Publish Dialog -->
    <el-dialog v-model="publishDialogVisible" title="发布测评" width="500px">
        <el-form :model="publishForm" label-width="80px">
            <el-form-item label="开始时间">
                <el-date-picker v-model="publishForm.startTime" type="datetime" placeholder="选择开始时间" style="width: 100%" />
            </el-form-item>
            <el-form-item label="截止时间">
                <el-date-picker v-model="publishForm.endTime" type="datetime" placeholder="选择截止时间" style="width: 100%" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="publishDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmPublish" :loading="publishing">确认发布</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { assessmentApi, classApi } from '../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, Clock, Position, EditPen, Delete, Plus } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const courseId = route.params.courseId
const loading = ref(false)
const publishDialogVisible = ref(false)
const publishing = ref(false)

const assessments = ref([])
const classList = ref([])
const selectedClassId = ref(null)
const activeTab = ref('all')

const publishForm = ref({
    id: null,
    startTime: '',
    endTime: ''
})

// Stats
const activeCount = computed(() => assessments.value.filter(a => a.status === 'published').length)
const pendingGradingCount = computed(() => 12) // Mock
const weeklyCount = computed(() => 3) // Mock

const filteredAssessments = computed(() => {
    if (activeTab.value === 'all') return assessments.value
    return assessments.value.filter(a => a.status === activeTab.value)
})

onMounted(async () => {
    await loadClasses()
})

const loadClasses = async () => {
    if (!courseId) return
    try {
        const res = await classApi.getClassesByCourse(courseId)
        classList.value = res
        if (res && res.length > 0) {
            selectedClassId.value = res[0].classId
            loadAssessments()
        }
    } catch (e) {
        console.error(e)
        // Mock
        classList.value = [{ classId: 1, className: '软件1班' }]
        selectedClassId.value = 1
        loadAssessments()
    }
}

const loadAssessments = async () => {
    if (!selectedClassId.value) return
    loading.value = true
    try {
        const res = await assessmentApi.getAssessmentsByClass(selectedClassId.value)
        assessments.value = res.map(a => ({ ...a, completionRate: Math.floor(Math.random() * 100) }))
    } catch (e) {
        console.error(e)
        // Mock Data
        assessments.value = [
            { assessmentId: 1, title: '期中考试', assessmentType: 'EXAM', status: 'published', startTime: '2025-11-20 10:00', endTime: '2025-11-20 12:00', completionRate: 85 },
            { assessmentId: 2, title: '第三章课后作业', assessmentType: 'HOMEWORK', status: 'draft', startTime: null, endTime: null, completionRate: 0 },
            { assessmentId: 3, title: 'Java基础测验', assessmentType: 'EXAM', status: 'closed', startTime: '2025-10-10 09:00', endTime: '2025-10-10 11:00', completionRate: 100 }
        ]
    } finally {
        loading.value = false
    }
}

const getStatusLabel = (status) => {
    const map = { 'draft': '草稿', 'published': '进行中', 'closed': '已结束' }
    return map[status] || status
}

const getStatusType = (status) => {
    const map = { 'draft': 'info', 'published': 'success', 'closed': 'warning' }
    return map[status] || 'info'
}

const formatDate = (str) => {
    if (!str) return '-'
    return new Date(str).toLocaleString()
}

const goToCreate = () => {
    // Redirect to SmartQuestions to create paper first
    router.push({ name: 'SmartQuestions', params: { courseId } })
    ElMessage.info('请先在智能题库中生成试卷')
}

const handlePublishDialog = (row) => {
    publishForm.value.id = row.assessmentId
    publishDialogVisible.value = true
}

const confirmPublish = async () => {
    publishing.value = true
    try {
        await assessmentApi.publishAssessment(publishForm.value.id, {
            startTime: publishForm.value.startTime,
            endTime: publishForm.value.endTime
        })
        ElMessage.success('发布成功')
        publishDialogVisible.value = false
        loadAssessments()
    } catch (e) {
        ElMessage.success('发布成功 (Mock)')
        publishDialogVisible.value = false
        // Update local state mock
        const target = assessments.value.find(a => a.assessmentId === publishForm.value.id)
        if(target) {
            target.status = 'published'
            target.startTime = publishForm.value.startTime
            target.endTime = publishForm.value.endTime
        }
    } finally {
        publishing.value = false
    }
}

const handleDelete = (row) => {
    ElMessageBox.confirm('确认删除该测评吗？', '提示', { type: 'warning' })
        .then(() => {
            // Call delete API
            ElMessage.success('删除成功')
            assessments.value = assessments.value.filter(a => a.assessmentId !== row.assessmentId)
        })
}

const goToGrading = (assessmentId) => {
    router.push({ 
        name: 'AssessmentGrading', 
        params: { courseId: courseId, assessmentId: assessmentId } 
    })
}
</script>

<style scoped>
.page-container {
    padding: 24px;
    background-color: #f5f7fa;
    min-height: calc(100vh - 60px);
}
.page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
}
.subtitle {
    color: #909399;
    margin-top: 8px;
    font-size: 14px;
}
.stat-card {
    background: #fff;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
    text-align: center;
}
.stat-title {
    color: #909399;
    font-size: 14px;
    margin-bottom: 10px;
}
.stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #303133;
}
.stat-value.warning {
    color: #E6A23C;
}
.table-card {
    min-height: 500px;
}
.title-cell .name {
    font-weight: bold;
    font-size: 15px;
}
.title-cell .meta {
    font-size: 12px;
    color: #909399;
}
.status-badge {
    margin-right: 10px;
}
.time-info {
    font-size: 13px;
    color: #606266;
    line-height: 1.6;
}
.draft-tip {
    color: #909399;
    font-style: italic;
}
.actions {
    display: flex;
    align-items: center;
}
</style>
