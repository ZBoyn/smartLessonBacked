import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  
  // Role: 'TEACHER' | 'STUDENT'
  const role = computed(() => userInfo.value?.role || '')
  const username = computed(() => userInfo.value?.username || '')

  const setLoginState = (data) => {
    token.value = data.token
    userInfo.value = data.user // 假设后端 LoginResponse 包含 user 对象
    
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    localStorage.setItem('role', data.user.role)
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.clear()
  }

  const isTeacher = computed(() => role.value === 'TEACHER')
  const isStudent = computed(() => role.value === 'STUDENT')

  return {
    token,
    userInfo,
    role,
    username,
    isTeacher,
    isStudent,
    setLoginState,
    logout
  }
})

