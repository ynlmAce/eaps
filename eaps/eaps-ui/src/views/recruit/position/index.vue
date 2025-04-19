<template>
  <div class="position-manage-container">
    <div class="page-header">
      <div class="header-title">
        <h2>职位管理</h2>
        <el-button type="primary" @click="handleAddPosition">
          <el-icon><Plus /></el-icon>发布新职位
        </el-button>
      </div>
      <el-alert
        title="在这里管理您企业发布的招聘职位"
        type="info"
        description="您可以发布、编辑、下线职位，并查看每个职位的申请情况"
        show-icon
        :closable="false"
        style="margin-top: 15px; margin-bottom: 20px;"
      />
    </div>
    
    <div class="search-box">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="职位名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入职位名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="职位状态">
          <el-select v-model="queryParams.status" placeholder="职位状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="草稿" value="draft" />
            <el-option label="待审核" value="pending" />
            <el-option label="已发布" value="active" />
            <el-option label="已下线" value="inactive" />
            <el-option label="审核拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="职位类型">
          <el-select v-model="queryParams.positionType" placeholder="职位类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="全职" value="fulltime" />
            <el-option label="兼职" value="parttime" />
            <el-option label="实习" value="intern" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="data-overview">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon total-positions">
              <el-icon><Briefcase /></el-icon>
            </div>
            <div class="overview-data">
              <div class="overview-value">{{ statistics.totalPositions || 0 }}</div>
              <div class="overview-label">职位总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon active-positions">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
            <div class="overview-data">
              <div class="overview-value">{{ statistics.activePositions || 0 }}</div>
              <div class="overview-label">在线职位</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon total-applications">
              <el-icon><Document /></el-icon>
            </div>
            <div class="overview-data">
              <div class="overview-value">{{ statistics.totalApplications || 0 }}</div>
              <div class="overview-label">申请总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="overview-icon pending-applications">
              <el-icon><Bell /></el-icon>
            </div>
            <div class="overview-data">
              <div class="overview-value">{{ statistics.pendingApplications || 0 }}</div>
              <div class="overview-label">待处理申请</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card v-if="loading" class="loading-card">
      <el-skeleton :rows="5" animated />
    </el-card>
    
    <div v-else-if="positionList.length === 0" class="empty-content">
      <el-empty description="暂无职位数据" />
      <el-button type="primary" @click="handleAddPosition">立即发布职位</el-button>
    </div>
    
    <div v-else class="position-list">
      <el-table
        :data="positionList"
        style="width: 100%"
        border
        stripe
        highlight-current-row
      >
        <el-table-column prop="name" label="职位名称" min-width="150">
          <template #default="scope">
            <el-link type="primary" @click="viewPositionDetail(scope.row.id)">{{ scope.row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="positionType" label="职位类型" width="100">
          <template #default="scope">
            <el-tag :type="getPositionTypeTag(scope.row.positionType)">
              {{ getPositionTypeText(scope.row.positionType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="薪资范围" width="150">
          <template #default="scope">
            {{ scope.row.salaryLower }}k-{{ scope.row.salaryUpper }}k·{{ scope.row.salaryCount }}薪
          </template>
        </el-table-column>
        <el-table-column prop="workLocation" label="工作地点" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicationCount" label="申请数" width="80" align="center" />
        <el-table-column prop="viewCount" label="浏览数" width="80" align="center" />
        <el-table-column label="发布时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.publishTime) || '未发布' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              @click="handleApplications(scope.row.id)"
              :disabled="scope.row.applicationCount === 0"
            >
              查看申请
            </el-button>
            <el-button
              type="primary"
              link
              @click="handleEdit(scope.row.id)"
              :disabled="!['draft', 'active', 'inactive', 'rejected'].includes(scope.row.status)"
            >
              编辑
            </el-button>
            <el-button
              v-if="scope.row.status === 'active'"
              type="warning"
              link
              @click="handleDeactivate(scope.row.id)"
            >
              下线
            </el-button>
            <el-button
              v-if="scope.row.status === 'inactive'"
              type="success"
              link
              @click="handleActivate(scope.row.id)"
            >
              上线
            </el-button>
            <el-button
              v-if="['draft', 'inactive', 'rejected'].includes(scope.row.status)"
              type="danger"
              link
              @click="handleDelete(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          :current-page="queryParams.pageNum"
          :page-size="queryParams.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 30, 50]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Search, 
  Refresh, 
  Briefcase, 
  CircleCheckFilled, 
  Document, 
  Bell
} from '@element-plus/icons-vue'
import { 
  getPositionList, 
  getPositionStatistics, 
  activatePosition, 
  deactivatePosition, 
  deletePosition 
} from '@/api/position' // 假设这些API已实现

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const positionList = ref([])
const statistics = ref({})

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  status: '',
  positionType: ''
})

// 获取职位列表
const getPositions = async () => {
  loading.value = true
  try {
    const res = await getPositionList({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      name: queryParams.name,
      status: queryParams.status,
      positionType: queryParams.positionType
    })
    positionList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取职位列表失败:', error)
    ElMessage.error('获取职位列表失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const getStatisticsData = async () => {
  try {
    const res = await getPositionStatistics()
    statistics.value = res || {}
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取职位类型标签样式
const getPositionTypeTag = (type) => {
  const map = {
    'fulltime': 'primary',
    'parttime': 'success',
    'intern': 'warning'
  }
  return map[type] || 'info'
}

// 获取职位类型文本
const getPositionTypeText = (type) => {
  const map = {
    'fulltime': '全职',
    'parttime': '兼职',
    'intern': '实习'
  }
  return map[type] || '未知'
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const map = {
    'draft': 'info',
    'pending': 'warning',
    'active': 'success',
    'inactive': 'danger',
    'rejected': 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    'draft': '草稿',
    'pending': '待审核',
    'active': '已发布',
    'inactive': '已下线',
    'rejected': '已拒绝'
  }
  return map[status] || '未知'
}

// 添加职位
const handleAddPosition = () => {
  router.push('/recruit/position/add')
}

// 编辑职位
const handleEdit = (id) => {
  router.push(`/recruit/position/edit/${id}`)
}

// 查看职位详情
const viewPositionDetail = (id) => {
  router.push(`/job/position/${id}`)
}

// 查看申请
const handleApplications = (id) => {
  router.push(`/recruit/application?positionId=${id}`)
}

// 激活职位
const handleActivate = (id) => {
  ElMessageBox.confirm('确定要上线该职位吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await activatePosition(id)
      ElMessage.success('职位已上线')
      getPositions()
      getStatisticsData()
    } catch (error) {
      console.error('激活职位失败:', error)
      ElMessage.error('激活职位失败')
    }
  }).catch(() => {})
}

// 下线职位
const handleDeactivate = (id) => {
  ElMessageBox.confirm('确定要下线该职位吗？下线后求职者将无法看到该职位', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deactivatePosition(id)
      ElMessage.success('职位已下线')
      getPositions()
      getStatisticsData()
    } catch (error) {
      console.error('下线职位失败:', error)
      ElMessage.error('下线职位失败')
    }
  }).catch(() => {})
}

// 删除职位
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该职位吗？删除后不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePosition(id)
      ElMessage.success('删除成功')
      getPositions()
      getStatisticsData()
    } catch (error) {
      console.error('删除职位失败:', error)
      ElMessage.error('删除职位失败')
    }
  }).catch(() => {})
}

// 处理查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getPositions()
}

// 重置查询
const resetQuery = () => {
  queryParams.name = ''
  queryParams.status = ''
  queryParams.positionType = ''
  queryParams.pageNum = 1
  getPositions()
}

// 分页处理
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getPositions()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getPositions()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 页面加载时获取数据
onMounted(() => {
  getPositions()
  getStatisticsData()
})
</script>

<style lang="scss" scoped>
.position-manage-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    
    .header-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h2 {
        margin: 0;
        font-weight: bold;
      }
    }
  }
  
  .search-box {
    margin-bottom: 20px;
    background-color: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
  }
  
  .data-overview {
    margin-bottom: 20px;
    
    .overview-card {
      margin-bottom: 20px;
      
      .overview-item {
        display: flex;
        align-items: center;
        
        .overview-icon {
          width: 60px;
          height: 60px;
          border-radius: 50%;
          display: flex;
          justify-content: center;
          align-items: center;
          margin-right: 15px;
          
          .el-icon {
            font-size: 24px;
            color: #fff;
          }
          
          &.total-positions {
            background-color: #409eff;
          }
          
          &.active-positions {
            background-color: #67c23a;
          }
          
          &.total-applications {
            background-color: #e6a23c;
          }
          
          &.pending-applications {
            background-color: #f56c6c;
          }
        }
        
        .overview-data {
          .overview-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            line-height: 1;
            margin-bottom: 5px;
          }
          
          .overview-label {
            color: #909399;
            font-size: 14px;
          }
        }
      }
    }
  }
  
  .loading-card {
    margin-bottom: 20px;
  }
  
  .empty-content {
    margin: 40px 0;
    text-align: center;
    
    .el-button {
      margin-top: 20px;
    }
  }
  
  .position-list {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}
</style> 