<template>
  <div class="login-wrapper">
    <div class="login-content">
      <div class="login-left">
        <div class="login-info">
          <h1 class="app-title">Smart Lesson</h1>
          <p class="app-desc">智能评估与学情分析引擎</p>
          <ul class="feature-list">
            <li>
              <el-icon><DataAnalysis /></el-icon> 知识图谱深度分析
            </li>
            <li>
              <el-icon><EditPen /></el-icon> AI 驱动智能批改
            </li>
            <li>
              <el-icon><Collection /></el-icon> 智能化题库组卷
            </li>
          </ul>
        </div>
        <div class="login-bg-circle"></div>
        <div class="login-bg-circle-2"></div>
      </div>
      
      <div class="login-right">
        <div class="form-container">
          <h2 class="form-title">欢迎登录</h2>
          <p class="form-subtitle">请使用您的教职工/学生账号访问</p>
          
          <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form" size="large">
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="用户名 / 邮箱" 
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="密码" 
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            
            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <el-link type="primary" :underline="false">忘记密码？</el-link>
            </div>

            <el-form-item>
              <el-button 
                type="primary" 
                class="submit-btn" 
                :loading="loading" 
                @click="handleLogin"
              >
                立即登录
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="form-footer">
            <span>还没有账号？</span>
            <el-link type="primary" @click="goToRegister">立即注册</el-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 使用新的 API 模块
        const res = await request.post('/auth/login', loginForm)
        
        // 使用 Pinia 存储状态
        userStore.setLoginState({
            token: res.token,
            user: res.user || { username: loginForm.username, role: 'TEACHER' } // Mock role if backend missing it
        })
        
        ElMessage.success('登录成功，欢迎回来！')
        
        // 根据角色跳转
        if (userStore.isStudent) {
            router.push('/student/dashboard')
        } else {
            router.push('/dashboard')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  ElMessage.info('注册功能暂未开放')
}
</script>

<style scoped>
.login-wrapper {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
  background-image: url('data:image/svg+xml,%3Csvg width="64" height="64" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg"%3E%3Cpath d="M8 16c4.418 0 8-3.582 8-8s-3.582-8-8-8-8 3.582-8 8 3.582 8 8 8zm0-2c3.314 0 6-2.686 6-6s-2.686-6-6-6-6 2.686-6 6 2.686 6 6 6zm33.414 0c5.91 0 10.931-3.763 12.586-9h-5.172c-1.29 2.292-3.669 4-6.414 4-4.418 0-8-3.582-8-8s3.582-8 8-8c2.745 0 5.124 1.708 6.414 4h5.172c-1.655-5.237-6.676-9-12.586-9C34.82 0 30 4.82 30 10s4.82 10 10.414 10zM40 62c4.418 0 8-3.582 8-8s-3.582-8-8-8-8 3.582-8 8 3.582 8 8 8zm0-2c3.314 0 6-2.686 6-6s-2.686-6-6-6-6 2.686-6 6 2.686 6 6 6z" fill="%239C92AC" fill-opacity="0.05" fill-rule="evenodd"/%3E%3C/svg%3E');
}

.login-content {
  width: 1000px;
  height: 600px;
  background: #fff;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
  display: flex;
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #3a7bd5 0%, #00d2ff 100%);
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
  position: relative;
}

.login-bg-circle {
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  top: -50px;
  left: -50px;
}

.login-bg-circle-2 {
  position: absolute;
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  bottom: -30px;
  right: -30px;
}

.app-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 10px;
  position: relative;
  z-index: 1;
}

.app-desc {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 40px;
  position: relative;
  z-index: 1;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  position: relative;
  z-index: 1;
}

.feature-list li {
  font-size: 16px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.form-container {
  width: 360px;
  padding: 0 20px;
}

.form-title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 10px;
  font-weight: 600;
}

.form-subtitle {
  font-size: 14px;
  color: #909399;
  margin-bottom: 40px;
}

.login-form {
  margin-bottom: 20px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 2px;
  border-radius: 8px;
  margin-top: 10px;
  background: linear-gradient(90deg, #3a7bd5, #00d2ff);
  border: none;
  transition: transform 0.2s;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(58, 123, 213, 0.3);
}

.form-footer {
  text-align: center;
  font-size: 14px;
  color: #606266;
}
</style>
