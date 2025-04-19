<template>
  <div class="application-manage-container">
    <el-card class="filter-card">
      <div class="filter-header">
        <span class="title">筛选条件</span>
        <div class="operations">
          <el-button type="primary" @click="fetchApplications">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </div>
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="职位名称">
          <el-select v-model="queryParams.jobId" placeholder="请选择职位" clearable>
            <el-option
              v-for="item in jobOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="申请状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="已申请" value="applied" />
            <el-option label="已查看" value="viewed" />
            <el-option label="邀请面试" value="interviewing" />
            <el-option label="不合适" value="rejected" />
            <el-option label="已录用" value="hired" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateChange"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="学生姓名/学校/专业"
            clearable
            style="width: 240px"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="application-list-card">
      <div class="list-header">
        <span class="title">申请列表</span>
        <div class="batch-actions" v-if="multipleSelection.length > 0">
          <el-dropdown @command="handleBatchAction">
            <el-button type="primary">
              批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="view">标记为已查看</el-dropdown-item>
                <el-dropdown-item command="interview">邀请面试</el-dropdown-item>
                <el-dropdown-item command="reject">标记为不合适</el-dropdown-item>
                <el-dropdown-item command="hire">标记为已录用</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="applicationList"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column label="申请ID" prop="id" width="80" />
        <el-table-column label="职位名称" prop="jobTitle" min-width="180" />
        <el-table-column label="学生姓名" prop="studentName" min-width="120" />
        <el-table-column label="学校" prop="university" min-width="180" />
        <el-table-column label="专业" prop="major" min-width="150" />
        <el-table-column label="申请时间" prop="applicationTime" min-width="180" />
        <el-table-column label="状态" min-width="120">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.status)"
              effect="light"
            >
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="handleViewDetail(scope.row)"
            >查看详情</el-button>
            <el-dropdown @command="(command) => handleStatusChange(command, scope.row)">
              <el-button type="primary" link>
                更改状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="viewed" :disabled="isStatusDisabled('viewed', scope.row)">标记为已查看</el-dropdown-item>
                  <el-dropdown-item command="interviewing" :disabled="isStatusDisabled('interviewing', scope.row)">邀请面试</el-dropdown-item>
                  <el-dropdown-item command="rejected" :disabled="isStatusDisabled('rejected', scope.row)">标记为不合适</el-dropdown-item>
                  <el-dropdown-item command="hired" :disabled="isStatusDisabled('hired', scope.row)">标记为已录用</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <!-- 申请详情抽屉 -->
    <el-drawer
      v-model="detailDrawer.visible"
      title="申请详情"
      size="60%"
      direction="rtl"
    >
      <div v-loading="detailDrawer.loading" class="application-detail">
        <template v-if="detailDrawer.data">
          <el-descriptions title="学生信息" :column="2" border>
            <el-descriptions-item label="学生姓名">{{ detailDrawer.data.studentName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ detailDrawer.data.phone }}</el-descriptions-item>
            <el-descriptions-item label="电子邮箱">{{ detailDrawer.data.email }}</el-descriptions-item>
            <el-descriptions-item label="学校">{{ detailDrawer.data.university }}</el-descriptions-item>
            <el-descriptions-item label="院系">{{ detailDrawer.data.faculty }}</el-descriptions-item>
            <el-descriptions-item label="专业">{{ detailDrawer.data.major }}</el-descriptions-item>
            <el-descriptions-item label="学历">{{ detailDrawer.data.degree }}</el-descriptions-item>
            <el-descriptions-item label="毕业年份">{{ detailDrawer.data.graduationYear }}</el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <el-descriptions title="申请信息" :column="2" border>
            <el-descriptions-item label="申请职位">{{ detailDrawer.data.jobTitle }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">{{ detailDrawer.data.applicationTime }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusType(detailDrawer.data.status)">
                {{ getStatusText(detailDrawer.data.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最后更新时间">{{ detailDrawer.data.updateTime }}</el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <h3>个人简历</h3>
          <div class="resume-actions">
            <el-button type="primary" @click="previewResume" :disabled="!detailDrawer.data.resumePath">
              预览简历
            </el-button>
            <el-button type="success" @click="downloadResume" :disabled="!detailDrawer.data.resumePath">
              下载简历
            </el-button>
          </div>

          <el-divider />

          <h3>求职信</h3>
          <div v-if="detailDrawer.data.coverLetter" class="cover-letter">
            {{ detailDrawer.data.coverLetter }}
          </div>
          <el-empty v-else description="未提供求职信" />

          <el-divider />

          <div class="action-buttons">
            <el-button-group>
              <el-button 
                type="primary" 
                @click="handleStatusChange('viewed', detailDrawer.data)"
                :disabled="isStatusDisabled('viewed', detailDrawer.data)"
              >标记为已查看</el-button>
              <el-button 
                type="success" 
                @click="handleStatusChange('interviewing', detailDrawer.data)"
                :disabled="isStatusDisabled('interviewing', detailDrawer.data)"
              >邀请面试</el-button>
              <el-button 
                type="danger" 
                @click="handleStatusChange('rejected', detailDrawer.data)"
                :disabled="isStatusDisabled('rejected', detailDrawer.data)"
              >标记为不合适</el-button>
              <el-button 
                type="warning" 
                @click="handleStatusChange('hired', detailDrawer.data)"
                :disabled="isStatusDisabled('hired', detailDrawer.data)"
              >标记为已录用</el-button>
            </el-button-group>
          </div>
        </template>
      </div>
    </el-drawer>

    <!-- 状态变更对话框 -->
    <el-dialog
      v-model="statusDialog.visible"
      :title="statusDialog.title"
      width="500px"
    >
      <el-form ref="statusForm" :model="statusDialog.form" label-width="100px">
        <el-form-item label="状态变更理由" prop="reason">
          <el-input
            v-model="statusDialog.form.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入状态变更理由，将通知给学生"
          />
        </el-form-item>
        <template v-if="statusDialog.status === 'interviewing'">
          <el-form-item label="面试时间" prop="interviewTime">
            <el-date-picker
              v-model="statusDialog.form.interviewTime"
              type="datetime"
              placeholder="请选择面试时间"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="面试地点" prop="interviewLocation">
            <el-input v-model="statusDialog.form.interviewLocation" placeholder="请输入面试地点" />
          </el-form-item>
          <el-form-item label="面试方式" prop="interviewType">
            <el-radio-group v-model="statusDialog.form.interviewType">
              <el-radio label="onsite">现场面试</el-radio>
              <el-radio label="online">线上面试</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="statusDialog.form.interviewType === 'online'" label="会议链接" prop="meetingUrl">
            <el-input v-model="statusDialog.form.meetingUrl" placeholder="请输入线上会议链接" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="confirmStatusChange">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 简历预览对话框 -->
    <el-dialog
      v-model="resumePreview.visible"
      title="简历预览"
      width="80%"
      :before-close="closeResumePreview"
    >
      <div class="resume-preview-container">
        <iframe v-if="resumePreview.url" :src="resumePreview.url" frameborder="0" width="100%" height="600px"></iframe>
        <el-empty v-else description="无法预览该简历" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowDown,
} from '@element-plus/icons-vue'
import { getApplicationList, getApplicationDetail, updateApplicationStatus } from '@/api/recruit'

// 查询参数
const queryParams = reactive({
  jobId: '',
  status: '',
  startTime: '',
  endTime: '',
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

// 日期范围
const dateRange = ref([])

// 表格数据
const loading = ref(false)
const applicationList = ref([])
const total = ref(0)
const multipleSelection = ref([])
const jobOptions = ref([])

// 详情抽屉
const detailDrawer = reactive({
  visible: false,
  loading: false,
  data: null
})

// 状态变更对话框
const statusDialog = reactive({
  visible: false,
  title: '',
  status: '',
  application: null,
  batch: false,
  selectedIds: [],
  form: {
    reason: '',
    interviewTime: '',
    interviewLocation: '',
    interviewType: 'onsite',
    meetingUrl: ''
  }
})

// 简历预览
const resumePreview = reactive({
  visible: false,
  url: ''
})

// 获取职位选项
const fetchJobOptions = async () => {
  // 实际项目中应该从API获取企业的职位列表
  jobOptions.value = [
    { value: '1', label: '前端开发工程师' },
    { value: '2', label: '后端开发工程师' },
    { value: '3', label: '产品经理' },
    { value: '4', label: '用户体验设计师' },
    { value: '5', label: '测试工程师' }
  ]
}

// 获取申请列表
const fetchApplications = async () => {
  loading.value = true
  try {
    // 实际项目中应该调用API获取数据
    // const response = await getApplicationList(queryParams)
    // applicationList.value = response.data.rows
    // total.value = response.data.total

    // 模拟数据
    setTimeout(() => {
      applicationList.value = [
        {
          id: 1,
          jobId: 1,
          jobTitle: '前端开发工程师',
          studentId: 101,
          studentName: '张三',
          university: '北京大学',
          major: '计算机科学与技术',
          applicationTime: '2023-05-15 10:30:00',
          status: 'applied',
          updateTime: '2023-05-15 10:30:00'
        },
        {
          id: 2,
          jobId: 2,
          jobTitle: '后端开发工程师',
          studentId: 102,
          studentName: '李四',
          university: '清华大学',
          major: '软件工程',
          applicationTime: '2023-05-16 14:20:00',
          status: 'viewed',
          updateTime: '2023-05-16 15:00:00'
        },
        {
          id: 3,
          jobId: 3,
          jobTitle: '产品经理',
          studentId: 103,
          studentName: '王五',
          university: '中国人民大学',
          major: '工商管理',
          applicationTime: '2023-05-17 09:15:00',
          status: 'interviewing',
          updateTime: '2023-05-17 11:30:00'
        },
        {
          id: 4,
          jobId: 4,
          jobTitle: '用户体验设计师',
          studentId: 104,
          studentName: '赵六',
          university: '浙江大学',
          major: '工业设计',
          applicationTime: '2023-05-18 16:45:00',
          status: 'rejected',
          updateTime: '2023-05-19 10:20:00'
        },
        {
          id: 5,
          jobId: 5,
          jobTitle: '测试工程师',
          studentId: 105,
          studentName: '钱七',
          university: '南京大学',
          major: '计算机科学与技术',
          applicationTime: '2023-05-19 11:30:00',
          status: 'hired',
          updateTime: '2023-05-20 14:10:00'
        }
      ]
      total.value = 5
      loading.value = false
    }, 500)
  } catch (error) {
    console.error('获取申请列表失败:', error)
    ElMessage.error('获取申请列表失败')
    loading.value = false
  }
}

// 查看申请详情
const handleViewDetail = async (row) => {
  detailDrawer.visible = true
  detailDrawer.loading = true
  
  try {
    // 实际项目中应该调用API获取详情
    // const response = await getApplicationDetail(row.id)
    // detailDrawer.data = response.data

    // 模拟数据
    setTimeout(() => {
      detailDrawer.data = {
        ...row,
        phone: '138****6789',
        email: 'student****@example.com',
        faculty: '计算机学院',
        degree: '本科',
        graduationYear: '2023',
        resumePath: 'https://example.com/resume/123.pdf',
        coverLetter: '尊敬的招聘负责人：\n\n我怀着极大的热情申请贵公司的职位。在大学期间，我参与了多个实践项目，积累了丰富的经验。我相信自己的技能和态度可以为贵公司带来价值。\n\n感谢您的考虑！',
        updateTime: row.updateTime
      }
      detailDrawer.loading = false
    }, 500)
  } catch (error) {
    console.error('获取申请详情失败:', error)
    ElMessage.error('获取申请详情失败')
    detailDrawer.loading = false
  }
}

// 预览简历
const previewResume = () => {
  resumePreview.visible = true
  // 实际项目中应该根据简历的格式设置预览链接，可能需要后端支持
  resumePreview.url = detailDrawer.data.resumePath
}

// 关闭简历预览
const closeResumePreview = () => {
  resumePreview.visible = false
  resumePreview.url = ''
}

// 下载简历
const downloadResume = () => {
  // 实际项目中应该调用下载API或直接使用a标签下载
  ElMessage.success('简历下载中...')
  window.open(detailDrawer.data.resumePath, '_blank')
}

// 处理多选变化
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection
}

// 处理批量操作
const handleBatchAction = (command) => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning('请选择需要操作的申请')
    return
  }

  statusDialog.batch = true
  statusDialog.selectedIds = multipleSelection.value.map(item => item.id)
  
  switch (command) {
    case 'view':
      statusDialog.title = '批量标记为已查看'
      statusDialog.status = 'viewed'
      break
    case 'interview':
      statusDialog.title = '批量邀请面试'
      statusDialog.status = 'interviewing'
      break
    case 'reject':
      statusDialog.title = '批量标记为不合适'
      statusDialog.status = 'rejected'
      break
    case 'hire':
      statusDialog.title = '批量标记为已录用'
      statusDialog.status = 'hired'
      break
  }
  
  statusDialog.visible = true
}

// 处理状态变更
const handleStatusChange = (status, row) => {
  statusDialog.batch = false
  statusDialog.application = row
  statusDialog.status = status
  
  switch (status) {
    case 'viewed':
      statusDialog.title = '标记为已查看'
      break
    case 'interviewing':
      statusDialog.title = '邀请面试'
      break
    case 'rejected':
      statusDialog.title = '标记为不合适'
      break
    case 'hired':
      statusDialog.title = '标记为已录用'
      break
  }
  
  // 重置表单
  statusDialog.form = {
    reason: '',
    interviewTime: '',
    interviewLocation: '',
    interviewType: 'onsite',
    meetingUrl: ''
  }
  
  statusDialog.visible = true
}

// 确认状态变更
const confirmStatusChange = async () => {
  try {
    // 实际项目中应该调用API更新状态
    if (statusDialog.batch) {
      // const response = await updateApplicationStatus({
      //   ids: statusDialog.selectedIds,
      //   status: statusDialog.status,
      //   ...statusDialog.form
      // })
      
      // 模拟成功响应
      ElMessage.success('批量更新申请状态成功')
    } else {
      // const response = await updateApplicationStatus({
      //   id: statusDialog.application.id,
      //   status: statusDialog.status,
      //   ...statusDialog.form
      // })
      
      // 模拟成功响应
      ElMessage.success('更新申请状态成功')
      
      // 更新详情中的状态
      if (detailDrawer.data && detailDrawer.data.id === statusDialog.application.id) {
        detailDrawer.data.status = statusDialog.status
        detailDrawer.data.updateTime = new Date().toLocaleString()
      }
    }
    
    // 关闭对话框并刷新数据
    statusDialog.visible = false
    fetchApplications()
  } catch (error) {
    console.error('更新申请状态失败:', error)
    ElMessage.error('更新申请状态失败')
  }
}

// 处理日期变化
const handleDateChange = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startTime = dateRange.value[0]
    queryParams.endTime = dateRange.value[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
}

// 重置筛选条件
const resetFilter = () => {
  queryParams.jobId = ''
  queryParams.status = ''
  queryParams.keyword = ''
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  fetchApplications()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchApplications()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchApplications()
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'applied': 'info',
    'viewed': 'warning',
    'interviewing': 'success',
    'rejected': 'danger',
    'hired': 'success'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'applied': '已申请',
    'viewed': '已查看',
    'interviewing': '邀请面试',
    'rejected': '不合适',
    'hired': '已录用'
  }
  return textMap[status] || '未知状态'
}

// 判断状态按钮是否禁用
const isStatusDisabled = (targetStatus, row) => {
  const currentStatus = row.status
  
  // 已查看：只有"已申请"状态的申请才能标记为已查看
  if (targetStatus === 'viewed' && currentStatus !== 'applied') {
    return true
  }
  
  // 邀请面试：只有"已申请"或"已查看"状态的申请才能邀请面试
  if (targetStatus === 'interviewing' && 
     (currentStatus !== 'applied' && currentStatus !== 'viewed')) {
    return true
  }
  
  // 不合适：只有非"不合适"和非"已录用"状态的申请才能标记为不合适
  if (targetStatus === 'rejected' && 
     (currentStatus === 'rejected' || currentStatus === 'hired')) {
    return true
  }
  
  // 已录用：只有非"不合适"和非"已录用"状态的申请才能标记为已录用
  if (targetStatus === 'hired' && 
     (currentStatus === 'rejected' || currentStatus === 'hired')) {
    return true
  }
  
  return false
}

// 生命周期
onMounted(() => {
  fetchJobOptions()
  fetchApplications()
})
</script>

<style scoped>
.application-manage-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.title {
  font-size: 16px;
  font-weight: bold;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.pagination-container {
  margin-top: 15px;
  text-align: right;
}

.application-detail {
  padding: 0 20px;
}

.resume-actions {
  margin-top: 15px;
}

.cover-letter {
  background-color: #f8f8f8;
  padding: 15px;
  border-radius: 4px;
  white-space: pre-line;
}

.action-buttons {
  margin-top: 20px;
  text-align: center;
}

.resume-preview-container {
  height: 600px;
  overflow: hidden;
}
</style> 