<template>
  <div class="position-container">
    <!-- 搜索筛选区域 -->
    <el-card class="filter-container" shadow="never">
      <el-form :model="queryParams" :inline="true" ref="queryForm">
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="queryParams.keyword" placeholder="职位名称/企业名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        
        <el-form-item label="职位类型" prop="positionType">
          <el-select v-model="queryParams.positionType" placeholder="全部类型" clearable style="width: 180px">
            <el-option
              v-for="item in positionTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="工作地点" prop="location">
          <el-cascader
            v-model="queryParams.location"
            :options="locationOptions"
            placeholder="全部地区"
            clearable
            style="width: 220px"
          />
        </el-form-item>
        
        <el-form-item label="薪资范围" prop="salaryRange">
          <el-select v-model="queryParams.salaryRange" placeholder="全部薪资" clearable style="width: 150px">
            <el-option
              v-for="item in salaryRanges"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="发布时间" prop="publishTime">
          <el-select v-model="queryParams.publishTime" placeholder="全部时间" clearable style="width: 150px">
            <el-option
              v-for="item in publishTimes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="企业性质" prop="enterpriseType">
          <el-select v-model="queryParams.enterpriseType" placeholder="全部性质" clearable style="width: 150px">
            <el-option
              v-for="item in enterpriseTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 视图切换和排序 -->
    <div class="view-control">
      <div class="view-toggle">
        <el-radio-group v-model="viewType" size="small">
          <el-radio-button label="list">
            <el-icon><List /></el-icon>
            列表
          </el-radio-button>
          <el-radio-button label="card">
            <el-icon><Grid /></el-icon>
            卡片
          </el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="sort-control">
        <span class="sort-label">排序：</span>
        <el-radio-group v-model="sortType" size="small">
          <el-radio-button label="latest">最新发布</el-radio-button>
          <el-radio-button label="hot">热门推荐</el-radio-button>
          <el-radio-button label="salary">薪资最高</el-radio-button>
        </el-radio-group>
      </div>
    </div>
    
    <!-- 职位列表视图 -->
    <div v-if="viewType === 'list'" class="position-list">
      <template v-if="positionList.length === 0">
        <el-empty description="暂无职位信息" />
      </template>
      <el-card v-else v-for="position in positionList" :key="position.id" class="position-item" shadow="hover" @click="viewPositionDetail(position.id)">
        <div class="position-header">
          <div class="position-title-container">
            <h3 class="position-title">{{ position.positionName }}</h3>
            <el-tag v-if="position.urgent" type="danger" size="small">急聘</el-tag>
          </div>
          <div class="position-salary">{{ position.salaryRange }}</div>
        </div>
        
        <div class="position-company">
          <span>{{ position.enterpriseName }}</span>
          <el-divider direction="vertical" />
          <span>{{ position.industry }}</span>
          <el-divider direction="vertical" />
          <span>{{ position.scale }}</span>
          <el-divider direction="vertical" />
          <span>{{ position.type }}</span>
        </div>
        
        <div class="position-tags">
          <el-tag v-for="(tag, index) in position.tags" :key="index" type="info" size="small" class="tag-item">{{ tag }}</el-tag>
        </div>
        
        <div class="position-footer">
          <div class="position-location">
            <el-icon><Location /></el-icon>
            <span>{{ position.location }}</span>
          </div>
          <div class="position-requirements">
            <span v-if="position.education">{{ position.education }}</span>
            <el-divider v-if="position.education && position.experience" direction="vertical" />
            <span v-if="position.experience">{{ position.experience }}</span>
          </div>
          <div class="position-time">{{ position.publishTime }}</div>
        </div>
      </el-card>
    </div>
    
    <!-- 职位卡片视图 -->
    <div v-else class="position-cards">
      <template v-if="positionList.length === 0">
        <el-empty description="暂无职位信息" />
      </template>
      <el-row v-else :gutter="20">
        <el-col v-for="position in positionList" :key="position.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="6" class="card-col">
          <el-card class="position-card" shadow="hover" body-style="padding: 15px" @click="viewPositionDetail(position.id)">
            <div class="card-header">
              <h3 class="card-title">{{ position.positionName }}</h3>
              <div class="card-salary">{{ position.salaryRange }}</div>
            </div>
            
            <div class="card-company">
              <el-avatar :size="40" :src="position.logo || defaultLogo"></el-avatar>
              <div class="company-info">
                <div class="company-name">{{ position.enterpriseName }}</div>
                <div class="company-type">{{ position.type }} | {{ position.scale }}</div>
              </div>
            </div>
            
            <div class="card-tags">
              <el-tag v-for="(tag, index) in position.tags.slice(0, 3)" :key="index" type="info" size="small" class="tag-item">{{ tag }}</el-tag>
            </div>
            
            <div class="card-footer">
              <div class="card-location">
                <el-icon><Location /></el-icon>
                <span>{{ position.location }}</span>
              </div>
              <div class="card-time">{{ position.publishTime }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 分页控件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:currentPage="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { List, Grid, Location } from '@element-plus/icons-vue'

const router = useRouter()
const defaultLogo = '@/assets/default-logo.png'

// 查询条件
const queryParams = reactive({
  keyword: '',
  positionType: '',
  location: [],
  salaryRange: '',
  publishTime: '',
  enterpriseType: '',
  pageNum: 1,
  pageSize: 12
})

// 视图类型和排序
const viewType = ref('list')  // list 或 card
const sortType = ref('latest')  // latest, hot, salary

// 数据量
const total = ref(0)

// 职位列表数据
const positionList = ref([
  {
    id: 1,
    positionName: '前端开发工程师',
    enterpriseName: '科技有限公司',
    logo: '',
    industry: '互联网',
    type: '民营企业',
    scale: '500-1000人',
    salaryRange: '15k-20k',
    location: '北京市 海淀区',
    education: '本科',
    experience: '3-5年',
    tags: ['React', 'Vue', 'JavaScript', '前端'],
    publishTime: '2023-05-01',
    urgent: true
  },
  {
    id: 2,
    positionName: 'Java后端工程师',
    enterpriseName: '互联网科技有限公司',
    logo: '',
    industry: '互联网',
    type: '上市公司',
    scale: '1000-5000人',
    salaryRange: '20k-30k',
    location: '上海市 浦东新区',
    education: '本科',
    experience: '3-5年',
    tags: ['Java', 'Spring Boot', 'MySQL', '微服务'],
    publishTime: '2023-04-29',
    urgent: false
  },
  {
    id: 3,
    positionName: '产品经理',
    enterpriseName: '数字科技有限公司',
    logo: '',
    industry: '互联网',
    type: '外资企业',
    scale: '500-1000人',
    salaryRange: '18k-25k',
    location: '广州市 天河区',
    education: '本科',
    experience: '3-5年',
    tags: ['产品设计', '需求分析', '项目管理', 'B端产品'],
    publishTime: '2023-04-28',
    urgent: false
  },
  {
    id: 4,
    positionName: 'UI设计师',
    enterpriseName: '创新科技有限公司',
    logo: '',
    industry: '互联网',
    type: '民营企业',
    scale: '100-500人',
    salaryRange: '12k-18k',
    location: '深圳市 南山区',
    education: '本科',
    experience: '1-3年',
    tags: ['UI设计', 'Figma', 'Sketch', '交互设计'],
    publishTime: '2023-04-27',
    urgent: false
  },
  {
    id: 5,
    positionName: '算法工程师',
    enterpriseName: '智能科技有限公司',
    logo: '',
    industry: '人工智能',
    type: '上市公司',
    scale: '1000-5000人',
    salaryRange: '25k-35k',
    location: '杭州市 西湖区',
    education: '硕士及以上',
    experience: '3-5年',
    tags: ['机器学习', '深度学习', 'Python', 'TensorFlow'],
    publishTime: '2023-04-26',
    urgent: true
  },
  {
    id: 6,
    positionName: '运维工程师',
    enterpriseName: '云计算科技有限公司',
    logo: '',
    industry: '云计算',
    type: '合资企业',
    scale: '500-1000人',
    salaryRange: '15k-22k',
    location: '北京市 朝阳区',
    education: '本科',
    experience: '3-5年',
    tags: ['Linux', 'Docker', 'Kubernetes', '自动化运维'],
    publishTime: '2023-04-25',
    urgent: false
  }
])

// 下拉选项数据
const positionTypes = [
  { value: 'tech', label: '技术' },
  { value: 'product', label: '产品' },
  { value: 'design', label: '设计' },
  { value: 'operation', label: '运营' },
  { value: 'marketing', label: '市场' },
  { value: 'sales', label: '销售' },
  { value: 'finance', label: '财务' },
  { value: 'hr', label: '人力资源' },
  { value: 'admin', label: '行政' },
  { value: 'others', label: '其他' }
]

const salaryRanges = [
  { value: '0-5k', label: '5K以下' },
  { value: '5k-10k', label: '5K-10K' },
  { value: '10k-15k', label: '10K-15K' },
  { value: '15k-20k', label: '15K-20K' },
  { value: '20k-30k', label: '20K-30K' },
  { value: '30k+', label: '30K以上' }
]

const publishTimes = [
  { value: '1', label: '24小时内' },
  { value: '3', label: '3天内' },
  { value: '7', label: '一周内' },
  { value: '30', label: '一月内' }
]

const enterpriseTypes = [
  { value: 'state', label: '国有企业' },
  { value: 'private', label: '民营企业' },
  { value: 'foreign', label: '外资企业' },
  { value: 'joint', label: '合资企业' },
  { value: 'listed', label: '上市公司' }
]

// 地区级联选择器数据
const locationOptions = [
  {
    value: 'beijing',
    label: '北京市',
    children: [
      { value: 'haidian', label: '海淀区' },
      { value: 'chaoyang', label: '朝阳区' },
      { value: 'dongcheng', label: '东城区' },
      { value: 'xicheng', label: '西城区' },
      { value: 'others', label: '其他区域' }
    ]
  },
  {
    value: 'shanghai',
    label: '上海市',
    children: [
      { value: 'pudong', label: '浦东新区' },
      { value: 'xuhui', label: '徐汇区' },
      { value: 'changning', label: '长宁区' },
      { value: 'jingan', label: '静安区' },
      { value: 'others', label: '其他区域' }
    ]
  },
  {
    value: 'guangzhou',
    label: '广州市',
    children: [
      { value: 'tianhe', label: '天河区' },
      { value: 'yuexiu', label: '越秀区' },
      { value: 'haizhu', label: '海珠区' },
      { value: 'panyu', label: '番禺区' },
      { value: 'others', label: '其他区域' }
    ]
  },
  {
    value: 'shenzhen',
    label: '深圳市',
    children: [
      { value: 'nanshan', label: '南山区' },
      { value: 'futian', label: '福田区' },
      { value: 'luohu', label: '罗湖区' },
      { value: 'longgang', label: '龙岗区' },
      { value: 'others', label: '其他区域' }
    ]
  },
  {
    value: 'hangzhou',
    label: '杭州市',
    children: [
      { value: 'xihu', label: '西湖区' },
      { value: 'shangcheng', label: '上城区' },
      { value: 'xiacheng', label: '下城区' },
      { value: 'jianggan', label: '江干区' },
      { value: 'others', label: '其他区域' }
    ]
  }
]

// 查询事件
function handleQuery() {
  queryParams.pageNum = 1
  // TODO: 实际查询逻辑
  console.log('查询参数:', queryParams)
  // 模拟获取数据
  loadPositionList()
}

// 重置查询条件
function resetQuery() {
  queryParams.keyword = ''
  queryParams.positionType = ''
  queryParams.location = []
  queryParams.salaryRange = ''
  queryParams.publishTime = ''
  queryParams.enterpriseType = ''
  queryParams.pageNum = 1
  // 重置后自动查询
  handleQuery()
}

// 加载职位列表数据
function loadPositionList() {
  // 这里应调用实际的API，暂时使用示例数据
  // 模拟设置数据总量
  total.value = positionList.value.length >= 6 ? 120 : positionList.value.length
}

// 跳转到职位详情页
function viewPositionDetail(id) {
  router.push(`/job/position/${id}`)
}

// 分页控件事件
function handleSizeChange(size) {
  queryParams.pageSize = size
  loadPositionList()
}

function handleCurrentChange(page) {
  queryParams.pageNum = page
  loadPositionList()
}

// 页面加载时执行查询
onMounted(() => {
  handleQuery()
})
</script>

<style lang="scss" scoped>
.position-container {
  padding: 20px;
}

.filter-container {
  margin-bottom: 20px;
}

.view-control {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .sort-control {
    display: flex;
    align-items: center;
    
    .sort-label {
      margin-right: 10px;
      color: #606266;
    }
  }
}

.position-list {
  .position-item {
    margin-bottom: 15px;
    cursor: pointer;
    
    &:hover {
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }
    
    .position-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      .position-title-container {
        display: flex;
        align-items: center;
        
        .position-title {
          margin: 0;
          font-size: 18px;
          font-weight: bold;
          color: #303133;
          margin-right: 10px;
        }
      }
      
      .position-salary {
        color: #f56c6c;
        font-size: 16px;
        font-weight: bold;
      }
    }
    
    .position-company {
      color: #606266;
      font-size: 14px;
      margin-bottom: 15px;
    }
    
    .position-tags {
      margin-bottom: 15px;
      
      .tag-item {
        margin-right: 8px;
        margin-bottom: 5px;
      }
    }
    
    .position-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 13px;
      color: #909399;
      
      .position-location {
        display: flex;
        align-items: center;
        
        .el-icon {
          margin-right: 5px;
        }
      }
      
      .position-requirements {
        flex: 1;
        margin: 0 15px;
        text-align: center;
      }
    }
  }
}

.position-cards {
  .card-col {
    margin-bottom: 20px;
  }
  
  .position-card {
    height: 100%;
    cursor: pointer;
    
    &:hover {
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 15px;
      
      .card-title {
        margin: 0;
        font-size: 16px;
        font-weight: bold;
        color: #303133;
        flex: 1;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 1;
        overflow: hidden;
      }
      
      .card-salary {
        color: #f56c6c;
        font-size: 14px;
        font-weight: bold;
        white-space: nowrap;
        margin-left: 10px;
      }
    }
    
    .card-company {
      display: flex;
      align-items: center;
      margin-bottom: 15px;
      
      .company-info {
        margin-left: 10px;
        
        .company-name {
          font-size: 14px;
          color: #303133;
          margin-bottom: 5px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .company-type {
          font-size: 12px;
          color: #909399;
        }
      }
    }
    
    .card-tags {
      margin-bottom: 15px;
      min-height: 24px;
      
      .tag-item {
        margin-right: 5px;
        margin-bottom: 5px;
      }
    }
    
    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 12px;
      color: #909399;
      
      .card-location {
        display: flex;
        align-items: center;
        
        .el-icon {
          margin-right: 5px;
        }
      }
    }
  }
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style> 