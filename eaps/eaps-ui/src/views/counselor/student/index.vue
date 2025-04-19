<template>
  <div class="student-manage-container">
    <page-header title="学生管理"></page-header>
    
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
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 学生列表卡片 -->
    <el-card class="student-list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>学生列表</span>
          <div class="header-operations">
            <el-button type="success" size="small" @click="exportStudentData">
              <el-icon><Download /></el-icon>导出数据
            </el-button>
            <el-button type="primary" size="small" @click="batchEmploymentUpdate">
              <el-icon><Edit /></el-icon>批量更新就业状态
            </el-button>
          </div>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="studentList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="学号" width="100" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="scope">
            {{ scope.row.gender === 'male' ? '男' : '女' }}
          </template>
        </el-table-column>
        <el-table-column prop="major" label="专业" width="150" />
        <el-table-column prop="grade" label="年级" width="80" />
        <el-table-column prop="employmentStatus" label="就业状态">
          <template #default="scope">
            <el-tag :type="scope.row.employmentStatus === 'employed' ? 'success' : 'info'">
              {{ scope.row.employmentStatus === 'employed' ? '已就业' : '未就业' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="company" label="就业单位" />
        <el-table-column prop="position" label="就业岗位" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button
              size="small"
              @click="viewStudentDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button
              size="small"
              type="primary"
              @click="updateEmployment(scope.row)"
            >
              更新就业
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
    
    <!-- 学生详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      title="学生详情"
      direction="rtl"
      size="500px"
    >
      <div v-if="selectedStudent" class="student-detail">
        <div class="detail-header">
          <el-avatar :size="64" :src="selectedStudent.avatar || '/avatar-placeholder.png'" />
          <div class="student-info">
            <h3>{{ selectedStudent.name }}</h3>
            <p>{{ selectedStudent.major }} - {{ selectedStudent.grade }}级</p>
          </div>
        </div>
        
        <el-divider content-position="left">基本信息</el-divider>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学号">{{ selectedStudent.id }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ selectedStudent.gender === 'male' ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ selectedStudent.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedStudent.email }}</el-descriptions-item>
          <el-descriptions-item label="入学时间">{{ selectedStudent.enrollmentDate }}</el-descriptions-item>
          <el-descriptions-item label="预计毕业时间">{{ selectedStudent.expectedGraduationDate }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">就业信息</el-divider>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="就业状态">
            <el-tag :type="selectedStudent.employmentStatus === 'employed' ? 'success' : 'info'">
              {{ selectedStudent.employmentStatus === 'employed' ? '已就业' : '未就业' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedStudent.employmentStatus === 'employed'" label="就业单位">
            {{ selectedStudent.company }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedStudent.employmentStatus === 'employed'" label="就业岗位">
            {{ selectedStudent.position }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedStudent.employmentStatus === 'employed'" label="就业日期">
            {{ selectedStudent.employmentDate }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedStudent.employmentStatus === 'employed'" label="工作地点">
            {{ selectedStudent.workLocation }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedStudent.employmentStatus === 'employed'" label="薪资">
            {{ selectedStudent.salary }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider content-position="left">学业信息</el-divider>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="平均绩点">{{ selectedStudent.gpa }}</el-descriptions-item>
          <el-descriptions-item label="学位">{{ selectedStudent.degree }}</el-descriptions-item>
          <el-descriptions-item label="专业排名">{{ selectedStudent.ranking || '暂无' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="drawer-footer">
          <el-button type="primary" @click="updateEmployment(selectedStudent)">更新就业信息</el-button>
          <el-button @click="exportStudentInfo(selectedStudent)">导出信息</el-button>
          <el-button @click="drawerVisible = false">关闭</el-button>
        </div>
      </div>
    </el-drawer>
    
    <!-- 就业信息更新对话框 -->
    <el-dialog
      v-model="employmentDialogVisible"
      title="更新就业信息"
      width="500px"
    >
      <el-form
        :model="employmentForm"
        :rules="employmentRules"
        ref="employmentFormRef"
        label-width="100px"
      >
        <el-form-item label="就业状态" prop="status">
          <el-radio-group v-model="employmentForm.status">
            <el-radio label="employed">已就业</el-radio>
            <el-radio label="unemployed">未就业</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <template v-if="employmentForm.status === 'employed'">
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
          <el-button @click="employmentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEmployment" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 批量更新就业状态对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量更新就业状态"
      width="500px"
    >
      <div v-if="selectedStudents.length === 0" class="empty-selection">
        <el-empty description="请先选择学生"></el-empty>
      </div>
      <div v-else>
        <p class="selected-count">已选择 {{ selectedStudents.length }} 名学生</p>
        
        <el-form
          :model="batchForm"
          :rules="batchRules"
          ref="batchFormRef"
          label-width="100px"
        >
          <el-form-item label="就业状态" prop="status">
            <el-radio-group v-model="batchForm.status">
              <el-radio label="employed">已就业</el-radio>
              <el-radio label="unemployed">未就业</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <template v-if="batchForm.status === 'employed'">
            <el-form-item label="就业日期" prop="employmentDate">
              <el-date-picker
                v-model="batchForm.employmentDate"
                type="date"
                placeholder="选择就业日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </template>
          
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="batchForm.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入批量更新备注信息"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="batchDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            @click="submitBatchUpdate"
            :loading="batchSubmitting"
            :disabled="selectedStudents.length === 0"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Edit } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader/index.vue'
import { 
  getStudentList, 
  getStudentDetail, 
  updateStudentEmployment, 
  batchUpdateEmployment, 
  exportStudentData as exportStudentDataApi 
} from '@/api/counselor'

// 组件状态
const loading = ref(false)
const submitting = ref(false)
const batchSubmitting = ref(false)
const drawerVisible = ref(false)
const employmentDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const selectedStudent = ref(null)
const selectedStudents = ref([])
const employmentFormRef = ref(null)
const batchFormRef = ref(null)

// 分页状态
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const studentList = ref([])

// 筛选表单
const filterForm = reactive({
  studentId: '',
  name: '',
  major: '',
  grade: '',
  employmentStatus: ''
})

// 更新就业表单
const employmentForm = reactive({
  studentId: '',
  status: 'employed',
  company: '',
  position: '',
  employmentDate: '',
  workLocation: '',
  salary: 0,
  remark: ''
})

// 批量更新表单
const batchForm = reactive({
  studentIds: [],
  status: 'employed',
  employmentDate: '',
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
  status: [
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

// 批量更新验证规则
const batchRules = {
  status: [
    { required: true, message: '请选择就业状态', trigger: 'change' }
  ],
  employmentDate: [
    { required: true, message: '请选择就业日期', trigger: 'change' }
  ]
}

// 获取学生列表
const fetchStudentList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...filterForm
    }
    
    const res = await getStudentList(params)
    
    if (res && res.data) {
      studentList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('获取学生列表失败:', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

// 查询处理
const handleSearch = () => {
  pagination.currentPage = 1
  fetchStudentList()
}

// 重置筛选条件
const resetFilter = () => {
  Object.keys(filterForm).forEach(key => {
    filterForm[key] = ''
  })
  handleSearch()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.currentPage = val
  fetchStudentList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  fetchStudentList()
}

// 查看学生详情
const viewStudentDetail = async (student) => {
  try {
    const res = await getStudentDetail(student.id)
    
    if (res && res.data) {
      selectedStudent.value = res.data
      drawerVisible.value = true
    }
  } catch (error) {
    console.error('获取学生详情失败:', error)
    ElMessage.error('获取学生详情失败')
  }
}

// 更新就业信息
const updateEmployment = (student) => {
  // 重置表单
  if (employmentFormRef.value) {
    employmentFormRef.value.resetFields()
  }
  
  // 根据学生就业状态设置表单初始值
  employmentForm.studentId = student.id
  employmentForm.status = student.employmentStatus || 'unemployed'
  
  if (student.employmentStatus === 'employed') {
    employmentForm.company = student.company || ''
    employmentForm.position = student.position || ''
    employmentForm.employmentDate = student.employmentDate || ''
    employmentForm.workLocation = student.workLocation || ''
    employmentForm.salary = student.salary || 0
  } else {
    employmentForm.company = ''
    employmentForm.position = ''
    employmentForm.employmentDate = ''
    employmentForm.workLocation = ''
    employmentForm.salary = 0
  }
  
  employmentForm.remark = ''
  
  // 显示对话框
  employmentDialogVisible.value = true
}

// 提交就业信息更新
const submitEmployment = async () => {
  if (!employmentFormRef.value) return
  
  await employmentFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    
    try {
      const data = {
        ...employmentForm
      }
      
      const res = await updateStudentEmployment(data)
      
      if (res && res.code === 200) {
        ElMessage.success('就业信息更新成功')
        employmentDialogVisible.value = false
        
        // 更新列表和详情
        fetchStudentList()
        
        // 如果抽屉打开，更新学生详情
        if (drawerVisible.value && selectedStudent.value && selectedStudent.value.id === employmentForm.studentId) {
          viewStudentDetail({ id: employmentForm.studentId })
        }
      }
    } catch (error) {
      console.error('更新就业信息失败:', error)
      ElMessage.error('更新就业信息失败')
    } finally {
      submitting.value = false
    }
  })
}

// 批量更新就业状态
const batchEmploymentUpdate = () => {
  if (selectedStudents.value.length === 0) {
    ElMessage.warning('请先选择学生')
    return
  }
  
  // 重置表单
  if (batchFormRef.value) {
    batchFormRef.value.resetFields()
  }
  
  batchForm.studentIds = selectedStudents.value.map(item => item.id)
  batchForm.status = 'employed'
  batchForm.employmentDate = ''
  batchForm.remark = ''
  
  batchDialogVisible.value = true
}

// 提交批量更新
const submitBatchUpdate = async () => {
  if (!batchFormRef.value) return
  
  await batchFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    batchSubmitting.value = true
    
    try {
      const data = {
        ...batchForm
      }
      
      const res = await batchUpdateEmployment(data)
      
      if (res && res.code === 200) {
        ElMessage.success(`成功更新 ${selectedStudents.value.length} 名学生的就业状态`)
        batchDialogVisible.value = false
        
        // 更新列表
        fetchStudentList()
      }
    } catch (error) {
      console.error('批量更新就业状态失败:', error)
      ElMessage.error('批量更新就业状态失败')
    } finally {
      batchSubmitting.value = false
    }
  })
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedStudents.value = selection
}

// 导出学生数据
const exportStudentData = async () => {
  try {
    const params = { ...filterForm }
    const res = await exportStudentDataApi(params)
    
    // 处理文件下载
    if (res && res.data) {
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', '学生就业数据.xlsx')
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('导出学生数据成功')
    }
  } catch (error) {
    console.error('导出学生数据失败:', error)
    ElMessage.error('导出学生数据失败')
  }
}

// 导出单个学生信息
const exportStudentInfo = async (student) => {
  try {
    const res = await exportStudentDataApi({ studentId: student.id })
    
    // 处理文件下载
    if (res && res.data) {
      const url = window.URL.createObjectURL(new Blob([res.data]))
      const link = document.createElement('a')
      link.href = url
      link.setAttribute('download', `学生信息_${student.id}.xlsx`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('导出学生信息成功')
    }
  } catch (error) {
    console.error('导出学生信息失败:', error)
    ElMessage.error('导出学生信息失败')
  }
}

// 初始化
onMounted(() => {
  fetchStudentList()
})
</script>

<style lang="scss" scoped>
.student-manage-container {
  padding: 20px;
  
  .filter-card {
    margin-bottom: 20px;
  }
  
  .student-list-card {
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
  
  .student-detail {
    padding: 10px;
    
    .detail-header {
      display: flex;
      align-items: center;
      margin-bottom: 20px;
      
      .student-info {
        margin-left: 15px;
        
        h3 {
          margin: 0;
          font-size: 18px;
          color: #303133;
        }
        
        p {
          margin: 5px 0 0;
          font-size: 14px;
          color: #606266;
        }
      }
    }
    
    .drawer-footer {
      position: absolute;
      bottom: 20px;
      left: 20px;
      right: 20px;
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
  
  .empty-selection {
    display: flex;
    justify-content: center;
    padding: 30px 0;
  }
  
  .selected-count {
    text-align: center;
    margin-bottom: 20px;
    font-size: 16px;
    font-weight: 500;
    color: #409EFF;
  }
}
</style> 