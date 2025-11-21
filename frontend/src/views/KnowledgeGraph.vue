<template>
  <div class="page-container">
    <div class="page-header">
      <h2>知识图谱</h2>
      <div class="actions">
          <el-button icon="Refresh" @click="fetchGraph">刷新图谱</el-button>
          <el-button type="primary" icon="Plus" @click="dialogVisible = true">添加知识点</el-button>
          <el-button type="success" icon="Share" @click="relationDialogVisible = true">添加关系</el-button>
      </div>
    </div>

    <el-card class="graph-container">
      <div class="placeholder-graph" v-if="!graphData || (!graphData.nodes && !graphData.edges)">
        <el-icon class="graph-icon"><Share /></el-icon>
        <p>知识图谱可视化区域</p>
        <p class="sub-text">此处将展示 D3.js 或 ECharts 渲染的知识节点关系</p>
      </div>
      <div v-else class="graph-content">
          <!-- Mock Visualization: 简单的列表展示，后续接 ECharts -->
          <div class="node-list">
              <h4>节点 (Nodes)</h4>
              <el-tag v-for="node in graphData.nodes" :key="node.id" class="graph-tag">{{ node.label }}</el-tag>
          </div>
          <el-divider />
          <div class="edge-list">
              <h4>关系 (Edges)</h4>
              <div v-for="(edge, idx) in graphData.edges" :key="idx" class="edge-item">
                  {{ getLabel(edge.source) }} -> {{ getLabel(edge.target) }} 
                  <span class="edge-type">[{{ edge.type }}]</span>
              </div>
          </div>
      </div>
    </el-card>

    <!-- 添加节点弹窗 -->
    <el-dialog v-model="dialogVisible" title="添加知识点" width="400px">
        <el-form :model="nodeForm">
            <el-form-item label="知识点名称">
                <el-input v-model="nodeForm.label" placeholder="例如：HashMap" />
            </el-form-item>
            <el-form-item label="描述">
                <el-input v-model="nodeForm.description" type="textarea" />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleAddNode">确认添加</el-button>
        </template>
    </el-dialog>

    <!-- 添加关系弹窗 -->
    <el-dialog v-model="relationDialogVisible" title="建立知识关联" width="500px">
        <el-form :model="relationForm" label-width="80px">
            <el-form-item label="源节点">
                <el-select v-model="relationForm.sourceId" placeholder="选择起始知识点" style="width: 100%">
                    <el-option v-for="n in graphNodes" :key="n.id" :label="n.label" :value="n.id" />
                </el-select>
            </el-form-item>
            <el-form-item label="关系类型">
                <el-select v-model="relationForm.type" placeholder="选择关系类型" style="width: 100%">
                    <el-option label="包含 (Includes)" value="INCLUDES" />
                    <el-option label="前置 (Prerequisite)" value="PREREQUISITE" />
                    <el-option label="相关 (Related)" value="RELATED" />
                </el-select>
            </el-form-item>
            <el-form-item label="目标节点">
                <el-select v-model="relationForm.targetId" placeholder="选择指向知识点" style="width: 100%">
                    <el-option v-for="n in graphNodes" :key="n.id" :label="n.label" :value="n.id" />
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="relationDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleAddRelation">确认建立</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import { graphApi } from '../../api/modules'
import { ElMessage } from 'element-plus'

const route = useRoute()
const courseId = route.params.courseId
const graphData = ref({ nodes: [], edges: [] })

const dialogVisible = ref(false)
const relationDialogVisible = ref(false)

const nodeForm = ref({ label: '', description: '' })
const relationForm = ref({ sourceId: null, targetId: null, type: '' })

const graphNodes = computed(() => graphData.value?.nodes || [])

const getLabel = (id) => {
    const node = graphNodes.value.find(n => n.id === id)
    return node ? node.label : id
}

const fetchGraph = async () => {
    if (!courseId) return
    try {
        const res = await graphApi.getGraph(courseId)
        // 后端可能返回 null 或空结构，做防御性处理
        graphData.value = res || { nodes: [], edges: [] }
    } catch (e) {
        console.error(e)
        graphData.value = { nodes: [], edges: [] }
    }
}

const handleAddNode = async () => {
    try {
        await graphApi.addNode({
            ...nodeForm.value,
            courseId: parseInt(courseId)
        })
        ElMessage.success('知识点添加成功')
        dialogVisible.value = false
        nodeForm.value = { label: '', description: '' }
        fetchGraph()
    } catch (e) {
        ElMessage.error('添加失败')
    }
}

const handleAddRelation = async () => {
    if (relationForm.value.sourceId === relationForm.value.targetId) {
        ElMessage.warning('源节点和目标节点不能相同')
        return
    }
    try {
        await graphApi.addRelation({
            ...relationForm.value,
            courseId: parseInt(courseId)
        })
        ElMessage.success('关系建立成功')
        relationDialogVisible.value = false
        relationForm.value = { sourceId: null, targetId: null, type: '' }
        fetchGraph()
    } catch (e) {
        ElMessage.error('操作失败')
    }
}

onMounted(() => {
    fetchGraph()
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.graph-container {
  min-height: 600px;
  overflow: auto;
}

.placeholder-graph {
  text-align: center;
  color: #909399;
  margin-top: 100px;
}

.graph-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.graph-tag {
    margin: 5px;
}
.edge-item {
    font-size: 14px;
    margin-bottom: 8px;
    color: #606266;
}
.edge-type {
    color: #909399;
    font-size: 12px;
    margin-left: 5px;
}
</style>
