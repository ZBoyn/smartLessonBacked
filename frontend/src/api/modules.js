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
  getCourseById: (id) => request.get(`/courses/${id}`)
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
  getGraph: (courseId) => request.get(`/courses/${courseId}/knowledge-graph`),
  addNode: (data) => request.post('/teacher/knowledge-points', data),
  addRelation: (data) => request.post('/teacher/knowledge-relations', data)
}

// ==========================================
// QuestionController
// ==========================================
export const questionApi = {
  createQuestion: (data) => request.post('/teacher/questions', data),
  // 获取特定课程的题目
  getQuestionsByCourse: (courseId) => request.get(`/teacher/courses/${courseId}/questions`),
  generatePaper: (data) => request.post('/teacher/questions/generate', data)
}

// ==========================================
// AssessmentController
// ==========================================
export const assessmentApi = {
  createAssessment: (data) => request.post('/teacher/assessments', data),
  publishAssessment: (id) => request.post(`/teacher/assessments/${id}/publish`),
  getPendingAssessments: () => request.get('/student/assessments/pending')
}

// ==========================================
// SubmissionController
// ==========================================
export const submissionApi = {
  submitAssessment: (assessmentId, data) => request.post(`/student/assessments/${assessmentId}/submit`, data),
  getSubmissions: (assessmentId) => request.get(`/teacher/assessments/${assessmentId}/submissions`)
}
