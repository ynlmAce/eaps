<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :xs="24" :sm="12" :md="6" v-for="item in statisticsData" :key="item.title">
        <el-card class="statistic-card" shadow="hover">
          <div class="statistic-icon" :style="{ backgroundColor: item.color }">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="statistic-content">
            <div class="statistic-value">{{ item.value }}</div>
            <div class="statistic-title">{{ item.title }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 左侧区域 -->
      <el-col :span="16">
        <!-- 最新职位 -->
        <el-card class="box-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>最新职位</span>
              <el-button text @click="goToJobList">查看更多</el-button>
            </div>
          </template>
          <div v-if="latestJobs.length === 0" class="empty-data">
            暂无数据
          </div>
          <div v-else class="job-list">
            <div v-for="job in latestJobs" :key="job.id" class="job-item" @click="goToJobDetail(job.id)">
              <div class="job-main-info">
                <div class="job-title">{{ job.positionName }}</div>
                <div class="job-salary">{{ job.salaryRange }}</div>
              </div>
              <div class="job-sub-info">
                <div class="job-company">{{ job.enterpriseName }}</div>
                <div class="job-location">{{ job.location }}</div>
                <div class="job-time">{{ job.publishTime }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 行业分布 -->
        <el-card class="box-card" shadow="hover" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>行业分布</span>
            </div>
          </template>
          <div id="industryChart" class="chart-container"></div>
        </el-card>
      </el-col>

      <!-- 右侧区域 -->
      <el-col :span="8">
        <!-- 热门企业 -->
        <el-card class="box-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>热门企业</span>
              <el-button text @click="goToEnterpriseList">查看更多</el-button>
            </div>
          </template>
          <div v-if="hotEnterprises.length === 0" class="empty-data">
            暂无数据
          </div>
          <div v-else class="enterprise-list">
            <div v-for="enterprise in hotEnterprises" :key="enterprise.id" 
                 class="enterprise-item" @click="goToEnterpriseDetail(enterprise.id)">
              <el-avatar :src="enterprise.logo || defaultLogo" :size="40"></el-avatar>
              <div class="enterprise-info">
                <div class="enterprise-name">{{ enterprise.enterpriseName }}</div>
                <div class="enterprise-industry">{{ enterprise.industry }}</div>
              </div>
              <div class="enterprise-rating">
                <el-rate v-model="enterprise.rating" disabled text-color="#ff9900" />
              </div>
            </div>
          </div>
        </el-card>

        <!-- 通知公告 -->
        <el-card class="box-card notice-card" shadow="hover" style="margin-top: 20px">
          <template #header>
            <div class="card-header">
              <span>通知公告</span>
            </div>
          </template>
          <div v-if="notices.length === 0" class="empty-data">
            暂无公告
          </div>
          <el-timeline v-else>
            <el-timeline-item
              v-for="notice in notices"
              :key="notice.id"
              :timestamp="notice.time"
              placement="top"
              :type="notice.type"
              :color="notice.color"
            >
              <div class="notice-content">{{ notice.content }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { Briefcase, OfficeBuilding, Star, User } from '@element-plus/icons-vue'

// 注册必需的组件
echarts.use([TitleComponent, TooltipComponent, LegendComponent, PieChart, CanvasRenderer])

const router = useRouter()
const defaultLogo = '@/assets/default-logo.png'

// 统计数据
const statisticsData = ref([
  { title: '职位总数', value: 1286, icon: Briefcase, color: '#409EFF' },
  { title: '企业总数', value: 436, icon: OfficeBuilding, color: '#67C23A' },
  { title: '申请总数', value: 3658, icon: User, color: '#E6A23C' },
  { title: '评分总数', value: 2189, icon: Star, color: '#F56C6C' }
])

// 最新职位
const latestJobs = ref([
  {
    id: 1,
    positionName: '前端开发工程师',
    enterpriseName: '科技有限公司',
    location: '北京',
    salaryRange: '15k-20k',
    publishTime: '2023-05-01'
  },
  {
    id: 2,
    positionName: 'Java后端工程师',
    enterpriseName: '互联网科技有限公司',
    location: '上海',
    salaryRange: '20k-30k',
    publishTime: '2023-04-29'
  },
  {
    id: 3,
    positionName: '产品经理',
    enterpriseName: '数字科技有限公司',
    location: '广州',
    salaryRange: '18k-25k',
    publishTime: '2023-04-28'
  },
  {
    id: 4,
    positionName: 'UI设计师',
    enterpriseName: '创新科技有限公司',
    location: '深圳',
    salaryRange: '12k-18k',
    publishTime: '2023-04-27'
  },
  {
    id: 5,
    positionName: '算法工程师',
    enterpriseName: '智能科技有限公司',
    location: '杭州',
    salaryRange: '25k-35k',
    publishTime: '2023-04-26'
  }
])

// 热门企业
const hotEnterprises = ref([
  {
    id: 1,
    enterpriseName: '阿里巴巴',
    industry: '互联网',
    logo: '',
    rating: 4.8
  },
  {
    id: 2,
    enterpriseName: '腾讯',
    industry: '互联网',
    logo: '',
    rating: 4.7
  },
  {
    id: 3,
    enterpriseName: '百度',
    industry: '互联网',
    logo: '',
    rating: 4.5
  },
  {
    id: 4,
    enterpriseName: '华为',
    industry: '通信',
    logo: '',
    rating: 4.9
  },
  {
    id: 5,
    enterpriseName: '小米',
    industry: '电子产品',
    logo: '',
    rating: 4.6
  }
])

// 通知公告
const notices = ref([
  {
    id: 1,
    content: '2023年春季校园招聘会将于5月15日在学校体育馆举行',
    time: '2023-04-30',
    type: 'primary',
    color: '#409EFF'
  },
  {
    id: 2,
    content: '关于举办就业指导讲座的通知',
    time: '2023-04-25',
    type: 'success',
    color: '#67C23A'
  },
  {
    id: 3,
    content: '毕业生就业信息登记系统已开放',
    time: '2023-04-20',
    type: 'warning',
    color: '#E6A23C'
  }
])

// 行业分布数据
const industryData = [
  { value: 35, name: '互联网' },
  { value: 20, name: '金融' },
  { value: 15, name: '教育' },
  { value: 10, name: '医疗' },
  { value: 20, name: '其他' }
]

// 页面跳转
function goToJobList() {
  router.push('/job/position')
}

function goToJobDetail(id) {
  router.push(`/job/position/${id}`)
}

function goToEnterpriseList() {
  router.push('/enterprise/list')
}

function goToEnterpriseDetail(id) {
  router.push(`/enterprise/detail/${id}`)
}

// 初始化图表
function initIndustryChart() {
  const chartDom = document.getElementById('industryChart')
  if (!chartDom) return
  
  const myChart = echarts.init(chartDom)
  const option = {
    title: {
      text: '职位行业分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: industryData.map(item => item.name)
    },
    series: [
      {
        name: '行业分布',
        type: 'pie',
        radius: '60%',
        center: ['50%', '50%'],
        data: industryData,
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
  
  option && myChart.setOption(option)
  
  // 窗口大小变化时重绘
  window.addEventListener('resize', function() {
    myChart.resize()
  })
}

onMounted(() => {
  initIndustryChart()
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.statistic-card {
  display: flex;
  height: 100px;
  
  .statistic-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 80px;
    height: 80px;
    border-radius: 8px;
    margin-right: 15px;
    
    .el-icon {
      font-size: 30px;
      color: #fff;
    }
  }
  
  .statistic-content {
    display: flex;
    flex-direction: column;
    justify-content: center;
    
    .statistic-value {
      font-size: 24px;
      font-weight: bold;
      margin-bottom: 5px;
    }
    
    .statistic-title {
      font-size: 14px;
      color: #909399;
    }
  }
}

.box-card {
  margin-bottom: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}

.job-list {
  .job-item {
    padding: 15px 0;
    border-bottom: 1px solid #EBEEF5;
    cursor: pointer;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      background-color: #F5F7FA;
    }
    
    .job-main-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      
      .job-title {
        font-size: 16px;
        font-weight: bold;
        color: #303133;
      }
      
      .job-salary {
        color: #F56C6C;
        font-weight: bold;
      }
    }
    
    .job-sub-info {
      display: flex;
      color: #909399;
      font-size: 14px;
      
      .job-company, .job-location {
        margin-right: 15px;
      }
    }
  }
}

.enterprise-list {
  .enterprise-item {
    display: flex;
    align-items: center;
    padding: 10px 0;
    border-bottom: 1px solid #EBEEF5;
    cursor: pointer;
    
    &:last-child {
      border-bottom: none;
    }
    
    &:hover {
      background-color: #F5F7FA;
    }
    
    .enterprise-info {
      flex: 1;
      margin-left: 10px;
      overflow: hidden;
      
      .enterprise-name {
        font-size: 14px;
        color: #303133;
        margin-bottom: 4px;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
      
      .enterprise-industry {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .enterprise-rating {
      display: flex;
      align-items: center;
    }
  }
}

.notice-card {
  .notice-content {
    padding: 5px 0;
    font-size: 14px;
  }
}

.chart-container {
  width: 100%;
  height: 300px;
}

.empty-data {
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 14px;
}
</style> 