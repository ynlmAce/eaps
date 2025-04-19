<template>
  <div class="promotion-list-container">
    <div class="page-header">
      <div class="page-title">企业宣传</div>
      <div class="page-actions">
        <el-button v-if="isEnterpriseAdmin" type="primary" @click="createPromotion">
          <el-icon><Plus /></el-icon>
          发布宣传
        </el-button>
      </div>
    </div>
    
    <div class="filter-card">
      <div class="search-box">
        <el-input
          v-model="searchForm.keyword"
          placeholder="搜索标题或企业名称"
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
          <span class="label">状态：</span>
          <el-select
            v-model="searchForm.status"
            placeholder="所有状态"
            clearable
            @change="handleSearch"
          >
            <el-option
              v-for="item in statusOptions"
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
    
    <div v-if="loading" class="loading-container">
      <div v-for="i in 4" :key="i" class="loading-item">
        <el-skeleton :rows="3" animated />
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
            :class="{ 'is-top': item.isTop }"
            @click="viewDetail(item.id)"
          >
            <div class="top-flag" v-if="item.isTop">置顶</div>
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
                  <span class="views">{{ item.viewCount }} 浏览</span>
                </div>
              </div>
              <div class="card-status" v-if="isEnterpriseAdmin && item.enterprise.id === currentEnterpriseId">
                <el-tag :type="getStatusType(item.status)" size="small">
                  {{ getStatusText(item.status) }}
                </el-tag>
                <div class="card-actions">
                  <el-dropdown @command="(command) => handleAction(command, item)">
                    <el-icon><MoreFilled /></el-icon>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="edit" v-if="item.status !== 'APPROVED' || isEnterpriseAdmin">
                          <el-icon><Edit /></el-icon> 编辑
                        </el-dropdown-item>
                        <el-dropdown-item command="top" v-if="isEnterpriseAdmin && !item.isTop && item.status === 'APPROVED'">
                          <el-icon><Top /></el-icon> 置顶
                        </el-dropdown-item>
                        <el-dropdown-item command="untop" v-if="isEnterpriseAdmin && item.isTop">
                          <el-icon><Bottom /></el-icon> 取消置顶
                        </el-dropdown-item>
                        <el-dropdown-item command="delete" divided>
                          <el-icon><Delete /></el-icon> 删除
                        </el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
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
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Plus, Edit, Delete, MoreFilled, 
  Top, Bottom
} from '@element-plus/icons-vue'
import { 
  getEnterprisePromotionList, 
  deleteEnterprisePromotion,
  setPromotionTopStatus
} from '@/api/enterprise'
import { useUserStore } from '@/stores/user'
import { formatTimeAgo } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()
const isEnterpriseAdmin = computed(() => userStore.isEnterpriseAdmin)
const currentEnterpriseId = computed(() => userStore.enterpriseId)

const loading = ref(false)
const promotionList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const searchForm = reactive({
  keyword: '',
  industry: '',
  status: '',
  orderBy: 'createTime_desc'
})

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

const statusOptions = [
  { label: '已审核', value: 'APPROVED' },
  { label: '待审核', value: 'PENDING' },
  { label: '已拒绝', value: 'REJECTED' }
]

const sortOptions = [
  { label: '最新发布', value: 'createTime_desc' },
  { label: '最早发布', value: 'createTime_asc' },
  { label: '最多浏览', value: 'viewCount_desc' }
]

// 获取宣传列表
const fetchPromotionList = async () => {
  loading.value = true
  
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      ...searchForm
    }
    
    // 如果是企业管理员，可以添加企业ID筛选自己的宣传
    if (isEnterpriseAdmin.value && currentEnterpriseId.value) {
      params.enterpriseId = currentEnterpriseId.value
    }
    
    const res = await getEnterprisePromotionList(params)
    
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

// 处理每页条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchPromotionList()
}

// 查看详情
const viewDetail = (id) => {
  router.push(`/enterprise/promotion/${id}`)
}

// 查看企业详情
const viewEnterpriseDetail = (id) => {
  router.push(`/enterprise/${id}`)
}

// 创建宣传
const createPromotion = () => {
  router.push('/enterprise/promotion/edit')
}

// 处理操作
const handleAction = (command, item) => {
  switch (command) {
    case 'edit':
      router.push(`/enterprise/promotion/edit/${item.id}`)
      break
    case 'delete':
      confirmDelete(item)
      break
    case 'top':
      setTopStatus(item.id, true)
      break
    case 'untop':
      setTopStatus(item.id, false)
      break
  }
}

// 确认删除
const confirmDelete = (item) => {
  ElMessageBox.confirm(`确定要删除宣传"${item.title}"吗？`, '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteEnterprisePromotion(item.id)
      ElMessage.success('删除成功')
      fetchPromotionList()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 设置置顶状态
const setTopStatus = async (id, isTop) => {
  try {
    await setPromotionTopStatus(id, { isTop })
    ElMessage.success(isTop ? '置顶成功' : '取消置顶成功')
    fetchPromotionList()
  } catch (error) {
    console.error('设置置顶状态失败:', error)
    ElMessage.error('操作失败')
  }
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'APPROVED': return 'success'
    case 'PENDING': return 'warning'
    case 'REJECTED': return 'danger'
    default: return 'info'
  }
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'APPROVED': return '已审核'
    case 'PENDING': return '待审核'
    case 'REJECTED': return '已拒绝'
    default: return '未知状态'
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchPromotionList()
})
</script>

<style lang="scss" scoped>
.promotion-list-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .page-title {
      font-size: 22px;
      font-weight: 500;
      color: #303133;
    }
    
    .page-actions {
      .el-button {
        .el-icon {
          margin-right: 5px;
        }
      }
    }
  }
  
  .filter-card {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 20px;
    margin-bottom: 20px;
    
    .search-box {
      margin-bottom: 15px;
    }
    
    .filter-options {
      display: flex;
      flex-wrap: wrap;
      gap: 15px;
      
      .filter-item {
        display: flex;
        align-items: center;
        
        .label {
          margin-right: 8px;
          color: #606266;
        }
        
        .el-select {
          width: 160px;
        }
      }
    }
  }
  
  .loading-container {
    margin-top: 20px;
    
    .loading-item {
      background-color: #fff;
      border-radius: 4px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      padding: 20px;
      margin-bottom: 20px;
    }
  }
  
  .empty-container {
    margin-top: 50px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 40px 0;
  }
  
  .promotion-grid {
    margin-top: 20px;
    
    .promotion-card {
      position: relative;
      background-color: #fff;
      border-radius: 4px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
      overflow: hidden;
      margin-bottom: 20px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      }
      
      &.is-top {
        border: 1px solid #f56c6c;
      }
      
      .top-flag {
        position: absolute;
        top: 0;
        right: 0;
        background-color: #f56c6c;
        color: #fff;
        padding: 2px 8px;
        font-size: 12px;
        border-bottom-left-radius: 4px;
        z-index: 1;
      }
      
      .card-cover {
        height: 160px;
        overflow: hidden;
        
        .el-image {
          width: 100%;
          height: 100%;
        }
      }
      
      .card-content {
        padding: 15px;
        
        .card-title {
          font-size: 16px;
          font-weight: 500;
          color: #303133;
          margin-bottom: 10px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          line-height: 1.5;
          height: 48px;
        }
        
        .card-desc {
          font-size: 14px;
          color: #606266;
          margin-bottom: 15px;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          line-height: 1.5;
          height: 42px;
        }
        
        .card-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
          
          .enterprise-info {
            display: flex;
            align-items: center;
            
            .enterprise-name {
              margin-left: 8px;
              font-size: 14px;
              color: #303133;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              max-width: 120px;
            }
            
            &:hover {
              .enterprise-name {
                color: #409eff;
              }
            }
          }
          
          .meta-data {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            font-size: 12px;
            color: #909399;
            
            .time {
              margin-bottom: 3px;
            }
          }
        }
        
        .card-status {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .card-actions {
            .el-icon {
              font-size: 16px;
              color: #909399;
              cursor: pointer;
              
              &:hover {
                color: #409eff;
              }
            }
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

@media (max-width: 768px) {
  .promotion-list-container {
    padding: 10px;
    
    .filter-card {
      padding: 15px;
      
      .filter-options {
        flex-direction: column;
        gap: 10px;
        
        .filter-item {
          .el-select {
            width: 100%;
          }
        }
      }
    }
    
    .promotion-grid {
      .promotion-card {
        .card-content {
          .card-meta {
            flex-direction: column;
            align-items: flex-start;
            
            .enterprise-info {
              margin-bottom: 10px;
              
              .enterprise-name {
                max-width: 200px;
              }
            }
            
            .meta-data {
              align-items: flex-start;
            }
          }
        }
      }
    }
  }
}
</style> 