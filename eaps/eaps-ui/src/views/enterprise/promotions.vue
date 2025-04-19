<template>
  <div class="enterprise-promotions-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="!enterpriseData" class="not-found">
      <el-empty description="企业不存在或已被删除" />
      <el-button type="primary" @click="router.push('/enterprise/list')">返回企业列表</el-button>
    </div>
    <div v-else>
      <!-- 返回按钮 -->
      <div class="back-btn">
        <el-button @click="router.push(`/enterprise/detail/${enterpriseId}`)" plain>
          <el-icon><ArrowLeft /></el-icon>返回企业详情
        </el-button>
      </div>

      <!-- 企业基本信息 -->
      <el-card class="info-card">
        <div class="enterprise-header">
          <div class="enterprise-logo">
            <el-avatar :size="60" :src="enterpriseData.logo || '/default-logo.png'" />
          </div>
          <div class="enterprise-info">
            <h1 class="enterprise-name">{{ enterpriseData.name }}</h1>
            <div class="enterprise-meta">
              <span>{{ enterpriseData.industry }}</span>
              <el-divider direction="vertical" />
              <span>{{ enterpriseData.scale }}</span>
              <el-divider direction="vertical" />
              <span>{{ enterpriseData.type }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 筛选器 -->
      <el-card class="filter-card">
        <div class="filter-container">
          <div class="filter-title">企业宣传</div>
          <div class="filter-actions">
            <el-input
              v-model="queryParams.keyword"
              placeholder="搜索关键词"
              clearable
              @keyup.enter="handleFilter"
              style="width: 220px;"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="handleFilter" style="margin-left: 10px;">搜索</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </div>
        </div>
      </el-card>

      <!-- 宣传内容列表 -->
      <div v-if="promotionList.length === 0" class="empty-container">
        <el-empty description="暂无企业宣传内容" />
      </div>
      <div v-else class="promotion-list">
        <el-card v-for="item in promotionList" :key="item.id" class="promotion-item" shadow="hover">
          <div class="promotion-header">
            <h3 class="promotion-title" @click="viewPromotionDetail(item.id)">{{ item.title }}</h3>
            <div class="promotion-meta">
              <span class="publish-time">{{ formatDate(item.publishTime) }}</span>
              <span class="view-count"><el-icon><View /></el-icon>{{ item.viewCount }}</span>
            </div>
          </div>
          
          <div class="promotion-content" v-if="item.content">
            <div v-html="formatContent(item.content)" class="content-html"></div>
          </div>
          
          <div class="promotion-footer">
            <el-button type="primary" link @click="viewPromotionDetail(item.id)">查看详情</el-button>
            <el-button type="success" link @click="router.push(`/job/positions?enterpriseId=${enterpriseId}`)">查看职位</el-button>
          </div>
        </el-card>
      </div>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 30, 50]"
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Search, View } from '@element-plus/icons-vue'
import { getEnterpriseDetail, getEnterprisePromotions } from '@/api/enterprise'

const router = useRouter()
const route = useRoute()
const enterpriseId = ref(route.params.id)
const loading = ref(true)
const enterpriseData = ref(null)
const promotionList = ref([])
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  sortBy: 'time'
})

// 获取企业详情
const fetchEnterpriseDetail = async () => {
  try {
    const res = await getEnterpriseDetail(enterpriseId.value)
    enterpriseData.value = res
  } catch (error) {
    console.error('获取企业详情失败:', error)
    ElMessage.error('获取企业详情失败')
  }
}

// 获取企业宣传内容
const fetchPromotions = async () => {
  try {
    const res = await getEnterprisePromotions(enterpriseId.value, queryParams)
    promotionList.value = res.list || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取企业宣传内容失败:', error)
    ElMessage.error('获取企业宣传内容失败')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化内容（替换换行符为<br>，最多显示500个字符）
const formatContent = (content) => {
  if (!content) return ''
  const truncated = content.length > 500 ? content.substring(0, 500) + '...' : content
  return truncated.replace(/\n/g, '<br>')
}

// 查看宣传详情
const viewPromotionDetail = (id) => {
  router.push(`/enterprise/promotion/${id}`)
}

// 筛选处理
const handleFilter = () => {
  queryParams.pageNum = 1
  fetchPromotions()
}

// 重置筛选
const resetFilter = () => {
  queryParams.keyword = ''
  queryParams.pageNum = 1
  fetchPromotions()
}

// 分页处理
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  fetchPromotions()
}

const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  fetchPromotions()
}

// 页面加载
onMounted(async () => {
  if (!enterpriseId.value) {
    ElMessage.error('企业ID不能为空')
    router.push('/enterprise/list')
    return
  }
  
  loading.value = true
  try {
    await fetchEnterpriseDetail()
    if (enterpriseData.value) {
      await fetchPromotions()
    }
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.enterprise-promotions-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  padding: 20px;
}

.not-found {
  text-align: center;
  padding: 40px 0;
}

.back-btn {
  margin-bottom: 20px;
}

.info-card {
  margin-bottom: 20px;
  
  .enterprise-header {
    display: flex;
    align-items: center;
    
    .enterprise-logo {
      margin-right: 20px;
    }
    
    .enterprise-info {
      .enterprise-name {
        margin: 0 0 10px 0;
        font-size: 24px;
        font-weight: bold;
      }
      
      .enterprise-meta {
        color: #606266;
      }
    }
  }
}

.filter-card {
  margin-bottom: 20px;
  
  .filter-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .filter-title {
      font-size: 18px;
      font-weight: bold;
    }
    
    .filter-actions {
      display: flex;
      align-items: center;
    }
    
    @media (max-width: 768px) {
      flex-direction: column;
      align-items: flex-start;
      
      .filter-title {
        margin-bottom: 15px;
      }
      
      .filter-actions {
        width: 100%;
        
        .el-input {
          width: 100% !important;
          margin-bottom: 10px;
        }
        
        .el-button {
          margin-left: 0 !important;
          margin-right: 10px;
        }
      }
    }
  }
}

.empty-container {
  padding: 40px 0;
  text-align: center;
}

.promotion-list {
  margin-bottom: 20px;
  
  .promotion-item {
    margin-bottom: 20px;
    
    .promotion-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 15px;
      
      .promotion-title {
        margin: 0;
        font-size: 18px;
        color: #409EFF;
        cursor: pointer;
        
        &:hover {
          text-decoration: underline;
        }
      }
      
      .promotion-meta {
        display: flex;
        align-items: center;
        color: #909399;
        font-size: 14px;
        
        .publish-time {
          margin-right: 15px;
        }
        
        .view-count {
          display: flex;
          align-items: center;
          
          .el-icon {
            margin-right: 5px;
          }
        }
      }
      
      @media (max-width: 576px) {
        flex-direction: column;
        
        .promotion-meta {
          margin-top: 10px;
        }
      }
    }
    
    .promotion-content {
      margin-bottom: 20px;
      color: #606266;
      line-height: 1.6;
      
      .content-html {
        overflow-wrap: break-word;
      }
    }
    
    .promotion-footer {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 