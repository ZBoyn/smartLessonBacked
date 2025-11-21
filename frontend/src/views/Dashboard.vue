<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>我的课程</span>
            </div>
          </template>
          <div class="stat-value">12</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>我的班级</span>
            </div>
          </template>
          <div class="stat-value">5</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>待批改作业</span>
            </div>
          </template>
          <div class="stat-value important">24</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="welcome-card" style="margin-top: 20px;">
      <h2>欢迎回到 Smart Lesson 智慧教学系统</h2>
      <p>这里是您的教学仪表盘，您可以在这里快速查看教学概况。</p>
    </el-card>
  </div>
</template>

<style scoped>
.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
  padding: 10px 0;
}

.stat-value.important {
  color: #F56C6C;
}
</style>

