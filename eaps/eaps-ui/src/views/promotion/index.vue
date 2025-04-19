<template>
  <div class="promotion-page-container">
    <!-- 推荐宣传区域 -->
    <div class="top-promotions-section">
      <el-carousel 
        :height="isMobile ? '200px' : '400px'" 
        indicator-position="outside"
        arrow="always"
        :interval="5000"
      >
        <el-carousel-item v-for="item in topPromotions" :key="item.id" @click="viewDetail(item.id)">
          <div class="carousel-content" :style="{ backgroundImage: `url(${item.coverImage})` }">
            <div class="carousel-overlay">
              <div class="carousel-caption">
                <h3>{{ item.title }}</h3>
                <p>{{ item.summary }}</p>
                <div class="enterprise-info">
                  <el-avatar :size="24" :src="item.enterprise.logo">
                    {{ item.enterprise.name.substring(0, 1) }}
                  </el-avatar>
                  <span>{{ item.enterprise.name }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    
    <!-- 过滤区域 -->
    <div class="filter-section">
      <div class="search-box">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索宣传标题或企业名称"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
      
      <div class="filter-options">
        <div class="filter-item">
          <span class="label">行业：</span>
          <el-select
            v-model="searchForm.industry"
            placeholder="全部行业"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="item in industryOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        
        <div class="filter-item">
          <span class="label">排序：</span>
          <el-select
            v-model="searchForm.orderBy"
            placeholder="默认排序"
            @change="handleSearch"
          >
            <el-option
              v-for="item in sortOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
      </div>
    </div>
    
    <!-- 宣传列表 -->
    <div class="promotions-list-section">
      <div class="section-title">企业宣传</div>
      
      <div v-if="loading" class="loading-container">
        <div v-for="i in 8" :key="i" class="loading-item">
          <el-skeleton :rows="4" animated />
        </div>
      </div>
      
      <div v-else-if="promotionList.length === 0" class="empty-container">
        <el-empty description="暂无相关宣传内容" />
      </div>
      
      <div v-else class="promotion-grid">
        <el-row :gutter="20">
          <el-col 
            v-for="item in promotionList" 
            :key="item.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6"
          >
            <div 
              class="promotion-card" 
              :class="{ 'is-featured': item.isRecommended }"
              @click="viewDetail(item.id)"
            >
              <div class="feature-flag" v-if="item.isRecommended">推荐</div>
              <div class="card-cover" v-if="item.coverImage">
                <el-image :src="item.coverImage" fit="cover" />
              </div>
              <div class="card-content">
                <div class="card-title">{{ item.title }}</div>
                <div class="card-desc">{{ item.summary }}</div>
                <div class="card-meta">
                  <div class="enterprise-info" @click.stop="viewEnterpriseDetail(item.enterprise.id)">
                    <el-avatar :size="24" :src="item.enterprise.logo">
                      {{ item.enterprise.name.substr(0, 1) }}
                    </el-avatar>
                    <span class="enterprise-name">{{ item.enterprise.name }}</span>
                  </div>
                  <div class="meta-data">
                    <span class="time">{{ formatTimeAgo(item.createTime) }}</span>
                    <span class="views">{{ item.viewCount || 0 }} 浏览</span>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36, 48]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { 
  getPromotionList, 
  getRecommendedPromotions 
} from '@/api/promotion'
import { formatTimeAgo } from '@/utils/format'

// 获取路由器和响应式状态
const router = useRouter()
const loading = ref(false)
const promotionList = ref([])
const topPromotions = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const isMobile = ref(window.innerWidth <= 768)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  industry: '',
  orderBy: 'createTime_desc'
})

// 行业选项
const industryOptions = [
  { label: '互联网/IT', value: 'IT' },
  { label: '金融/银行', value: 'FINANCE' },
  { label: '教育/培训', value: 'EDUCATION' },
  { label: '医疗/健康', value: 'HEALTHCARE' },
  { label: '制造业', value: 'MANUFACTURING' },
  { label: '房地产', value: 'REAL_ESTATE' },
  { label: '文化/传媒', value: 'MEDIA' },
  { label: '其他行业', value: 'OTHER' }
]

// 排序选项
const sortOptions = [
  { label: '最新发布', value: 'createTime_desc' },
  { label: '最早发布', value: 'createTime_asc' },
  { label: '最多浏览', value: 'viewCount_desc' }
]

// 监听窗口大小变化
const resizeHandler = () => {
  isMobile.value = window.innerWidth <= 768
}

// 获取宣传列表
const fetchPromotionList = async () => {
  loading.value = true
  
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: 'APPROVED', // 只获取已审核通过的宣传
      ...searchForm
    }
    
    const res = await getPromotionList(params)
    
    if (res && res.data) {
      promotionList.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取宣传列表失败:', error)
    ElMessage.error('获取宣传列表失败')
  } finally {
    loading.value = false
  }
}

// 获取推荐宣传
const fetchTopPromotions = async () => {
  try {
    const params = {
      limit: 5 // 限制获取5条推荐宣传
    }
    
    const res = await getRecommendedPromotions(params)
    
    if (res && res.data) {
      topPromotions.value = res.data || []
    }
  } catch (error) {
    console.error('获取推荐宣传失败:', error)
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchPromotionList()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchPromotionList()
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchPromotionList()
}

// 查看宣传详情
const viewDetail = (id) => {
  router.push(`/promotion/${id}`)
}

// 查看企业详情
const viewEnterpriseDetail = (id) => {
  router.push(`/enterprise/${id}`)
}

// 生命周期钩子
onMounted(() => {
  window.addEventListener('resize', resizeHandler)
  fetchPromotionList()
  fetchTopPromotions()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler)
})
</script>

<style lang="scss" scoped>
.promotion-page-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  
  .top-promotions-section {
    margin-bottom: 30px;
    
    :deep(.el-carousel__item) {
      cursor: pointer;
    }
    
    .carousel-content {
      width: 100%;
      height: 100%;
      background-size: cover;
      background-position: center;
      position: relative;
      
      .carousel-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
        padding: 40px 20px 20px;
        
        .carousel-caption {
          color: #fff;
          
          h3 {
            font-size: 24px;
            margin-bottom: 10px;
            font-weight: 500;
            
            @media (max-width: 768px) {
              font-size: 18px;
              margin-bottom: 5px;
            }
          }
          
          p {
            font-size: 16px;
            margin-bottom: 10px;
            opacity: 0.9;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            
            @media (max-width: 768px) {
              font-size: 14px;
              -webkit-line-clamp: 1;
            }
          }
          
          .enterprise-info {
            display: flex;
            align-items: center;
            
            .el-avatar {
              margin-right: 8px;
            }
            
            span {
              font-size: 14px;
              opacity: 0.8;
            }
          }
        }
      }
    }
  }
  
  .filter-section {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .search-box {
      margin-bottom: 20px;
    }
    
    .filter-options {
      display: flex;
      flex-wrap: wrap;
      gap: 20px;
      
      .filter-item {
        display: flex;
        align-items: center;
        
        .label {
          margin-right: 10px;
          color: #606266;
        }
      }
    }
  }
  
  .promotions-list-section {
    .section-title {
      font-size: 22px;
      font-weight: 500;
      margin-bottom: 20px;
      color: #303133;
      position: relative;
      padding-left: 15px;
      
      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 0;
        height: 100%;
        width: 4px;
        background-color: #409eff;
        border-radius: 2px;
      }
    }
    
    .loading-container {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
      gap: 20px;
      
      .loading-item {
        background-color: #fff;
        border-radius: 8px;
        padding: 20px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      }
    }
    
    .empty-container {
      background-color: #fff;
      border-radius: 8px;
      padding: 40px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      display: flex;
      justify-content: center;
    }
    
    .promotion-grid {
      .promotion-card {
        background-color: #fff;
        border-radius: 8px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow: hidden;
        margin-bottom: 20px;
        transition: transform 0.3s, box-shadow 0.3s;
        position: relative;
        cursor: pointer;
        
        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        }
        
        &.is-featured {
          border: 1px solid #f56c6c;
        }
        
        .feature-flag {
          position: absolute;
          top: 10px;
          right: 10px;
          background-color: #f56c6c;
          color: #fff;
          padding: 2px 8px;
          font-size: 12px;
          border-radius: 4px;
          z-index: 2;
        }
        
        .card-cover {
          height: 160px;
          overflow: hidden;
          
          .el-image {
            width: 100%;
            height: 100%;
            transition: transform 0.6s;
            
            &:hover {
              transform: scale(1.05);
            }
          }
        }
        
        .card-content {
          padding: 15px;
          
          .card-title {
            font-size: 18px;
            font-weight: 500;
            color: #303133;
            margin-bottom: 8px;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            line-height: 1.5;
            height: 54px;
          }
          
          .card-desc {
            font-size: 14px;
            color: #606266;
            margin-bottom: 15px;
            display: -webkit-box;
            -webkit-line-clamp: 3;
            -webkit-box-orient: vertical;
            overflow: hidden;
            line-height: 1.5;
            height: 63px;
          }
          
          .card-meta {
            display: flex;
            flex-direction: column;
            gap: 10px;
            
            .enterprise-info {
              display: flex;
              align-items: center;
              
              .el-avatar {
                margin-right: 8px;
              }
              
              .enterprise-name {
                font-size: 14px;
                color: #409eff;
                
                &:hover {
                  text-decoration: underline;
                }
              }
            }
            
            .meta-data {
              display: flex;
              justify-content: space-between;
              color: #909399;
              font-size: 13px;
            }
          }
        }
      }
    }
    
    .pagination-container {
      margin-top: 30px;
      display: flex;
      justify-content: center;
    }
  }
}
</style> 