<template>
  <div class="page-container">
    <div class="header-actions">
      <h3>课程资源库</h3>
      <el-button type="primary" icon="Upload" @click="dialogVisible = true">上传资源</el-button>
    </div>

    <el-card>
      <el-table :data="resourceList" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="资源名称" min-width="200">
           <template #default="scope">
              <div class="resource-name">
                <el-icon v-if="scope.row.resourceType === 'VIDEO'"><VideoPlay /></el-icon>
                <el-icon v-else><Document /></el-icon>
                <span style="margin-left: 8px">{{ scope.row.title }}</span>
              </div>
           </template>
        </el-table-column>
        <el-table-column prop="resourceType" label="类型" width="100" />
        <el-table-column label="关联知识点" min-width="200">
          <template #default="scope">
            <el-tag 
              v-for="kp in scope.row.knowledgePoints" 
              :key="kp" 
              size="small" 
              style="margin-right: 5px"
            >
              {{ kp }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="previewResource(scope.row)">预览/下载</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row.resourceId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 上传弹窗 -->
    <el-dialog v-model="dialogVisible" title="上传课程资源" width="500px">
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="资源标题">
          <el-input v-model="uploadForm.title" placeholder="例如：第一章 PPT" />
        </el-form-item>
        <el-form-item label="关联知识点">
           <el-select 
             v-model="uploadForm.kpIds" 
             multiple 
             placeholder="请选择关联的知识点"
             style="width: 100%"
           >
             <el-option 
               v-for="item in kpOptions" 
               :key="item.id" 
               :label="item.name" 
               :value="item.id" 
             />
           </el-select>
        </el-form-item>
        <el-form-item label="文件">
          <el-upload
            class="upload-demo"
            :auto-upload="false"
            :limit="1"
            :on-change="handleFileChange"
            :file-list="fileList"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpload" :loading="uploading">确认上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { resourceApi, graphApi } from '../../api/modules'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const courseId = route.params.courseId

const loading = ref(false)
const resourceList = ref([])
const dialogVisible = ref(false)
const uploading = ref(false)
const kpOptions = ref([]) // 知识点下拉选项

const uploadForm = ref({
  title: '',
  kpIds: []
})
const fileList = ref([])
const selectedFile = ref(null)

// 获取资源列表
const fetchResources = async () => {
  loading.value = true
  try {
    const res = await resourceApi.getResources(courseId)
    resourceList.value = res
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 获取知识点列表 (用于上传时的下拉选择)
const fetchKPs = async () => {
  try {
    // 假设 Graph 接口返回的数据结构包含 nodes
    const res = await graphApi.getGraph(courseId)
    if (res && res.nodes) {
        kpOptions.value = res.nodes.map(n => ({ id: n.id, name: n.label }))
    }
  } catch (e) {
    console.error('加载知识点失败', e)
  }
}

onMounted(() => {
  fetchResources()
  fetchKPs()
})

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const submitUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }
  if (uploadForm.value.kpIds.length === 0) {
    ElMessage.warning('请至少关联一个知识点')
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('title', uploadForm.value.title)
    // 注意：Spring Boot 接收 List<Integer> 可能需要特定格式，通常重复 key 或逗号分隔
    // 这里使用重复 key: knowledgePointIds=1&knowledgePointIds=2
    uploadForm.value.kpIds.forEach(id => {
        formData.append('knowledgePointIds', id)
    })

    await resourceApi.uploadResource(courseId, formData)
    ElMessage.success('上传成功')
    dialogVisible.value = false
    fetchResources()
    // 重置表单
    uploadForm.value = { title: '', kpIds: [] }
    fileList.value = []
    selectedFile.value = null
  } catch (e) {
    ElMessage.error('上传失败')
  } finally {
    uploading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确认删除该资源吗?', '警告', { type: 'warning' })
    await resourceApi.deleteResource(id)
    ElMessage.success('删除成功')
    fetchResources()
  } catch (e) {
    // cancel or error
  }
}

const previewResource = (row) => {
    // 这里可以做一个预览窗口，或者直接打开新标签页下载
    const url = resourceApi.getDownloadUrl(row.resourceId)
    // 因为是 JWT 鉴权，直接 window.open 可能带不上 header
    // 简单的做法是 token 放在 url query param ?token=... (需要后端支持)
    // 或者使用 Blob 下载。这里暂且提示。
    ElMessage.info(`正在尝试请求资源: ${url}`)
    // 实际项目中通常使用 Blob 方式下载
}
</script>

<style scoped>
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.resource-name {
    display: flex;
    align-items: center;
}
</style>

