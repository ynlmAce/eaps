<template>
  <div class="my-applications-container">
    <el-card shadow="never" class="filter-card">
      <div class="filter-header">
        <h2 class="page-title">我的申请</h2>
        <div class="filter-actions">
          <el-select v-model="queryParams.status" placeholder="申请状态" clearable @change="handleFilter">
            <el-option 
              v-for="item in statusOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索职位名称/企业名称"
            clearable
            @keyup.enter="handleFilter"
            style="width: 220px; margin-left: 10px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </div>
    </el-card>

    <el-tabs v-model="activeTab" class="application-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all"></el-tab-pane>
      <el-tab-pane label="待处理" name="pending"></el-tab-pane>
      <el-tab-pane label="已查看" name="viewed"></el-tab-pane>
      <el-tab-pane label="已通过" name="accepted"></el-tab-pane>
      <el-tab-pane label="已拒绝" name="rejected"></el-tab-pane>
    </el-tabs>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="applicationList.length === 0" class="empty-container">
      <el-empty description="暂无申请记录" />
    </div>
    <div v-else>
      <el-card v-for="item in applicationList" :key="item.id" class="application-item" shadow="hover">
        <div class="application-header">
          <div class="job-info">
            <div class="job-title" @click="viewJobDetail(item.positionId)">{{ item.positionName }}</div>
            <div class="enterprise-name" @click="viewEnterpriseDetail(item.enterpriseId)">{{ item.enterpriseName }}</div>
          </div>
          <div class="application-status">
            <el-tag :type="getStatusType(item.status)">{{ getStatusText(item.status) }}</el-tag>
          </div>
        </div>
        
        <div class="application-content">
          <div class="application-meta">
            <span class="meta-item">
              <el-icon><Calendar /></el-icon>申请时间：{{ formatDate(item.applyTime) }}
            </span>
            <span class="meta-item" v-if="item.viewTime">
              <el-icon><View /></el-icon>查看时间：{{ formatDate(item.viewTime) }}
            </span>
            <span class="meta-item" v-if="item.processTime">
              <el-icon><Timer /></el-icon>处理时间：{{ formatDate(item.processTime) }}
            </span>
          </div>
          
          <div class="application-resume">
            <span class="resume-label">申请简历：</span>
            <el-button type="primary" link @click="viewResume(item.resumeId)">
              {{ item.resumeName }}
            </el-button>
          </div>
          
          <div class="application-remark" v-if="item.remark">
            <span class="remark-label">企业回复：</span>
            <div class="remark-content">{{ item.remark }}</div>
          </div>
        </div>
        
        <div class="application-footer">
          <el-button type="primary" @click="reapply(item)" v-if="canReapply(item)">再次申请</el-button>
          <el-button type="danger" @click="cancelApplication(item.id)" v-if="item.status === 'pending'">取消申请</el-button>
          <el-button @click="viewApplicationDetail(item.id)">查看详情</el-button>
        </div>
      </el-card>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 申请详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="申请详情" width="650px">
      <div v-if="currentApplication" class="application-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="职位名称" span="2">{{ currentApplication.positionName }}</el-descriptions-item>
          <el-descriptions-item label="申请企业" span="2">{{ currentApplication.enterpriseName }}</el-descriptions-item>
          <el-descriptions-item label="申请状态">
            <el-tag :type="getStatusType(currentApplication.status)">{{ getStatusText(currentApplication.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(currentApplication.applyTime) }}</el-descriptions-item>
          <el-descriptions-item label="申请简历">{{ currentApplication.resumeName }}</el-descriptions-item>
          <el-descriptions-item label="查看时间">{{ currentApplication.viewTime ? formatDate(currentApplication.viewTime) : '暂未查看' }}</el-descriptions-item>
          <el-descriptions-item label="求职信" span="2">
            <div class="cover-letter">{{ currentApplication.coverLetter || '未填写求职信' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="企业回复" span="2">
            <div class="reply-content">{{ currentApplication.remark || '暂无回复' }}</div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="dialog-footer">
          <el-button @click="viewJobDetail(currentApplication.positionId)">查看职位</el-button>
          <el-button @click="viewEnterpriseDetail(currentApplication.enterpriseId)">查看企业</el-button>
          <el-button @click="viewResume(currentApplication.resumeId)">查看简历</el-button>
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Calendar, View, Timer } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyApplications, cancelApplication, getApplicationDetail } from '@/api/job'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const activeTab = ref('all')
const applicationList = ref([])
const detailDialogVisible = ref(false)
const currentApplication = ref(null)

// 查询条件
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: '',
  keyword: ''
})

// 状态选项
const statusOptions = [
  { value: 'pending', label: '待处理' },
  { value: 'viewed', label: '已查看' },
  { value: 'accepted', label: '已通过' },
  { value: 'rejected', label: '已拒绝' }
]

// 处理Tab切换
const handleTabChange = (tab) => {
  switch (tab) {
    case 'all':
      queryParams.status = ''
      break
    case 'pending':
      queryParams.status = 'pending'
      break
    case 'viewed':
      queryParams.status = 'viewed'
      break
    case 'accepted':
      queryParams.status = 'accepted'
      break
    case 'rejected':
      queryParams.status = 'rejected'
      break
  }
  queryParams.pageNum = 1
  fetchApplicationList()
}

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending':
      return 'info'
    case 'viewed':
      return 'warning'
    case 'accepted':
      return 'success'
    case 'rejected':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待处理'
    case 'viewed':
      return '已查看'
    case 'accepted':
      return '已通过'
    case 'rejected':
      return '已拒绝'
    default:
      return '未知状态'
  }
}

// 判断是否可以再次申请
const canReapply = (application) => {
  return application.status === 'rejected' && 
         new Date(application.processTime).getTime() + 30 * 24 * 60 * 60 * 1000 < Date.now()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 获取申请列表
const fetchApplicationList = async () => {
  loading.value = true
  try {
    const res = await getMyApplications(queryParams)
    applicationList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
  } finally {
    loading.value = false
  }
}

// 查看职位详情
const viewJobDetail = (positionId) => {
  router.push(`/job/position/${positionId}`)
}

// 查看企业详情
const viewEnterpriseDetail = (enterpriseId) => {
  router.push(`/enterprise/detail/${enterpriseId}`)
}

// 查看简历
const viewResume = (resumeId) => {
  router.push(`/resume/view/${resumeId}`)
}

// 取消申请
const cancelApplication = (id) => {
  ElMessageBox.confirm('确定要取消该申请吗？取消后不可恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelApplication(id)
      ElMessage.success('申请已取消')
      fetchApplicationList()
    } catch (error) {
      console.error('取消申请失败:', error)
      ElMessage.error('取消申请失败')
    }
  }).catch(() => {})
}

// 再次申请
const reapply = (application) => {
  router.push(`/job/position/${application.positionId}`)
}

// 查看申请详情
const viewApplicationDetail = async (id) => {
  try {
    const res = await getApplicationDetail(id)
    if (res) {
      currentApplication.value = res
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取申请详情失败:', error)
    ElMessage.error('获取申请详情失败')
  }
}

// 筛选处理
const handleFilter = () => {
  queryParams.pageNum = 1
  fetchApplicationList()
}

// 重置筛选
const resetFilter = () => {
  queryParams.status = ''
  queryParams.keyword = ''
  activeTab.value = 'all'
  queryParams.pageNum = 1
  fetchApplicationList()
}

// 分页处理
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  fetchApplicationList()
}

const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  fetchApplicationList()
}

onMounted(() => {
  fetchApplicationList()
})
</script>

<style lang="scss" scoped>
.my-applications-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
  
  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: bold;
    }
    
    .filter-actions {
      display: flex;
      align-items: center;
    }
  }
}

.application-tabs {
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px 0;
  text-align: center;
}

.application-item {
  margin-bottom: 15px;
  
  .application-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 15px;
    
    .job-info {
      .job-title {
        font-size: 18px;
        font-weight: bold;
        color: #409EFF;
        margin-bottom: 5px;
        cursor: pointer;
        
        &:hover {
          text-decoration: underline;
        }
      }
      
      .enterprise-name {
        color: #606266;
        cursor: pointer;
        
        &:hover {
          color: #409EFF;
          text-decoration: underline;
        }
      }
    }
  }
  
  .application-content {
    margin-bottom: 15px;
    
    .application-meta {
      display: flex;
      flex-wrap: wrap;
      margin-bottom: 10px;
      color: #606266;
      
      .meta-item {
        display: flex;
        align-items: center;
        margin-right: 20px;
        margin-bottom: 5px;
        
        .el-icon {
          margin-right: 5px;
        }
      }
    }
    
    .application-resume, .application-remark {
      margin-top: 10px;
      
      .resume-label, .remark-label {
        color: #606266;
        font-weight: bold;
      }
      
      .remark-content {
        padding: 10px;
        background-color: #f9f9f9;
        border-radius: 4px;
        margin-top: 5px;
        color: #606266;
        line-height: 1.5;
      }
    }
  }
  
  .application-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.application-detail {
  .cover-letter, .reply-content {
    padding: 10px;
    background-color: #f9f9f9;
    border-radius: 4px;
    line-height: 1.6;
    white-space: pre-line;
    min-height: 40px;
  }
  
  .dialog-footer {
    margin-top: 20px;
    display: flex;
    justify-content: center;
    gap: 10px;
  }
}
</style>