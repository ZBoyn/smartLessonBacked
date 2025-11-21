<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>学情深度分析</h2>
        <p class="subtitle">基于知识图谱的学生掌握度与薄弱点追踪</p>
      </div>
      <el-select v-model="selectedClass" placeholder="选择班级">
        <el-option label="2021级 计算机 1 班" value="1" />
        <el-option label="2021级 计算机 2 班" value="2" />
      </el-select>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card header="班级知识点掌握度雷达">
           <div class="chart-placeholder">
             <!-- 这里后续集成 ECharts 雷达图 -->
             <div class="mock-radar">
               <div class="radar-circle"></div>
               <span>图表区域</span>
             </div>
           </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="薄弱知识点预警">
          <el-table :data="weakPoints" stripe style="width: 100%">
            <el-table-column prop="kpName" label="知识点" />
            <el-table-column prop="masteryRate" label="平均掌握率">
              <template #default="scope">
                <el-progress 
                  :percentage="scope.row.masteryRate" 
                  :status="getStatus(scope.row.masteryRate)" 
                />
              </template>
            </el-table-column>
            <el-table-column prop="riskLevel" label="风险等级" width="100">
              <template #default="scope">
                <el-tag :type="getRiskTag(scope.row.riskLevel)">{{ scope.row.riskLevel }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card header="学生个人画像" style="margin-top: 20px">
      <el-table :data="studentList" style="width: 100%">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="id" label="学号" width="150" />
        <el-table-column prop="score" label="综合评分" width="120">
             <template #default="scope">
                <span style="font-weight: bold; color: #409EFF">{{ scope.row.score }}</span>
             </template>
        </el-table-column>
        <el-table-column prop="suggestion" label="AI 学习建议" />
        <el-table-column label="操作" width="120">
          <template #default>
            <el-button size="small">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const selectedClass = ref('1')

const weakPoints = ref([
  { kpName: '动态规划算法', masteryRate: 45, riskLevel: '高' },
  { kpName: '红黑树实现', masteryRate: 52, riskLevel: '中' },
  { kpName: 'JVM 内存模型', masteryRate: 68, riskLevel: '低' },
])

const studentList = ref([
  { name: '李四', id: '2021001', score: 88, suggestion: '基础扎实，建议加强算法实战训练。' },
  { name: '王五', id: '2021002', score: 62, suggestion: '数据结构部分薄弱，建议复习链表与树相关章节。' },
])

const getStatus = (rate) => {
  if (rate < 60) return 'exception'
  if (rate < 80) return 'warning'
  return 'success'
}

const getRiskTag = (level) => {
  const map = { '高': 'danger', '中': 'warning', '低': 'info' }
  return map[level]
}
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.subtitle {
  color: #909399;
  font-size: 14px;
  margin-top: 4px;
}
.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>

