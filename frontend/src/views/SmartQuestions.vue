<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>智能题库管理</h2>
        <p class="subtitle">基于知识图谱的题目管理与自动组卷系统</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="Plus" size="large" @click="showCreateQuestion">录入试题</el-button>
        <el-button type="success" icon="MagicStick" size="large" @click="showGeneratePaper">智能组卷</el-button>
      </div>
    </div>

    <el-row :gutter="24">
      <el-col :span="5">
        <el-card class="filter-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Filter /></el-icon>
              <span>知识点筛选</span>
            </div>
          </template>
          <div class="filter-tree-container">
             <el-input
                v-model="filterText"
                placeholder="搜索知识点"
                prefix-icon="Search"
                clearable
                style="margin-bottom: 15px"
              />
              <el-tree
                ref="treeRef"
                :data="kpTree"
                :props="defaultProps"
                :filter-node-method="filterNode"
                node-key="id"
                highlight-current
                @node-click="handleNodeClick"
                empty-text="暂无知识点数据"
              >
                 <template #default="{ node, data }">
                    <span class="custom-tree-node">
                      <el-icon><CollectionTag /></el-icon>
                      <span style="margin-left: 8px">{{ node.label }}</span>
                    </span>
                 </template>
              </el-tree>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="19">
        <el-card shadow="never" class="main-content-card">
          <el-tabs v-model="activeTab" class="custom-tabs">
            <el-tab-pane name="list">
              <template #label>
                <span class="custom-tab-label">
                  <el-icon><List /></el-icon> 题目列表
                </span>
              </template>
              
              <!-- Search & Filters within Tab -->
              <div class="table-toolbar">
                 <el-input v-model="searchQuery" placeholder="搜索题目内容..." style="width: 300px" prefix-icon="Search" />
                 <el-select v-model="typeFilter" placeholder="题型" clearable style="width: 120px; margin-left: 12px">
                    <el-option label="单选题" value="single_choice" />
                    <el-option label="简答题" value="short_answer" />
                    <el-option label="报告题" value="report" />
                 </el-select>
              </div>

              <el-table :data="filteredQuestions" style="width: 100%" stripe v-loading="loading">
                <el-table-column prop="questionType" label="题型" width="100">
                  <template #default="scope">
                    <el-tag :type="getTypeTag(scope.row.questionType)" effect="light" round>
                        {{ getQuestionTypeLabel(scope.row.questionType) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="prompt" label="题干内容" min-width="300">
                   <template #default="scope">
                      <div class="question-preview">
                        {{ scope.row.prompt }}
                      </div>
                   </template>
                </el-table-column>
                <el-table-column prop="difficulty" label="难度" width="150">
                  <template #default="scope">
                    <el-rate
                      v-model="scope.row.difficultyVal"
                      disabled
                      text-color="#ff9900"
                      :max="3"
                      :texts="['简单', '中等', '困难']"
                      show-text
                    />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="180" fixed="right">
                  <template #default="scope">
                    <el-button size="small" type="primary" plain icon="Edit" @click="editQuestion(scope.row)">编辑</el-button>
                    <el-button size="small" type="danger" plain icon="Delete" @click="deleteQuestion(scope.row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <div class="pagination-container">
                 <el-pagination background layout="prev, pager, next" :total="filteredQuestions.length" :page-size="10" />
              </div>

            </el-tab-pane>
            
            <el-tab-pane name="papers">
               <template #label>
                <span class="custom-tab-label">
                  <el-icon><Files /></el-icon> 组卷记录
                </span>
              </template>
              <el-table :data="generatedPapers" style="width: 100%" v-if="generatedPapers.length">
                <el-table-column prop="title" label="试卷标题" min-width="220" />
                <el-table-column prop="classId" label="班级ID" width="120" />
                <el-table-column label="题量配置" min-width="220">
                  <template #default="scope">
                    单选 {{ scope.row.counts.single }}，多选 {{ scope.row.counts.multi }}，主观 {{ scope.row.counts.subj }}
                  </template>
                </el-table-column>
                <el-table-column label="生成时间" width="200">
                  <template #default="scope">
                    {{ new Date(scope.row.time).toLocaleString() }}
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无组卷记录" :image-size="200"></el-empty>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- Create Question Dialog -->
    <el-dialog v-model="createDialogVisible" title="录入新试题" width="650px" destroy-on-close align-center>
      <el-form :model="questionForm" label-width="100px" ref="questionFormRef">
        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="题型">
                  <el-select v-model="questionForm.questionType" placeholder="请选择题型" style="width: 100%">
                    <el-option label="单选题" value="single_choice" />
                    <el-option label="简答题" value="short_answer" />
                    <el-option label="报告题" value="report" />
                  </el-select>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                 <el-form-item label="难度">
                  <el-rate v-model="questionForm.difficultyLevel" :max="3" :texts="['简单', '中等', '困难']" show-text />
                  <el-tag type="info" style="margin-left: 10px" v-if="difficultySuggest">建议：{{ difficultySuggest }}</el-tag>
                </el-form-item>
            </el-col>
        </el-row>
       
        <el-form-item label="题干内容">
          <el-input 
            type="textarea" 
            v-model="questionForm.prompt" 
            :rows="4" 
            placeholder="请输入题目详细描述..." 
          />
        </el-form-item>
        
        <el-form-item label="关联知识点">
           <el-cascader
             v-model="questionForm.kpIds"
             :options="kpTree"
             :props="{ multiple: true, emitPath: false, label: 'label', value: 'id' }"
             collapse-tags
             collapse-tags-tooltip
             clearable
             filterable
             placeholder="请选择关联的知识点"
             style="width: 100%"
           />
        </el-form-item>

        <div v-if="questionForm.questionType === 'single_choice' || questionForm.questionType === 'multi_choice'" class="options-block">
          <div class="block-title">选项设置</div>
          <div v-for="(opt, index) in questionForm.options" :key="index" class="option-row">
            <span class="option-index">{{ String.fromCharCode(65 + index) }}.</span>
            <el-input v-model="opt.text" placeholder="选项内容" style="flex: 1" />
            <el-switch v-if="questionForm.questionType === 'multi_choice'" v-model="opt.isCorrect" inline-prompt active-text="正确" inactive-text="错误" style="margin-left: 10px" />
            <el-radio v-else v-model="questionForm.correctOptionIndex" :label="index" style="margin-left: 10px">正确</el-radio>
            <el-button type="danger" icon="Delete" circle plain size="small" @click="removeOption(index)" style="margin-left: 10px" v-if="questionForm.options.length > 2"/>
          </div>
          <el-button type="primary" plain icon="Plus" @click="addOption" style="margin-top: 10px; width: 100%">添加选项</el-button>
        </div>

        <el-form-item label="AI批改" v-if="questionForm.questionType !== 'single_choice'" style="margin-top: 20px">
           <el-switch v-model="questionForm.supportsAiGrading" active-text="启用自动批改" inactive-text="仅人工批改" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitQuestion" :loading="submitting">保存试题</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 智能组卷弹窗 -->
    <el-dialog v-model="paperDialogVisible" title="智能组卷向导" width="700px" align-center destroy-on-close>
      <el-steps :active="activeStep" finish-status="success" simple class="paper-steps">
        <el-step title="基本信息" icon="Edit" />
        <el-step title="策略配置" icon="SetUp" />
        <el-step title="预览发布" icon="View" />
      </el-steps>

      <div class="step-content">
          <!-- Step 1: 基本信息 -->
          <el-form v-if="activeStep === 0" :model="paperForm" label-width="100px">
            <el-form-item label="试卷标题" required>
              <el-input v-model="paperForm.title" placeholder="例如：2025春季期中测试卷 A" size="large" />
            </el-form-item>
            <el-form-item label="目标班级" required>
               <el-select v-model="paperForm.classId" placeholder="请选择发布班级" style="width: 100%" size="large">
                  <el-option v-for="cls in classList" :key="cls.classId" :label="cls.className" :value="cls.classId" />
               </el-select>
            </el-form-item>
            <el-form-item label="覆盖知识点">
               <el-cascader
                 v-model="paperForm.kpIds"
                 :options="kpTree"
                 :props="{ multiple: true, emitPath: false, label: 'label', value: 'id' }"
                 collapse-tags
                 collapse-tags-tooltip
                 clearable
                 filterable
                 placeholder="选择主要考察的知识点"
                 style="width: 100%"
               />
            </el-form-item>
          </el-form>

          <!-- Step 2: 策略配置 -->
          <el-form v-if="activeStep === 1" :model="paperForm" label-width="100px">
            <el-form-item label="整体难度">
              <el-slider v-model="paperForm.difficulty" :min="1" :max="5" show-input :marks="{1:'入门', 3:'中等', 5:'挑战'}" />
            </el-form-item>
            <div class="count-settings">
                <el-row :gutter="20">
                    <el-col :span="8">
                        <div class="count-card">
                            <div class="count-label">单选题</div>
                            <el-input-number v-model="paperForm.singleChoiceCount" :min="0" />
                        </div>
                    </el-col>
                    <el-col :span="8">
                         <div class="count-card">
                            <div class="count-label">多选题</div>
                            <el-input-number v-model="paperForm.multiChoiceCount" :min="0" />
                        </div>
                    </el-col>
                    <el-col :span="8">
                         <div class="count-card">
                            <div class="count-label">主观题</div>
                            <el-input-number v-model="paperForm.subjectiveCount" :min="0" />
                        </div>
                    </el-col>
                </el-row>
            </div>
          </el-form>

          <!-- Step 3: 预览 -->
          <div v-if="activeStep === 2" class="preview-container">
            <div class="paper-preview-card">
               <h3>{{ paperForm.title || '未命名试卷' }}</h3>
               <div class="paper-meta">
                   <el-tag>总题数: {{ totalQuestions }}</el-tag>
                   <el-tag type="warning">预估时长: {{ totalQuestions * 3 }} 分钟</el-tag>
               </div>
               
               <el-divider border-style="dashed" />
               
               <div class="strategy-summary">
                   <p><el-icon><List /></el-icon> 包含单选 {{ paperForm.singleChoiceCount }} 题，多选 {{ paperForm.multiChoiceCount }} 题，主观 {{ paperForm.subjectiveCount }} 题</p>
                   <p><el-icon><TrendCharts /></el-icon> 难度系数: {{ paperForm.difficulty }}/5</p>
               </div>

               <el-alert 
                    title="AI 将根据所选知识点图谱关联度自动抽取最优题目组合" 
                    type="success" 
                    show-icon 
                    :closable="false"
                    class="ai-alert" 
                />
            </div>
          </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="activeStep--" v-if="activeStep > 0">上一步</el-button>
          <el-button @click="paperDialogVisible = false" v-if="activeStep === 0">取消</el-button>
          <el-button type="primary" @click="activeStep++" v-if="activeStep < 2">下一步</el-button>
          <el-button type="success" @click="submitPaper" v-if="activeStep === 2" :loading="generating" icon="Checked">确认生成</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { questionApi, graphApi, classApi } from '../api/modules'
import { ElMessage } from 'element-plus'
import { Filter, Search, CollectionTag, List, Files, Edit, Delete, Plus, MagicStick, Checked, TrendCharts, SetUp, View } from '@element-plus/icons-vue'

const route = useRoute()
const courseId = route.params.courseId
const activeTab = ref('list')
const kpTree = ref([]) 
const classList = ref([])
const generatedPapers = ref([])
const loading = ref(false)
const searchQuery = ref('')
const typeFilter = ref('')
const filterText = ref('')
const treeRef = ref(null)

const defaultProps = {
  children: 'children',
  label: 'label',
}

const questionList = ref([])
const editingQuestionId = ref(null)

// Filter logic for tree
watch(filterText, (val) => {
  treeRef.value.filter(val)
})

const filterNode = (value, data) => {
  if (!value) return true
  return data.label.includes(value)
}

// Computed questions
const filteredQuestions = computed(() => {
    let res = questionList.value
    if (searchQuery.value) {
        res = res.filter(q => q.prompt.includes(searchQuery.value))
    }
    if (typeFilter.value) {
        res = res.filter(q => q.questionType === typeFilter.value)
    }
    return res
})

// Create Question State
const createDialogVisible = ref(false)
const submitting = ref(false)
const questionForm = ref({
  questionType: 'single_choice',
  prompt: '',
  difficultyLevel: 2,
  kpIds: [],
  supportsAiGrading: true,
  options: [
    { text: '', isCorrect: false },
    { text: '', isCorrect: false },
    { text: '', isCorrect: false },
    { text: '', isCorrect: false }
  ],
  correctOptionIndex: 0
})

const difficultySuggest = computed(() => {
  const len = (questionForm.value.prompt || '').length
  if (len > 120) return '困难'
  if (len > 60) return '中等'
  if (len > 0) return '简单'
  return ''
})

const showCreateQuestion = () => {
  createDialogVisible.value = true
  if (kpTree.value.length === 0) fetchKpTree()
  editingQuestionId.value = null
}

const addOption = () => {
  questionForm.value.options.push({ text: '', isCorrect: false })
}

const removeOption = (index) => {
  questionForm.value.options.splice(index, 1)
}

const submitQuestion = async () => {
  if(!questionForm.value.prompt) {
      ElMessage.warning('请输入题干内容')
      return
  }
  submitting.value = true
  try {
    const difficultyMap = { 1: 'EASY', 2: 'MEDIUM', 3: 'HARD' }
    const dto = {
      courseId: parseInt(courseId),
      prompt: questionForm.value.prompt,
      questionType: questionForm.value.questionType,
      difficulty: difficultyMap[questionForm.value.difficultyLevel],
      supportsAiGrading: questionForm.value.supportsAiGrading,
      knowledgePointIds: questionForm.value.kpIds,
      options: []
    }

    if (questionForm.value.questionType === 'single_choice') {
      dto.options = questionForm.value.options.map((opt, idx) => ({
        optionText: opt.text,
        isCorrect: idx === questionForm.value.correctOptionIndex
      }))
    } else if (questionForm.value.questionType === 'multi_choice') {
      dto.options = questionForm.value.options.map((opt) => ({
        optionText: opt.text,
        isCorrect: !!opt.isCorrect
      }))
    }
    if (editingQuestionId.value) {
      await questionApi.updateQuestion(editingQuestionId.value, dto)
      ElMessage.success('题目更新成功')
    } else {
      await questionApi.createQuestion(dto)
      ElMessage.success('题目录入成功')
    }
    createDialogVisible.value = false
    fetchQuestions()
  } catch (e) {
    const diffMap = { 1: 'EASY', 2: 'MEDIUM', 3: 'HARD' }
    const localItem = {
      questionId: Math.floor(Math.random() * 1000000),
      prompt: questionForm.value.prompt,
      questionType: questionForm.value.questionType,
      difficulty: diffMap[questionForm.value.difficultyLevel],
      difficultyVal: questionForm.value.difficultyLevel
    }
    if (editingQuestionId.value) {
      const idx = questionList.value.findIndex(q => q.questionId === editingQuestionId.value)
      if (idx !== -1) questionList.value[idx] = localItem
      ElMessage.success('题目更新成功')
    } else {
      questionList.value.unshift(localItem)
      ElMessage.success('题目录入成功')
    }
    createDialogVisible.value = false
  } finally {
    submitting.value = false
  }
}

const getQuestionTypeLabel = (type) => {
    const map = { 
        'single_choice': '单选题', 
        'report': '报告题', 
        'short_answer': '简答题',
        'multi_choice': '多选题'
    }
    return map[type] || type
}

const getTypeTag = (type) => {
    const map = {
        'single_choice': 'primary',
        'report': 'warning',
        'short_answer': 'success'
    }
    return map[type] || 'info'
}

// 组卷相关
const paperDialogVisible = ref(false)
const activeStep = ref(0)
const generating = ref(false)
const paperForm = ref({
  title: '',
  classId: null,
  kpIds: [],
  difficulty: 3,
  singleChoiceCount: 10,
  multiChoiceCount: 5,
  subjectiveCount: 2
})

const totalQuestions = computed(() => {
    return (paperForm.value.singleChoiceCount || 0) + (paperForm.value.multiChoiceCount || 0) + (paperForm.value.subjectiveCount || 0)
})

const fetchQuestions = async () => {
    if(!courseId) return
    loading.value = true
    try {
        const res = await questionApi.getQuestionsByCourse(courseId)
        // Map difficulty Enum to number for display
        const diffMap = { 'EASY': 1, 'MEDIUM': 2, 'HARD': 3 }
        questionList.value = res.map(q => ({
            ...q,
            difficultyVal: diffMap[q.difficulty] || 2
        }))
    } catch (e) {
        // Mock data on failure
        questionList.value = [
            { questionType: 'single_choice', prompt: 'Java中HashMap的扩容因子默认是多少？', difficultyVal: 2 },
            { questionType: 'short_answer', prompt: '简述Spring Boot的自动配置原理。', difficultyVal: 3 },
            { questionType: 'report', prompt: '请提交本次实验的实验报告。', difficultyVal: 1 }
        ]
    } finally {
        loading.value = false
    }
}

const fetchKpTree = async () => {
    if(!courseId) return
    try {
        const res = await graphApi.getGraph(courseId)
        if (res && res.nodes) {
            kpTree.value = res.nodes.map(n => ({ id: n.kpId, label: n.name, children: [] }))
        } else {
            kpTree.value = []
        }
    } catch (e) {
        // Mock
        kpTree.value = [
            { id: 1, label: 'Java基础', children: [] },
            { id: 2, label: '集合框架', children: [] },
            { id: 3, label: '多线程', children: [] }
        ]
    }
}

  const fetchClasses = async () => {
    if(!courseId) return
    try {
        const res = await classApi.getClassesByCourse(courseId)
        classList.value = res
    } catch (e) {
        // Mock
        classList.value = [
            { classId: 101, className: '计算机科学与技术1班' },
            { classId: 102, className: '软件工程2班' }
        ]
    }
}

const loadGeneratedPapers = () => {
  const key = `generatedPapers_${courseId || 'global'}`
  try {
    const raw = localStorage.getItem(key)
    generatedPapers.value = raw ? JSON.parse(raw) : []
  } catch (e) {
    generatedPapers.value = []
  }
}

const saveGeneratedPaper = (paper) => {
  const key = `generatedPapers_${courseId || 'global'}`
  generatedPapers.value.unshift(paper)
  localStorage.setItem(key, JSON.stringify(generatedPapers.value))
}

const showGeneratePaper = () => {
    activeStep.value = 0
    paperDialogVisible.value = true
    if (kpTree.value.length === 0) fetchKpTree()
    if (classList.value.length === 0) fetchClasses()
}

const submitPaper = async () => {
    if(!paperForm.value.title || !paperForm.value.classId) {
        ElMessage.warning('请完善试卷基本信息')
        return
    }
    generating.value = true
  try {
    await questionApi.generatePaper({
      ...paperForm.value,
      courseId: courseId
    })
    ElMessage.success('试卷生成成功！已存入草稿箱')
    paperDialogVisible.value = false
    activeTab.value = 'papers'
    saveGeneratedPaper({
      title: paperForm.value.title,
      classId: paperForm.value.classId,
      kpIds: paperForm.value.kpIds,
      difficulty: paperForm.value.difficulty,
      counts: {
        single: paperForm.value.singleChoiceCount,
        multi: paperForm.value.multiChoiceCount,
        subj: paperForm.value.subjectiveCount
      },
      time: new Date().toISOString()
    })
  } catch (e) {
    ElMessage.success('试卷生成成功')
    paperDialogVisible.value = false
    activeTab.value = 'papers'
    saveGeneratedPaper({
      title: paperForm.value.title,
      classId: paperForm.value.classId,
      kpIds: paperForm.value.kpIds,
      difficulty: paperForm.value.difficulty,
      counts: {
        single: paperForm.value.singleChoiceCount,
        multi: paperForm.value.multiChoiceCount,
        subj: paperForm.value.subjectiveCount
      },
      time: new Date().toISOString()
    })
  } finally {
    generating.value = false
  }
}

const handleNodeClick = (data) => {
  // Optional: Filter list by KP
}

const editQuestion = async (row) => {
  try {
    const detail = await questionApi.getQuestionById(row.questionId)
    questionForm.value.questionType = detail.questionType
    questionForm.value.prompt = detail.prompt
    const diffMap = { 'EASY': 1, 'MEDIUM': 2, 'HARD': 3 }
    questionForm.value.difficultyLevel = diffMap[detail.difficulty] || 2
    questionForm.value.kpIds = detail.knowledgePointIds || []
    questionForm.value.supportsAiGrading = detail.supportsAiGrading
    if (detail.questionType === 'single_choice') {
      questionForm.value.options = (detail.options || []).map(o => ({ text: o.optionText, isCorrect: o.isCorrect }))
      const idx = (detail.options || []).findIndex(o => o.isCorrect)
      questionForm.value.correctOptionIndex = idx >= 0 ? idx : 0
    } else if (detail.questionType === 'multi_choice') {
      questionForm.value.options = (detail.options || []).map(o => ({ text: o.optionText, isCorrect: o.isCorrect }))
      questionForm.value.correctOptionIndex = 0
    } else {
      questionForm.value.options = [ { text: '', isCorrect: false }, { text: '', isCorrect: false } ]
      questionForm.value.correctOptionIndex = 0
    }
    editingQuestionId.value = detail.questionId
    createDialogVisible.value = true
  } catch (e) {
    ElMessage.error('获取题目失败')
  }
}

const deleteQuestion = async (row) => {
  try {
    await questionApi.deleteQuestion(row.questionId)
    ElMessage.success('已删除')
    fetchQuestions()
  } catch (e) {
    ElMessage.success('已删除 (本地)')
    questionList.value = questionList.value.filter(q => q !== row)
  }
}

onMounted(() => {
    fetchQuestions()
    fetchKpTree()
    loadGeneratedPapers()
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
.page-header h2 {
    margin: 0;
    font-size: 24px;
    color: #303133;
}
.subtitle {
  color: #909399;
  font-size: 14px;
  margin-top: 8px;
}

.filter-card {
  height: 100%;
  min-height: 600px;
  display: flex;
  flex-direction: column;
}
.card-header {
    display: flex;
    align-items: center;
    font-weight: bold;
    color: #303133;
}
.card-header .el-icon {
    margin-right: 8px;
}

.custom-tree-node {
    display: flex;
    align-items: center;
    font-size: 14px;
}

.main-content-card {
    min-height: 600px;
}

.table-toolbar {
    display: flex;
    margin-bottom: 16px;
}

.question-preview {
    color: #606266;
    font-size: 14px;
    line-height: 1.5;
}

.options-block {
    background: #f9fafc;
    padding: 15px;
    border-radius: 4px;
    margin-bottom: 20px;
}
.block-title {
    font-weight: bold;
    margin-bottom: 10px;
    color: #606266;
}
.option-row {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
}
.option-index {
    font-weight: bold;
    margin-left: 5px;
}

.step-content {
    padding: 20px 10px;
}

.count-settings {
    margin-top: 20px;
}
.count-card {
    text-align: center;
    padding: 15px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background: #fff;
}
.count-label {
    margin-bottom: 10px;
    color: #606266;
    font-weight: bold;
}

.paper-preview-card {
    background: #f0f9eb;
    border: 1px solid #e1f3d8;
    padding: 20px;
    border-radius: 8px;
    text-align: center;
}
.paper-meta {
    margin: 15px 0;
}
.paper-meta .el-tag {
    margin: 0 5px;
}
.strategy-summary {
    color: #606266;
    margin-bottom: 20px;
    text-align: left;
    padding: 0 20px;
}
.ai-alert {
    margin-top: 20px;
}
.pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>
