<template>
  <div class="counselor-statistics-container">
    <el-card class="overview-card">
      <template #header>
        <div class="card-header">
          <span>就业统计概览</span>
          <el-button type="primary" @click="exportStatistics">导出统计数据</el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6" v-for="(item, index) in overviewData" :key="index">
          <div class="overview-item">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-value">{{ item.value }}</div>
            <div class="item-change" :class="{ 'up': item.change > 0, 'down': item.change < 0 }">
              <el-icon v-if="item.change > 0"><ArrowUp /></el-icon>
              <el-icon v-else-if="item.change < 0"><ArrowDown /></el-icon>
              <span>{{ Math.abs(item.change) }}%</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>就业率趋势</span>
              <el-select v-model="trendTimeRange" placeholder="时间范围" size="small">
                <el-option label="近一周" value="week" />
                <el-option label="近一月" value="month" />
                <el-option label="近一年" value="year" />
              </el-select>
            </div>
          </template>
          <div class="chart-container" id="trend-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>就业进度</span>
              <el-select v-model="selectedClass" placeholder="选择班级" size="small">
                <el-option
                  v-for="item in classList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </div>
          </template>
          <div class="chart-container" id="progress-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>行业分布</span>
            </div>
          </template>
          <div class="chart-container" id="industry-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>地区分布</span>
            </div>
          </template>
          <div class="chart-container" id="region-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="student-list-card">
      <template #header>
        <div class="card-header">
          <span>学生就业情况</span>
          <div class="filter-container">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索学生姓名/学号"
              style="width: 200px"
              class="filter-item"
              size="small"
              @keyup.enter="handleSearch"
            />
            <el-select v-model="employmentStatus" placeholder="就业状态" size="small" class="filter-item">
              <el-option label="全部" value="" />
              <el-option label="已就业" value="employed" />
              <el-option label="未就业" value="unemployed" />
              <el-option label="考研" value="graduate_study" />
              <el-option label="创业" value="startup" />
            </el-select>
            <el-button type="primary" size="small" @click="handleSearch">搜索</el-button>
            <el-button size="small" @click="resetSearch">重置</el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="studentList"
        style="width: 100%"
        border
      >
        <el-table-column prop="id" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="className" label="班级" width="150" />
        <el-table-column prop="major" label="专业" width="150" />
        <el-table-column prop="status" label="就业状态">
          <template #default="scope">
            <el-tag
              :type="getStatusType(scope.row.status)"
              effect="plain"
            >
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="company" label="就业单位" />
        <el-table-column prop="position" label="职位" />
        <el-table-column prop="location" label="地区" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              link
              @click="handleViewDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 30, 50]"
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
      size="30%"
      destroy-on-close
    >
      <div v-if="currentStudent" class="student-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学号">{{ currentStudent.id }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ currentStudent.className }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
          <el-descriptions-item label="就业状态">
            <el-tag :type="getStatusType(currentStudent.status)">
              {{ getStatusLabel(currentStudent.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="就业单位">{{ currentStudent.company || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="职位">{{ currentStudent.position || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="地区">{{ currentStudent.location || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="薪资范围">{{ currentStudent.salary || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="入职时间">{{ currentStudent.entryDate || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentStudent.remark || '暂无' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ArrowUp, ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import {
  getEmploymentStatistics,
  getStudentList,
  getStudentDetail,
  getClassList,
  getEmploymentProgress,
  exportEmploymentStatistics,
  getEmploymentTrend,
  getIndustryDistribution,
  getRegionDistribution
} from '@/api/counselor'

// 统计概览数据
const overviewData = ref([
  { title: '总学生数', value: 0, change: 0 },
  { title: '就业率', value: '0%', change: 0 },
  { title: '考研率', value: '0%', change: 0 },
  { title: '创业率', value: '0%', change: 0 }
])

// 图表相关
const trendTimeRange = ref('month')
const selectedClass = ref('')
const classList = ref([])
let trendChart = null
let progressChart = null
let industryChart = null
let regionChart = null

// 学生列表相关
const loading = ref(false)
const studentList = ref([])
const searchKeyword = ref('')
const employmentStatus = ref('')
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 学生详情相关
const drawerVisible = ref(false)
const currentStudent = ref(null)

// 初始化数据
onMounted(() => {
  fetchStatisticsData()
  fetchClassList()
  fetchStudentList()
  
  // 初始化图表
  initCharts()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

// 获取统计数据
const fetchStatisticsData = async () => {
  try {
    const res = await getEmploymentStatistics()
    if (res.data) {
      const data = res.data
      overviewData.value = [
        { title: '总学生数', value: data.totalStudents, change: data.totalStudentsChange },
        { title: '就业率', value: `${data.employmentRate}%`, change: data.employmentRateChange },
        { title: '考研率', value: `${data.graduateStudyRate}%`, change: data.graduateStudyRateChange },
        { title: '创业率', value: `${data.startupRate}%`, change: data.startupRateChange }
      ]
    }
    
    // 获取趋势数据
    fetchTrendData()
    
    // 获取行业分布数据
    fetchIndustryData()
    
    // 获取地区分布数据
    fetchRegionData()
    
    // 获取就业进度数据
    fetchProgressData()
  } catch (error) {
    console.error('获取统计数据失败', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取班级列表
const fetchClassList = async () => {
  try {
    const res = await getClassList()
    if (res.data) {
      classList.value = res.data
      if (classList.value.length > 0) {
        selectedClass.value = classList.value[0].id
      }
    }
  } catch (error) {
    console.error('获取班级列表失败', error)
    ElMessage.error('获取班级列表失败')
  }
}

// 获取学生列表
const fetchStudentList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.currentPage,
      size: pagination.value.pageSize,
      keyword: searchKeyword.value,
      status: employmentStatus.value
    }
    
    const res = await getStudentList(params)
    if (res.data) {
      studentList.value = res.data.records
      pagination.value.total = res.data.total
    }
  } catch (error) {
    console.error('获取学生列表失败', error)
    ElMessage.error('获取学生列表失败')
  } finally {
    loading.value = false
  }
}

// 获取趋势数据
const fetchTrendData = async () => {
  try {
    const params = { timeRange: trendTimeRange.value }
    const res = await getEmploymentTrend(params)
    if (res.data && trendChart) {
      updateTrendChart(res.data)
    }
  } catch (error) {
    console.error('获取趋势数据失败', error)
    ElMessage.error('获取趋势数据失败')
  }
}

// 获取行业分布数据
const fetchIndustryData = async () => {
  try {
    const res = await getIndustryDistribution()
    if (res.data && industryChart) {
      updateIndustryChart(res.data)
    }
  } catch (error) {
    console.error('获取行业分布数据失败', error)
    ElMessage.error('获取行业分布数据失败')
  }
}

// 获取地区分布数据
const fetchRegionData = async () => {
  try {
    const res = await getRegionDistribution()
    if (res.data && regionChart) {
      updateRegionChart(res.data)
    }
  } catch (error) {
    console.error('获取地区分布数据失败', error)
    ElMessage.error('获取地区分布数据失败')
  }
}

// 获取就业进度数据
const fetchProgressData = async () => {
  try {
    const params = { classId: selectedClass.value }
    const res = await getEmploymentProgress(params)
    if (res.data && progressChart) {
      updateProgressChart(res.data)
    }
  } catch (error) {
    console.error('获取就业进度数据失败', error)
    ElMessage.error('获取就业进度数据失败')
  }
}

// 初始化图表
const initCharts = () => {
  // 就业率趋势图表
  trendChart = echarts.init(document.getElementById('trend-chart'))
  
  // 就业进度图表
  progressChart = echarts.init(document.getElementById('progress-chart'))
  
  // 行业分布图表
  industryChart = echarts.init(document.getElementById('industry-chart'))
  
  // 地区分布图表
  regionChart = echarts.init(document.getElementById('region-chart'))
  
  // 设置图表初始配置
  setInitialChartOptions()
}

// 设置图表初始配置
const setInitialChartOptions = () => {
  // 趋势图初始配置
  trendChart.setOption({
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['就业率', '考研率', '创业率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: []
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
        type: 'line',
        data: []
      },
      {
        name: '考研率',
        type: 'line',
        data: []
      },
      {
        name: '创业率',
        type: 'line',
        data: []
      }
    ]
  })
  
  // 就业进度图初始配置
  progressChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 10,
      data: ['已就业', '未就业', '考研', '创业']
    },
    series: [
      {
        name: '就业进度',
        type: 'pie',
        radius: ['50%', '70%'],
        avoidLabelOverlap: false,
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '18',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: []
      }
    ]
  })
  
  // 行业分布图初始配置
  industryChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: []
    },
    series: [
      {
        name: '行业分布',
        type: 'pie',
        radius: ['0', '70%'],
        data: []
      }
    ]
  })
  
  // 地区分布图初始配置
  regionChart.setOption({
    tooltip: {
      trigger: 'item'
    },
    series: [
      {
        name: '地区分布',
        type: 'pie',
        radius: '70%',
        data: [],
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

// 更新趋势图
const updateTrendChart = (data) => {
  trendChart.setOption({
    xAxis: {
      data: data.dates
    },
    series: [
      {
        name: '就业率',
        data: data.employmentRates
      },
      {
        name: '考研率',
        data: data.graduateStudyRates
      },
      {
        name: '创业率',
        data: data.startupRates
      }
    ]
  })
}

// 更新就业进度图
const updateProgressChart = (data) => {
  progressChart.setOption({
    series: [
      {
        data: [
          { value: data.employed, name: '已就业' },
          { value: data.unemployed, name: '未就业' },
          { value: data.graduateStudy, name: '考研' },
          { value: data.startup, name: '创业' }
        ]
      }
    ]
  })
}

// 更新行业分布图
const updateIndustryChart = (data) => {
  const legendData = data.map(item => item.name)
  industryChart.setOption({
    legend: {
      data: legendData
    },
    series: [
      {
        data: data
      }
    ]
  })
}

// 更新地区分布图
const updateRegionChart = (data) => {
  regionChart.setOption({
    series: [
      {
        data: data
      }
    ]
  })
}

// 窗口大小变化时重新调整图表大小
const handleResize = () => {
  trendChart && trendChart.resize()
  progressChart && progressChart.resize()
  industryChart && industryChart.resize()
  regionChart && regionChart.resize()
}

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    employed: '已就业',
    unemployed: '未就业',
    graduate_study: '考研',
    startup: '创业'
  }
  return statusMap[status] || status
}

// 获取状态对应的标签类型
const getStatusType = (status) => {
  const statusTypeMap = {
    employed: 'success',
    unemployed: 'danger',
    graduate_study: 'warning',
    startup: 'info'
  }
  return statusTypeMap[status] || ''
}

// 查看学生详情
const handleViewDetail = async (student) => {
  try {
    const res = await getStudentDetail(student.id)
    if (res.data) {
      currentStudent.value = res.data
      drawerVisible.value = true
    }
  } catch (error) {
    console.error('获取学生详情失败', error)
    ElMessage.error('获取学生详情失败')
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  fetchStudentList()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  employmentStatus.value = ''
  pagination.value.currentPage = 1
  fetchStudentList()
}

// 导出统计数据
const exportStatistics = async () => {
  try {
    const res = await exportEmploymentStatistics()
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `就业统计数据_${new Date().toLocaleDateString()}.xlsx`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (error) {
    console.error('导出统计数据失败', error)
    ElMessage.error('导出统计数据失败')
  }
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  fetchStudentList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  pagination.value.currentPage = val
  fetchStudentList()
}

// 监听趋势图时间范围变化
watch(trendTimeRange, () => {
  fetchTrendData()
})

// 监听班级选择变化
watch(selectedClass, () => {
  fetchProgressData()
})
</script>

<style scoped>
.counselor-statistics-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overview-card {
  margin-bottom: 20px;
}

.overview-item {
  padding: 20px;
  border-radius: 8px;
  background-color: #f5f7fa;
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.item-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.item-change {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.item-change.up {
  color: #67c23a;
}

.item-change.down {
  color: #f56c6c;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 400px;
}

.chart-container {
  height: 320px;
}

.student-list-card {
  margin-bottom: 20px;
}

.filter-container {
  display: flex;
  gap: 10px;
}

.filter-item {
  margin-right: 10px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.student-detail {
  padding: 20px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .overview-item {
    margin-bottom: 15px;
    height: 100px;
  }
  
  .chart-card {
    height: 300px;
    margin-bottom: 15px;
  }
  
  .chart-container {
    height: 220px;
  }
  
  .filter-container {
    flex-direction: column;
    gap: 10px;
  }
  
  .filter-item {
    width: 100%;
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style> 