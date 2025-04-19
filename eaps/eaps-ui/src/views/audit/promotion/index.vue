<template>
  <div class="promotion-audit-container">
    <el-card class="filter-card" shadow="hover">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="标题" prop="title">
          <el-input v-model="queryParams.title" placeholder="请输入宣传标题" clearable />
        </el-form-item>
        <el-form-item label="企业名称" prop="enterpriseName">
          <el-input v-model="queryParams.enterpriseName" placeholder="请输入企业名称" clearable />
        </el-form-item>
        <el-form-item label="行业" prop="industry">
          <el-select v-model="queryParams.industry" placeholder="选择行业" clearable>
            <el-option
              v-for="dict in industryOptions"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
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
          <span class="card-title">宣传审核列表</span>
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
        :data="promotionList"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="标题" prop="title" :show-overflow-tooltip="true" min-width="180" />
        <el-table-column label="封面" width="100">
          <template #default="scope">
            <el-image
              :src="scope.row.coverImage"
              style="width: 60px; height: 60px"
              :preview-src-list="[scope.row.coverImage]"
              fit="cover"
            >
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column label="企业信息" min-width="200">
          <template #default="scope">
            <div>
              <div>{{ scope.row.enterpriseName }}</div>
              <div class="text-muted">{{ scope.row.industry }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="浏览量" prop="viewCount" width="100" sortable />
        <el-table-column label="提交时间" prop="createTime" width="180" sortable />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleView(scope.row)">查看</el-button>
            <el-button type="success" link @click="handleApprove(scope.row)">通过</el-button>
            <el-button type="danger" link @click="handleReject(scope.row)">驳回</el-button>
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

    <!-- 宣传详情对话框 -->
    <el-dialog
      v-model="viewDialog"
      title="宣传详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="宣传ID">{{ currentPromotion.id }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ currentPromotion.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="宣传标题">{{ currentPromotion.title }}</el-descriptions-item>
        <el-descriptions-item label="行业">{{ currentPromotion.industry }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ currentPromotion.createTime }}</el-descriptions-item>
        <el-descriptions-item label="浏览量">{{ currentPromotion.viewCount }}</el-descriptions-item>
      </el-descriptions>

      <div class="promotion-content">
        <h3>宣传封面</h3>
        <div class="image-container">
          <el-image
            :src="currentPromotion.coverImage"
            style="max-width: 100%; max-height: 300px;"
            :preview-src-list="[currentPromotion.coverImage]"
          />
        </div>
        
        <h3>宣传内容</h3>
        <div class="content-html" v-html="currentPromotion.content"></div>
        
        <h3>宣传图片</h3>
        <div class="image-gallery">
          <el-image
            v-for="(img, index) in currentPromotion.images"
            :key="index"
            :src="img"
            style="width: 120px; height: 120px; margin: 5px"
            :preview-src-list="currentPromotion.images"
            fit="cover"
          />
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewDialog = false">关闭</el-button>
          <el-button type="success" @click="handleApprove(currentPromotion)">通过</el-button>
          <el-button type="danger" @click="handleReject(currentPromotion)">驳回</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 驳回原因对话框 -->
    <el-dialog
      v-model="rejectDialog"
      title="驳回原因"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" ref="rejectFormRef" :rules="rejectRules" label-width="80px">
        <el-form-item label="驳回原因" prop="reason">
          <el-select v-model="rejectForm.reason" placeholder="选择驳回原因" style="width: 100%">
            <el-option label="内容不符合规定" value="内容不符合规定" />
            <el-option label="含有违规信息" value="含有违规信息" />
            <el-option label="信息不真实" value="信息不真实" />
            <el-option label="内容质量较差" value="内容质量较差" />
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
      <el-form :model="batchRejectForm" ref="batchRejectFormRef" :rules="rejectRules" label-width="80px">
        <el-form-item label="驳回原因" prop="reason">
          <el-select v-model="batchRejectForm.reason" placeholder="选择驳回原因" style="width: 100%">
            <el-option label="内容不符合规定" value="内容不符合规定" />
            <el-option label="含有违规信息" value="含有违规信息" />
            <el-option label="信息不真实" value="信息不真实" />
            <el-option label="内容质量较差" value="内容质量较差" />
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
import { Search, Refresh, Picture } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPendingPromotionList, reviewPromotion, batchReviewPromotions } from '@/api/admin'

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 宣传列表数据
const promotionList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  enterpriseName: '',
  industry: ''
})
const dateRange = ref([])

// 表单引用
const queryForm = ref(null)
const rejectFormRef = ref(null)
const batchRejectFormRef = ref(null)

// 选中行
const selectedRows = ref([])

// 对话框控制
const viewDialog = ref(false)
const rejectDialog = ref(false)
const batchRejectDialog = ref(false)

// 当前查看的宣传
const currentPromotion = ref({
  id: null,
  title: '',
  enterpriseName: '',
  industry: '',
  createTime: '',
  viewCount: 0,
  coverImage: '',
  content: '',
  images: []
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

// 行业选项
const industryOptions = [
  { label: '互联网/IT', value: '互联网/IT' },
  { label: '金融/银行/保险', value: '金融/银行/保险' },
  { label: '教育/培训', value: '教育/培训' },
  { label: '医疗/健康', value: '医疗/健康' },
  { label: '房地产/建筑', value: '房地产/建筑' },
  { label: '制造业', value: '制造业' },
  { label: '服务业', value: '服务业' },
  { label: '文化/传媒', value: '文化/传媒' },
  { label: '物流/运输', value: '物流/运输' },
  { label: '政府/事业单位', value: '政府/事业单位' },
  { label: '其他', value: '其他' }
]

// 获取宣传列表
const getList = async () => {
  loading.value = true
  try {
    // 处理日期范围
    let params = { ...queryParams }
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    
    const res = await getPendingPromotionList(params)
    promotionList.value = res.data.rows || []
    total.value = res.data.total
  } catch (error) {
    console.error('获取宣传列表失败:', error)
    ElMessage.error('获取宣传列表失败')
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
const handleView = (row) => {
  currentPromotion.value = { ...row }
  viewDialog.value = true
}

// 处理通过审核
const handleApprove = (row) => {
  ElMessageBox.confirm(`确定通过 "${row.title}" 的审核吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    submitting.value = true
    try {
      await reviewPromotion(row.id, {
        status: 'approved',
        comment: '内容符合要求'
      })
      ElMessage.success('审核通过成功')
      getList()
      viewDialog.value = false
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
      await reviewPromotion(rejectForm.id, {
        status: 'rejected',
        reason: rejectForm.reason,
        comment: rejectForm.comment
      })
      ElMessage.success('驳回成功')
      rejectDialog.value = false
      viewDialog.value = false
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
  
  ElMessageBox.confirm(`确定批量通过所选的 ${selectedRows.value.length} 条宣传吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'success'
  }).then(async () => {
    submitting.value = true
    try {
      const ids = selectedRows.value.map(item => item.id)
      await batchReviewPromotions(ids, {
        status: 'approved',
        comment: '内容符合要求'
      })
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
      await batchReviewPromotions(batchRejectForm.ids, {
        status: 'rejected',
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
.promotion-audit-container {
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

.text-muted {
  color: #909399;
  font-size: 12px;
}

.image-placeholder {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 60px;
  height: 60px;
  background-color: #f5f7fa;
  border: 1px dashed #d9d9d9;
  color: #909399;
}

.promotion-content {
  margin-top: 20px;
}

.promotion-content h3 {
  margin-top: 16px;
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.image-container {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.content-html {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fafafa;
  min-height: 150px;
  max-height: 300px;
  overflow-y: auto;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 16px;
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