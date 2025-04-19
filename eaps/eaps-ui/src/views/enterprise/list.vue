<template>
  <div class="enterprise-list-container">
    <el-card shadow="never" class="filter-card">
      <div class="filter-header">
        <h2 class="page-title">企业列表</h2>
        <div class="filter-actions">
          <el-input
            v-model="queryParams.keyword"
            placeholder="搜索企业名称/行业"
            clearable
            @keyup.enter="handleFilter"
            style="width: 220px; margin-right: 10px;"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select v-model="queryParams.industry" placeholder="行业" clearable @change="handleFilter" style="width: 150px; margin-right: 10px;">
            <el-option 
              v-for="item in industryOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
          
          <el-select v-model="queryParams.scale" placeholder="规模" clearable @change="handleFilter" style="width: 150px; margin-right: 10px;">
            <el-option 
              v-for="item in scaleOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
          
          <el-select v-model="queryParams.type" placeholder="企业类型" clearable @change="handleFilter" style="width: 150px; margin-right: 10px;">
            <el-option 
              v-for="item in typeOptions" 
              :key="item.value" 
              :label="item.label" 
              :value="item.value" 
            />
          </el-select>
          
          <el-button type="primary" @click="handleFilter">搜索</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </div>
      </div>
    </el-card>

    <div class="view-toggle">
      <el-radio-group v-model="viewType" size="large">
        <el-radio-button label="card">
          <el-icon><Grid /></el-icon>卡片视图
        </el-radio-button>
        <el-radio-button label="list">
          <el-icon><List /></el-icon>列表视图
        </el-radio-button>
      </el-radio-group>
      
      <div class="sort-options">
        <span class="sort-label">排序：</span>
        <el-radio-group v-model="queryParams.sortBy" size="default" @change="handleSortChange">
          <el-radio-button label="time">最新</el-radio-button>
          <el-radio-button label="rating">评分最高</el-radio-button>
          <el-radio-button label="jobs">职位最多</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="enterpriseList.length === 0" class="empty-container">
      <el-empty description="暂无企业数据" />
    </div>
    <div v-else>
      <!-- 卡片视图 -->
      <div v-if="viewType === 'card'" class="card-view">
        <el-row :gutter="20">
          <el-col v-for="item in enterpriseList" :key="item.id" :xs="24" :sm="12" :md="8" :lg="6" :xl="6">
            <el-card class="enterprise-card" shadow="hover" @click="viewEnterpriseDetail(item.id)">
              <div class="enterprise-logo">
                <el-avatar :size="70" :src="item.logo || '/default-logo.png'" fit="cover" />
              </div>
              <h3 class="enterprise-name">{{ item.name }}</h3>
              <div class="enterprise-info">
                <div class="info-item">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>{{ item.industry }}</span>
                </div>
                <div class="info-item">
                  <el-icon><User /></el-icon>
                  <span>{{ item.scale }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Collection /></el-icon>
                  <span>{{ item.type }}</span>
                </div>
                <div class="info-item" v-if="item.location">
                  <el-icon><Location /></el-icon>
                  <span>{{ item.location }}</span>
                </div>
              </div>
              
              <div class="enterprise-rating" v-if="item.avgRating > 0">
                <el-rate
                  v-model="item.avgRating"
                  disabled
                  show-score
                  text-color="#ff9900"
                />
                <span class="rating-count">({{ item.ratingCount }}条评价)</span>
              </div>
              <div class="enterprise-rating" v-else>
                <span class="no-rating">暂无评分</span>
              </div>
              
              <div class="enterprise-positions">
                <span class="position-count">
                  <el-tag size="small" type="success">{{ item.jobCount || 0 }}个职位</el-tag>
                </span>
                <span class="position-time" v-if="item.lastUpdateTime">{{ formatDate(item.lastUpdateTime) }}</span>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 列表视图 -->
      <div v-else class="list-view">
        <el-card v-for="item in enterpriseList" :key="item.id" class="enterprise-list-item" shadow="hover">
          <div class="list-item-content" @click="viewEnterpriseDetail(item.id)">
            <div class="enterprise-logo">
              <el-avatar :size="60" :src="item.logo || '/default-logo.png'" fit="cover" />
            </div>
            
            <div class="enterprise-content">
              <div class="enterprise-header">
                <h3 class="enterprise-name">{{ item.name }}</h3>
                <div class="enterprise-rating" v-if="item.avgRating > 0">
                  <el-rate
                    v-model="item.avgRating"
                    disabled
                    show-score
                    text-color="#ff9900"
                  />
                  <span class="rating-count">({{ item.ratingCount }}条评价)</span>
                </div>
                <div class="enterprise-rating" v-else>
                  <span class="no-rating">暂无评分</span>
                </div>
              </div>
              
              <div class="enterprise-meta">
                <div class="meta-item"><el-icon><OfficeBuilding /></el-icon>{{ item.industry }}</div>
                <div class="meta-item"><el-icon><User /></el-icon>{{ item.scale }}</div>
                <div class="meta-item"><el-icon><Collection /></el-icon>{{ item.type }}</div>
                <div class="meta-item" v-if="item.location"><el-icon><Location /></el-icon>{{ item.location }}</div>
              </div>
              
              <div class="enterprise-description" v-if="item.description">
                {{ item.description.length > 100 ? item.description.substring(0, 100) + '...' : item.description }}
              </div>
              
              <div class="enterprise-footer">
                <el-tag size="small" type="success">{{ item.jobCount || 0 }}个职位</el-tag>
                <span class="last-update" v-if="item.lastUpdateTime">最近更新：{{ formatDate(item.lastUpdateTime) }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[12, 24, 36, 48]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Grid, List, OfficeBuilding, User, Collection, Location } from '@element-plus/icons-vue'
import { getEnterpriseList } from '@/api/enterprise'

const router = useRouter()
const loading = ref(false)
const total = ref(0)
const enterpriseList = ref([])
const viewType = ref('card')

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 12,
  keyword: '',
  industry: '',
  scale: '',
  type: '',
  sortBy: 'time'
})

// 行业选项
const industryOptions = [
  { value: '互联网/IT', label: '互联网/IT' },
  { value: '金融', label: '金融' },
  { value: '教育', label: '教育' },
  { value: '医疗', label: '医疗' },
  { value: '制造业', label: '制造业' },
  { value: '房地产', label: '房地产' },
  { value: '文化/传媒', label: '文化/传媒' },
  { value: '服务业', label: '服务业' },
  { value: '其他', label: '其他' }
]

// 规模选项
const scaleOptions = [
  { value: '少于50人', label: '少于50人' },
  { value: '50-200人', label: '50-200人' },
  { value: '200-500人', label: '200-500人' },
  { value: '500-1000人', label: '500-1000人' },
  { value: '1000-5000人', label: '1000-5000人' },
  { value: '5000人以上', label: '5000人以上' }
]

// 企业类型选项
const typeOptions = [
  { value: '国企', label: '国企' },
  { value: '私企', label: '私企' },
  { value: '外企', label: '外企' },
  { value: '合资', label: '合资' },
  { value: '事业单位', label: '事业单位' },
  { value: '非营利组织', label: '非营利组织' },
  { value: '其他', label: '其他' }
]

// 获取企业列表
const fetchEnterpriseList = async () => {
  loading.value = true
  try {
    const res = await getEnterpriseList(queryParams)
    enterpriseList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取企业列表失败:', error)
    ElMessage.error('获取企业列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 查看企业详情
const viewEnterpriseDetail = (id) => {
  router.push(`/enterprise/detail/${id}`)
}

// 筛选处理
const handleFilter = () => {
  queryParams.pageNum = 1
  fetchEnterpriseList()
}

// 重置筛选
const resetFilter = () => {
  queryParams.keyword = ''
  queryParams.industry = ''
  queryParams.scale = ''
  queryParams.type = ''
  queryParams.sortBy = 'time'
  queryParams.pageNum = 1
  fetchEnterpriseList()
}

// 排序变更
const handleSortChange = () => {
  queryParams.pageNum = 1
  fetchEnterpriseList()
}

// 分页处理
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  fetchEnterpriseList()
}

const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  fetchEnterpriseList()
}

onMounted(() => {
  fetchEnterpriseList()
})
</script>

<style lang="scss" scoped>
.enterprise-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 20px;
  
  .filter-header {
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
    align-items: center;
    
    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: bold;
    }
    
    .filter-actions {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 10px;
      margin-top: 10px;
      
      @media (max-width: 768px) {
        flex-direction: column;
        align-items: stretch;
        width: 100%;
        
        .el-input, .el-select {
          width: 100% !important;
          margin-right: 0 !important;
          margin-bottom: 10px;
        }
        
        .el-button {
          width: 100%;
          margin-left: 0 !important;
        }
      }
    }
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
    }
  }
}

.view-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .sort-options {
    display: flex;
    align-items: center;
    
    .sort-label {
      margin-right: 10px;
      color: #606266;
    }
  }
  
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: flex-start;
    
    .sort-options {
      margin-top: 15px;
    }
  }
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px 0;
  text-align: center;
}

.card-view {
  .enterprise-card {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px;
    margin-bottom: 20px;
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
    
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .enterprise-logo {
      margin-bottom: 15px;
    }
    
    .enterprise-name {
      font-size: 18px;
      font-weight: bold;
      margin: 0 0 15px 0;
      text-align: center;
      color: #303133;
    }
    
    .enterprise-info {
      width: 100%;
      margin-bottom: 15px;
      
      .info-item {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        color: #606266;
        
        .el-icon {
          margin-right: 5px;
        }
      }
    }
    
    .enterprise-rating {
      width: 100%;
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 15px;
      
      .rating-count {
        font-size: 12px;
        color: #909399;
        margin-top: 5px;
      }
      
      .no-rating {
        color: #909399;
        font-size: 14px;
      }
    }
    
    .enterprise-positions {
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .position-time {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.list-view {
  .enterprise-list-item {
    margin-bottom: 15px;
    cursor: pointer;
    transition: transform 0.3s, box-shadow 0.3s;
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }
    
    .list-item-content {
      display: flex;
      
      .enterprise-logo {
        margin-right: 20px;
      }
      
      .enterprise-content {
        flex: 1;
        
        .enterprise-header {
          display: flex;
          align-items: center;
          margin-bottom: 10px;
          
          .enterprise-name {
            font-size: 18px;
            font-weight: bold;
            margin: 0 15px 0 0;
            color: #303133;
          }
          
          .enterprise-rating {
            display: flex;
            align-items: center;
            
            .rating-count {
              font-size: 12px;
              color: #909399;
              margin-left: 5px;
            }
            
            .no-rating {
              color: #909399;
              font-size: 14px;
            }
          }
          
          @media (max-width: 768px) {
            flex-direction: column;
            align-items: flex-start;
            
            .enterprise-name {
              margin-bottom: 10px;
            }
          }
        }
        
        .enterprise-meta {
          display: flex;
          flex-wrap: wrap;
          margin-bottom: 10px;
          
          .meta-item {
            display: flex;
            align-items: center;
            margin-right: 15px;
            margin-bottom: 5px;
            color: #606266;
            
            .el-icon {
              margin-right: 5px;
            }
          }
        }
        
        .enterprise-description {
          margin-bottom: 10px;
          color: #606266;
          line-height: 1.5;
        }
        
        .enterprise-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .last-update {
            font-size: 12px;
            color: #909399;
          }
        }
      }
      
      @media (max-width: 576px) {
        flex-direction: column;
        
        .enterprise-logo {
          margin-right: 0;
          margin-bottom: 15px;
          align-self: center;
        }
      }
    }
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 