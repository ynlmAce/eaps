<template>
  <div class="appeal-audit-container">
    <el-card class="filter-card" shadow="hover">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="queryParams.keyword" placeholder="企业名称/申诉原因" clearable />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="选择状态" clearable>
            <el-option label="待处理" value="pending" />
            <el-option label="已接受" value="accepted" />
            <el-option label="已拒绝" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item label="申诉时间">
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
          <span class="card-title">申诉处理列表</span>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="appealList"
        border
      >
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="相关评价ID" prop="ratingId" width="120" />
        <el-table-column label="企业名称" prop="enterpriseName" min-width="180" />
        <el-table-column label="申诉原因" prop="reason" min-width="200" :show-overflow-tooltip="true" />
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
        <el-table-column label="申诉时间" prop="createTime" width="170" sortable />
        <el-table-column label="处理时间" prop="handleTime" width="170" sortable />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleView(scope.row)">查看详情</el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              type="success" 
              link 
              @click="handleAccept(scope.row)"
            >
              接受申诉
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending'" 
              type="danger" 
              link 
              @click="handleReject(scope.row)"
            >
              拒绝申诉
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

    <!-- 申诉详情抽屉 -->
    <el-drawer
      v-model="viewDrawer"
      title="申诉详情"
      size="60%"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <template #header>
        <div class="drawer-header">
          <span>申诉详情</span>
          <div class="drawer-actions" v-if="currentAppeal.status === 'pending'">
            <el-button type="success" @click="handleAccept(currentAppeal)">接受申诉</el-button>
            <el-button type="danger" @click="handleReject(currentAppeal)">拒绝申诉</el-button>
          </div>
        </div>
      </template>

      <el-descriptions title="申诉信息" :column="2" border>
        <el-descriptions-item label="申诉ID" width="150px">{{ currentAppeal.id }}</el-descriptions-item>
        <el-descriptions-item label="申诉状态">
          <el-tag :type="getStatusType(currentAppeal.status)">
            {{ getStatusText(currentAppeal.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ currentAppeal.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="企业联系人">{{ currentAppeal.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentAppeal.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="申诉时间">{{ currentAppeal.createTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section">
        <h3>申诉原因</h3>
        <div class="content-box">
          {{ currentAppeal.reason || '无申诉原因' }}
        </div>
      </div>

      <div class="detail-section">
        <h3>申诉证据</h3>
        <div class="content-box">
          {{ currentAppeal.evidence || '无申诉证据' }}
        </div>
        
        <div v-if="currentAppeal.images && currentAppeal.images.length > 0" class="image-gallery">
          <el-image
            v-for="(img, index) in currentAppeal.images"
            :key="index"
            :src="img"
            fit="cover"
            class="gallery-image"
            :preview-src-list="currentAppeal.images"
          />
        </div>
      </div>

      <el-divider content-position="center">相关评价信息</el-divider>

      <el-descriptions title="评价信息" :column="2" border v-if="relatedRating.id">
        <el-descriptions-item label="评价ID" width="150px">{{ relatedRating.id }}</el-descriptions-item>
        <el-descriptions-item label="评价状态">
          <el-tag :type="getStatusType(relatedRating.status)">
            {{ getStatusText(relatedRating.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户名">{{ relatedRating.userName }}</el-descriptions-item>
        <el-descriptions-item label="评分">
          <el-rate
            v-model="relatedRating.rating"
            disabled
            text-color="#ff9900"
            score-template="{value}"
          />
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ relatedRating.createTime }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section" v-if="relatedRating.id">
        <h3>评价内容</h3>
        <div class="content-box">
          {{ relatedRating.content || '无评价内容' }}
        </div>
      </div>

      <div class="detail-section" v-if="relatedRating.id && relatedRating.tags && relatedRating.tags.length > 0">
        <h3>评价标签</h3>
        <div class="tags-container">
          <el-tag
            v-for="tag in relatedRating.tags"
            :key="tag"
            class="tag-item"
            effect="plain"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <div class="detail-section" v-if="currentAppeal.status !== 'pending'">
        <h3>处理结果</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="处理结果">
            {{ currentAppeal.status === 'accepted' ? '接受申诉' : '拒绝申诉' }}
          </el-descriptions-item>
          <el-descriptions-item label="处理原因">{{ currentAppeal.handleReason || '无' }}</el-descriptions-item>
          <el-descriptions-item label="详细说明">{{ currentAppeal.handleComment || '无' }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ currentAppeal.handleTime }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>

    <!-- 接受申诉对话框 -->
    <el-dialog
      v-model="acceptDialog"
      title="接受申诉"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="warning-tip">
        <el-alert
          title="接受申诉将会删除相关评价，请谨慎操作！"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>
      <el-form :model="acceptForm" ref="acceptFormRef" :rules="handleRules" label-width="100px">
        <el-form-item label="处理原因" prop="reason">
          <el-select v-model="acceptForm.reason" placeholder="选择处理原因" style="width: 100%">
            <el-option label="评价内容不实" value="评价内容不实" />
            <el-option label="评价违反平台规定" value="评价违反平台规定" />
            <el-option label="评价与事实不符" value="评价与事实不符" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="comment">
          <el-input
            v-model="acceptForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入详细说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="acceptDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmAccept" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝申诉对话框 -->
    <el-dialog
      v-model="rejectDialog"
      title="拒绝申诉"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" ref="rejectFormRef" :rules="handleRules" label-width="100px">
        <el-form-item label="处理原因" prop="reason">
          <el-select v-model="rejectForm.reason" placeholder="选择处理原因" style="width: 100%">
            <el-option label="申诉理由不充分" value="申诉理由不充分" />
            <el-option label="提供证据不足" value="提供证据不足" />
            <el-option label="评价符合事实" value="评价符合事实" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAppealsList, 
  getAppealDetail, 
  acceptAppeal, 
  rejectAppeal
} from '@/api/rating'
import { getRatingDetail } from '@/api/rating'

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 申诉列表数据
const appealList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  status: '',
})
const dateRange = ref([])

// 表单引用
const queryForm = ref(null)
const acceptFormRef = ref(null)
const rejectFormRef = ref(null)

// 对话框控制
const viewDrawer = ref(false)
const acceptDialog = ref(false)
const rejectDialog = ref(false)

// 当前查看的申诉
const currentAppeal = ref({
  id: null,
  ratingId: null,
  enterpriseName: '',
  contactName: '',
  contactPhone: '',
  reason: '',
  evidence: '',
  images: [],
  status: '',
  createTime: '',
  handleTime: '',
  handleReason: '',
  handleComment: ''
})

// 相关评价信息
const relatedRating = ref({
  id: null,
  userName: '',
  rating: 0,
  content: '',
  tags: [],
  status: '',
  createTime: ''
})

// 接受申诉表单
const acceptForm = reactive({
  id: null,
  reason: '',
  comment: ''
})

// 拒绝申诉表单
const rejectForm = reactive({
  id: null,
  reason: '',
  comment: ''
})

// 表单验证规则
const handleRules = {
  reason: [
    { required: true, message: '请选择处理原因', trigger: 'change' }
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
    accepted: 'success',
    rejected: 'danger',
    approved: 'success',
    rejected: 'danger'
  }
  return statusMap[status] || 'info'
}

// 状态文本映射
const getStatusText = (status) => {
  const statusMap = {
    pending: '待处理',
    accepted: '已接受',
    rejected: '已拒绝',
    approved: '已通过',
    rejected: '已驳回'
  }
  return statusMap[status] || status
}

// 获取申诉列表
const getList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    let params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const res = await getAppealsList(params)
    appealList.value = res.rows || []
    total.value = res.total
  } catch (error) {
    console.error('获取申诉列表失败:', error)
    ElMessage.error('获取申诉列表失败')
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

// 处理查看详情
const handleView = async (row) => {
  try {
    loading.value = true
    const res = await getAppealDetail(row.id)
    currentAppeal.value = res.data || {}
    
    // 获取相关评价信息
    if (currentAppeal.value.ratingId) {
      const ratingRes = await getRatingDetail(currentAppeal.value.ratingId)
      relatedRating.value = ratingRes.data || {}
    } else {
      relatedRating.value = { id: null }
    }
    
    viewDrawer.value = true
  } catch (error) {
    console.error('获取申诉详情失败:', error)
    ElMessage.error('获取申诉详情失败')
  } finally {
    loading.value = false
  }
}

// 处理接受申诉
const handleAccept = (row) => {
  acceptForm.id = row.id
  acceptForm.reason = ''
  acceptForm.comment = ''
  acceptDialog.value = true
}

// 确认接受申诉
const confirmAccept = async () => {
  if (!acceptFormRef.value) return
  
  await acceptFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await acceptAppeal(acceptForm.id, {
        reason: acceptForm.reason,
        comment: acceptForm.comment
      })
      ElMessage.success('已接受申诉并删除相关评价')
      acceptDialog.value = false
      viewDrawer.value = false
      getList()
    } catch (error) {
      console.error('接受申诉失败:', error)
      ElMessage.error('接受申诉失败')
    } finally {
      submitting.value = false
    }
  })
}

// 处理拒绝申诉
const handleReject = (row) => {
  rejectForm.id = row.id
  rejectForm.reason = ''
  rejectForm.comment = ''
  rejectDialog.value = true
}

// 确认拒绝申诉
const confirmReject = async () => {
  if (!rejectFormRef.value) return
  
  await rejectFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      await rejectAppeal(rejectForm.id, {
        reason: rejectForm.reason,
        comment: rejectForm.comment
      })
      ElMessage.success('已拒绝申诉')
      rejectDialog.value = false
      viewDrawer.value = false
      getList()
    } catch (error) {
      console.error('拒绝申诉失败:', error)
      ElMessage.error('拒绝申诉失败')
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
.appeal-audit-container {
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
  min-height: 80px;
  margin-bottom: 15px;
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

.warning-tip {
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 