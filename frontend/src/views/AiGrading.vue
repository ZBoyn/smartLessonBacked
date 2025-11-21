<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>AI 智能批改</h2>
        <p class="subtitle">LLM 驱动的主观题自动评分与反馈生成</p>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="task-card">
          <template #header>
            <div class="card-header">
              <span>待批改提交 (24)</span>
              <el-button type="primary" link>全部批改</el-button>
            </div>
          </template>
          
          <div class="submission-list">
            <div v-for="i in 3" :key="i" class="submission-item">
              <div class="sub-info">
                <div class="student-name">张三</div>
                <div class="sub-time">提交于 2023-11-19 14:30</div>
              </div>
              <div class="sub-content">
                <p><strong>题目：</strong>请简述 Spring Boot 的核心特性。</p>
                <p class="answer"><strong>学生回答：</strong>Spring Boot 提供了自动配置功能，可以快速启动项目...</p>
              </div>
              <div class="ai-feedback">
                <el-tag type="warning" effect="dark">AI 分析中</el-tag>
                <p class="feedback-text">正在调用 LLM API 分析逻辑结构与知识点覆盖度...</p>
              </div>
              <div class="actions">
                <el-button size="small" type="primary">查看详情</el-button>
                <el-button size="small">人工复核</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>批改概况</span>
          </template>
          <div class="stats">
            <div class="stat-item">
              <div class="label">今日处理</div>
              <div class="value">128</div>
            </div>
            <div class="stat-item">
              <div class="label">AI 采纳率</div>
              <div class="value">85%</div>
            </div>
            <div class="stat-item">
              <div class="label">节省时间</div>
              <div class="value">4.5h</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-header {
  margin-bottom: 24px;
}
.subtitle {
  color: #909399;
  font-size: 14px;
  margin-top: 4px;
}

.submission-item {
  border-bottom: 1px solid #eee;
  padding: 16px 0;
}

.sub-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.student-name {
  font-weight: bold;
  color: #303133;
}

.sub-content {
  background: #f9fafc;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 12px;
  font-size: 14px;
}

.answer {
  color: #303133;
  margin-top: 8px;
}

.ai-feedback {
  background: #fdf6ec;
  padding: 12px;
  border-radius: 4px;
  border-left: 4px solid #e6a23c;
  margin-bottom: 12px;
}

.feedback-text {
  font-size: 13px;
  color: #e6a23c;
  margin-top: 8px;
}

.stats {
  display: flex;
  justify-content: space-between;
  text-align: center;
}

.stat-item .value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-top: 8px;
}
</style>

