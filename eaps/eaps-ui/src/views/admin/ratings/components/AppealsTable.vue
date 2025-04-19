<template>
  <div class="appeals-table-container">
    <!-- 筛选卡片 -->
    <el-card class="filter-card">
      <el-form :model="filterForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="filterForm.keyword" placeholder="申诉内容/企业名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="申诉原因">
          <el-select v-model="filterForm.reason" clearable placeholder="选择申诉原因">
            <el-option label="内容不实" value="FALSE_INFORMATION" />
            <el-option label="恶意评价" value="MALICIOUS_RATING" />
            <el-option label="违规内容" value="ILLEGAL_CONTENT" />
            <el-option label="其他原因" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 申诉列表 -->
    <el-card class="appeals-list-card">
      <div class="table-operations" v-if="type === 'PENDING'">
        <el-button type="success" :disabled="selectedAppeals.length === 0" @click="batchApprove">批量通过</el-button>
        <el-button type="danger" :disabled="selectedAppeals.length === 0" @click="openBatchRejectDialog">批量拒绝</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="appealsList"
        @selection-change="handleSelectionChange"
        border
        style="width: 100%"
      >
        <el-table-column
          v-if="type === 'PENDING'"
          type="selection"
          width="55"
        />
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="企业" width="150">
          <template #default="{ row }">
            <div class="enterprise-info">
              <el-avatar :size="30" :src="row.enterpriseAvatar" />
              <span>{{ row.enterpriseName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评价ID" prop="ratingId" width="80" />
        <el-table-column label="申诉原因" width="120">
          <template #default="{ row }">
            <el-tag>{{ getReasonText(row.reason) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申诉内容" prop="content" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="160">
          <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewAppealDetail(row)">查看详情</el-button>
            <template v-if="type === 'PENDING'">
              <el-button link type="success" @click="approveAppeal(row.id)">通过</el-button>
              <el-button link type="danger" @click="openRejectDialog(row)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝申诉"
      width="500px"
    >
      <el-form :model="rejectForm">
        <el-form-item label="拒绝原因" :label-width="'120px'" required>
          <el-select v-model="rejectForm.reason" placeholder="请选择拒绝原因" style="width: 100%">
            <el-option label="证据不足" value="INSUFFICIENT_EVIDENCE" />
            <el-option label="理由不充分" value="INSUFFICIENT_REASON" />
            <el-option label="评价内容属实" value="RATING_IS_VALID" />
            <el-option label="其他原因" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" :label-width="'120px'">
          <el-input
            v-model="rejectForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入拒绝的详细原因..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 申诉详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      title="申诉详情"
      direction="rtl"
      size="60%"
    >
      <div v-if="currentAppeal" class="appeal-detail">
        <div class="appeal-header">
          <div class="appeal-enterprise">
            <el-avatar :size="50" :src="currentAppeal.enterpriseAvatar" />
            <div class="enterprise-name">{{ currentAppeal.enterpriseName }}</div>
          </div>
          <div class="appeal-info">
            <div class="appeal-status">
              <span>状态：</span>
              <el-tag :type="getStatusTagType(currentAppeal.status)">{{ getStatusText(currentAppeal.status) }}</el-tag>
            </div>
            <div class="appeal-time">提交时间：{{ formatDate(currentAppeal.createTime) }}</div>
          </div>
        </div>
        
        <el-divider content-position="left">相关评价</el-divider>
        
        <div v-if="relatedRating" class="related-rating">
          <div class="rating-user-info">
            <el-avatar :size="40" :src="relatedRating.userAvatar" />
            <span>{{ relatedRating.userName }}</span>
            <span class="rating-score">
              <el-rate v-model="relatedRating.rating" disabled text-color="#ff9900" />
            </span>
          </div>
          
          <div class="rating-content">
            {{ relatedRating.content }}
          </div>
          
          <div class="rating-images" v-if="relatedRating.images && relatedRating.images.length > 0">
            <el-image
              v-for="(img, index) in relatedRating.images"
              :key="index"
              :src="img"
              :preview-src-list="relatedRating.images"
              fit="cover"
              class="rating-image"
            />
          </div>
          
          <div class="rating-time">评价时间：{{ formatDate(relatedRating.createTime) }}</div>
        </div>
        
        <el-divider content-position="left">申诉信息</el-divider>
        
        <div class="appeal-reason">
          <div class="reason-title">申诉原因：</div>
          <el-tag>{{ getReasonText(currentAppeal.reason) }}</el-tag>
        </div>
        
        <div class="appeal-content">
          <div class="content-title">申诉详情：</div>
          <div class="content-text">{{ currentAppeal.content }}</div>
        </div>
        
        <div class="appeal-evidences" v-if="currentAppeal.evidences && currentAppeal.evidences.length > 0">
          <div class="evidences-title">证据材料：</div>
          <div class="evidences-list">
            <el-image
              v-for="(img, index) in currentAppeal.evidences"
              :key="index"
              :src="img"
              :preview-src-list="currentAppeal.evidences"
              fit="cover"
              class="evidence-image"
            />
          </div>
        </div>
        
        <div class="appeal-result" v-if="currentAppeal.status !== 'PENDING'">
          <div class="result-title">处理结果：</div>
          <div class="result-content">
            <el-tag :type="currentAppeal.status === 'APPROVED' ? 'success' : 'danger'">
              {{ currentAppeal.status === 'APPROVED' ? '申诉通过' : '申诉拒绝' }}
            </el-tag>
            <div v-if="currentAppeal.status === 'REJECTED' && currentAppeal.rejectReason" class="reject-reason">
              <div>拒绝原因：{{ getRejectReasonText(currentAppeal.rejectReason) }}</div>
              <div v-if="currentAppeal.rejectComment">详细说明：{{ currentAppeal.rejectComment }}</div>
            </div>
          </div>
        </div>
        
        <div class="admin-actions" v-if="type === 'PENDING'">
          <el-button type="success" @click="approveAppeal(currentAppeal.id)">通过申诉</el-button>
          <el-button type="danger" @click="openRejectDialog(currentAppeal)">拒绝申诉</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAppealsList, 
  approveAppeal, 
  rejectAppeal, 
  batchApproveAppeals, 
  batchRejectAppeals,
  getAppealDetail,
  getRatingDetail
} from '@/api/admin'

const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (value) => ['PENDING', 'PROCESSED'].includes(value)
  }
})

// 状态相关
const loading = ref(false)
const appealsList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedAppeals = ref([])

// 筛选表单
const filterForm = reactive({
  keyword: '',
  reason: null,
  dateRange: null,
  status: props.type === 'PENDING' ? 'PENDING' : 'ALL'
})

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  id: null,
  reason: '',
  comment: '',
  batch: false,
  ids: []
})

// 详情抽屉
const detailDrawerVisible = ref(false)
const currentAppeal = ref(null)
const relatedRating = ref(null)

// 获取申诉列表
const fetchAppealsList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: filterForm.keyword,
      reason: filterForm.reason,
      status: filterForm.status
    }
    
    // 添加日期范围参数
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startDate = filterForm.dateRange[0]
      params.endDate = filterForm.dateRange[1]
    }
    
    const res = await getAppealsList(params)
    appealsList.value = res.data.content
    total.value = res.data.totalElements
  } catch (error) {
    console.error('获取申诉列表失败:', error)
    ElMessage.error('获取申诉列表失败')
  } finally {
    loading.value = false
  }
}

// 处理筛选和分页
const handleSearch = () => {
  currentPage.value = 1
  fetchAppealsList()
}

const resetFilter = () => {
  Object.assign(filterForm, {
    keyword: '',
    reason: null,
    dateRange: null,
    status: props.type === 'PENDING' ? 'PENDING' : 'ALL'
  })
  currentPage.value = 1
  fetchAppealsList()
}

const handleSizeChange = (val) => {
  pageSize.value = val
  fetchAppealsList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchAppealsList()
}

const handleSelectionChange = (selection) => {
  selectedAppeals.value = selection
}

// 状态和原因文本处理
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待处理',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return typeMap[status] || ''
}

const getReasonText = (reason) => {
  const reasonMap = {
    'FALSE_INFORMATION': '内容不实',
    'MALICIOUS_RATING': '恶意评价',
    'ILLEGAL_CONTENT': '违规内容',
    'OTHER': '其他原因'
  }
  return reasonMap[reason] || reason
}

const getRejectReasonText = (reason) => {
  const reasonMap = {
    'INSUFFICIENT_EVIDENCE': '证据不足',
    'INSUFFICIENT_REASON': '理由不充分',
    'RATING_IS_VALID': '评价内容属实',
    'OTHER': '其他原因'
  }
  return reasonMap[reason] || reason
}

// 查看详情
const viewAppealDetail = async (row) => {
  try {
    // 获取申诉详情
    const appealRes = await getAppealDetail(row.id)
    currentAppeal.value = appealRes.data
    
    // 获取相关评价详情
    const ratingRes = await getRatingDetail(row.ratingId)
    relatedRating.value = ratingRes.data
    
    detailDrawerVisible.value = true
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error('获取详情失败')
  }
}

// 通过申诉
const approveAppeal = async (id) => {
  try {
    await ElMessageBox.confirm('通过此申诉将同时删除相关评价，确定要通过此申诉吗？', '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await approveAppeal(id)
    ElMessage.success('申诉已通过')
    fetchAppealsList()
    
    if (detailDrawerVisible.value) {
      detailDrawerVisible.value = false
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 批量通过
const batchApprove = async () => {
  if (selectedAppeals.value.length === 0) {
    ElMessage.warning('请选择要批量通过的申诉')
    return
  }
  
  try {
    await ElMessageBox.confirm(`通过这些申诉将同时删除相关评价，确定批量通过选中的 ${selectedAppeals.value.length} 条申诉吗？`, '确认操作', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const ids = selectedAppeals.value.map(item => item.id)
    await batchApproveAppeals(ids)
    ElMessage.success('批量通过操作成功')
    fetchAppealsList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 拒绝申诉
const openRejectDialog = (row) => {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectForm.comment = ''
  rejectForm.batch = false
  rejectDialogVisible.value = true
}

const openBatchRejectDialog = () => {
  if (selectedAppeals.value.length === 0) {
    ElMessage.warning('请选择要批量拒绝的申诉')
    return
  }
  
  rejectForm.id = null
  rejectForm.reason = ''
  rejectForm.comment = ''
  rejectForm.batch = true
  rejectForm.ids = selectedAppeals.value.map(item => item.id)
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectForm.reason) {
    ElMessage.warning('请选择拒绝原因')
    return
  }
  
  try {
    if (rejectForm.batch) {
      await batchRejectAppeals({
        appealIds: rejectForm.ids,
        reason: rejectForm.reason,
        comment: rejectForm.comment
      })
      ElMessage.success('批量拒绝操作成功')
    } else {
      await rejectAppeal(rejectForm.id, {
        reason: rejectForm.reason,
        comment: rejectForm.comment
      })
      ElMessage.success('申诉已拒绝')
    }
    
    rejectDialogVisible.value = false
    fetchAppealsList()
    
    if (detailDrawerVisible.value) {
      detailDrawerVisible.value = false
    }
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString()
}

onMounted(() => {
  fetchAppealsList()
})
</script>

<style lang="scss" scoped>
.appeals-table-container {
  .filter-card {
    margin-bottom: 20px;
  }
  
  .appeals-list-card {
    .table-operations {
      margin-bottom: 15px;
      
      .el-button {
        margin-right: 10px;
      }
    }
  }
  
  .enterprise-info {
    display: flex;
    align-items: center;
    
    .el-avatar {
      margin-right: 8px;
    }
  }
  
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
  
  .appeal-detail {
    padding: 20px;
    
    .appeal-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 20px;
      
      .appeal-enterprise {
        display: flex;
        align-items: center;
        
        .enterprise-name {
          font-size: 18px;
          font-weight: 500;
          margin-left: 10px;
        }
      }
      
      .appeal-info {
        .appeal-status {
          display: flex;
          align-items: center;
          margin-bottom: 5px;
          
          span {
            margin-right: 8px;
          }
        }
        
        .appeal-time {
          color: #909399;
          font-size: 14px;
        }
      }
    }
    
    .related-rating {
      background-color: #f8f8f8;
      padding: 15px;
      border-radius: 4px;
      margin-bottom: 20px;
      
      .rating-user-info {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        
        span {
          margin-left: 10px;
        }
        
        .rating-score {
          margin-left: auto;
        }
      }
      
      .rating-content {
        padding: 10px 0;
        line-height: 1.6;
      }
      
      .rating-images {
        display: flex;
        flex-wrap: wrap;
        margin-top: 10px;
        
        .rating-image {
          width: 80px;
          height: 80px;
          margin-right: 10px;
          margin-bottom: 10px;
          border-radius: 4px;
          cursor: pointer;
        }
      }
      
      .rating-time {
        margin-top: 10px;
        font-size: 12px;
        color: #909399;
        text-align: right;
      }
    }
    
    .appeal-reason, .appeal-content, .appeal-evidences, .appeal-result {
      margin-bottom: 20px;
      
      .reason-title, .content-title, .evidences-title, .result-title {
        font-weight: 500;
        margin-bottom: 10px;
      }
      
      .content-text {
        line-height: 1.6;
        color: #303133;
        white-space: pre-line;
      }
    }
    
    .appeal-evidences {
      .evidences-list {
        display: flex;
        flex-wrap: wrap;
        
        .evidence-image {
          width: 100px;
          height: 100px;
          margin-right: 10px;
          margin-bottom: 10px;
          border-radius: 4px;
          cursor: pointer;
        }
      }
    }
    
    .appeal-result {
      .result-content {
        .reject-reason {
          margin-top: 10px;
          padding: 10px;
          border-radius: 4px;
          background-color: #f8f8f8;
          line-height: 1.5;
        }
      }
    }
    
    .admin-actions {
      margin-top: 30px;
      display: flex;
      justify-content: center;
      
      .el-button {
        min-width: 120px;
        margin: 0 10px;
      }
    }
  }
}
</style> 