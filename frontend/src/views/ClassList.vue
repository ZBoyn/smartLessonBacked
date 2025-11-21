<template>
  <div class="page-container">
    <div class="header-actions">
        <h3>班级管理</h3>
        <el-button type="primary" icon="Plus" @click="dialogVisible = true">创建新班级</el-button>
    </div>

    <el-card>
      <el-table :data="classList" style="width: 100%" v-loading="loading">
        <el-table-column prop="classId" label="班级ID" width="100" />
        <el-table-column prop="className" label="班级名称" />
        <el-table-column prop="semester" label="学期" />
        <el-table-column label="学生人数" width="120">
            <template #default>
                <!-- 暂时 Mock，因为 ClassResponseDto 可能没返回 count -->
                <el-tag type="info">0 人</el-tag>
            </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default>
            <el-button size="small">学生名单</el-button>
            <el-button size="small" type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 创建班级弹窗 -->
    <el-dialog v-model="dialogVisible" title="创建班级" width="400px">
        <el-form :model="createForm">
            <el-form-item label="班级名称">
                <el-input v-model="createForm.className" placeholder="例如：2023级 计算机1班" />
            </el-form-item>
            <el-form-item label="所属学期">
                <el-input v-model="createForm.semester" placeholder="例如：2023-2024-1" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleCreate">确认创建</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { classApi } from '../api/modules'
import { ElMessage } from 'element-plus'

const route = useRoute()
// 必须能够响应 courseId 的变化，或者在 onMounted 中获取
const courseId = route.params.courseId

const loading = ref(false)
const classList = ref([])
const dialogVisible = ref(false)
const createForm = ref({
    className: '',
    semester: ''
})

const fetchClasses = async () => {
    if (!courseId) return
    loading.value = true
    try {
        const res = await classApi.getClassesByCourse(courseId)
        classList.value = res
    } catch (e) {
        console.error(e)
    } finally {
        loading.value = false
    }
}

const handleCreate = async () => {
    try {
        await classApi.createClass(courseId, createForm.value)
        ElMessage.success('创建成功')
        dialogVisible.value = false
        fetchClasses()
    } catch (e) {
        ElMessage.error('创建失败')
    }
}

onMounted(() => {
    fetchClasses()
})
</script>

<style scoped>
.header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}
</style>
