<template>
  <div class="ratings-table-component">
    <!-- 过滤卡片 -->
    <el-card class="filter-card">
      <el-form :model="filter" inline>
        <el-form-item label="关键词">
          <el-input v-model="filter.keyword" placeholder="评价内容/企业名称/用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="filter.rating" placeholder="选择评分" clearable>
            <el-option v-for="i in 5" :key="i" :label="`${i}星`" :value="i" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="filter.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :shortcuts="dateShortcuts"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 评分列表卡片 -->
    <el-card class="ratings-list-card">
      <div class="header">
        <span class="title">{{ title }}</span>
        <div class="batch-actions" v-if="showBatchActions && selectedRatings.length > 0">
          <el-button type="success" size="small" @click="batchApprove">批量通过</el-button>
          <el-button type="danger" size="small" @click="batchReject">批量拒绝</el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="ratingsList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" v-if="showSelection" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="120" />
        <el-table-column prop="userName" label="用户姓名" min-width="120" />
        <el-table-column label="评分" width="120">
          <template #default="scope">
            <el-rate
              v-model="scope.row.rating"
              disabled
              allow-half
              show-score
              text-color="#ff9900"
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'pending'" type="warning">待审核</el-tag>
            <el-tag v-else-if="scope.row.status === 'approved'" type="success">已通过</el-tag>
            <el-tag v-else-if="scope.row.status === 'rejected'" type="danger">已拒绝</el-tag>
            <el-tag v-else-if="scope.row.status === 'appealing'" type="info">申诉中</el-tag>
            <el-tag v-else-if="scope.row.status === 'appeal_approved'" type="success">申诉成功</el-tag>
            <el-tag v-else-if="scope.row.status === 'appeal_rejected'" type="danger">申诉失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" min-width="150">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">查看</el-button>
            <template v-if="scope.row.status === 'pending'">
              <el-button size="small" type="success" @click="approveRating(scope.row.id)">通过</el-button>
              <el-button size="small" type="danger" @click="rejectRating(scope.row.id)">拒绝</el-button>
            </template>
            <template v-if="type === 'pending_appeals' && scope.row.status === 'appealing'">
              <el-button size="small" type="primary" @click="handleAppeal(scope.row)">处理申诉</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          :page-size="pagination.pageSize"
          :current-page="pagination.currentPage"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 拒绝评分对话框 -->
    <el-dialog
      v-model="rejectDialogVisible"
      title="拒绝原因"
      width="500px"
      destroy-on-close
    >
      <el-form :model="rejectForm" ref="rejectFormRef" :rules="rejectRules">
        <el-form-item label="拒绝原因" prop="reason">
          <el-select v-model="rejectForm.reason" placeholder="请选择拒绝原因" style="width: 100%">
            <el-option label="包含不适当内容" value="inappropriate_content" />
            <el-option label="恶意评价" value="malicious_rating" />
            <el-option label="虚假信息" value="false_information" />
            <el-option label="违反社区规则" value="community_guidelines" />
            <el-option label="其他原因" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="comment">
          <el-input
            type="textarea"
            v-model="rejectForm.comment"
            rows="4"
            placeholder="请输入详细说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确认</el-button>
      </template>
    </el-dialog>

    <!-- 处理申诉对话框 -->
    <el-dialog
      v-model="appealDialogVisible"
      title="处理申诉"
      width="500px"
    >
      <el-form :model="appealForm" ref="appealFormRef" :rules="appealRules">
        <el-form-item label="处理结果" prop="action">
          <el-radio-group v-model="appealForm.action">
            <el-radio label="reject">驳回申诉</el-radio>
            <el-radio label="approve">同意申诉</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明" prop="comment">
          <el-input
            v-model="appealForm.comment"
            type="textarea"
            rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="appealDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAppealHandle">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="detailDrawerVisible"
      :title="'评价详情'"
      size="50%"
      :destroy-on-close="true"
    >
      <div v-if="currentDetail" class="detail-container">
        <div class="detail-header">
          <div class="enterprise-info">
            <h3>{{ currentDetail.enterpriseName }}</h3>
            <el-tag size="small">{{ currentDetail.enterpriseType }}</el-tag>
          </div>
          <div class="rating-info">
            <span class="rating-label">综合评分：</span>
            <el-rate
              v-model="currentDetail.rating"
              disabled
              allow-half
              show-score
              text-color="#ff9900"
            />
          </div>
        </div>

        <el-divider />
        
        <div class="detail-content">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="评价人">{{ currentDetail.userName }}</el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ formatDate(currentDetail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="评价内容">{{ currentDetail.content }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag v-if="currentDetail.status === 'pending'" type="warning">待审核</el-tag>
              <el-tag v-else-if="currentDetail.status === 'approved'" type="success">已通过</el-tag>
              <el-tag v-else-if="currentDetail.status === 'rejected'" type="danger">已拒绝</el-tag>
              <el-tag v-else-if="currentDetail.status === 'appealing'" type="info">申诉中</el-tag>
              <el-tag v-else-if="currentDetail.status === 'appeal_approved'" type="success">申诉成功</el-tag>
              <el-tag v-else-if="currentDetail.status === 'appeal_rejected'" type="danger">申诉失败</el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="currentDetail.rejectReason" label="拒绝原因">
              {{ getRejectReasonText(currentDetail.rejectReason) }}: {{ currentDetail.rejectComment }}
            </el-descriptions-item>
            <el-descriptions-item v-if="currentDetail.appealReason" label="申诉原因">
              {{ currentDetail.appealReason }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="currentDetail.images && currentDetail.images.length > 0" class="detail-images">
          <h4>评价图片</h4>
          <div class="image-list">
            <div v-for="(image, index) in currentDetail.images" :key="index" class="image-item">
              <el-image
                :src="image"
                :preview-src-list="currentDetail.images"
                :initial-index="index"
                fit="cover"
              />
            </div>
          </div>
        </div>
        
        <div v-if="currentDetail.tags && currentDetail.tags.length > 0" class="detail-tags">
          <h4>评价标签</h4>
          <div class="tags-list">
            <el-tag
              v-for="tag in currentDetail.tags"
              :key="tag"
              class="tag-item"
              size="small"
            >
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getRatingsList, 
  getAppealsList, 
  getRatingDetail, 
  reviewRating, 
  handleAppeal,
  batchApproveRatings,
  batchRejectRatings
} from '@/api/rating'

// 定义组件接收的属性
const props = defineProps({
  type: {
    type: String,
    required: true,
    validator: (value) => {
      return ['pending_ratings', 'pending_appeals', 'processed_ratings', 'processed_appeals'].includes(value)
    }
  },
  title: {
    type: String,
    default: '评分列表'
  }
})

// 表格标题计算属性
const title = computed(() => {
  return props.title || (
    props.type === 'pending_ratings' ? '待审核评价' :
    props.type === 'pending_appeals' ? '待处理申诉' :
    props.type === 'processed_ratings' ? '已处理评价' :
    props.type === 'processed_appeals' ? '已处理申诉' : '评分列表'
  )
})

// 是否显示批量操作
const showBatchActions = computed(() => {
  return props.type === 'pending_ratings'
})

// 是否显示选择列
const showSelection = computed(() => {
  return props.type === 'pending_ratings'
})

// 加载状态
const loading = ref(false)
const detailLoading = ref(false)

// 评分列表数据
const ratingsList = ref([])

// 分页信息
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 筛选表单
const filter = reactive({
  keyword: '',
  rating: '',
  dateRange: null,
  status: getStatusByType(props.type)
})

// 已选择的行
const selectedRatings = ref([])

// 详情抽屉
const detailDrawerVisible = ref(false)
const currentDetail = ref(null)

// 拒绝对话框
const rejectDialogVisible = ref(false)
const rejectFormRef = ref(null)
const rejectForm = reactive({
  reason: '',
  comment: ''
})

// 处理申诉对话框
const appealDialogVisible = ref(false)
const appealFormRef = ref(null)
const appealForm = reactive({
  action: 'reject',
  comment: ''
})

// 拒绝表单校验规则
const rejectRules = {
  reason: [{ required: true, message: '请选择拒绝原因', trigger: 'change' }],
  comment: [{ required: true, message: '请输入详细说明', trigger: 'blur' }]
}

// 申诉表单校验规则
const appealRules = {
  action: [{ required: true, message: '请选择处理结果', trigger: 'change' }],
  comment: [{ required: true, message: '请输入处理说明', trigger: 'blur' }]
}

// 监听类型变化重新加载
watch(() => props.type, (newType) => {
  filter.status = getStatusByType(newType)
  pagination.currentPage = 1
  fetchRatingsList()
})

// 初始化加载数据
onMounted(() => {
  fetchRatingsList()
})

// 获取评分列表
const fetchRatingsList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      keyword: filter.keyword,
      rating: filter.rating,
      startDate: filter.dateRange?.[0] || null,
      endDate: filter.dateRange?.[1] || null,
      status: filter.status
    }
    
    let response
    if (props.type.includes('appeals')) {
      response = await getAppealsList(params)
    } else {
      response = await getRatingsList(params)
    }
    
    if (response.code === 200) {
      ratingsList.value = response.data.list
      pagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '获取评分列表失败')
    }
  } catch (error) {
    console.error('获取评分列表出错：', error)
    ElMessage.error('获取评分列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 应用筛选条件
const handleSearch = () => {
  pagination.currentPage = 1 // 重置到第一页
  fetchRatingsList()
}

// 重置筛选条件
const resetFilter = () => {
  filter.keyword = ''
  filter.rating = ''
  filter.dateRange = null
  pagination.currentPage = 1
  fetchRatingsList()
}

// 处理每页显示条数变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchRatingsList()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.currentPage = page
  fetchRatingsList()
}

// 处理表格选择变化
const handleSelectionChange = (rows) => {
  selectedRatings.value = rows
}

// 查看评分详情
const viewDetail = async (row) => {
  try {
    loading.value = true
    const response = await getRatingDetail(row.id)
    if (response.code === 200) {
      currentDetail.value = response.data
      detailDrawerVisible.value = true
    } else {
      ElMessage.error(response.msg || '获取评分详情失败')
    }
  } catch (error) {
    console.error('获取评分详情出错：', error)
    ElMessage.error('获取评分详情失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 通过评分
const approveRating = async (id) => {
  try {
    await ElMessageBox.confirm('确认通过该评价？', '确认操作', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const response = await reviewRating(id, {
      status: 'approved'
    })
    
    if (response.code === 200) {
      ElMessage.success('评价已通过')
      fetchRatingsList()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('通过评分出错：', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 拒绝评分
const rejectRating = async (id) => {
  currentDetail.value.id = id
  rejectForm.reason = ''
  rejectForm.comment = ''
  rejectDialogVisible.value = true
}

// 确认拒绝评分
const confirmReject = async () => {
  if (!rejectFormRef.value) return
  
  try {
    await rejectFormRef.value.validate()
    
    loading.value = true
    let response
    
    response = await reviewRating(currentDetail.value.id, {
      status: 'rejected',
      reason: rejectForm.reason,
      comment: rejectForm.comment
    })
    
    if (response.code === 200) {
      ElMessage.success('评价已拒绝')
      rejectDialogVisible.value = false
      fetchRatingsList()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('拒绝评分出错：', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理申诉
const handleAppeal = async (row) => {
  currentDetail.value = row
  appealForm.action = 'reject'
  appealForm.comment = ''
  appealDialogVisible.value = true
}

// 确认处理申诉
const confirmAppealHandle = async () => {
  if (!appealFormRef.value) return
  
  try {
    await appealFormRef.value.validate()
    
    loading.value = true
    let response
    
    response = await handleAppeal(currentDetail.value.id, {
      action: appealForm.action,
      comment: appealForm.comment
    })
    
    if (response.code === 200) {
      ElMessage.success('申诉处理成功')
      appealDialogVisible.value = false
      fetchRatingsList()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('处理申诉出错：', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 批量通过评分
const batchApprove = async () => {
  if (selectedRatings.value.length === 0) {
    ElMessage.warning('请选择要批量通过的评价')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确认通过已选择的 ${selectedRatings.value.length} 条评价？`, '确认操作', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const ids = selectedRatings.value.map(item => item.id)
    const response = await batchApproveRatings(ids)
    
    if (response.code === 200) {
      ElMessage.success('批量通过操作成功')
      fetchRatingsList()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量通过评分出错：', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

// 批量拒绝评分
const batchReject = async () => {
  if (selectedRatings.value.length === 0) {
    ElMessage.warning('请选择要批量拒绝的评价')
    return
  }
  
  rejectForm.reason = ''
  rejectForm.comment = ''
  rejectDialogVisible.value = true
}

// 确认批量拒绝
const confirmBatchReject = async () => {
  if (!rejectFormRef.value) return
  
  try {
    await rejectFormRef.value.validate()
    
    loading.value = true
    const ids = selectedRatings.value.map(item => item.id)
    const response = await batchRejectRatings(ids, {
      reason: rejectForm.reason,
      comment: rejectForm.comment
    })
    
    if (response.code === 200) {
      ElMessage.success('批量拒绝成功')
      rejectDialogVisible.value = false
      fetchRatingsList()
    } else {
      ElMessage.error(response.msg || '操作失败')
    }
  } catch (error) {
    console.error('批量拒绝出错：', error)
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取拒绝原因文本
const getRejectReasonText = (reason) => {
  const reasonMap = {
    'inappropriate_content': '包含不适当内容',
    'malicious_rating': '恶意评价',
    'false_information': '虚假信息',
    'community_guidelines': '违反社区规则',
    'other': '其他原因'
  }
  return reasonMap[reason] || reason
}

// 状态相关辅助函数
const getStatusByType = (type) => {
  switch (type) {
    case 'pending_ratings':
      return 'pending'
    case 'processed_ratings':
      return 'processed'
    case 'pending_appeals':
      return 'appeal_pending'
    case 'processed_appeals':
      return 'appeal_processed'
    default:
      return 'pending'
  }
}

function getStatusText(status) {
  switch (status) {
    case 'pending':
      return '待审核'
    case 'approved':
      return '已通过'
    case 'rejected':
      return '已拒绝'
    case 'appealed':
      return '已申诉'
    case 'appeal_pending':
      return '申诉待处理'
    case 'appeal_approved':
      return '申诉通过'
    case 'appeal_rejected':
      return '申诉驳回'
    default:
      return '未知状态'
  }
}

function getStatusType(status) {
  switch (status) {
    case 'pending':
    case 'appeal_pending':
      return 'warning'
    case 'approved':
    case 'appeal_approved':
      return 'success'
    case 'rejected':
    case 'appeal_rejected':
      return 'danger'
    case 'appealed':
      return 'info'
    default:
      return 'info'
  }
}

function getAppealStatusText(status) {
  switch (status) {
    case 'pending':
      return '待处理'
    case 'approved':
      return '已通过'
    case 'rejected':
      return '已驳回'
    default:
      return '未知状态'
  }
}
</script>

<style scoped>
.ratings-table-component {
  width: 100%;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.filter-header .title {
  font-size: 16px;
  font-weight: bold;
  margin-right: auto;
}

.ratings-list-card .header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.ratings-list-card .header .title {
  font-size: 16px;
  font-weight: bold;
  margin-right: auto;
}

.pagination-container {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.detail-container {
  padding: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.enterprise-info h3 {
  margin: 0 0 8px 0;
}

.rating-info {
  display: flex;
  align-items: center;
}

.rating-label {
  margin-right: 8px;
}

.detail-content {
  margin-bottom: 20px;
}

.detail-images {
  margin-top: 20px;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.image-item {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  overflow: hidden;
}

.detail-tags {
  margin-top: 20px;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}
</style> 