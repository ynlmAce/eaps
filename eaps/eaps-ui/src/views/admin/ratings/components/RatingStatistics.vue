<template>
  <div class="rating-statistics-container">
    <el-card class="statistics-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>评分统计</span>
          <el-button type="primary" size="small" @click="refreshStats">刷新数据</el-button>
        </div>
      </template>
      
      <div class="statistics-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-title">总评分数</div>
              <div class="stat-value">{{ statistics.totalRatings || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-title">平均评分</div>
              <div class="stat-value">{{ (statistics.averageRating || 0).toFixed(1) }}
                <el-rate
                  v-model="statistics.averageRating"
                  disabled
                  text-color="#ff9900"
                  score-template="{value}"
                  size="small"
                  :show-score="false"
                />
              </div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-title">待审核评分</div>
              <div class="stat-value">{{ statistics.pendingRatings || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-title">未处理申诉</div>
              <div class="stat-value">{{ statistics.pendingAppeals || 0 }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="chart-container">
        <div class="chart-wrapper">
          <h3>评分分布</h3>
          <div ref="ratingDistributionChart" class="chart"></div>
        </div>
        <div class="chart-wrapper">
          <h3>近期评分趋势</h3>
          <div ref="ratingTrendChart" class="chart"></div>
        </div>
      </div>

      <div class="top-enterprises">
        <h3>评分排名前10企业</h3>
        <el-table :data="statistics.topEnterprises || []" stripe style="width: 100%">
          <el-table-column label="排名" type="index" width="80" />
          <el-table-column prop="name" label="企业名称" min-width="180" />
          <el-table-column prop="averageRating" label="平均评分" width="120">
            <template #default="{ row }">
              <div class="rating-display">
                <span>{{ (row.averageRating || 0).toFixed(1) }}</span>
                <el-rate
                  v-model="row.averageRating"
                  disabled
                  text-color="#ff9900"
                  score-template="{value}"
                  size="small"
                  :show-score="false"
                />
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="totalRatings" label="评分数量" width="120" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                @click="viewEnterpriseDetail(row.id)"
              >查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { getRatingStatistics } from '@/api/rating'
import * as echarts from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册 ECharts 组件
echarts.use([
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  CanvasRenderer
])

const router = useRouter()
const loading = ref(false)
const statistics = ref({
  totalRatings: 0,
  averageRating: 0,
  pendingRatings: 0,
  pendingAppeals: 0,
  ratingDistribution: [],
  ratingTrend: [],
  topEnterprises: []
})

const ratingDistributionChart = ref(null)
const ratingTrendChart = ref(null)
let distributionChartInstance = null
let trendChartInstance = null

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  try {
    const res = await getRatingStatistics()
    statistics.value = res.data
    nextTick(() => {
      initCharts()
    })
  } catch (error) {
    console.error('获取评分统计数据失败', error)
    ElMessage.error('获取评分统计数据失败')
  } finally {
    loading.value = false
  }
}

// 刷新统计数据
const refreshStats = () => {
  fetchStatistics()
}

// 初始化图表
const initCharts = () => {
  initDistributionChart()
  initTrendChart()
}

// 初始化评分分布图表
const initDistributionChart = () => {
  if (distributionChartInstance) {
    distributionChartInstance.dispose()
  }
  
  distributionChartInstance = echarts.init(ratingDistributionChart.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: ['1星', '2星', '3星', '4星', '5星']
    },
    series: [
      {
        name: '评分分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: statistics.value.ratingDistribution?.[0] || 0, name: '1星' },
          { value: statistics.value.ratingDistribution?.[1] || 0, name: '2星' },
          { value: statistics.value.ratingDistribution?.[2] || 0, name: '3星' },
          { value: statistics.value.ratingDistribution?.[3] || 0, name: '4星' },
          { value: statistics.value.ratingDistribution?.[4] || 0, name: '5星' }
        ]
      }
    ]
  }
  
  distributionChartInstance.setOption(option)
  window.addEventListener('resize', () => {
    distributionChartInstance.resize()
  })
}

// 初始化评分趋势图表
const initTrendChart = () => {
  if (trendChartInstance) {
    trendChartInstance.dispose()
  }
  
  trendChartInstance = echarts.init(ratingTrendChart.value)
  
  const dates = statistics.value.ratingTrend?.map(item => item.date) || []
  const values = statistics.value.ratingTrend?.map(item => item.count) || []
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '评分数量',
        type: 'line',
        data: values,
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          width: 3
        },
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }
  
  trendChartInstance.setOption(option)
  window.addEventListener('resize', () => {
    trendChartInstance.resize()
  })
}

// 查看企业详情
const viewEnterpriseDetail = (enterpriseId) => {
  router.push(`/enterprise/detail/${enterpriseId}`)
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.rating-statistics-container {
  padding: 20px;
}

.statistics-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.statistics-overview {
  margin-bottom: 30px;
}

.stat-item {
  background-color: #f5f7fa;
  border-radius: 6px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 100%;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chart-container {
  display: flex;
  margin-bottom: 30px;
}

.chart-wrapper {
  flex: 1;
  height: 300px;
}

.chart-wrapper h3 {
  text-align: center;
  margin-bottom: 15px;
  font-weight: normal;
  color: #606266;
}

.chart {
  height: 250px;
}

.top-enterprises h3 {
  margin-bottom: 15px;
  font-weight: normal;
  color: #606266;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 5px;
}
</style> 