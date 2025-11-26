<template>
  <div class="exam-page">
    <el-card class="paper-card" v-loading="loading">
      <template #header>
        <div class="paper-header">
          <div class="title-section">
            <h2>{{ assessment.title }}</h2>
            <p class="timer" v-if="assessment.endTime">
                <el-icon><Timer /></el-icon> 截止时间: {{ formatTime(assessment.endTime) }}
            </p>
          </div>
          <el-button type="primary" @click="submitPaper">提交试卷</el-button>
        </div>
      </template>

      <div class="questions-container" v-if="assessment.questions">
        <div 
          v-for="(question, index) in assessment.questions" 
          :key="question.questionId" 
          class="question-item"
        >
          <div class="q-title">
            <span class="q-index">{{ index + 1 }}.</span>
            <span class="q-type">[{{ formatType(question.questionType) }}]</span>
            <span class="q-prompt">{{ question.prompt }}</span>
          </div>

          <!-- Single Choice -->
          <div v-if="question.questionType === 'single_choice'" class="q-body">
            <el-radio-group v-model="answers[question.questionId].value">
              <el-radio 
                v-for="opt in question.options" 
                :key="opt.optionId" 
                :label="opt.optionId"
                class="option-item"
              >
                {{ opt.optionText }}
              </el-radio>
            </el-radio-group>
          </div>

          <!-- Report / Short Answer -->
          <div v-else class="q-body">
            <el-input
              v-model="answers[question.questionId].value"
              type="textarea"
              :rows="6"
              placeholder="请输入您的答案..."
            />
            <!-- File Upload for Report -->
             <div class="upload-section" v-if="question.questionType === 'report'">
                <el-upload
                    class="upload-demo"
                    action="#"
                    :http-request="(opts) => handleUpload(opts, question.questionId)"
                    :limit="1"
                    :on-remove="() => handleRemoveFile(question.questionId)"
                >
                    <el-button type="primary" link>上传附件 (PDF/Word)</el-button>
                </el-upload>
             </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const route = useRoute()
const router = useRouter()
const assessmentId = route.params.assessmentId

const loading = ref(false)
const assessment = ref({})
const answers = reactive({})

onMounted(async () => {
    if (!assessmentId) return
    loading.value = true
    try {
        const res = await request.get(`/student/assessments/${assessmentId}`)
        assessment.value = res
        
        // Initialize answers structure
        res.questions.forEach(q => {
            answers[q.questionId] = {
                type: q.questionType,
                value: null, // optionId or text
                file: null
            }
        })
    } catch (error) {
        console.error(error)
        ElMessage.error('获取试卷失败')
    } finally {
        loading.value = false
    }
})

const formatTime = (t) => {
    return t ? new Date(t).toLocaleString() : '无限制'
}

const formatType = (type) => {
    const map = {
        'single_choice': '单选题',
        'report': '简答/报告',
        'short_answer': '简答题'
    }
    return map[type] || type
}

const handleUpload = async (options, questionId) => {
    const { file } = options
    const formData = new FormData()
    formData.append('file', file)
    
    try {
        // First save answer record to get ID (if backend requires) or just upload to question endpoint
        // Backend endpoint: /student/assessments/{assessmentId}/answers/{questionId}/upload
        const res = await request.post(
            `/student/assessments/${assessmentId}/answers/${questionId}/upload`, 
            formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        )
        answers[questionId].file = res.url || res.filePath
        ElMessage.success('上传成功')
    } catch (error) {
        console.error(error)
        ElMessage.error('上传失败')
    }
}

const handleRemoveFile = (questionId) => {
    answers[questionId].file = null
}

const submitPaper = async () => {
    try {
        await ElMessageBox.confirm('确定要提交试卷吗？提交后无法修改。', '提示', {
            confirmButtonText: '提交',
            cancelButtonText: '取消',
            type: 'warning'
        })
        
        const submissionData = {
            answers: Object.keys(answers).map(qid => ({
                questionId: parseInt(qid),
                answerText: typeof answers[qid].value === 'string' ? answers[qid].value : null,
                optionId: typeof answers[qid].value === 'number' ? answers[qid].value : null,
                // File path should be handled by backend association or we send it if we got it from upload
                // Current backend flow might need text/optionId here.
            }))
        }

        // Save answers
        await request.post(`/student/assessments/${assessmentId}/answers`, submissionData)
        
        // Finalize
        await request.post(`/student/assessments/${assessmentId}/submit`)
        
        ElMessage.success('提交成功！')
        router.push('/student/assessments')
        
    } catch (e) {
        if (e !== 'cancel') {
            console.error(e)
            ElMessage.error('提交失败')
        }
    }
}
</script>

<style scoped>
.exam-page {
    max-width: 1000px;
    margin: 0 auto;
    padding: 20px;
}
.paper-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.timer {
    color: #f56c6c;
    font-weight: bold;
    margin-top: 5px;
    display: flex;
    align-items: center;
    gap: 5px;
}
.question-item {
    margin-bottom: 30px;
    padding-bottom: 20px;
    border-bottom: 1px dashed #eee;
}
.q-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 15px;
    line-height: 1.5;
}
.q-index {
    font-weight: bold;
    margin-right: 5px;
}
.q-type {
    color: #409EFF;
    margin-right: 10px;
}
.option-item {
    display: block;
    margin: 10px 0;
}
.upload-section {
    margin-top: 10px;
}
</style>

