<template>
  <div class="student-dashboard">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="welcome-card">
          <div class="welcome-content">
            <h3>今日学习任务</h3>
            <div class="task-list">
              <div class="task-item">
                <el-tag type="danger" size="small">考试</el-tag>
                <span class="task-title">Java 基础期中测试</span>
                <span class="task-time">截止: 今天 18:00</span>
                <el-button type="primary" link size="small">去完成</el-button>
              </div>
              <div class="task-item">
                <el-tag type="warning" size="small">作业</el-tag>
                <span class="task-title">Spring Boot 环境搭建实验报告</span>
                <span class="task-time">截止: 明天 23:59</span>
                <el-button type="primary" link size="small">提交</el-button>
              </div>
            </div>
          </div>
        </el-card>
        
        <el-card class="chart-card" header="近七日学习时长">
            <div style="height: 250px; display: flex; align-items: center; justify-content: center; color: #999;">
                图表区域 (对接 ECharts)
            </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card header="我的课程">
           <div class="mini-course-list">
               <div v-for="i in 3" :key="i" class="mini-course-item">
                   <el-avatar shape="square" :size="50" style="background: #e6f7ff; color: #1890ff">Java</el-avatar>
                   <div class="course-info">
                       <div class="course-name">Java 程序设计</div>
                       <el-progress :percentage="45 + i*10" :stroke-width="6" />
                   </div>
               </div>
           </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.welcome-card {
  margin-bottom: 20px;
}
.task-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.task-title {
  flex: 1;
  font-weight: 500;
}
.task-time {
  color: #909399;
  font-size: 12px;
}
.mini-course-item {
    display: flex;
    gap: 15px;
    margin-bottom: 15px;
}
.course-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
}
.course-name {
    font-weight: 600;
    margin-bottom: 6px;
}
</style>

