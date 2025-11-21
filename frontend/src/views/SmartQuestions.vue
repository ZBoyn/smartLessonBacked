<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>智能题库</h2>
        <p class="subtitle">基于知识图谱的题目管理与自动组卷</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" icon="Plus">录入试题</el-button>
        <el-button type="success" icon="MagicStick" @click="showGeneratePaper">智能组卷</el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="filter-card">
          <template #header>
            <div class="card-header">
              <span>知识点筛选</span>
            </div>
          </template>
          <el-tree
            :data="kpTree"
            :props="defaultProps"
            @node-click="handleNodeClick"
          />
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="题目列表" name="list">
              <el-table :data="questionList" style="width: 100%">
                <el-table-column prop="type" label="题型" width="100">
                  <template #default="scope">
                    <el-tag size="small">{{ scope.row.type }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="content" label="题干内容" show-overflow-tooltip />
                <el-table-column prop="difficulty" label="难度" width="100">
                  <template #default="scope">
                    <el-rate
                      v-model="scope.row.difficulty"
                      disabled
                      text-color="#ff9900"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="150">
                  <template #default>
                    <el-button size="small" text type="primary">编辑</el-button>
                    <el-button size="small" text type="danger">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="组卷记录" name="papers">
              <el-empty description="暂无组卷记录"></el-empty>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 智能组卷弹窗 -->
    <el-dialog v-model="paperDialogVisible" title="智能组卷向导" width="600px">
      <el-steps :active="activeStep" finish-status="success" simple style="margin-bottom: 20px">
        <el-step title="基本信息" />
        <el-step title="策略配置" />
        <el-step title="预览发布" />
      </el-steps>

      <!-- Step 1: 基本信息 -->
      <el-form v-if="activeStep === 0" :model="paperForm" label-width="100px">
        <el-form-item label="试卷标题">
          <el-input v-model="paperForm.title" placeholder="例如：期中测试卷 A" />
        </el-form-item>
        <el-form-item label="覆盖知识点">
           <el-cascader
             v-model="paperForm.kpIds"
             :options="kpTree"
             :props="{ multiple: true, emitPath: false, label: 'label', value: 'id' }"
             collapse-tags
             clearable
             style="width: 100%"
           />
        </el-form-item>
      </el-form>

      <!-- Step 2: 策略配置 -->
      <el-form v-if="activeStep === 1" :model="paperForm" label-width="100px">
        <el-form-item label="难度偏好">
          <el-slider v-model="paperForm.difficulty" :min="1" :max="5" show-input />
        </el-form-item>
        <el-form-item label="单选题数">
          <el-input-number v-model="paperForm.singleChoiceCount" :min="0" />
        </el-form-item>
        <el-form-item label="多选题数">
          <el-input-number v-model="paperForm.multiChoiceCount" :min="0" />
        </el-form-item>
        <el-form-item label="简答题数">
          <el-input-number v-model="paperForm.subjectiveCount" :min="0" />
        </el-form-item>
      </el-form>

      <!-- Step 3: 预览 -->
      <div v-if="activeStep === 2">
        <div class="preview-box">
           <p><strong>标题:</strong> {{ paperForm.title }}</p>
           <p><strong>总题数:</strong> {{ totalQuestions }} 题</p>
           <p><strong>预估时长:</strong> {{ totalQuestions * 2 }} 分钟</p>
           <el-alert title="AI 将根据所选知识点和难度自动抽取题目" type="info" show-icon :closable="false" />
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="activeStep--" v-if="activeStep > 0">上一步</el-button>
          <el-button @click="paperDialogVisible = false" v-if="activeStep === 0">取消</el-button>
          <el-button type="primary" @click="activeStep++" v-if="activeStep < 2">下一步</el-button>
          <el-button type="success" @click="submitPaper" v-if="activeStep === 2" :loading="generating">生成试卷</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { questionApi, graphApi } from '../../api/modules'
import { ElMessage } from 'element-plus'

const route = useRoute()
const courseId = route.params.courseId
const activeTab = ref('list')
const kpTree = ref([]) 
const defaultProps = {
  children: 'children',
  label: 'label',
}

const questionList = ref([])

// 组卷相关
const paperDialogVisible = ref(false)
const activeStep = ref(0)
const generating = ref(false)
const paperForm = ref({
  title: '',
  kpIds: [],
  difficulty: 3,
  singleChoiceCount: 10,
  multiChoiceCount: 5,
  subjectiveCount: 2
})

const totalQuestions = computed(() => {
    return paperForm.value.singleChoiceCount + paperForm.value.multiChoiceCount + paperForm.value.subjectiveCount
})

const fetchQuestions = async () => {
    if(!courseId) return
    try {
        const res = await questionApi.getQuestionsByCourse(courseId)
        questionList.value = res
    } catch (e) {
        questionList.value = []
    }
}

const fetchKpTree = async () => {
    if(!courseId) return
    try {
        // 简单将图谱节点转换为树形结构供筛选
        // 实际可能需要后端提供 dedicated tree API
        const res = await graphApi.getGraph(courseId)
        if (res && res.nodes) {
            kpTree.value = res.nodes.map(n => ({ id: n.id, label: n.label, children: [] }))
        }
    } catch (e) {
        console.error(e)
    }
}

const showGeneratePaper = () => {
    activeStep.value = 0
    paperDialogVisible.value = true
    if (kpTree.value.length === 0) fetchKpTree()
}

const submitPaper = async () => {
    generating.value = true
    try {
        await questionApi.generatePaper({
            ...paperForm.value,
            courseId: courseId
        })
        ElMessage.success('试卷生成成功！已存入草稿箱')
        paperDialogVisible.value = false
        activeTab.value = 'papers'
    } catch (e) {
        ElMessage.error('生成失败')
    } finally {
        generating.value = false
    }
}

const handleNodeClick = (data) => {
  console.log('Filter by KP:', data)
}

onMounted(() => {
    fetchQuestions()
    fetchKpTree()
})
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

.filter-card {
  min-height: 500px;
}

.preview-box {
    padding: 20px;
    background: #f5f7fa;
    border-radius: 4px;
}
.preview-box p {
    margin: 10px 0;
}
</style>
