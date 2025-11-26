<template>
  <div class="page-container">
    <div class="page-header">
      <div class="title-section">
        <h2><el-icon class="icon-header"><Share /></el-icon> 课程知识图谱</h2>
        <p class="subtitle">可视化展示课程知识点关联结构</p>
      </div>
      <div class="actions">
          <el-button-group>
            <el-button icon="Refresh" @click="fetchGraph">刷新</el-button>
            <el-button type="primary" icon="Plus" @click="dialogVisible = true">添加知识点</el-button>
            <el-button type="success" icon="Connection" @click="relationDialogVisible = true">建立关联</el-button>
          </el-button-group>
      </div>
    </div>

    <el-row :gutter="20">
        <el-col :span="18">
            <el-card class="graph-card" shadow="hover">
              <div v-loading="loading" element-loading-text="正在构建图谱..." class="chart-wrapper">
                  <div id="graph-chart" class="chart-container" ref="chartRef"></div>
                  <div v-if="!hasData && !loading" class="empty-state">
                      <el-empty description="暂无图谱数据，请添加知识点" />
                  </div>
              </div>
            </el-card>
        </el-col>
        <el-col :span="6">
            <el-card class="info-card" shadow="hover">
                <template #header>
                    <div class="card-header">
                        <span>节点详情</span>
                    </div>
                </template>
                <div v-if="selectedNode" class="node-detail">
                    <h3>{{ selectedNode.name }}</h3>
                    <p class="node-desc">{{ selectedNode.description || '暂无描述' }}</p>
                    <el-divider>关联统计</el-divider>
                    <div class="stats">
                        <div class="stat-item">
                            <div class="stat-val">{{ getNodeDegree(selectedNode.id) }}</div>
                            <div class="stat-label">关联数</div>
                        </div>
                        <div class="stat-item">
                             <div class="stat-val">{{ getRelatedQuestionsCount(selectedNode.id) }}</div>
                            <div class="stat-label">题目数</div>
                        </div>
                    </div>
                    <el-button type="primary" plain style="width: 100%; margin-top: 20px" @click="viewQuestions(selectedNode)">查看相关题目</el-button>
                </div>
                <div v-else class="no-selection">
                    <el-icon size="48" color="#dcdfe6"><Pointer /></el-icon>
                    <p>点击图谱节点查看详情</p>
                </div>
            </el-card>
        </el-col>
    </el-row>

    <!-- 添加节点弹窗 -->
    <el-dialog v-model="dialogVisible" title="添加知识点" width="450px" destroy-on-close>
        <el-form :model="nodeForm" label-width="80px" ref="nodeFormRef">
            <el-form-item label="名称" required>
                <el-input v-model="nodeForm.label" placeholder="例如：HashMap原理" />
            </el-form-item>
            <el-form-item label="描述">
                <el-input v-model="nodeForm.description" type="textarea" rows="3" placeholder="简要描述该知识点..." />
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleAddNode" :loading="submitting">确认添加</el-button>
        </template>
    </el-dialog>

    <!-- 添加关系弹窗 -->
    <el-dialog v-model="relationDialogVisible" title="建立知识关联" width="500px" destroy-on-close>
        <el-form :model="relationForm" label-width="80px">
            <el-form-item label="源节点">
                <el-select v-model="relationForm.sourceId" placeholder="选择起始知识点" style="width: 100%" filterable>
                    <el-option v-for="n in graphNodes" :key="n.id" :label="n.name" :value="n.id" />
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
                <el-select v-model="relationForm.targetId" placeholder="选择指向知识点" style="width: 100%" filterable>
                    <el-option v-for="n in graphNodes" :key="n.id" :label="n.name" :value="n.id" />
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="relationDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleAddRelation" :loading="submitting">确认建立</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { graphApi } from '../api/modules'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { Share, Refresh, Plus, Connection, Pointer } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const courseId = route.params.courseId
const loading = ref(false)
const submitting = ref(false)
const chartRef = ref(null)
let myChart = null

const graphData = ref({ nodes: [], edges: [] })
const selectedNode = ref(null)

const dialogVisible = ref(false)
const relationDialogVisible = ref(false)

const nodeForm = ref({ label: '', description: '' })
const relationForm = ref({ sourceId: null, targetId: null, type: 'RELATED' })

const graphNodes = computed(() => graphData.value?.nodes || [])
const hasData = computed(() => graphNodes.value.length > 0)

const fetchGraph = async () => {
    if (!courseId) return
    loading.value = true
    try {
        const res = await graphApi.getGraph(courseId)
        if (res) {
            // Transform data for ECharts
            // ECharts Graph requires 'name' as ID usually, or use id and specify id in links
            // We will use index or id. Let's use id as string.
            const nodes = (res.nodes || []).map(n => ({
                id: String(n.kpId),
                name: n.name,
                value: n.kpId,
                symbolSize: 40,
                category: 0,
                description: n.description,
                questionCount: n.questionCount || 0,
                draggable: true
            }))
            
            const edges = (res.edges || []).map(e => ({
                source: String(e.sourceKpId),
                target: String(e.targetKpId),
                value: e.relationType
            }))

            graphData.value = { nodes, edges }
            initChart(nodes, edges)
        } else {
            graphData.value = { nodes: [], edges: [] }
        }
    } catch (e) {
        console.error(e)
        // Mock Data for Display if API fails
        const mockNodes = [
            { id: '1', name: 'Java基础', symbolSize: 50, category: 0 },
            { id: '2', name: '面向对象', symbolSize: 40, category: 1 },
            { id: '3', name: '集合框架', symbolSize: 40, category: 1 },
            { id: '4', name: '多线程', symbolSize: 40, category: 1 }
        ]
        const mockEdges = [
            { source: '1', target: '2', value: '包含' },
            { source: '1', target: '3', value: '包含' },
            { source: '1', target: '4', value: '包含' }
        ]
        graphData.value = { nodes: mockNodes, edges: mockEdges }
        initChart(mockNodes, mockEdges)
        if(!e.message?.includes('404')) ElMessage.warning('加载图谱失败，显示模拟数据')
    } finally {
        loading.value = false
    }
}

const initChart = (nodes, edges) => {
    if (!chartRef.value) return
    if (myChart) myChart.dispose()
    
    myChart = echarts.init(chartRef.value)
    
    const option = {
        tooltip: {
            trigger: 'item',
            formatter: '{b}'
        },
        legend: [{
            data: nodes.map(a => a.category).filter((v, i, a) => a.indexOf(v) === i)
        }],
        series: [
            {
                name: 'Knowledge Graph',
                type: 'graph',
                layout: 'force',
                data: nodes,
                links: edges,
                categories: [{ name: '核心知识点' }, { name: '子知识点' }],
                roam: true,
                label: {
                    show: true,
                    position: 'bottom', // Show text below node
                    formatter: '{b}',
                    fontSize: 12
                },
                lineStyle: {
                    color: 'source',
                    curveness: 0.3
                },
                force: {
                    repulsion: 300,
                    edgeLength: 120
                },
                emphasis: {
                    focus: 'adjacency',
                    lineStyle: {
                        width: 4
                    }
                }
            }
        ]
    }
    
    myChart.setOption(option)
    
    myChart.on('click', (params) => {
        if (params.dataType === 'node') {
            selectedNode.value = params.data
        }
    })
    
    // Resize handler
    window.addEventListener('resize', resizeChart)
}

const resizeChart = () => {
    myChart && myChart.resize()
}

const handleAddNode = async () => {
    if(!nodeForm.value.label) return
    submitting.value = true
    try {
        await graphApi.addNode({
            name: nodeForm.value.label,
            description: nodeForm.value.description,
            courseId: parseInt(courseId)
        })
        ElMessage.success('知识点添加成功')
        dialogVisible.value = false
        nodeForm.value = { label: '', description: '' }
        fetchGraph()
    } catch (e) {
        ElMessage.error('添加失败')
    } finally {
        submitting.value = false
    }
}

const handleAddRelation = async () => {
    if (relationForm.value.sourceId === relationForm.value.targetId) {
        ElMessage.warning('源节点和目标节点不能相同')
        return
    }
    if (!relationForm.value.sourceId || !relationForm.value.targetId) return

    submitting.value = true
    try {
        const typeMap = { INCLUDES: 'belongs_to', PREREQUISITE: 'prerequisite', RELATED: 'explains' }
        await graphApi.addRelation({
            sourceKpId: relationForm.value.sourceId,
            targetKpId: relationForm.value.targetId,
            relationType: typeMap[relationForm.value.type] || 'belongs_to',
            courseId: parseInt(courseId)
        })
        ElMessage.success('关系建立成功')
        relationDialogVisible.value = false
        relationForm.value = { sourceId: null, targetId: null, type: 'RELATED' }
        fetchGraph()
    } catch (e) {
        ElMessage.error('操作失败')
    } finally {
        submitting.value = false
    }
}

const getNodeDegree = (nodeId) => {
    // Simple calculation of degree based on edges
    const edges = graphData.value.edges
    return edges.filter(e => e.source === nodeId || e.target === nodeId).length
}

const getRelatedQuestionsCount = (nodeId) => {
    const node = graphNodes.value.find(n => n.id === String(nodeId))
    return node ? (node.questionCount || 0) : 0
}

const viewQuestions = (node) => {
    router.push({ 
        name: 'SmartQuestions', 
        params: { courseId }, 
        query: { kpId: node.id } 
    })
}

onMounted(() => {
    fetchGraph()
})

onUnmounted(() => {
    window.removeEventListener('resize', resizeChart)
    if (myChart) myChart.dispose()
})
</script>

<style scoped>
.page-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.icon-header {
    vertical-align: middle;
    margin-right: 8px;
    color: #409EFF;
}
.subtitle {
    margin: 5px 0 0 36px;
    color: #909399;
    font-size: 13px;
}

.graph-card {
  height: 650px;
  display: flex;
  flex-direction: column;
}
.chart-wrapper {
    height: 600px;
    position: relative;
}
.chart-container {
  width: 100%;
  height: 100%;
}
.empty-state {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}

.info-card {
    height: 650px;
}
.node-detail {
    text-align: center;
}
.node-desc {
    color: #606266;
    margin: 15px 0;
    text-align: left;
    line-height: 1.6;
    background: #f9fafc;
    padding: 10px;
    border-radius: 4px;
}
.stats {
    display: flex;
    justify-content: space-around;
    margin: 20px 0;
}
.stat-item {
    text-align: center;
}
.stat-val {
    font-size: 24px;
    font-weight: bold;
    color: #409EFF;
}
.stat-label {
    font-size: 12px;
    color: #909399;
}
.no-selection {
    margin-top: 100px;
    text-align: center;
    color: #909399;
}
</style>
