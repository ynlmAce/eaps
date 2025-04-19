<template>
  <div class="employment-statistics-container">
    <page-header title="就业统计"></page-header>
    
    <!-- 统计概览卡片 -->
    <el-card class="statistics-overview" shadow="hover">
      <div class="overview-items">
        <div class="overview-item">
          <div class="item-value">{{ statistics.totalStudents }}</div>
          <div class="item-label">总学生数</div>
        </div>
        <div class="overview-item">
          <div class="item-value">{{ statistics.employedCount }}</div>
          <div class="item-label">已就业人数</div>
        </div>
        <div class="overview-item">
          <div class="item-value">{{ statistics.employmentRate }}%</div>
          <div class="item-label">就业率</div>
        </div>
        <div class="overview-item">
          <div class="item-value">{{ statistics.averageSalary }}</div>
          <div class="item-label">平均薪资</div>
        </div>
      </div>
    </el-card>
    
    <!-- 筛选卡片 -->
    <el-card class="filter-card" shadow="hover">
      <el-form :model="filterForm" inline>
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
    
    <!-- 统计图表卡片 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>各专业就业率</span>
            </div>
          </template>
          <div class="chart-container" ref="majorChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>就业行业分布</span>
            </div>
          </template>
          <div class="chart-container" ref="industryChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>薪资分布</span>
            </div>
          </template>
          <div class="chart-container" ref="salaryChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>月度就业趋势</span>
            </div>
          </template>
          <div class="chart-container" ref="trendChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 学生就业列表 -->
    <el-card class="student-list-card" shadow="hover">
      <template #header>
        <div class="student-list-header">
          <span>学生就业明细</span>
          <el-button type="primary" size="small" @click="exportEmploymentData">导出数据</el-button>
        </div>
      </template>
      
      <el-table
        v-loading="loading"
        :data="studentList"
        border
        style="width: 100%"
      >
        <el-table-column prop="id" label="学号" width="100" />
        <el-table-column prop="name" label="姓名" width="100" />
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
        <el-table-column prop="salary" label="薪资" />
        <el-table-column prop="employmentDate" label="就业日期" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button
              size="small"
              @click="viewStudentDetail(scope.row)"
            >
              详情
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
      title="学生就业详情"
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
          <el-descriptions-item label="性别">{{ selectedStudent.gender }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ selectedStudent.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedStudent.email }}</el-descriptions-item>
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
          <el-descriptions-item v-if="selectedStudent.employmentStatus === 'employed'" label="工作地点">
            {{ selectedStudent.workLocation }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedStudent.employmentStatus === 'employed'" label="薪资">
            {{ selectedStudent.salary }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedStudent.employmentStatus === 'employed'" label="就业日期">
            {{ selectedStudent.employmentDate }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts/core'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader/index.vue'
import { 
  getEmploymentStatistics,
  getEmploymentStudentList,
  getMajorEmploymentRate,
  getIndustryDistribution,
  getSalaryDistribution,
  getEmploymentTrend,
  exportEmploymentData as exportData
} from '@/api/employment'

// 注册必须的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  BarChart,
  PieChart,
  LineChart,
  CanvasRenderer
])

// 统计数据
const statistics = reactive({
  totalStudents: 0,
  employedCount: 0,
  employmentRate: 0,
  averageSalary: 0
})

// 筛选表单
const filterForm = reactive({
  major: '',
  grade: '',
  employmentStatus: ''
})

// 专业和年级选项
const majorOptions = ref([
  { value: '计算机科学与技术', label: '计算机科学与技术' },
  { value: '软件工程', label: '软件工程' },
  { value: '人工智能', label: '人工智能' },
  { value: '数据科学与大数据技术', label: '数据科学与大数据技术' }
])

const gradeOptions = ref([
  { value: '2020', label: '2020级' },
  { value: '2021', label: '2021级' },
  { value: '2022', label: '2022级' },
  { value: '2023', label: '2023级' }
])

// 学生列表数据
const loading = ref(false)
const studentList = ref([])
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 抽屉相关
const drawerVisible = ref(false)
const selectedStudent = ref(null)

// 图表引用
const majorChartRef = ref(null)
const industryChartRef = ref(null)
const salaryChartRef = ref(null)
const trendChartRef = ref(null)

// 图表实例
let majorChart = null
let industryChart = null
let salaryChart = null
let trendChart = null

// 初始化
onMounted(() => {
  fetchStatisticsData()
  fetchStudentList()
  nextTick(() => {
    initCharts()
  })
})

// 获取统计数据
const fetchStatisticsData = async () => {
  try {
    const { data } = await getEmploymentStatistics()
    Object.assign(statistics, data)
  } catch (error) {
    console.error('获取统计数据失败', error)
    ElMessage.error('获取统计数据失败')
    // 使用模拟数据作为后备
    Object.assign(statistics, {
      totalStudents: 1200,
      employedCount: 980,
      employmentRate: 81.67,
      averageSalary: '8500元'
    })
  }
}

// 获取学生列表
const fetchStudentList = async () => {
  loading.value = true
  try {
    const { data } = await getEmploymentStudentList({
      page: pagination.currentPage,
      pageSize: pagination.pageSize,
      ...filterForm
    })
    
    studentList.value = data.list
    pagination.total = data.total
  } catch (error) {
    console.error('获取学生列表失败', error)
    ElMessage.error('获取学生列表失败')
    // 使用模拟数据作为后备
    const mockData = {
      total: 125,
      list: Array.from({ length: 10 }, (_, index) => ({
        id: `2020${String(index + 1).padStart(6, '0')}`,
        name: `学生${index + 1}`,
        gender: index % 2 === 0 ? '男' : '女',
        major: majorOptions.value[index % 4].value,
        grade: gradeOptions.value[index % 4].value,
        employmentStatus: index < 8 ? 'employed' : 'unemployed',
        company: index < 8 ? `公司${index + 1}` : '',
        position: index < 8 ? `职位${index + 1}` : '',
        salary: index < 8 ? `${7000 + index * 500}元` : '',
        employmentDate: index < 8 ? `2023-${String(index + 1).padStart(2, '0')}-01` : '',
        phone: `1391234${String(index + 1).padStart(4, '0')}`,
        email: `student${index + 1}@example.com`,
        workLocation: index < 8 ? ['北京', '上海', '广州', '深圳'][index % 4] : ''
      }))
    }
    
    studentList.value = mockData.list
    pagination.total = mockData.total
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = () => {
  initMajorChart()
  initIndustryChart()
  initSalaryChart()
  initTrendChart()
}

// 专业就业率图表
const initMajorChart = async () => {
  if (majorChartRef.value) {
    majorChart = echarts.init(majorChartRef.value)
    
    try {
      const { data } = await getMajorEmploymentRate()
      
      const option = {
        title: {
          text: '各专业就业率',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: data.majors || ['计算机科学与技术', '软件工程', '人工智能', '数据科学与大数据技术']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: '就业率',
            type: 'bar',
            data: data.rates || [85, 92, 78, 88],
            itemStyle: {
              color: '#409EFF'
            }
          }
        ]
      }
      
      majorChart.setOption(option)
    } catch (error) {
      console.error('获取专业就业率数据失败', error)
      // 使用备用数据
      majorChart.setOption({
        title: {
          text: '各专业就业率',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: ['计算机科学与技术', '软件工程', '人工智能', '数据科学与大数据技术']
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value}%'
          }
        },
        series: [
          {
            name: '就业率',
            type: 'bar',
            data: [85, 92, 78, 88],
            itemStyle: {
              color: '#409EFF'
            }
          }
        ]
      })
    }
  }
}

// 行业分布图表
const initIndustryChart = async () => {
  if (industryChartRef.value) {
    industryChart = echarts.init(industryChartRef.value)
    
    try {
      const { data } = await getIndustryDistribution()
      
      const option = {
        title: {
          text: '就业行业分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: data.industries || ['互联网', '金融', '教育', '制造业', '其他']
        },
        series: [
          {
            name: '行业分布',
            type: 'pie',
            radius: '60%',
            center: ['50%', '60%'],
            data: data.distribution || [
              { value: 45, name: '互联网' },
              { value: 20, name: '金融' },
              { value: 15, name: '教育' },
              { value: 10, name: '制造业' },
              { value: 10, name: '其他' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      }
      
      industryChart.setOption(option)
    } catch (error) {
      console.error('获取行业分布数据失败', error)
      // 使用备用数据
      industryChart.setOption({
        title: {
          text: '就业行业分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['互联网', '金融', '教育', '制造业', '其他']
        },
        series: [
          {
            name: '行业分布',
            type: 'pie',
            radius: '60%',
            center: ['50%', '60%'],
            data: [
              { value: 45, name: '互联网' },
              { value: 20, name: '金融' },
              { value: 15, name: '教育' },
              { value: 10, name: '制造业' },
              { value: 10, name: '其他' }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      })
    }
  }
}

// 薪资分布图表
const initSalaryChart = async () => {
  if (salaryChartRef.value) {
    salaryChart = echarts.init(salaryChartRef.value)
    
    try {
      const { data } = await getSalaryDistribution()
      
      const option = {
        title: {
          text: '薪资分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: data.ranges || ['5k以下', '5k-8k', '8k-12k', '12k-15k', '15k-20k', '20k以上']
        },
        yAxis: {
          type: 'value',
          name: '人数'
        },
        series: [
          {
            name: '人数',
            type: 'bar',
            data: data.counts || [50, 180, 360, 250, 100, 40],
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      }
      
      salaryChart.setOption(option)
    } catch (error) {
      console.error('获取薪资分布数据失败', error)
      // 使用备用数据
      salaryChart.setOption({
        title: {
          text: '薪资分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: ['5k以下', '5k-8k', '8k-12k', '12k-15k', '15k-20k', '20k以上']
        },
        yAxis: {
          type: 'value',
          name: '人数'
        },
        series: [
          {
            name: '人数',
            type: 'bar',
            data: [50, 180, 360, 250, 100, 40],
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      })
    }
  }
}

// 就业趋势图表
const initTrendChart = async () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    
    try {
      const { data } = await getEmploymentTrend({ year: new Date().getFullYear() })
      
      const option = {
        title: {
          text: '月度就业趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.months || ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        },
        yAxis: {
          type: 'value',
          name: '就业人数'
        },
        series: [
          {
            name: '就业人数',
            type: 'line',
            data: data.counts || [30, 40, 50, 60, 70, 120, 160, 140, 100, 80, 70, 60],
            smooth: true,
            lineStyle: {
              color: '#F56C6C'
            },
            itemStyle: {
              color: '#F56C6C'
            }
          }
        ]
      }
      
      trendChart.setOption(option)
    } catch (error) {
      console.error('获取就业趋势数据失败', error)
      // 使用备用数据
      trendChart.setOption({
        title: {
          text: '月度就业趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月']
        },
        yAxis: {
          type: 'value',
          name: '就业人数'
        },
        series: [
          {
            name: '就业人数',
            type: 'line',
            data: [30, 40, 50, 60, 70, 120, 160, 140, 100, 80, 70, 60],
            smooth: true,
            lineStyle: {
              color: '#F56C6C'
            },
            itemStyle: {
              color: '#F56C6C'
            }
          }
        ]
      })
    }
  }
}

// 处理搜索
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
const viewStudentDetail = (student) => {
  selectedStudent.value = student
  drawerVisible.value = true
}

// 导出就业数据
const exportEmploymentData = async () => {
  try {
    ElMessageBox.confirm('确定要导出当前筛选条件下的就业数据吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    }).then(async () => {
      try {
        const response = await exportData(filterForm)
        
        // 创建下载链接
        const blob = new Blob([response.data])
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = `就业数据_${new Date().getTime()}.xlsx`
        link.click()
        URL.revokeObjectURL(link.href)
        
        ElMessage.success('导出成功')
      } catch (error) {
        console.error('导出失败', error)
        ElMessage.error('导出失败')
      }
    }).catch(() => {})
  } catch (error) {
    ElMessage.error('导出数据失败')
  }
}

// 窗口大小变化时重绘图表
window.addEventListener('resize', () => {
  majorChart?.resize()
  industryChart?.resize()
  salaryChart?.resize()
  trendChart?.resize()
})
</script>

<style scoped>
.employment-statistics-container {
  padding: 20px;
}

.statistics-overview {
  margin-bottom: 20px;
}

.overview-items {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
}

.overview-item {
  text-align: center;
  padding: 15px;
}

.item-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 8px;
}

.item-label {
  font-size: 14px;
  color: #606266;
}

.filter-card {
  margin-bottom: 20px;
  padding: 10px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
  height: 350px;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 280px;
}

.student-list-card {
  margin-bottom: 20px;
}

.student-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.student-detail {
  padding: 20px;
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.student-info {
  margin-left: 15px;
}

.student-info h3 {
  margin: 0;
  font-size: 18px;
}

.student-info p {
  margin: 5px 0 0;
  color: #606266;
}

@media (max-width: 768px) {
  .overview-items {
    flex-direction: column;
  }
  
  .chart-row .el-col {
    width: 100%;
  }
}
</style> 