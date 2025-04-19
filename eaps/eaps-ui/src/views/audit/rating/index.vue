<template>
  <div class="rating-audit-container">
    <el-card class="filter-card" shadow="hover">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="queryParams.keyword" placeholder="企业名称/评价内容" clearable />
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-select v-model="queryParams.rating" placeholder="选择评分" clearable>
            <el-option label="1星" value="1" />
            <el-option label="2星" value="2" />
            <el-option label="3星" value="3" />
            <el-option label="4星" value="4" />
            <el-option label="5星" value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="提交时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
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
    </el-card>

    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">评分审核列表</span>
          <div class="card-tools">
            <el-button
              type="success"
              :disabled="selectedRows.length === 0"
              @click="handleBatchApprove"
            >
              批量通过
            </el-button>
            <el-button
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchReject"
            >
              批量驳回
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="ratingList"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="企业名称" prop="enterpriseName" min-width="180" />
        <el-table-column label="用户名" prop="userName" width="120" />
        <el-table-column label="评分" width="100">
          <template #default="scope">
            <el-rate
              v-model="scope.row.rating"
              disabled
              text-color="#ff9900"
              score-template="{value}"
            />
          </template>
        </el-table-column>
        <el-table-column label="评价内容" prop="content" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.status)"
              effect="light"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="createTime" width="170" sortable />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleView(scope.row)">查看详情</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              type="success" 
              link 
              @click="handleApprove(scope.row)"
            >
              通过
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              type="danger" 
              link 
              @click="handleReject(scope.row)"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 评价详情抽屉 -->
    <el-drawer
      v-model="viewDrawer"
      title="评价详情"
      size="50%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <template #header>
        <div class="drawer-header">
          <span>评价详情</span>
          <div class="drawer-actions" v-if="currentRating.status === 'pending'">
            <el-button type="success" @click="handleApprove(currentRating)">通过</el-button>
            <el-button type="danger" @click="handleReject(currentRating)">驳回</el-button>
          </div>
        </div>
      </template>

      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="评价ID" width="150px">{{ currentRating.id }}</el-descriptions-item>
        <el-descriptions-item label="评价状态">
          <el-tag :type="getStatusType(currentRating.status)">
            {{ getStatusText(currentRating.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ currentRating.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentRating.userName }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate
            v-model="currentRating.rating"
            disabled
            text-color="#ff9900"
            score-template="{value}"
          />
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentRating.createTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section">
        <h3>评价内容</h3>
        <div class="content-box">
          {{ currentRating.content || '无评价内容' }}
        </div>
      </div>

      <div class="detail-section" v-if="currentRating.tags && currentRating.tags.length > 0">
        <h3>标签</h3>
        <div class="tags-container">
          <el-tag
            v-for="tag in currentRating.tags"
            :key="tag"
            class="tag-item"
            effect="plain"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <div class="detail-section" v-if="currentRating.images && currentRating.images.length > 0">
        <h3>附件图片</h3>
        <div class="image-gallery">
          <el-image
            v-for="(img, index) in currentRating.images"
            :key="index"
            :src="img"
            fit="cover"
            class="gallery-image"
            :preview-src-list="currentRating.images"
          />
        </div>
      </div>

      <div class="detail-section" v-if="currentRating.rejectReason">
        <h3>驳回信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="驳回原因">{{ currentRating.rejectReason }}</el-descriptions-item>
          <el-descriptions-item label="详细说明">{{ currentRating.rejectComment || '无' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ currentRating.handleTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>

    <!-- 驳回原因对话框 -->
    <el-dialog
      v-model="rejectDialog"
      title="驳回原因"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" ref="rejectFormRef" :rules="rejectRules" label-width="100px">
        <el-form-item label="驳回原因" prop="reason">
          <el-select v-model="rejectForm.reason" placeholder="选择驳回原因" style="width: 100%">
            <el-option label="包含攻击性语言" value="包含攻击性语言" />
            <el-option label="含有虚假信息" value="含有虚假信息" />
            <el-option label="与该企业无关" value="与该企业无关" />
            <el-option label="含有广告信息" value="含有广告信息" />
            <el-option label="内容违规" value="内容违规" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="comment">
          <el-input
            v-model="rejectForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入详细说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmReject" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量驳回对话框 -->
    <el-dialog
      v-model="batchRejectDialog"
      title="批量驳回原因"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="selected-info">已选择 {{ selectedRows.length }} 条记录</div>
      <el-form :model="batchRejectForm" ref="batchRejectFormRef" :rules="rejectRules" label-width="100px">
        <el-form-item label="驳回原因" prop="reason">
          <el-select v-model="batchRejectForm.reason" placeholder="选择驳回原因" style="width: 100%">
            <el-option label="包含攻击性语言" value="包含攻击性语言" />
            <el-option label="含有虚假信息" value="含有虚假信息" />
            <el-option label="与该企业无关" value="与该企业无关" />
            <el-option label="含有广告信息" value="含有广告信息" />
            <el-option label="内容违规" value="内容违规" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="comment">
          <el-input
            v-model="batchRejectForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入详细说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchRejectDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmBatchReject" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getRatingsList, 
  getRatingDetail, 
  approveRating, 
  rejectRating, 
  batchApproveRatings, 
  batchRejectRatings
} from '@/api/rating'

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 评价列表数据
const ratingList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  rating: '',
  status: 'pending' // 默认查询待审核评价
})
const dateRange = ref([])

// 表单引用
const queryForm = ref(null)
const rejectFormRef = ref(null)
const batchRejectFormRef = ref(null)

// 选中行
const selectedRows = ref([])

// 对话框控制
const viewDrawer = ref(false)
const rejectDialog = ref(false)
const batchRejectDialog = ref(false)

// 当前查看的评价
const currentRating = ref({
  id: null,
  enterpriseName: '',
  userName: '',
  rating: 0,
  content: '',
  tags: [],
  images: [],
  status: '',
  createTime: '',
  rejectReason: '',
  rejectComment: '',
  handleTime: ''
})

// 驳回表单
const rejectForm = reactive({
  id: null,
  reason: '',
  comment: ''
})

// 批量驳回表单
const batchRejectForm = reactive({
  ids: [],
  reason: '',
  comment: ''
})

// 驳回表单验证规则
const rejectRules = {
  reason: [
    { required: true, message: '请选择驳回原因', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入详细说明', trigger: 'blur' },
    { min: 5, max: 200, message: '详细说明长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 状态类型映射
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return statusMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status) => {
  const statusMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回'
  }
  return statusMap[status] || status
}

// 获取评价列表
const getList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    let params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const res = await getRatingsList(params)
    ratingList.value = res.rows || []
    total.value = res.total
  } catch (error) {
    console.error('获取评价列表失败:', error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryForm.value?.resetFields()
  dateRange.value = []
  handleQuery()
}

// 处理选择变更
const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

// 处理查看详情
const handleView = async (row) => {
  try {
    loading.value = true
    const res = await getRatingDetail(row.id)
    currentRating.value = res.data || {}
    viewDrawer.value = true
  } catch (error) {
    console.error('获取评价详情失败:', error)
    ElMessage.error('获取评价详情失败')
  } finally {
    loading.value = false
  }
}

// 处理通过审核
const handleApprove = (row) => {
  ElMessageBox.confirm(`确定通过该评价吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    submitting.value = true
    try {
      await approveRating(row.id)
      ElMessage.success('审核通过成功')
      getList()
      viewDrawer.value = false
    } catch (error) {
      console.error('审核通过失败:', error)
      ElMessage.error('审核通过失败')
    } finally {
      submitting.value = false
    }
  }).catch(() => {})
}

// 处理驳回
const handleReject = (row) => {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectForm.comment = ''
  rejectDialog.value = true
}

// 确认驳回
const confirmReject = async () => {
  if (!rejectFormRef.value) return
  
  await rejectFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await rejectRating(rejectForm.id, {
        reason: rejectForm.reason,
        comment: rejectForm.comment
      })
      ElMessage.success('驳回成功')
      rejectDialog.value = false
      viewDrawer.value = false
      getList()
    } catch (error) {
      console.error('驳回失败:', error)
      ElMessage.error('驳回失败')
    } finally {
      submitting.value = false
    }
  })
}

// 批量通过审核
const handleBatchApprove = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  ElMessageBox.confirm(`确定批量通过所选的 ${selectedRows.value.length} 条评价吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    submitting.value = true
    try {
      const ids = selectedRows.value.map(item => item.id)
      await batchApproveRatings(ids)
      ElMessage.success('批量审核通过成功')
      getList()
    } catch (error) {
      console.error('批量审核通过失败:', error)
      ElMessage.error('批量审核通过失败')
    } finally {
      submitting.value = false
    }
  }).catch(() => {})
}

// 批量驳回
const handleBatchReject = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  batchRejectForm.ids = selectedRows.value.map(item => item.id)
  batchRejectForm.reason = ''
  batchRejectForm.comment = ''
  batchRejectDialog.value = true
}

// 确认批量驳回
const confirmBatchReject = async () => {
  if (!batchRejectFormRef.value) return
  
  await batchRejectFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await batchRejectRatings(batchRejectForm.ids, {
        reason: batchRejectForm.reason,
        comment: batchRejectForm.comment
      })
      ElMessage.success('批量驳回成功')
      batchRejectDialog.value = false
      getList()
    } catch (error) {
      console.error('批量驳回失败:', error)
      ElMessage.error('批量驳回失败')
    } finally {
      submitting.value = false
    }
  })
}

// 分页大小变更
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

// 页码变更
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

// 初始化
onMounted(() => {
  getList()
})
</script>

<style scoped>
.rating-audit-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-weight: bold;
  font-size: 16px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.drawer-actions {
  display: flex;
  gap: 10px;
}

.detail-section {
  margin-top: 20px;
}

.detail-section h3 {
  margin-bottom: 10px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.content-box {
  padding: 15px;
  background-color: #f8f8f8;
  border-radius: 4px;
  line-height: 1.6;
  min-height: 100px;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  margin-right: 5px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.gallery-image {
  width: 120px;
  height: 120px;
  border-radius: 4px;
  object-fit: cover;
}

.selected-info {
  margin-bottom: 16px;
  color: #409EFF;
  font-weight: bold;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>