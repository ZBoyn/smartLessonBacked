import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../store/user'

// 教师端组件
const MainLayout = () => import('../layout/MainLayout.vue')
const Dashboard = () => import('../views/Dashboard.vue')
const CourseList = () => import('../views/CourseList.vue')

// 课程内部组件 (Course Context)
const CourseLayout = () => import('../layout/CourseLayout.vue')
const CourseOverview = () => import('../views/course/Overview.vue') // 需要新建
const CourseClasses = () => import('../views/ClassList.vue')        // 复用，但需改造
const CourseResources = () => import('../views/course/CourseResources.vue')
const CourseGraph = () => import('../views/KnowledgeGraph.vue')     // 复用，但需改造
const CourseQuestions = () => import('../views/SmartQuestions.vue') // 复用，但需改造

// 学生端组件
const StudentLayout = () => import('../layout/StudentLayout.vue')
const StudentDashboard = () => import('../views/student/StudentDashboard.vue')
const StudentCourseList = () => import('../views/student/MyCourses.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  // Teacher Routes
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    meta: { roles: ['TEACHER'] },
    children: [
      {
        path: 'dashboard',
        component: Dashboard,
        meta: { title: '教师仪表盘' }
      },
      {
        path: 'courses',
        component: CourseList,
        meta: { title: '课程列表' }
      },
      // 学情分析作为全局视图，或也移入课程内？暂且全局
      {
        path: 'analysis',
        component: () => import('../views/StudyAnalysis.vue'),
        meta: { title: '全局学情分析' }
      }
    ]
  },
  // Teacher Course Context Routes (Nested)
  {
    path: '/teacher/course/:courseId',
    component: CourseLayout,
    redirect: { name: 'CourseOverview' },
    meta: { roles: ['TEACHER'] },
    children: [
      {
        path: 'overview',
        name: 'CourseOverview',
        component: CourseOverview,
        meta: { title: '课程概览' }
      },
      {
        path: 'classes',
        name: 'CourseClasses',
        component: CourseClasses,
        meta: { title: '班级管理' }
      },
      {
        path: 'resources',
        name: 'CourseResources',
        component: CourseResources,
        meta: { title: '资源管理' }
      },
      {
        path: 'knowledge-graph',
        name: 'CourseGraph',
        component: CourseGraph,
        meta: { title: '知识图谱' }
      },
      {
        path: 'questions',
        name: 'CourseQuestions',
        component: CourseQuestions,
        meta: { title: '智能题库' }
      },
      {
        path: 'assessments',
        name: 'CourseAssessments',
        component: () => import('../views/AiGrading.vue'), // 暂时复用 AI Grading 页面作为入口
        meta: { title: '考试测评' }
      }
    ]
  },
  // Student Routes
  {
    path: '/student',
    component: StudentLayout,
    redirect: '/student/dashboard',
    meta: { roles: ['STUDENT'] },
    children: [
      {
        path: 'dashboard',
        component: StudentDashboard,
        meta: { title: '学习中心' }
      },
      {
        path: 'my-courses',
        component: StudentCourseList,
        meta: { title: '我的课程' }
      },
      {
        path: 'assessments',
        component: () => import('../views/student/AssessmentList.vue'),
        meta: { title: '在线考试' }
      },
      {
        path: 'analysis',
        component: () => import('../views/student/GrowthAnalysis.vue'),
        meta: { title: '成长档案' }
      }
    ]
  },
  // Fallback
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }
  // 简单的角色检查
  if (to.meta.roles) {
    const userRole = userStore.role
    if (to.meta.roles.includes(userRole)) {
      next()
    } else {
       if (userRole === 'TEACHER') next('/dashboard')
       else if (userRole === 'STUDENT') next('/student/dashboard')
       else next('/login')
    }
  } else {
    next()
  }
})

export default router
