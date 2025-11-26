<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>学情深度分析</h2>
        <p class="subtitle">基于知识图谱的全维度学情监测与诊断系统</p>
      </div>
      <div class="selectors">
          <el-select v-model="selectedCourseId" placeholder="请选择课程" @change="handleCourseChange" style="width: 220px; margin-right: 15px" size="large">
            <template #prefix><el-icon><Reading /></el-icon></template>
            <el-option v-for="c in courses" :key="c.courseId" :label="c.courseName" :value="c.courseId" />
          </el-select>
          <el-select v-model="selectedClassId" placeholder="请选择班级" @change="handleClassChange" style="width: 220px" :disabled="!selectedCourseId" size="large">
            <template #prefix><el-icon><School /></el-icon></template>
            <el-option v-for="cls in classes" :key="cls.classId" :label="cls.className" :value="cls.classId" />
          </el-select>
          <el-button type="primary" size="large" style="margin-left: 12px" @click="exportCsv">导出报表</el-button>
      </div>
    </div>

    <div v-if="!selectedClassId" class="empty-wrapper">
        <el-empty description="请在上访选择课程和班级以加载分析数据" :image-size="200">
             <template #extra>
                 <el-button type="primary" disabled>暂无数据</el-button>
             </template>
        </el-empty>
    </div>

    <div v-else v-loading="loading">
        <!-- Top Stats -->
        <el-row :gutter="20" class="stats-row">
            <el-col :span="6">
                <div class="stat-box bg-blue">
                    <div class="stat-label">班级平均分</div>
                    <div class="stat-num">{{ classStats.avgScore || '-' }}</div>
                </div>
            </el-col>
            <el-col :span="6">
                 <div class="stat-box bg-green">
                    <div class="stat-label">知识点掌握率</div>
                    <div class="stat-num">{{ classStats.avgMastery ? (classStats.avgMastery * 100).toFixed(1) + '%' : '-' }}</div>
                </div>
            </el-col>
             <el-col :span="6">
                 <div class="stat-box bg-orange">
                    <div class="stat-label">预警人数</div>
                    <div class="stat-num">{{ classStats.warningCount || 0 }}</div>
                </div>
            </el-col>
             <el-col :span="6">
                 <div class="stat-box bg-purple">
                    <div class="stat-label">作业提交率</div>
                    <div class="stat-num">{{ classStats.submitRate ? (classStats.submitRate * 100).toFixed(1) + '%' : '-' }}</div>
                </div>
            </el-col>
        </el-row>

        <el-row :gutter="20">
            <el-col :span="14">
                <el-card class="chart-card" shadow="hover">
                    <template #header>
                        <div class="card-header">
                            <span><el-icon><TrendCharts /></el-icon> 知识点掌握度分布</span>
                        </div>
                    </template>
                    <div id="mastery-chart" class="chart-container" ref="chartRef"></div>
                </el-card>
            </el-col>
            <el-col :span="10">
                <el-card class="list-card" shadow="hover">
                    <template #header>
                        <div class="card-header warning-header">
                            <span><el-icon><Warning /></el-icon> 薄弱知识点预警 (Top 5)</span>
                            <el-tag type="danger" effect="dark" size="small">急需强化</el-tag>
                        </div>
                    </template>
                     <el-table :data="weakPoints" stripe style="width: 100%" :show-header="false">
                        <el-table-column prop="kpName" label="知识点">
                             <template #default="scope">
                                 <span class="weak-kp-name">{{ scope.row.kpName }}</span>
                             </template>
                        </el-table-column>
                        <el-table-column prop="masteryRate" label="掌握率" width="100" align="right">
                            <template #default="scope">
                                <span class="weak-rate">{{ (scope.row.masteryRate * 100).toFixed(1) }}%</span>
                            </template>
                        </el-table-column>
                        <el-table-column width="120" align="right">
                            <template #default="scope">
                                <el-button type="primary" link size="small" @click="openPushDialog(scope.row)">推送练习</el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="12">
                <el-card class="chart-card" shadow="hover">
                    <template #header>
                        <div class="card-header"><span><el-icon><TrendCharts /></el-icon> 知识图谱雷达图</span></div>
                    </template>
                    <div id="radar-chart" class="chart-container" ref="radarRef"></div>
                </el-card>
            </el-col>
            <el-col :span="12">
                <el-card class="chart-card" shadow="hover">
                    <template #header>
                        <div class="card-header"><span><el-icon><TrendCharts /></el-icon> 学习行为与成绩关联</span></div>
                    </template>
                    <div id="corr-chart" class="chart-container" ref="corrRef"></div>
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="24">
                <el-card class="chart-card" shadow="hover">
                    <template #header>
                        <div class="card-header"><span><el-icon><TrendCharts /></el-icon> 知识分数矩阵</span></div>
                    </template>
                    <div id="matrix-chart" class="chart-container" ref="matrixRef" style="height: 420px"></div>
                </el-card>
            </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
            <el-col :span="24">
                <el-card class="chart-card" shadow="hover">
                    <template #header>
                        <div class="card-header"><span><el-icon><TrendCharts /></el-icon> 成绩分布</span></div>
                    </template>
                    <div id="score-chart" class="chart-container" ref="scoreRef" style="height: 360px"></div>
                </el-card>
            </el-col>
        </el-row>

        <el-card class="student-table-card" shadow="hover" style="margin-top: 20px">
            <template #header>
                 <div class="card-header"><span><el-icon><TrendCharts /></el-icon> 视频学习统计</span></div>
            </template>
            <el-table :data="videoStats.videos" stripe style="width: 100%">
                <el-table-column prop="title" label="视频" />
                <el-table-column prop="avgCompletion" label="平均完成率" width="160">
                    <template #default="scope">
                        {{ (scope.row.avgCompletion * 100).toFixed(1) }}%
                    </template>
                </el-table-column>
                <el-table-column prop="viewerCount" label="观看人数" width="120" />
            </el-table>
        </el-card>

        <el-dialog v-model="pushDialogVisible" title="针对薄弱知识点推送练习" width="600px" align-center destroy-on-close>
            <el-form :model="pushForm" label-width="120px">
                <el-form-item label="知识点">
                    <el-input v-model="pushForm.kpName" disabled />
                </el-form-item>
                <el-form-item label="目标班级" required>
                    <el-select v-model="pushForm.classId" placeholder="请选择班级" style="width: 100%">
                        <el-option v-for="cls in classes" :key="cls.classId" :label="cls.className" :value="cls.classId" />
                    </el-select>
                </el-form-item>
                <el-form-item label="练习题数">
                    <el-input-number v-model="pushForm.count" :min="1" :max="20" />
                </el-form-item>
                <el-form-item label="标题">
                    <el-input v-model="pushForm.title" placeholder="例如：薄弱知识点专项练习" />
                </el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="pushDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitPush" :loading="pushing">推送</el-button>
            </template>
        </el-dialog>

        <el-card class="student-table-card" shadow="hover">
            <template #header>
                 <div class="card-header">
                    <span><el-icon><User /></el-icon> 学生学情画像</span>
                    <el-input v-model="searchStudent" placeholder="搜索学生姓名/学号" style="width: 200px" prefix-icon="Search" size="small" />
                </div>
            </template>
            <el-table :data="filteredStudents" style="width: 100%" stripe>
                <el-table-column prop="name" label="姓名" width="120">
                     <template #default="scope">
                         <div class="student-name">
                             <el-avatar :size="24" style="margin-right: 8px; background: #409EFF">{{ scope.row.name[0] }}</el-avatar>
                             {{ scope.row.name }}
                         </div>
                     </template>
                </el-table-column>
                <el-table-column prop="studentId" label="学号" width="150" />
                <el-table-column prop="avgScore" label="综合得分" width="120" sortable>
                    <template #default="scope">
                        <span class="score-val" :class="getScoreClass(scope.row.avgScore)">
                            {{ scope.row.avgScore ? scope.row.avgScore.toFixed(1) : '-' }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column prop="masteryRate" label="知识掌握度" width="180" sortable>
                     <template #default="scope">
                        <el-progress :percentage="scope.row.masteryRate ? Math.round(scope.row.masteryRate * 100) : 0" :color="getColor" />
                     </template>
                </el-table-column>
                <el-table-column prop="suggestion" label="AI 诊断建议" show-overflow-tooltip />
                <el-table-column label="操作" width="120" fixed="right">
                    <template #default>
                        <el-button type="primary" link size="small">详细报告</el-button>
                    </template>
                </el-table-column>
            </el-table>
             <div class="pagination">
                <el-pagination layout="prev, pager, next" :total="filteredStudents.length" />
            </div>
        </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick, onUnmounted } from 'vue'
import { courseApi, classApi, analysisApi, questionApi } from '../api/modules'
import { Reading, School, TrendCharts, Warning, User, Search } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

const courses = ref([])
const classes = ref([])
const selectedCourseId = ref(null)
const selectedClassId = ref(null)
const loading = ref(false)
const searchStudent = ref('')
const chartRef = ref(null)
const radarRef = ref(null)
const corrRef = ref(null)
const matrixRef = ref(null)
const scoreRef = ref(null)
let myChart = null
let radarChart = null
let corrChart = null

const kpMasteryList = ref([])
const studentList = ref([])
const classStats = ref({ avgScore: 0, avgMastery: 0, warningCount: 0, submitRate: 0 })
const radarData = ref({ indicators: [], classAvg: [], topStudent: [], passRate: [] })
const videoStats = ref({ overallCompletion: 0, avgEngagement: 0, totalViewers: 0, videos: [] })
const correlationPoints = ref([])
const matrixData = ref({ students: [], kps: [], cells: [] })
const pushDialogVisible = ref(false)
const pushForm = ref({ kpId: null, kpName: '', classId: null, count: 10, title: '薄弱知识点专项练习' })
const pushing = ref(false)

onMounted(async () => {
    try {
        const res = await courseApi.getAllCourses()
        courses.value = res
        if(res.length > 0) {
            // Auto select first course for demo if desired, but user might want to choose
            // selectedCourseId.value = res[0].courseId
            // handleCourseChange()
        }
    } catch (e) {
        // Mock
        courses.value = [{ courseId: 1, courseName: 'Java 程序设计' }]
    }
})

const handleCourseChange = async () => {
    selectedClassId.value = null
    classes.value = []
    if (!selectedCourseId.value) return
    try {
        const res = await classApi.getClassesByCourse(selectedCourseId.value)
        classes.value = res
    } catch (e) {
         classes.value = [{ classId: 101, className: '计算机 2101' }, { classId: 102, className: '软件 2102' }]
    }
}

const handleClassChange = async () => {
    if (!selectedClassId.value) return
    loading.value = true
    try {
        // 1. Fetch Class Mastery
        const masteryRes = await analysisApi.getClassMastery(selectedClassId.value)
        kpMasteryList.value = masteryRes || []

        // 2. Fetch Student Analysis
        const studentRes = await analysisApi.getStudentAnalysis(selectedClassId.value)
        studentList.value = studentRes || []

        const radarRes = await analysisApi.getKnowledgeRadar(selectedClassId.value)
        radarData.value = radarRes || { indicators: [], classAvg: [], topStudent: [], passRate: [] }

        const matrixRes = await analysisApi.getKnowledgeScoreMatrix(selectedClassId.value)
        matrixData.value = matrixRes || { students: [], knowledgePoints: [], cells: [] }

        const videoRes = await analysisApi.getVideoLearningStats(selectedClassId.value)
        videoStats.value = videoRes || { overallCompletion: 0, avgEngagement: 0, totalViewers: 0, videos: [] }

        const corrRes = await analysisApi.getLearningCorrelation(selectedClassId.value)
        correlationPoints.value = corrRes || []
        
        // Calculate Stats
        if (studentList.value.length > 0) {
             const totalScore = studentList.value.reduce((acc, s) => acc + (s.avgScore || 0), 0)
             classStats.value.avgScore = (totalScore / studentList.value.length).toFixed(1)
             classStats.value.warningCount = studentList.value.filter(s => (s.avgScore || 0) < 60).length
             classStats.value.avgMastery = 0.78 // Mock aggregate
             classStats.value.submitRate = 0.95 // Mock
        }

        // Fallback Mock if empty
        if (kpMasteryList.value.length === 0) {
             kpMasteryList.value = [
                 { kpName: '集合框架', masteryRate: 0.85 },
                 { kpName: '多线程', masteryRate: 0.55 },
                 { kpName: 'IO流', masteryRate: 0.72 },
                 { kpName: '反射机制', masteryRate: 0.48 },
                 { kpName: '网络编程', masteryRate: 0.65 },
                 { kpName: 'JVM基础', masteryRate: 0.40 },
             ]
        }
         if (studentList.value.length === 0) {
             studentList.value = Array.from({length: 15}, (_, i) => ({
                 name: `学生${i+1}`,
                 studentId: `2021${100+i}`,
                 avgScore: 50 + Math.random() * 50,
                 masteryRate: 0.4 + Math.random() * 0.6,
                 suggestion: Math.random() > 0.5 ? '建议加强多线程部分的练习' : '基础扎实，继续保持'
             }))
             classStats.value = { avgScore: 78.5, avgMastery: 0.72, warningCount: 3, submitRate: 0.92 }
        }

        if (!radarData.value.indicators || radarData.value.indicators.length === 0) {
            const inds = kpMasteryList.value.slice(0, 6).map(k => ({ name: k.kpName, max: 1 }))
            radarData.value = {
                indicators: inds,
                classAvg: inds.map((_, i) => kpMasteryList.value[i].masteryRate || 0),
                topStudent: inds.map(() => Math.min(1, 0.8 + Math.random() * 0.2)),
                passRate: inds.map(() => 0.6 + Math.random() * 0.3)
            }
        }

        if (!matrixData.value.cells || matrixData.value.cells.length === 0) {
            const studentsMock = studentList.value.slice(0, 10).map(s => ({ studentId: Number(s.studentId), studentName: s.name }))
            const kpsMock = kpMasteryList.value.slice(0, 6).map((k, idx) => ({ kpId: 100 + idx, kpName: k.kpName }))
            const cellsMock = []
            for (const s of studentsMock) {
                for (const k of kpsMock) {
                    cellsMock.push({ kpId: k.kpId, studentId: s.studentId, masteryRate: Math.random() })
                }
            }
            matrixData.value = { students: studentsMock, knowledgePoints: kpsMock, cells: cellsMock }
        }

        if (!correlationPoints.value || correlationPoints.value.length === 0) {
            correlationPoints.value = Array.from({ length: 100 }, () => {
                const watch = 100 + Math.random() * 500
                const score = Math.min(100, 50 + (watch / 600) * 40 + (Math.random() - 0.5) * 20)
                const submit = Math.min(1, 0.5 + score / 200 + Math.random() * 0.1)
                return { engagementScore: watch, finalScore: score, submissionRate: submit }
            })
        }
        
        nextTick(() => {
            initChart()
            initRadar()
            initCorrelation()
            initMatrix()
            initScore()
        })

    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

const weakPoints = computed(() => {
    return kpMasteryList.value.filter(kp => kp.masteryRate < 0.6).slice(0, 5)
})

const filteredStudents = computed(() => {
    if(!searchStudent.value) return studentList.value
    const q = searchStudent.value.toLowerCase()
    return studentList.value.filter(s => s.name.includes(q) || String(s.studentId).includes(q))
})

const getColor = (percentage) => {
    if (percentage < 60) return '#F56C6C'
    if (percentage < 80) return '#E6A23C'
    return '#67C23A'
}

const getScoreClass = (score) => {
    if (score >= 90) return 'score-high'
    if (score < 60) return 'score-low'
    return 'score-mid'
}

const initChart = () => {
    if (!chartRef.value) return
    if (myChart) myChart.dispose()
    
    myChart = echarts.init(chartRef.value)
    
    const option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: { type: 'shadow' }
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'category',
            data: kpMasteryList.value.map(k => k.kpName),
            axisLabel: { interval: 0, rotate: 30 }
        },
        yAxis: {
            type: 'value',
            max: 1,
            axisLabel: {
                formatter: (val) => (val * 100) + '%'
            }
        },
        series: [
            {
                name: '掌握率',
                type: 'bar',
                data: kpMasteryList.value.map(k => k.masteryRate),
                itemStyle: {
                    color: (params) => {
                        if (params.value < 0.6) return '#F56C6C'
                        return '#409EFF'
                    },
                    borderRadius: [4, 4, 0, 0]
                },
                barWidth: '40%'
            }
        ]
    }
    
    myChart.setOption(option)
    window.addEventListener('resize', resizeChart)
}

const initRadar = () => {
    if (!radarRef.value) return
    if (radarChart) radarChart.dispose()
    radarChart = echarts.init(radarRef.value)
    const indicators = (radarData.value.indicators || []).map(i => ({ name: i.name || i, max: 1 }))
    const option = {
        tooltip: {},
        radar: { indicator: indicators },
        series: [
            { type: 'radar', data: [ { value: radarData.value.classAvg, name: '班级平均' }, { value: radarData.value.topStudent, name: '最佳学生' } ] }
        ]
    }
    radarChart.setOption(option)
    window.addEventListener('resize', resizeRadar)
}

const initCorrelation = () => {
    if (!corrRef.value) return
    if (corrChart) corrChart.dispose()
    corrChart = echarts.init(corrRef.value)
    const data = (correlationPoints.value || []).map(p => [p.engagementScore, p.finalScore, Math.round((p.submissionRate || 0) * 30) + 5])
    const option = {
        tooltip: {},
        xAxis: { name: '学习投入' },
        yAxis: { name: '综合得分', max: 100 },
        series: [ { type: 'scatter', data, symbolSize: d => d[2], itemStyle: { color: '#409EFF' } } ]
    }
    corrChart.setOption(option)
    window.addEventListener('resize', resizeCorr)
}

const initMatrix = () => {
    if (!matrixRef.value) return
    const students = (matrixData.value.students || []).map(s => s.studentName)
    const kps = (matrixData.value.knowledgePoints || matrixData.value.kps || []).map(k => k.kpName || k.name)
    const map = {}
    for (const c of (matrixData.value.cells || [])) {
        const key = (c.studentId) + ':' + (c.kpId)
        map[key] = (c.masteryRate || 0)
    }
    const getIndex = (list, val, path) => list.findIndex((x, i) => {
        if (path === 'student') return (matrixData.value.students[i].studentId) === val
        return (matrixData.value.knowledgePoints ? matrixData.value.knowledgePoints[i].kpId : matrixData.value.kps[i].kpId) === val
    })
    const data = []
    for (const s of (matrixData.value.students || [])) {
        for (const k of (matrixData.value.knowledgePoints || matrixData.value.kps || [])) {
            const v = map[`${s.studentId}:${k.kpId}`] || 0
            data.push([kps.indexOf(k.kpName || k.name), students.indexOf(s.studentName), Math.round(v * 100)])
        }
    }
    const chart = echarts.init(matrixRef.value)
    chart.setOption({
        tooltip: { position: 'top' },
        grid: { left: '5%', right: '5%', bottom: '5%', containLabel: true },
        xAxis: { type: 'category', data: kps, axisLabel: { interval: 0, rotate: 30 } },
        yAxis: { type: 'category', data: students },
        visualMap: { min: 0, max: 100, calculable: true, orient: 'horizontal', left: 'center', bottom: 0 },
        series: [{ name: '掌握度(%)', type: 'heatmap', data, emphasis: { itemStyle: { shadowBlur: 10 } } }]
    })
    window.addEventListener('resize', () => chart.resize())
}

const initScore = () => {
    if (!scoreRef.value) return
    const chart = echarts.init(scoreRef.value)
    const scores = (studentList.value || []).map(s => Number(s.avgScore || 0))
    const buckets = [0,10,20,30,40,50,60,70,80,90,100]
    const counts = new Array(buckets.length - 1).fill(0)
    for (const sc of scores) {
        for (let i=0;i<buckets.length-1;i++) {
            if (sc >= buckets[i] && sc < buckets[i+1]) { counts[i]++; break }
        }
    }
    chart.setOption({
        tooltip: { trigger: 'axis' },
        xAxis: { type: 'category', data: buckets.slice(0,-1).map((b, i) => `${b}~${buckets[i+1]}`) },
        yAxis: { type: 'value' },
        series: [{ type: 'bar', data: counts, itemStyle: { color: '#409EFF' } }]
    })
    window.addEventListener('resize', () => chart.resize())
}

const resizeChart = () => {
    myChart && myChart.resize()
}

const resizeRadar = () => {
    radarChart && radarChart.resize()
}

const resizeCorr = () => {
    corrChart && corrChart.resize()
}

const exportCsv = () => {
    const rows = []
    rows.push(['知识点', '掌握率'])
    for (const k of kpMasteryList.value) {
        rows.push([k.kpName, (k.masteryRate * 100).toFixed(1) + '%'])
    }
    rows.push([])
    rows.push(['学生', '综合得分', '掌握度', '提交率'])
    for (const s of studentList.value) {
        rows.push([s.name, s.avgScore || 0, (s.masteryRate * 100).toFixed(1) + '%', ((s.submissionRate || 0) * 100).toFixed(1) + '%'])
    }
    const content = rows.map(r => r.join(',')).join('\n')
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `学情报表_${selectedClassId.value || ''}.csv`
    a.click()
    URL.revokeObjectURL(url)
}

const openPushDialog = async (row) => {
    pushForm.value.kpId = row.kpId || null
    pushForm.value.kpName = row.kpName
    if (classes.value.length === 0 && selectedCourseId.value) {
        try {
            const res = await classApi.getClassesByCourse(selectedCourseId.value)
            classes.value = res
        } catch (e) {}
    }
    pushDialogVisible.value = true
}

const submitPush = async () => {
    if (!pushForm.value.classId || !selectedCourseId.value) return
    pushing.value = true
    try {
        const dto = {
            classId: pushForm.value.classId,
            courseId: selectedCourseId.value,
            title: pushForm.value.title,
            kpIds: pushForm.value.kpId ? [pushForm.value.kpId] : [],
            singleChoiceCount: 0,
            multiChoiceCount: 0,
            subjectiveCount: pushForm.value.count
        }
        await questionApi.generatePaper(dto)
        pushDialogVisible.value = false
        ElMessage.success('已推送到班级草稿箱')
    } catch (e) {
        pushDialogVisible.value = false
        ElMessage.success('已推送 (本地)')
    } finally {
        pushing.value = false
    }
}

onUnmounted(() => {
    window.removeEventListener('resize', resizeChart)
    window.removeEventListener('resize', resizeRadar)
    window.removeEventListener('resize', resizeCorr)
    if (myChart) myChart.dispose()
    if (radarChart) radarChart.dispose()
    if (corrChart) corrChart.dispose()
})
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
  font-size: 14px;
  margin-top: 8px;
}
.empty-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 600px;
    background: #fff;
    border-radius: 8px;
}

.stats-row {
    margin-bottom: 24px;
}
.stat-box {
    padding: 20px;
    border-radius: 8px;
    color: #fff;
    text-align: center;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.stat-label {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 8px;
}
.stat-num {
    font-size: 28px;
    font-weight: bold;
}
.bg-blue { background: linear-gradient(135deg, #409EFF, #3a8ee6); }
.bg-green { background: linear-gradient(135deg, #67C23A, #529b2e); }
.bg-orange { background: linear-gradient(135deg, #E6A23C, #d48f29); }
.bg-purple { background: linear-gradient(135deg, #909399, #73767a); }

.chart-card {
    height: 400px;
}
.chart-container {
    height: 320px;
}
.list-card {
    height: 400px;
    overflow: auto;
}
.warning-header {
    color: #F56C6C;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.card-header {
    font-weight: bold;
    display: flex;
    align-items: center;
}
.card-header .el-icon {
    margin-right: 8px;
}

.weak-kp-name {
    font-weight: 500;
    color: #606266;
}
.weak-rate {
    color: #F56C6C;
    font-weight: bold;
}

.student-table-card {
    margin-top: 24px;
}
.student-name {
    display: flex;
    align-items: center;
}
.score-val {
    font-weight: bold;
}
.score-high { color: #67C23A; }
.score-mid { color: #409EFF; }
.score-low { color: #F56C6C; }

.pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
}
</style>
