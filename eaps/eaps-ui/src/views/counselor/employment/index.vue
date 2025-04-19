<template>
  <div class="employment-manage-container">
    <page-header title="就业情况管理"></page-header>
    
    <!-- 统计概览卡片 -->
    <el-row :gutter="20" class="overview-row">
      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="item-icon">
              <el-icon class="icon-users"><User /></el-icon>
            </div>
            <div class="item-content">
              <div class="item-value">{{ statistics.totalStudents }}</div>
              <div class="item-label">总学生数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="item-icon employment">
              <el-icon class="icon-employment"><Briefcase /></el-icon>
            </div>
            <div class="item-content">
              <div class="item-value">{{ statistics.employedCount }}</div>
              <div class="item-label">已就业人数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="item-icon rate">
              <el-icon class="icon-rate"><DataAnalysis /></el-icon>
            </div>
            <div class="item-content">
              <div class="item-value">{{ statistics.employmentRate }}%</div>
              <div class="item-label">就业率</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="overview-card" shadow="hover">
          <div class="overview-item">
            <div class="item-icon salary">
              <el-icon class="icon-salary"><Money /></el-icon>
            </div>
            <div class="item-content">
              <div class="item-value">{{ statistics.averageSalary }}</div>
              <div class="item-label">平均薪资</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 筛选卡片 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :model="filterForm" inline>
        <el-form-item label="学号">
          <el-input v-model="filterForm.studentId" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="filterForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="filterForm.major" placeholder="选择专业" clearable>
            <el-option
              v-for="item in majorOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="filterForm.grade" placeholder="选择年级" clearable>
            <el-option
              v-for="item in gradeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="就业状态">
          <el-select v-model="filterForm.employmentStatus" placeholder="选择就业状态" clearable>
            <el-option label="已就业" value="employed" />
            <el-option label="未就业" value="unemployed" />
          </el-select>
        </el-form-item>
        <el-form-item label="就业时间">
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
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 就业情况表格 -->
    <el-card class="employment-list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>就业情况列表</span>
          <div class="header-operations">
            <el-button type="success" size="small" @click="exportEmploymentData">
              <el-icon><Download /></el-icon>导出数据
            </el-button>
            <el-button type="primary" size="small" @click="addEmploymentRecord">
              <el-icon><Plus /></el-icon>添加就业记录
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="employmentList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="studentId" label="学号" width="100" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="major" label="专业" width="150" />
        <el-table-column prop="grade" label="年级" width="80" />
        <el-table-column prop="employmentStatus" label="就业状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.employmentStatus === 'employed' ? 'success' : 'info'">
              {{ scope.row.employmentStatus === 'employed' ? '已就业' : '未就业' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="company" label="就业单位" min-width="150" />
        <el-table-column prop="position" label="就业岗位" min-width="120" />
        <el-table-column prop="employmentDate" label="就业日期" width="120" />
        <el-table-column prop="salary" label="薪资" width="100">
          <template #default="scope">
            {{ scope.row.salary ? scope.row.salary + '元/月' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="workLocation" label="工作地点" min-width="120" />
        <el-table-column prop="createTime" label="记录时间" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              @click="editEmploymentRecord(scope.row)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="deleteEmploymentRecord(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 就业记录编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑就业记录' : '添加就业记录'"
      width="500px"
    >
      <el-form
        :model="employmentForm"
        :rules="employmentRules"
        ref="employmentFormRef"
        label-width="100px"
      >
        <el-form-item label="学生" prop="studentId">
          <el-select
            v-model="employmentForm.studentId"
            filterable
            remote
            :remote-method="searchStudents"
            :loading="studentLoading"
            placeholder="请输入学号或姓名搜索"
            style="width: 100%"
            @change="handleStudentChange"
          >
            <el-option
              v-for="item in studentOptions"
              :key="item.id"
              :label="`${item.name} (${item.id})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="就业状态" prop="employmentStatus">
          <el-radio-group v-model="employmentForm.employmentStatus">
            <el-radio label="employed">已就业</el-radio>
            <el-radio label="unemployed">未就业</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <template v-if="employmentForm.employmentStatus === 'employed'">
          <el-form-item label="就业单位" prop="company">
            <el-input v-model="employmentForm.company" placeholder="请输入就业单位名称" />
          </el-form-item>
          <el-form-item label="就业岗位" prop="position">
            <el-input v-model="employmentForm.position" placeholder="请输入就业岗位" />
          </el-form-item>
          <el-form-item label="就业日期" prop="employmentDate">
            <el-date-picker
              v-model="employmentForm.employmentDate"
              type="date"
              placeholder="选择就业日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item label="工作地点" prop="workLocation">
            <el-input v-model="employmentForm.workLocation" placeholder="请输入工作地点" />
          </el-form-item>
          <el-form-item label="薪资(月)" prop="salary">
            <el-input-number
              v-model="employmentForm.salary"
              :min="0"
              :step="1000"
              style="width: 100%"
              placeholder="请输入月薪"
            />
          </el-form-item>
          <el-form-item label="合同期限" prop="contractPeriod">
            <el-select v-model="employmentForm.contractPeriod" placeholder="选择合同期限" style="width: 100%">
              <el-option label="6个月" value="6" />
              <el-option label="1年" value="12" />
              <el-option label="2年" value="24" />
              <el-option label="3年" value="36" />
              <el-option label="5年" value="60" />
              <el-option label="无固定期限" value="-1" />
            </el-select>
          </el-form-item>
        </template>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="employmentForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEmploymentForm" :loading="submitting">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Briefcase, DataAnalysis, Money, Download, Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader/index.vue'
import { 
  getEmploymentStatistics, 
  getEmploymentList, 
  addEmploymentRecord as addEmploymentApi, 
  updateEmploymentRecord as updateEmploymentApi, 
  deleteEmploymentRecord as deleteEmploymentApi,
  exportEmploymentData as exportEmploymentApi,
  searchStudentOptions
} from '@/api/counselor'

// 组件状态
const loading = ref(false)
const submitting = ref(false)
const studentLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const studentOptions = ref([])
const selectedRecords = ref([])
const employmentFormRef = ref(null)

// 统计数据
const statistics = reactive({
  totalStudents: 0,
  employedCount: 0,
  employmentRate: 0,
  averageSalary: 0
})

// 分页状态
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const employmentList = ref([])

// 筛选表单
const filterForm = reactive({
  studentId: '',
  name: '',
  major: '',
  grade: '',
  employmentStatus: '',
  dateRange: []
})

// 就业记录表单
const employmentForm = reactive({
  id: null,
  studentId: '',
  studentName: '',
  employmentStatus: 'employed',
  company: '',
  position: '',
  employmentDate: '',
  workLocation: '',
  salary: 0,
  contractPeriod: '12',
  remark: ''
})

// 专业选项
const majorOptions = [
  { label: '计算机科学与技术', value: '计算机科学与技术' },
  { label: '软件工程', value: '软件工程' },
  { label: '数据科学与大数据技术', value: '数据科学与大数据技术' },
  { label: '信息安全', value: '信息安全' },
  { label: '人工智能', value: '人工智能' },
  { label: '电子信息工程', value: '电子信息工程' },
  { label: '通信工程', value: '通信工程' },
  { label: '工商管理', value: '工商管理' },
  { label: '会计学', value: '会计学' },
  { label: '金融学', value: '金融学' }
]

// 年级选项
const gradeOptions = [
  { label: '2020级', value: '2020' },
  { label: '2021级', value: '2021' },
  { label: '2022级', value: '2022' },
  { label: '2023级', value: '2023' },
  { label: '2024级', value: '2024' }
]

// 表单验证规则
const employmentRules = {
  studentId: [
    { required: true, message: '请选择学生', trigger: 'change' }
  ],
  employmentStatus: [
    { required: true, message: '请选择就业状态', trigger: 'change' }
  ],
  company: [
    { required: true, message: '请输入就业单位', trigger: 'blur' }
  ],
  position: [
    { required: true, message: '请输入就业岗位', trigger: 'blur' }
  ],
  employmentDate: [
    { required: true, message: '请选择就业日期', trigger: 'change' }
  ]
}

// 根据筛选条件生成查询参数
const queryParams = computed(() => {
  const params = { ...filterForm }
  
  if (filterForm.dateRange && filterForm.dateRange.length === 2) {
    params.startDate = filterForm.dateRange[0]
    params.endDate = filterForm.dateRange[1]
    delete params.dateRange
  }
  
  return params
})

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await getEmploymentStatistics(queryParams.value)
    
    if (res && res.data) {
      statistics.totalStudents = res.data.totalStudents || 0
      statistics.employedCount = res.data.employedCount || 0
      statistics.employmentRate = res.data.employmentRate || 0
      statistics.averageSalary = res.data.averageSalary || 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取就业记录列表
const fetchEmploymentList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...queryParams.value
    }
    
    const res = await getEmploymentList(params)
    
    if (res && res.data) {
      employmentList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取就业记录列表失败:', error)
    ElMessage.error('获取就业记录列表失败')
  } finally {
    loading.value = false
  }
}

// 查询处理
const handleSearch = () => {
  pagination.currentPage = 1
  fetchEmploymentList()
  fetchStatistics()
}

// 重置筛选条件
const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key] = ''
  })
  filterForm.dateRange = []
  handleSearch()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchEmploymentList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchEmploymentList()
}

// 搜索学生
const searchStudents = async (query) => {
  if (query.trim() === '') {
    studentOptions.value = []
    return
  }
  
  studentLoading.value = true
  try {
    const res = await searchStudentOptions(query)
    if (res && res.data) {
      studentOptions.value = res.data || []
    }
  } catch (error) {
    console.error('搜索学生失败:', error)
  } finally {
    studentLoading.value = false
  }
}

// 处理学生选择变化
const handleStudentChange = (value) => {
  if (!value) return
  
  const selectedStudent = studentOptions.value.find(item => item.id === value)
  if (selectedStudent) {
    employmentForm.studentName = selectedStudent.name
  }
}

// 添加就业记录
const addEmploymentRecord = () => {
  isEdit.value = false
  // 重置表单
  if (employmentFormRef.value) {
    employmentFormRef.value.resetFields()
  }
  
  // 设置默认值
  Object.keys(employmentForm).forEach(key => {
    if (key !== 'employmentStatus' && key !== 'contractPeriod') {
      employmentForm[key] = ''
    }
  })
  employmentForm.id = null
  employmentForm.studentId = ''
  employmentForm.studentName = ''
  employmentForm.employmentStatus = 'employed'
  employmentForm.salary = 0
  employmentForm.contractPeriod = '12'
  
  dialogVisible.value = true
}

// 编辑就业记录
const editEmploymentRecord = (record) => {
  isEdit.value = true
  // 重置表单
  if (employmentFormRef.value) {
    employmentFormRef.value.resetFields()
  }
  
  // 设置表单值
  Object.keys(employmentForm).forEach(key => {
    if (key in record) {
      employmentForm[key] = record[key]
    }
  })
  
  // 确保选项列表包含当前学生
  if (record.studentId && record.studentName) {
    const exists = studentOptions.value.some(item => item.id === record.studentId)
    if (!exists) {
      studentOptions.value.push({
        id: record.studentId,
        name: record.studentName
      })
    }
  }
  
  dialogVisible.value = true
}

// 提交就业记录表单
const submitEmploymentForm = async () => {
  if (!employmentFormRef.value) return
  
  await employmentFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      const formData = { ...employmentForm }
      
      let res
      if (isEdit.value) {
        res = await updateEmploymentApi(formData.id, formData)
      } else {
        res = await addEmploymentApi(formData)
      }
      
      if (res && res.code === 200) {
        ElMessage.success(isEdit.value ? '就业记录更新成功' : '就业记录添加成功')
        dialogVisible.value = false
        
        // 更新列表和统计
        fetchEmploymentList()
        fetchStatistics()
      }
    } catch (error) {
      console.error(isEdit.value ? '更新就业记录失败:' : '添加就业记录失败:', error)
      ElMessage.error(isEdit.value ? '更新就业记录失败' : '添加就业记录失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除就业记录
const deleteEmploymentRecord = (record) => {
  ElMessageBox.confirm(`确定要删除该就业记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteEmploymentApi(record.id)
      
      if (res && res.code === 200) {
        ElMessage.success('删除就业记录成功')
        
        // 更新列表和统计
        fetchEmploymentList()
        fetchStatistics()
      }
    } catch (error) {
      console.error('删除就业记录失败:', error)
      ElMessage.error('删除就业记录失败')
    }
  }).catch(() => {})
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedRecords.value = selection
}

// 导出就业数据
const exportEmploymentData = async () => {
  try {
    const params = { ...queryParams.value }
    const res = await exportEmploymentApi(params)
    
    // 处理文件下载
    if (res && res.data) {
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', '就业情况数据.xlsx')
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('导出就业数据成功')
    }
  } catch (error) {
    console.error('导出就业数据失败:', error)
    ElMessage.error('导出就业数据失败')
  }
}

// 初始化
onMounted(() => {
  fetchEmploymentList()
  fetchStatistics()
})
</script>

<style lang="scss" scoped>
.employment-manage-container {
  padding: 20px;
  
  .overview-row {
    margin-bottom: 20px;
    
    .overview-card {
      height: 120px;
      
      .overview-item {
        display: flex;
        align-items: center;
        height: 100%;
        
        .item-icon {
          width: 64px;
          height: 64px;
          border-radius: 50%;
          background-color: #ecf5ff;
          display: flex;
          justify-content: center;
          align-items: center;
          margin-right: 15px;
          
          &.employment {
            background-color: #f0f9eb;
          }
          
          &.rate {
            background-color: #fdf6ec;
          }
          
          &.salary {
            background-color: #fef0f0;
          }
          
          .el-icon {
            font-size: 32px;
            color: #409eff;
          }
          
          .icon-employment {
            color: #67c23a;
          }
          
          .icon-rate {
            color: #e6a23c;
          }
          
          .icon-salary {
            color: #f56c6c;
          }
        }
        
        .item-content {
          .item-value {
            font-size: 24px;
            font-weight: bold;
            color: #303133;
            line-height: 1.2;
          }
          
          .item-label {
            font-size: 14px;
            color: #909399;
            margin-top: 8px;
          }
        }
      }
    }
  }
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .employment-list-card {
    margin-bottom: 20px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-operations {
        display: flex;
        gap: 10px;
      }
    }
    
    .pagination-container {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style> 