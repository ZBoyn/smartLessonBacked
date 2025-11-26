import request from './request'

// ==========================================
// AuthController
// ==========================================
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data)
}

// ==========================================
// CourseController
// ==========================================
export const courseApi = {
  createCourse: (data) => request.post('/teacher/courses', data),
  getAllCourses: () => request.get('/teacher/courses'),
  getMyCourses: () => request.get('/student/courses'), 
  getCourseById: (id) => request.get(`/teacher/courses/${id}`)
}

// ==========================================
// ClassController
// ==========================================
export const classApi = {
  // 班级是依附于课程的
  createClass: (courseId, data) => request.post(`/teacher/courses/${courseId}/classes`, data),
  getClassesByCourse: (courseId) => request.get(`/teacher/courses/${courseId}/classes`)
}

// ==========================================
// ResourceController (新增)
// ==========================================
export const resourceApi = {
  // 上传资源 (需要 FormData)
  uploadResource: (courseId, formData) => request.post(`/teacher/courses/${courseId}/resources`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }),
  // 获取课程下的资源列表
  getResources: (courseId) => request.get(`/teacher/courses/${courseId}/resources`),
  // 删除资源
  deleteResource: (resourceId) => request.delete(`/teacher/resources/${resourceId}`),
  // 获取资源流/下载链接 (通常直接用 window.open 或 src)
  getDownloadUrl: (resourceId) => `/api/v1/resources/${resourceId}/stream`
}

// ==========================================
// KnowledgeGraphController
// ==========================================
export const graphApi = {
  // 获取特定课程的图谱
  getGraph: (courseId) => request.get(`/teacher/courses/${courseId}/graph`),
  addNode: (data) => request.post(`/teacher/courses/${data.courseId}/knowledge-points`, data),
  addRelation: (data) => request.post(`/teacher/courses/${data.courseId}/knowledge-relations`, data)
}

// ==========================================
// QuestionController
// ==========================================
export const questionApi = {
  createQuestion: (data) => request.post(`/teacher/courses/${data.courseId}/questions`, data),
  // 获取特定课程的题目
  getQuestionsByCourse: (courseId) => request.get(`/teacher/courses/${courseId}/questions`),
  getQuestionById: (questionId) => request.get(`/teacher/questions/${questionId}`),
  updateQuestion: (questionId, data) => request.put(`/teacher/questions/${questionId}`, data),
  deleteQuestion: (questionId) => request.delete(`/teacher/questions/${questionId}`),
  generatePaper: (data) => request.post('/teacher/questions/generate', data)
}

// ==========================================
// AssessmentController
// ==========================================
export const assessmentApi = {
  // Create assessment for a specific class
  createAssessment: (classId, data) => request.post(`/teacher/classes/${classId}/assessments`, data),
  // Publish assessment
  publishAssessment: (id, data) => request.post(`/teacher/assessments/${id}/publish`, data),
  // Get pending assessments for student
  getPendingAssessments: () => request.get('/student/assessments/pending'),
  // Get assessments by class (if needed)
  getAssessmentsByClass: (classId) => request.get(`/teacher/classes/${classId}/assessments`) 
}

// ==========================================
// SubmissionController (Student)
// ==========================================
export const submissionApi = {
  submitAssessment: (assessmentId, data) => request.post(`/student/assessments/${assessmentId}/submit`, data),
  // This seems to be for teacher getting submissions in original code, but logic moved to GradeController
  // getSubmissions: (assessmentId) => request.get(`/teacher/assessments/${assessmentId}/submissions`) 
}

// ==========================================
// GradeController (Teacher Grading)
// ==========================================
export const gradingApi = {
  // Get submissions for assessment
  getSubmissions: (assessmentId) => request.get(`/teacher/assessments/${assessmentId}/submissions`),
  // AI Grade specific answer
  aiGrade: (answerId) => request.post(`/teacher/answers/${answerId}/ai-grade`),
  // Manual grade specific answer
  manualGrade: (answerId, data) => request.put(`/teacher/answers/${answerId}/grade`, data),
  // Analysis per assessment
  getKnowledgeAnalysis: (assessmentId) => request.get(`/teacher/assessments/${assessmentId}/analysis/knowledge-points`),
  getAiFeedbackSummary: (assessmentId) => request.get(`/teacher/assessments/${assessmentId}/ai-feedback/summary`)
}

// ==========================================
// AnalysisController (Overall Analysis)
// ==========================================
export const analysisApi = {
  getClassMastery: (classId) => request.get(`/teacher/analysis/classes/${classId}/knowledge-mastery`),
  getStudentAnalysis: (classId) => request.get(`/teacher/analysis/classes/${classId}/students`),
  getKnowledgeRadar: (classId) => request.get(`/teacher/analysis/classes/${classId}/radar`),
  getKnowledgeScoreMatrix: (classId) => request.get(`/teacher/analysis/classes/${classId}/knowledge-scores`),
  getVideoLearningStats: (classId) => request.get(`/teacher/analysis/classes/${classId}/video-stats`),
  getLearningCorrelation: (classId) => request.get(`/teacher/analysis/classes/${classId}/behavior-correlation`)
}
