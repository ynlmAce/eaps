<template>
  <div class="position-audit-container">
    <div class="page-header">
      <div class="page-title">职位审核管理</div>
      <div class="operation-btns">
        <el-button type="primary" @click="batchApprove" :disabled="selectedRows.length === 0">
          <el-icon><Check /></el-icon>
          批量通过
        </el-button>
        <el-button type="danger" @click="openBatchRejectDialog" :disabled="selectedRows.length === 0">
          <el-icon><Close /></el-icon>
          批量拒绝
        </el-button>
      </div>
    </div>
    
    <!-- 筛选卡片 -->
    <el-card class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="职位名称/企业名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="职位类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable>
            <el-option
              v-for="item in positionTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="行业">
          <el-select v-model="searchForm.industry" placeholder="全部行业" clearable>
            <el-option
              v-for="item in industryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 职位列表 -->
    <el-card class="table-card">
      <div class="table-operations">
        <div class="selected-count" v-if="selectedRows.length > 0">
          已选择 <span class="count">{{ selectedRows.length }}</span> 项
        </div>
        <div class="right-operations">
          <el-tooltip content="刷新" placement="top">
            <el-button :icon="Refresh" circle @click="refreshTable" />
          </el-tooltip>
          <el-tooltip content="列设置" placement="top">
            <el-button :icon="SetUp" circle @click="showColumnSettings = true" />
          </el-tooltip>
        </div>
      </div>
      
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" fixed="left" align="center" />
        
        <el-table-column prop="id" label="ID" width="80" align="center" v-if="columnVisible.id" />
        
        <el-table-column label="职位名称" prop="name" min-width="180" v-if="columnVisible.basic">
          <template #default="{ row }">
            <span class="position-name" @click="viewPositionDetail(row.id)">{{ row.name }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="企业信息" min-width="200" v-if="columnVisible.enterprise">
          <template #default="{ row }">
            <div class="enterprise-info" v-if="row.enterprise">
              <el-avatar 
                :size="32" 
                :src="row.enterprise.logo"
                @click="viewEnterpriseDetail(row.enterprise.id)"
              />
              <div class="enterprise-meta">
                <div class="enterprise-name" @click="viewEnterpriseDetail(row.enterprise.id)">
                  {{ row.enterprise.name }}
                </div>
                <div class="enterprise-industry">{{ row.enterprise.industry }}</div>
              </div>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="职位类型" prop="type" width="120" v-if="columnVisible.basic">
          <template #default="{ row }">
            {{ getPositionTypeText(row.type) }}
          </template>
        </el-table-column>
        
        <el-table-column label="工作地点" prop="location" width="160" v-if="columnVisible.detail">
          <template #default="{ row }">
            {{ row.city }}{{ row.district ? ` - ${row.district}` : '' }}
          </template>
        </el-table-column>
        
        <el-table-column label="薪资范围" width="150" v-if="columnVisible.detail">
          <template #default="{ row }">
            {{ formatSalary(row.salaryMin, row.salaryMax, row.salaryUnit) }}
          </template>
        </el-table-column>
        
        <el-table-column label="学历要求" prop="education" width="120" v-if="columnVisible.detail">
          <template #default="{ row }">
            {{ getEducationText(row.education) }}
          </template>
        </el-table-column>
        
        <el-table-column label="经验要求" prop="experience" width="120" v-if="columnVisible.detail">
          <template #default="{ row }">
            {{ getExperienceText(row.experience) }}
          </template>
        </el-table-column>
        
        <el-table-column label="提交时间" width="180" align="center" v-if="columnVisible.time">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" fixed="right" width="180" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button 
                link 
                type="primary" 
                @click="viewPositionDetail(row.id)"
              >
                查看
              </el-button>
              
              <el-button 
                link 
                type="success" 
                @click="approvePosition(row)"
              >
                通过
              </el-button>
              
              <el-button 
                link 
                type="danger" 
                @click="rejectPosition(row)"
              >
                拒绝
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-form :model="rejectForm" ref="rejectFormRef" :rules="rejectRules">
        <el-form-item label="拒绝原因" prop="reason" label-width="80px">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因，将通知企业"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 职位详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="职位详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="职位名称" :span="2">{{ currentPosition.name }}</el-descriptions-item>
        <el-descriptions-item label="企业名称" :span="2">
          <a @click="viewEnterpriseDetail(currentPosition.enterprise?.id)" class="link-text">
            {{ currentPosition.enterprise?.name }}
          </a>
        </el-descriptions-item>
        <el-descriptions-item label="职位类型">{{ getPositionTypeText(currentPosition.type) }}</el-descriptions-item>
        <el-descriptions-item label="招聘人数">{{ currentPosition.count || '若干' }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">
          {{ currentPosition.city }}{{ currentPosition.district ? ` - ${currentPosition.district}` : '' }}
        </el-descriptions-item>
        <el-descriptions-item label="薪资范围">
          {{ formatSalary(currentPosition.salaryMin, currentPosition.salaryMax, currentPosition.salaryUnit) }}
        </el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ getEducationText(currentPosition.education) }}</el-descriptions-item>
        <el-descriptions-item label="经验要求">{{ getExperienceText(currentPosition.experience) }}</el-descriptions-item>
        <el-descriptions-item label="职位标签" :span="2">
          <el-tag 
            v-for="(tag, index) in currentPosition.tags" 
            :key="index"
            class="position-tag"
          >
            {{ tag }}
          </el-tag>
          <span v-if="!currentPosition.tags || currentPosition.tags.length === 0">无</span>
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="position-description">
        <div class="section-title">职位描述</div>
        <div class="description-content">{{ currentPosition.description || '无' }}</div>
      </div>
      
      <div class="position-requirement">
        <div class="section-title">任职要求</div>
        <div class="requirement-content">{{ currentPosition.requirement || '无' }}</div>
      </div>
      
      <div class="position-benefit">
        <div class="section-title">福利待遇</div>
        <div class="benefit-content">{{ currentPosition.benefit || '无' }}</div>
      </div>
      
      <div class="position-remark" v-if="currentPosition.remark">
        <div class="section-title">备注说明</div>
        <div class="remark-content">{{ currentPosition.remark }}</div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button type="success" @click="approvePosition(currentPosition)">通过</el-button>
          <el-button type="danger" @click="rejectPosition(currentPosition)">拒绝</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 列设置对话框 -->
    <el-drawer
      v-model="showColumnSettings"
      title="列设置"
      direction="rtl"
      size="300px"
    >
      <el-divider content-position="left">显示列</el-divider>
      <div class="column-settings">
        <el-checkbox v-model="columnVisible.id">ID</el-checkbox>
        <el-checkbox v-model="columnVisible.basic">基本信息</el-checkbox>
        <el-checkbox v-model="columnVisible.enterprise">企业信息</el-checkbox>
        <el-checkbox v-model="columnVisible.detail">详细要求</el-checkbox>
        <el-checkbox v-model="columnVisible.time">时间信息</el-checkbox>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Check, 
  Close, 
  SetUp 
} from '@element-plus/icons-vue'
import { 
  getPendingPositionList, 
  verifyPosition,
  batchVerifyPositions
} from '@/api/admin'

const router = useRouter()

// 状态相关
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref([])
const dateRange = ref([])
const submitting = ref(false)

// 显示配置
const columnVisible = reactive({
  id: true,
  basic: true,
  enterprise: true,
  detail: true,
  time: true
})
const showColumnSettings = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  type: '',
  industry: ''
})

// 职位类型选项
const positionTypeOptions = [
  { label: '全职', value: 'FULL_TIME' },
  { label: '兼职', value: 'PART_TIME' },
  { label: '实习', value: 'INTERNSHIP' },
  { label: '校招', value: 'CAMPUS' },
  { label: '其他', value: 'OTHER' }
]

// 行业选项
const industryOptions = [
  { label: 'IT/互联网', value: 'IT' },
  { label: '金融/银行', value: 'FINANCE' },
  { label: '教育培训', value: 'EDUCATION' },
  { label: '医疗健康', value: 'MEDICAL' },
  { label: '制造业', value: 'MANUFACTURE' },
  { label: '房地产', value: 'ESTATE' },
  { label: '文化传媒', value: 'MEDIA' },
  { label: '零售/贸易', value: 'TRADING' },
  { label: '其他行业', value: 'OTHER' }
]

// 拒绝相关
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  id: null,
  reason: '',
  batch: false,
  ids: []
})
const rejectFormRef = ref(null)
const rejectRules = {
  reason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]
}

// 详情相关
const detailDialogVisible = ref(false)
const currentPosition = ref({})

// 获取职位列表
const fetchPositionList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value,
      keyword: searchForm.keyword,
      type: searchForm.type,
      industry: searchForm.industry,
      status: 0 // 0表示待审核
    }
    
    // 添加日期范围参数
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const res = await getPendingPositionList(params)
    tableData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取职位列表失败:', error)
    ElMessage.error('获取职位列表失败')
  } finally {
    loading.value = false
  }
}

// 处理筛选和分页
const handleSearch = () => {
  currentPage.value = 1
  fetchPositionList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    type: '',
    industry: ''
  })
  dateRange.value = []
  currentPage.value = 1
  fetchPositionList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchPositionList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchPositionList()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const refreshTable = () => {
  fetchPositionList()
}

// 查看详情
const viewPositionDetail = (id) => {
  // 从当前数据中查找职位信息
  const position = tableData.value.find(item => item.id === id)
  if (position) {
    currentPosition.value = position
    detailDialogVisible.value = true
  } else {
    ElMessage.warning('找不到该职位信息')
  }
}

const viewEnterpriseDetail = (id) => {
  if (!id) return
  router.push(`/enterprise/${id}`)
}

// 格式化函数
const formatDateTime = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatSalary = (min, max, unit = 'MONTH') => {
  if (!min && !max) return '面议'
  
  const formatNumber = (num) => {
    if (num >= 10000) {
      return (num / 10000).toFixed(1) + '万'
    }
    return num
  }
  
  let result = ''
  if (min && max) {
    result = `${formatNumber(min)}-${formatNumber(max)}`
  } else if (min) {
    result = `${formatNumber(min)}以上`
  } else if (max) {
    result = `${formatNumber(max)}以下`
  }
  
  const unitText = unit === 'YEAR' ? '年' : '月'
  return `${result} 元/${unitText}`
}

const getPositionTypeText = (type) => {
  const typeMap = {
    'FULL_TIME': '全职',
    'PART_TIME': '兼职',
    'INTERNSHIP': '实习',
    'CAMPUS': '校招',
    'OTHER': '其他'
  }
  return typeMap[type] || type
}

const getEducationText = (edu) => {
  const eduMap = {
    'PRIMARY': '小学',
    'JUNIOR': '初中',
    'HIGH': '高中',
    'COLLEGE': '大专',
    'BACHELOR': '本科',
    'MASTER': '硕士',
    'DOCTOR': '博士',
    'NO_LIMIT': '不限'
  }
  return eduMap[edu] || edu
}

const getExperienceText = (exp) => {
  const expMap = {
    'NO_EXPERIENCE': '无经验',
    'LESS_THAN_ONE_YEAR': '1年以下',
    'ONE_TO_THREE_YEARS': '1-3年',
    'THREE_TO_FIVE_YEARS': '3-5年',
    'FIVE_TO_TEN_YEARS': '5-10年',
    'MORE_THAN_TEN_YEARS': '10年以上',
    'NO_LIMIT': '不限'
  }
  return expMap[exp] || exp
}

// 审核相关
const approvePosition = async (row) => {
  try {
    await ElMessageBox.confirm('确定通过该职位的审核吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await verifyPosition(row.id, {
      status: 1, // 1表示通过
      reason: ''
    })
    ElMessage.success('职位审核通过成功')
    detailDialogVisible.value = false
    refreshTable()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  }
}

const batchApprove = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要批量通过的职位')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定批量通过选中的 ${selectedRows.value.length} 个职位吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await batchVerifyPositions({
      ids: selectedRows.value.map(row => row.id),
      status: 1,
      reason: ''
    })
    
    ElMessage.success('批量审核通过成功')
    refreshTable()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败')
    }
  }
}

const rejectPosition = (row) => {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectForm.batch = false
  rejectForm.ids = []
  rejectDialogVisible.value = true
}

const openBatchRejectDialog = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要批量拒绝的职位')
    return
  }
  
  rejectForm.id = null
  rejectForm.reason = ''
  rejectForm.batch = true
  rejectForm.ids = selectedRows.value.map(row => row.id)
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  try {
    await rejectFormRef.value.validate()
    
    submitting.value = true
    
    if (rejectForm.batch) {
      // 批量拒绝
      await batchVerifyPositions({
        ids: rejectForm.ids,
        status: 2, // 2表示拒绝
        reason: rejectForm.reason
      })
      ElMessage.success('批量拒绝操作成功')
    } else {
      // 单个拒绝
      await verifyPosition(rejectForm.id, {
        status: 2,
        reason: rejectForm.reason
      })
      ElMessage.success('拒绝操作成功')
    }
    
    rejectDialogVisible.value = false
    detailDialogVisible.value = false
    refreshTable()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝操作失败:', error)
      ElMessage.error('拒绝操作失败')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchPositionList()
})
</script>

<style lang="scss" scoped>
.position-audit-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .page-title {
      font-size: 22px;
      font-weight: 500;
      color: #303133;
    }
    
    .operation-btns {
      .el-button {
        margin-left: 10px;
      }
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
    padding: 20px;
  }
  
  .table-card {
    margin-bottom: 20px;
    
    .table-operations {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      .selected-count {
        .count {
          color: #409EFF;
          font-weight: bold;
        }
      }
      
      .right-operations {
        .el-button {
          margin-left: 8px;
        }
      }
    }
    
    .position-name {
      color: #409EFF;
      cursor: pointer;
      font-weight: 500;
      
      &:hover {
        text-decoration: underline;
      }
    }
    
    .enterprise-info {
      display: flex;
      align-items: center;
      
      .el-avatar {
        flex-shrink: 0;
        cursor: pointer;
      }
      
      .enterprise-meta {
        margin-left: 10px;
        
        .enterprise-name {
          font-weight: 500;
          color: #409EFF;
          cursor: pointer;
          margin-bottom: 4px;
          
          &:hover {
            text-decoration: underline;
          }
        }
        
        .enterprise-industry {
          font-size: 12px;
          color: #909399;
        }
      }
    }
    
    .table-actions {
      white-space: nowrap;
      display: flex;
      justify-content: center;
    }
    
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
  
  .position-description,
  .position-requirement,
  .position-benefit,
  .position-remark {
    margin-top: 20px;
    
    .section-title {
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 10px;
      padding-left: 8px;
      border-left: 3px solid #409EFF;
    }
    
    .description-content,
    .requirement-content,
    .benefit-content,
    .remark-content {
      padding: 10px;
      line-height: 1.6;
      white-space: pre-line;
      color: #606266;
      background-color: #f5f7fa;
      border-radius: 4px;
    }
  }
  
  .position-tag {
    margin-right: 8px;
    margin-bottom: 5px;
  }
  
  .link-text {
    color: #409EFF;
    cursor: pointer;
    
    &:hover {
      text-decoration: underline;
    }
  }
  
  .column-settings {
    display: flex;
    flex-direction: column;
    gap: 15px;
    padding: 20px;
    
    .el-checkbox {
      margin-right: 0;
    }
  }
}
</style> 