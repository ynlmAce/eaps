<template>
  <div class="promotion-manage-container">
    <div class="page-header">
      <div class="page-title">企业宣传管理</div>
      <div class="operation-btns">
        <el-button type="primary" @click="batchApprove" :disabled="selectedRows.length === 0">
          <el-icon><Check /></el-icon>
          批量通过
        </el-button>
        <el-button type="danger" @click="batchReject" :disabled="selectedRows.length === 0">
          <el-icon><Close /></el-icon>
          批量拒绝
        </el-button>
      </div>
    </div>
    
    <div class="filter-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="标题/企业名称"
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
    </div>
    
    <div class="table-card">
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
        
        <el-table-column label="标题" prop="title" min-width="220" show-overflow-tooltip v-if="columnVisible.title">
          <template #default="{ row }">
            <div class="promotion-title">
              <el-tag v-if="row.isTop" type="danger" size="small" effect="dark" class="top-tag">
                置顶
              </el-tag>
              <span class="text" @click="viewPromotionDetail(row.id)">{{ row.title }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="封面" width="120" align="center" v-if="columnVisible.coverImage">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              fit="cover"
              style="width: 100px; height: 56px; border-radius: 4px"
              :preview-src-list="[row.coverImage]"
            />
            <span v-else>-</span>
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
        
        <el-table-column label="状态" width="100" align="center" v-if="columnVisible.status">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="浏览量" prop="viewCount" width="100" align="center" v-if="columnVisible.viewCount" />
        
        <el-table-column label="创建时间" width="180" align="center" v-if="columnVisible.createTime">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="更新时间" width="180" align="center" v-if="columnVisible.updateTime">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" fixed="right" width="200" align="center">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button 
                link 
                type="primary" 
                @click="viewPromotionDetail(row.id)"
              >
                查看
              </el-button>
              
              <el-button 
                v-if="row.status === 'PENDING'" 
                link 
                type="success" 
                @click="approvePromotion(row)"
              >
                通过
              </el-button>
              
              <el-button 
                v-if="row.status === 'PENDING'" 
                link 
                type="danger" 
                @click="rejectPromotion(row)"
              >
                拒绝
              </el-button>
              
              <el-dropdown v-if="row.status === 'APPROVED'">
                <el-button link type="primary">
                  更多<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="setTopStatus(row, !row.isTop)">
                      {{ row.isTop ? '取消置顶' : '设为置顶' }}
                    </el-dropdown-item>
                    <el-dropdown-item @click="deletePromotion(row)" divided>
                      <span style="color: var(--el-color-danger)">删除</span>
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
    </div>
    
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
    
    <!-- 列设置对话框 -->
    <el-drawer v-model="showColumnSettings" title="列设置" direction="rtl" size="300px">
      <div class="column-settings">
        <p class="setting-tip">勾选需要显示的列：</p>
        <el-checkbox-group v-model="visibleColumns">
          <div v-for="col in allColumns" :key="col.prop" class="column-item">
            <el-checkbox :label="col.prop">{{ col.label }}</el-checkbox>
          </div>
        </el-checkbox-group>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Check, Close, ArrowDown, SetUp
} from '@element-plus/icons-vue'
import { 
  getEnterprisePromotionList, 
  reviewPromotion,
  setPromotionTopStatus,
  deleteEnterprisePromotion
} from '@/api/enterprise'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const selectedRows = ref([])
const rejectDialogVisible = ref(false)
const submitting = ref(false)
const showColumnSettings = ref(false)
const dateRange = ref([])

const rejectForm = reactive({
  id: null,
  reason: '',
  batch: false,
  ids: []
})

const rejectRules = {
  reason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { min: 5, max: 200, message: '拒绝原因长度应在5-200个字符之间', trigger: 'blur' }
  ]
}

const searchForm = reactive({
  keyword: '',
  industry: '',
  status: 'PENDING', // 默认显示待审核
  startDate: '',
  endDate: ''
})

// 监听日期范围变化
watch(dateRange, (newVal) => {
  if (newVal) {
    searchForm.startDate = newVal[0] || ''
    searchForm.endDate = newVal[1] || ''
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
})

const industryOptions = [
  { label: '互联网/IT', value: 'IT' },
  { label: '金融/银行', value: 'FINANCE' },
  { label: '教育/培训', value: 'EDUCATION' },
  { label: '医疗/健康', value: 'HEALTHCARE' },
  { label: '制造业', value: 'MANUFACTURING' },
  { label: '房地产', value: 'REAL_ESTATE' },
  { label: '文化/传媒', value: 'MEDIA' },
  { label: '其他行业', value: 'OTHER' }
]

const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已拒绝', value: 'REJECTED' }
]

// 列设置
const allColumns = [
  { prop: 'id', label: 'ID' },
  { prop: 'title', label: '标题' },
  { prop: 'coverImage', label: '封面' },
  { prop: 'enterprise', label: '企业信息' },
  { prop: 'status', label: '状态' },
  { prop: 'viewCount', label: '浏览量' },
  { prop: 'createTime', label: '创建时间' },
  { prop: 'updateTime', label: '更新时间' },
]

const visibleColumns = ref(['id', 'title', 'coverImage', 'enterprise', 'status', 'viewCount', 'createTime'])

const columnVisible = computed(() => {
  const result = {}
  allColumns.forEach(col => {
    result[col.prop] = visibleColumns.value.includes(col.prop)
  })
  return result
})

// 获取宣传列表
const fetchPromotionList = async () => {
  loading.value = true
  
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    
    const res = await getEnterprisePromotionList(params)
    
    if (res && res.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取宣传列表失败:', error)
    ElMessage.error('获取宣传列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPromotionList()
}

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = ''
  searchForm.industry = ''
  searchForm.status = 'PENDING'
  dateRange.value = []
  searchForm.startDate = ''
  searchForm.endDate = ''
  handleSearch()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchPromotionList()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchPromotionList()
}

// 刷新表格
const refreshTable = () => {
  fetchPromotionList()
}

// 处理选择变化
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 查看宣传详情
const viewPromotionDetail = (id) => {
  router.push(`/enterprise/promotion/${id}`)
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
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'APPROVED': return '已通过'
    case 'PENDING': return '待审核'
    case 'REJECTED': return '已拒绝'
    default: return '未知状态'
  }
}

// 审核通过宣传
const approvePromotion = async (row) => {
  try {
    await reviewPromotion(row.id, {
      approved: true
    })
    ElMessage.success('审核通过成功')
    refreshTable()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

// 拒绝宣传
const rejectPromotion = (row) => {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectForm.batch = false
  rejectForm.ids = []
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  submitting.value = true
  
  try {
    if (rejectForm.batch) {
      // 批量拒绝
      await Promise.all(rejectForm.ids.map(id => 
        reviewPromotion(id, {
          approved: false,
          reason: rejectForm.reason
        })
      ))
      ElMessage.success(`已拒绝 ${rejectForm.ids.length} 条宣传`)
    } else {
      // 单条拒绝
      await reviewPromotion(rejectForm.id, {
        approved: false,
        reason: rejectForm.reason
      })
      ElMessage.success('已拒绝该宣传')
    }
    
    rejectDialogVisible.value = false
    refreshTable()
  } catch (error) {
    console.error('拒绝失败:', error)
    ElMessage.error('拒绝失败')
  } finally {
    submitting.value = false
  }
}

// 批量通过
const batchApprove = async () => {
  if (selectedRows.value.length === 0) return
  
  ElMessageBox.confirm(`确定要批量通过 ${selectedRows.value.length} 条宣传吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const pendingRows = selectedRows.value.filter(row => row.status === 'PENDING')
      
      if (pendingRows.length === 0) {
        ElMessage.warning('所选项中没有待审核的宣传')
        return
      }
      
      await Promise.all(pendingRows.map(row => 
        reviewPromotion(row.id, {
          approved: true
        })
      ))
      
      ElMessage.success(`已通过 ${pendingRows.length} 条宣传`)
      refreshTable()
    } catch (error) {
      console.error('批量通过失败:', error)
      ElMessage.error('批量通过失败')
    }
  }).catch(() => {})
}

// 批量拒绝
const batchReject = () => {
  if (selectedRows.value.length === 0) return
  
  const pendingRows = selectedRows.value.filter(row => row.status === 'PENDING')
  
  if (pendingRows.length === 0) {
    ElMessage.warning('所选项中没有待审核的宣传')
    return
  }
  
  rejectForm.id = null
  rejectForm.reason = ''
  rejectForm.batch = true
  rejectForm.ids = pendingRows.map(row => row.id)
  rejectDialogVisible.value = true
}

// 设置置顶状态
const setTopStatus = async (row, isTop) => {
  try {
    await setPromotionTopStatus(row.id, { isTop })
    ElMessage.success(isTop ? '置顶成功' : '取消置顶成功')
    refreshTable()
  } catch (error) {
    console.error('设置置顶状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 删除宣传
const deletePromotion = (row) => {
  ElMessageBox.confirm(`确定要删除宣传"${row.title}"吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteEnterprisePromotion(row.id)
      ElMessage.success('删除成功')
      refreshTable()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 组件挂载时获取数据
onMounted(() => {
  fetchPromotionList()
})
</script>

<style lang="scss" scoped>
.promotion-manage-container {
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
      display: flex;
      gap: 10px;
      
      .el-button {
        .el-icon {
          margin-right: 5px;
        }
      }
    }
  }
  
  .filter-card {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 20px;
    margin-bottom: 20px;
  }
  
  .table-card {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 20px;
    
    .table-operations {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15px;
      
      .selected-count {
        font-size: 14px;
        color: #606266;
        
        .count {
          color: #409eff;
          font-weight: bold;
        }
      }
      
      .right-operations {
        display: flex;
        gap: 10px;
      }
    }
    
    .promotion-title {
      display: flex;
      align-items: center;
      
      .top-tag {
        margin-right: 5px;
      }
      
      .text {
        color: #409eff;
        cursor: pointer;
        
        &:hover {
          text-decoration: underline;
        }
      }
    }
    
    .enterprise-info {
      display: flex;
      align-items: center;
      
      .el-avatar {
        margin-right: 10px;
        cursor: pointer;
      }
      
      .enterprise-meta {
        .enterprise-name {
          font-size: 14px;
          color: #409eff;
          margin-bottom: 3px;
          cursor: pointer;
          
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
      display: flex;
      justify-content: center;
      gap: 8px;
    }
    
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}

.column-settings {
  padding: 10px;
  
  .setting-tip {
    margin-bottom: 20px;
    color: #606266;
  }
  
  .column-item {
    margin-bottom: 12px;
  }
}
</style> 