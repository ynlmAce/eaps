<template>
  <div class="enterprise-audit-container">
    <div class="page-header">
      <div class="page-title">企业审核管理</div>
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
            placeholder="企业名称/联系人"
            clearable
            @keyup.enter="handleSearch"
          />
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
        
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="所有状态" clearable>
            <el-option
              v-for="item in statusOptions"
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
    
    <!-- 企业列表 -->
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
        
        <el-table-column label="企业信息" min-width="200" v-if="columnVisible.basic">
          <template #default="{ row }">
            <div class="enterprise-info">
              <el-avatar 
                :size="40" 
                :src="row.logo"
                @click="viewEnterpriseDetail(row.id)"
              />
              <div class="enterprise-meta">
                <div class="enterprise-name" @click="viewEnterpriseDetail(row.id)">
                  {{ row.name }}
                </div>
                <div class="enterprise-industry">{{ row.industry }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="联系人" prop="contactName" width="120" v-if="columnVisible.contact" />
        
        <el-table-column label="联系电话" prop="contactPhone" width="130" v-if="columnVisible.contact" />
        
        <el-table-column label="企业规模" prop="size" width="120" v-if="columnVisible.basic">
          <template #default="{ row }">
            {{ getEnterpriseSize(row.size) }}
          </template>
        </el-table-column>
        
        <el-table-column label="注册时间" width="180" align="center" v-if="columnVisible.time">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="100" align="center" v-if="columnVisible.status">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="营业执照" width="120" align="center" v-if="columnVisible.license">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="viewDocument(row.businessLicense)"
              v-if="row.businessLicense"
            >
              查看
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="企业资质" width="120" align="center" v-if="columnVisible.license">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              @click="viewDocuments(row.qualifications)"
              v-if="row.qualifications && row.qualifications.length > 0"
            >
              查看 ({{ row.qualifications.length }})
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" fixed="right" width="200" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button 
                link 
                type="primary" 
                @click="viewEnterpriseDetail(row.id)"
              >
                查看
              </el-button>
              
              <el-button 
                v-if="row.status === 'PENDING'" 
                link 
                type="success" 
                @click="approveEnterprise(row)"
              >
                通过
              </el-button>
              
              <el-button 
                v-if="row.status === 'PENDING'" 
                link 
                type="danger" 
                @click="rejectEnterprise(row)"
              >
                拒绝
              </el-button>
              
              <el-dropdown v-if="row.status !== 'PENDING'">
                <el-button link type="primary">
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="toggleEnterpriseStatus(row)">
                      {{ row.status === 'APPROVED' ? '禁用企业' : '启用企业' }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="resetPassword(row)" divided>
                      重置密码
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
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
    
    <!-- 资质文件预览对话框 -->
    <el-dialog v-model="documentDialogVisible" :title="documentDialogTitle" width="800px">
      <div class="document-preview">
        <template v-if="previewDocuments.length > 0">
          <el-carousel height="500px" indicator-position="outside" v-if="previewDocuments.length > 1">
            <el-carousel-item v-for="(doc, index) in previewDocuments" :key="index">
              <div class="document-item">
                <el-image
                  :src="doc.url"
                  fit="contain"
                  style="height: 450px; width: 100%"
                  :preview-src-list="previewDocuments.map(d => d.url)"
                />
                <div class="document-title">{{ doc.name || `文件 ${index + 1}` }}</div>
              </div>
            </el-carousel-item>
          </el-carousel>
          
          <div v-else class="single-document">
            <el-image
              :src="previewDocuments[0].url"
              fit="contain"
              style="height: 450px; width: 100%"
              :preview-src-list="previewDocuments.map(d => d.url)"
            />
            <div class="document-title">{{ previewDocuments[0].name || '文件' }}</div>
          </div>
        </template>
        
        <div v-else class="empty-documents">
          <el-empty description="无法预览文件" />
        </div>
      </div>
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
        <el-checkbox v-model="columnVisible.basic">企业基本信息</el-checkbox>
        <el-checkbox v-model="columnVisible.contact">联系人信息</el-checkbox>
        <el-checkbox v-model="columnVisible.license">资质证件</el-checkbox>
        <el-checkbox v-model="columnVisible.status">状态</el-checkbox>
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
  SetUp,
  ArrowDown
} from '@element-plus/icons-vue'
// 需要导入API
import { 
  getPendingEnterpriseList, 
  verifyEnterprise,
  resetEnterprisePassword,
  toggleEnterpriseStatus
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
  contact: true,
  license: true,
  status: true,
  time: true
})
const showColumnSettings = ref(false)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  industry: '',
  status: 'PENDING'
})

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

// 状态选项
const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' },
  { label: '已禁用', value: 'DISABLED' }
]

// 企业规模选项
const getEnterpriseSize = (size) => {
  const sizeMap = {
    'MINI': '微型企业(10人以下)',
    'SMALL': '小型企业(10-99人)',
    'MEDIUM': '中型企业(100-499人)',
    'LARGE': '大型企业(500人以上)',
    'VERY_LARGE': '特大型企业(1000人以上)'
  }
  return sizeMap[size] || size
}

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

// 文档预览相关
const documentDialogVisible = ref(false)
const documentDialogTitle = ref('文件预览')
const previewDocuments = ref([])

// 获取企业列表
const fetchEnterpriseList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value,
      keyword: searchForm.keyword,
      industry: searchForm.industry,
      status: searchForm.status
    }
    
    // 添加日期范围参数
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    
    const res = await getPendingEnterpriseList(params)
    tableData.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取企业列表失败:', error)
    ElMessage.error('获取企业列表失败')
  } finally {
    loading.value = false
  }
}

// 处理筛选和分页
const handleSearch = () => {
  currentPage.value = 1
  fetchEnterpriseList()
}

const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    industry: '',
    status: 'PENDING'
  })
  dateRange.value = []
  currentPage.value = 1
  fetchEnterpriseList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchEnterpriseList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchEnterpriseList()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const refreshTable = () => {
  fetchEnterpriseList()
}

// 查看企业详情
const viewEnterpriseDetail = (id) => {
  router.push(`/enterprise/${id}`)
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'APPROVED': return 'success'
    case 'PENDING': return 'warning'
    case 'REJECTED': return 'danger'
    case 'DISABLED': return 'info'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'APPROVED': return '已通过'
    case 'PENDING': return '待审核'
    case 'REJECTED': return '已拒绝'
    case 'DISABLED': return '已禁用'
    default: return '未知状态'
  }
}

// 格式化日期时间
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

// 查看文档
const viewDocument = (url) => {
  if (!url) {
    ElMessage.warning('文件不存在')
    return
  }
  
  documentDialogTitle.value = '营业执照'
  previewDocuments.value = [{ url, name: '营业执照' }]
  documentDialogVisible.value = true
}

const viewDocuments = (docs) => {
  if (!docs || docs.length === 0) {
    ElMessage.warning('文件不存在')
    return
  }
  
  documentDialogTitle.value = '企业资质'
  previewDocuments.value = docs.map((doc, index) => ({
    url: doc.url,
    name: doc.name || `资质文件 ${index + 1}`
  }))
  documentDialogVisible.value = true
}

// 审核通过企业
const approveEnterprise = async (row) => {
  try {
    await ElMessageBox.confirm('确定通过该企业的审核吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await verifyEnterprise(row.id, {
      verifyStatus: 1, // 1表示通过
      verifyComment: ''
    })
    ElMessage.success('企业审核通过成功')
    refreshTable()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  }
}

// 批量通过
const batchApprove = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要批量通过的企业')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定批量通过选中的 ${selectedRows.value.length} 家企业吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const promises = selectedRows.value.map(row => 
      verifyEnterprise(row.id, {
        verifyStatus: 1,
        verifyComment: ''
      })
    )
    
    await Promise.all(promises)
    ElMessage.success('批量审核通过成功')
    refreshTable()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量审核失败:', error)
      ElMessage.error('批量审核失败')
    }
  }
}

// 拒绝企业
const rejectEnterprise = (row) => {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectForm.batch = false
  rejectForm.ids = []
  rejectDialogVisible.value = true
}

// 批量拒绝
const openBatchRejectDialog = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要批量拒绝的企业')
    return
  }
  
  rejectForm.id = null
  rejectForm.reason = ''
  rejectForm.batch = true
  rejectForm.ids = selectedRows.value.map(row => row.id)
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  try {
    await rejectFormRef.value.validate()
    
    submitting.value = true
    
    if (rejectForm.batch) {
      // 批量拒绝
      const promises = rejectForm.ids.map(id => 
        verifyEnterprise(id, {
          verifyStatus: 2, // 2表示拒绝
          verifyComment: rejectForm.reason
        })
      )
      
      await Promise.all(promises)
      ElMessage.success('批量拒绝操作成功')
    } else {
      // 单个拒绝
      await verifyEnterprise(rejectForm.id, {
        verifyStatus: 2,
        verifyComment: rejectForm.reason
      })
      ElMessage.success('拒绝操作成功')
    }
    
    rejectDialogVisible.value = false
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

// 切换企业状态
const toggleEnterpriseStatus = async (row) => {
  try {
    const isEnable = row.status === 'DISABLED'
    const message = isEnable ? '确定启用该企业吗？' : '确定禁用该企业吗？'
    const successMsg = isEnable ? '企业已成功启用' : '企业已成功禁用'
    
    await ElMessageBox.confirm(message, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await toggleEnterpriseStatus(row.id, {
      status: isEnable ? 'APPROVED' : 'DISABLED'
    })
    
    ElMessage.success(successMsg)
    refreshTable()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 重置密码
const resetPassword = async (row) => {
  try {
    await ElMessageBox.confirm('确定要重置该企业账号的密码吗？密码将重置为默认密码', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await resetEnterprisePassword(row.id)
    ElMessage.success('密码重置成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

onMounted(() => {
  fetchEnterpriseList()
})
</script>

<style lang="scss" scoped>
.enterprise-audit-container {
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
  
  .document-preview {
    .document-item {
      height: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    
    .single-document {
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    
    .document-title {
      margin-top: 10px;
      font-size: 14px;
      color: #606266;
    }
    
    .empty-documents {
      display: flex;
      justify-content: center;
      align-items: center;
      height: 300px;
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