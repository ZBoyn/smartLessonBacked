<template>
  <div class="page-container">
    <h2>在线考试</h2>
    <el-card>
      <el-table :data="assessments" style="width: 100%">
        <el-table-column prop="title" label="考试名称" />
        <el-table-column prop="courseName" label="所属课程" width="180" />
        <el-table-column prop="deadline" label="截止时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'Pending' ? 'danger' : 'success'">
              {{ scope.row.status === 'Pending' ? '未完成' : '已提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              :disabled="scope.row.status !== 'Pending'"
              @click="startExam(scope.row.id)"
            >
              开始考试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { assessmentApi } from '../../api/modules'
import { useRouter } from 'vue-router'

const router = useRouter()
const assessments = ref([])

onMounted(async () => {
    try {
        const res = await assessmentApi.getPendingAssessments()
        assessments.value = res
    } catch (e) {
        assessments.value = [
            { assessmentId: 101, title: '第一次阶段测试', courseName: 'Java 程序设计', deadline: '2023-12-01 12:00', status: 'Pending' },
            { assessmentId: 102, title: '期末模拟考', courseName: '数据结构', deadline: '2023-12-10 10:00', status: 'Completed' }
        ]
    }
})

const startExam = (id) => {
    router.push({ name: 'StudentExam', params: { assessmentId: id } })
}
</script>

