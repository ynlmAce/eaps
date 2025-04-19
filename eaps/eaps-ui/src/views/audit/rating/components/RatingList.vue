<template>
  <div class="rating-list-container">
    <!-- 筛选卡片 -->
    <el-card class="filter-card">
      <el-form :model="queryParams" label-width="80px" :inline="true">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="企业名称/用户名/内容" clearable />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="queryParams.rating" placeholder="请选择" clearable>
            <el-option label="全部" value="" />
            <el-option v-for="i in 5" :key="i" :label="`${i}星`" :value="i" />
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
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮区域 -->
    <div class="action-container" v-if="status === 'pending'">
      <el-button type="primary" :disabled="selectedRows.length === 0" @click="handleBatchApprove">
        批量通过
      </el-button>
      <el-button :disabled="selectedRows.length === 0" @click="handleBatchReject">
        批量驳回
      </el-button>
    </div>

    <!-- 评价列表卡片 -->
    <el-card class="rating-table-card">
      <el-table
        v-loading="loading"
        :data="ratingList"
        @selection-change="handleSelectionChange"
        border
        stripe
      >
        <el-table-column v-if="status === 'pending'" type="selection" width="50" />
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="企业名称" prop="enterpriseName" min-width="120" show-overflow-tooltip />
        <el-table-column label="用户名" prop="userName" min-width="100" show-overflow-tooltip />
        <el-table-column label="评分" width="100">
          <template #default="{ row }">
            <el-rate :model-value="row.rating" disabled text-color="#ff9900" />
          </template>
        </el-table-column>
        <el-table-column label="内容" prop="content" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" prop="createTime" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看详情</el-button>
            <template v-if="status === 'pending'">
              <el-button link type="success" @click="handleApprove(row)">通过</el-button>
              <el-button link type="danger" @click="handleReject(row)">驳回</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 评价详情抽屉 -->
    <el-drawer v-model="detailDrawer.visible" title="评价详情" size="500px" direction="rtl">
      <div v-loading="detailDrawer.loading" class="drawer-content">
        <template v-if="detailDrawer.data">
          <h3>基本信息</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="评价ID">{{ detailDrawer.data.id }}</el-descriptions-item>
            <el-descriptions-item label="企业名称">{{ detailDrawer.data.enterpriseName }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ detailDrawer.data.userName }}</el-descriptions-item>
            <el-descriptions-item label="评分">
              <el-rate :model-value="detailDrawer.data.rating" disabled text-color="#ff9900" />
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ detailDrawer.data.createTime }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(detailDrawer.data.status)">
                {{ getStatusText(detailDrawer.data.status) }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>

          <h3>评价内容</h3>
          <div class="content-box">{{ detailDrawer.data.content }}</div>

          <h3 v-if="detailDrawer.data.tags && detailDrawer.data.tags.length > 0">评价标签</h3>
          <div v-if="detailDrawer.data.tags && detailDrawer.data.tags.length > 0" class="tags-container">
            <el-tag v-for="tag in detailDrawer.data.tags" :key="tag" class="tag-item">{{ tag }}</el-tag>
          </div>

          <h3 v-if="detailDrawer.data.images && detailDrawer.data.images.length > 0">评价图片</h3>
          <div v-if="detailDrawer.data.images && detailDrawer.data.images.length > 0" class="images-container">
            <el-image
              v-for="(image, index) in detailDrawer.data.images"
              :key="index"
              :src="image"
              class="image-item"
              :preview-src-list="detailDrawer.data.images"
              :initial-index="index"
              fit="cover"
            />
          </div>

          <!-- 驳回信息（如果有的话） -->
          <div v-if="detailDrawer.data.status === 'rejected'">
            <h3>驳回信息</h3>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="驳回原因">{{ detailDrawer.data.rejectReason }}</el-descriptions-item>
              <el-descriptions-item label="驳回说明">{{ detailDrawer.data.rejectComment }}</el-descriptions-item>
              <el-descriptions-item label="驳回时间">{{ detailDrawer.data.rejectTime }}</el-descriptions-item>
            </el-descriptions>
          </div>

          <!-- 审核操作按钮 -->
          <div v-if="status === 'pending'" class="drawer-footer">
            <el-button type="success" @click="handleApprove(detailDrawer.data)">通过</el-button>
            <el-button type="danger" @click="handleReject(detailDrawer.data)">驳回</el-button>
          </div>
        </template>
      </div>
    </el-drawer>

    <!-- 驳回原因对话框 -->
    <el-dialog
      v-model="rejectDialog.visible"
      title="驳回原因"
      width="500px"
      append-to-body
      destroy-on-close
    >
      <el-form
        ref="rejectFormRef"
        :model="rejectDialog.form"
        :rules="rejectFormRules"
        label-width="100px"
      >
        <el-form-item label="驳回原因" prop="reason">
          <el-select v-model="rejectDialog.form.reason" placeholder="请选择驳回原因">
            <el-option label="违反相关法律法规" value="illegal_content" />
            <el-option label="含有广告或垃圾信息" value="spam_content" />
            <el-option label="含有辱骂或歧视内容" value="abusive_content" />
            <el-option label="信息不实" value="false_information" />
            <el-option label="其他原因" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="comment">
          <el-input
            v-model="rejectDialog.form.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入详细说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="rejectDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 批量驳回对话框 -->
    <el-dialog
      v-model="batchRejectDialog.visible"
      title="批量驳回原因"
      width="500px"
      append-to-body
      destroy-on-close
    >
      <el-form
        ref="batchRejectFormRef"
        :model="batchRejectDialog.form"
        :rules="rejectFormRules"
        label-width="100px"
      >
        <el-form-item label="驳回原因" prop="reason">
          <el-select v-model="batchRejectDialog.form.reason" placeholder="请选择驳回原因">
            <el-option label="违反相关法律法规" value="illegal_content" />
            <el-option label="含有广告或垃圾信息" value="spam_content" />
            <el-option label="含有辱骂或歧视内容" value="abusive_content" />
            <el-option label="信息不实" value="false_information" />
            <el-option label="其他原因" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="comment">
          <el-input
            v-model="batchRejectDialog.form.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入详细说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="batchRejectDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="confirmBatchReject">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRatingsList, getRatingDetail, approveRating, rejectRating, batchApproveRatings, batchRejectRatings } from '@/api/rating'

// 组件接收的参数
const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: (value) => ['pending', 'processed'].includes(value)
  }
})

// 组件事件
const emit = defineEmits(['refresh'])

// 状态与查询参数
const loading = ref(false)
const ratingList = ref([])
const total = ref(0)
const selectedRows = ref([])
const dateRange = ref([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  rating: '',
  startDate: '',
  endDate: '',
  status: computed(() => props.status)
})

// 详情抽屉
const detailDrawer = reactive({
  visible: false,
  loading: false,
  data: null
})

// 驳回对话框
const rejectDialog = reactive({
  visible: false,
  currentId: null,
  form: {
    reason: '',
    comment: ''
  }
})

// 批量驳回对话框
const batchRejectDialog = reactive({
  visible: false,
  form: {
    reason: '',
    comment: ''
  }
})

// 表单引用
const rejectFormRef = ref(null)
const batchRejectFormRef = ref(null)

// 驳回表单验证规则
const rejectFormRules = {
  reason: [
    { required: true, message: '请选择驳回原因', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入详细说明', trigger: 'blur' },
    { min: 5, max: 200, message: '详细说明长度应在 5 到 200 个字符之间', trigger: 'blur' }
  ]
}

// 禁用日期
const disabledDate = (time) => {
  return time > new Date()
}

// 监听日期范围变化
const watchDateRange = computed({
  get() {
    if (queryParams.startDate && queryParams.endDate) {
      return [queryParams.startDate, queryParams.endDate]
    }
    return []
  },
  set(val) {
    if (val && val.length === 2) {
      queryParams.startDate = val[0]
      queryParams.endDate = val[1]
    } else {
      queryParams.startDate = ''
      queryParams.endDate = ''
    }
  }
})

// 获取状态类型样式
const getStatusType = (status) => {
  const types = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已驳回'
  }
  return texts[status] || '未知'
}

// 查询评价列表
const getList = async () => {
  loading.value = true
  try {
    const response = await getRatingsList(queryParams)
    ratingList.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    console.error('获取评价列表失败', error)
    ElMessage.error('获取评价列表失败')
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.rating = ''
  dateRange.value = []
  queryParams.startDate = ''
  queryParams.endDate = ''
  queryParams.pageNum = 1
  handleQuery()
}

// 处理查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 处理选择行变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 处理页码变化
const handleCurrentChange = (current) => {
  queryParams.pageNum = current
  getList()
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  getList()
}

// 查看详情
const viewDetail = async (row) => {
  detailDrawer.visible = true
  detailDrawer.loading = true
  detailDrawer.data = null

  try {
    const response = await getRatingDetail(row.id)
    detailDrawer.data = response.data
  } catch (error) {
    console.error('获取评价详情失败', error)
    ElMessage.error('获取评价详情失败')
  } finally {
    detailDrawer.loading = false
  }
}

// 处理通过
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm('确认通过此评价？', '确认提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    loading.value = true
    await approveRating(row.id)
    ElMessage.success('评价已通过')
    getList()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核评价失败', error)
      ElMessage.error('审核失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 处理驳回
const handleReject = (row) => {
  rejectDialog.currentId = row.id
  rejectDialog.form.reason = ''
  rejectDialog.form.comment = ''
  rejectDialog.visible = true
}

// 确认驳回
const confirmReject = async () => {
  if (!rejectFormRef.value) return

  await rejectFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      loading.value = true
      await rejectRating(rejectDialog.currentId, {
        reason: rejectDialog.form.reason,
        comment: rejectDialog.form.comment
      })
      
      ElMessage.success('评价已驳回')
      rejectDialog.visible = false
      getList()
      emit('refresh')
    } catch (error) {
      console.error('驳回评价失败', error)
      ElMessage.error('驳回失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

// 处理批量通过
const handleBatchApprove = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要通过的评价')
    return
  }

  try {
    await ElMessageBox.confirm(`确认通过选中的 ${selectedRows.value.length} 条评价？`, '确认提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const ids = selectedRows.value.map(row => row.id)
    loading.value = true
    await batchApproveRatings(ids)
    ElMessage.success('批量通过成功')
    getList()
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量通过评价失败', error)
      ElMessage.error('批量通过失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 处理批量驳回
const handleBatchReject = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要驳回的评价')
    return
  }

  batchRejectDialog.form.reason = ''
  batchRejectDialog.form.comment = ''
  batchRejectDialog.visible = true
}

// 确认批量驳回
const confirmBatchReject = async () => {
  if (!batchRejectFormRef.value) return

  await batchRejectFormRef.value.validate(async (valid) => {
    if (!valid) return

    try {
      const ids = selectedRows.value.map(row => row.id)
      loading.value = true
      await batchRejectRatings(ids, {
        reason: batchRejectDialog.form.reason,
        comment: batchRejectDialog.form.comment
      })
      
      ElMessage.success('批量驳回成功')
      batchRejectDialog.visible = false
      getList()
      emit('refresh')
    } catch (error) {
      console.error('批量驳回评价失败', error)
      ElMessage.error('批量驳回失败，请重试')
    } finally {
      loading.value = false
    }
  })
}

// 组件挂载时获取列表数据
onMounted(() => {
  getList()
})
</script>

<style scoped>
.rating-list-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card,
.rating-table-card {
  width: 100%;
}

.action-container {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  margin-bottom: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.drawer-content {
  padding: 20px;
}

.drawer-content h3 {
  margin-top: 20px;
  margin-bottom: 10px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.content-box {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  white-space: pre-wrap;
  word-break: break-all;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.images-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.image-item {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  object-fit: cover;
  cursor: pointer;
}

.drawer-footer {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>