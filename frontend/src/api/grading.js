import request from './request'

export default {
  // Get list of submissions for an assessment
  getSubmissions(assessmentId) {
    return request.get(`/teacher/assessments/${assessmentId}/submissions`)
  },

  // Teacher manually grades an answer
  manualGrade(answerId, data) {
    return request.put(`/teacher/answers/${answerId}/grade`, data)
  },

  // Trigger AI grading for an answer
  aiGrade(answerId) {
    return request.post(`/teacher/answers/${answerId}/ai-grade`)
  },

  // Get knowledge point analysis
  getKnowledgeAnalysis(assessmentId) {
    return request.get(`/teacher/assessments/${assessmentId}/analysis/knowledge-points`)
  }
}

