<template>
  <div class="rating-appeal-container">
    <div class="page-header">
      <h2>评分申诉管理</h2>
      <el-alert
        title="在此页面您可以查看所有对您企业的评分申诉及其处理状态"
        type="info"
        description="当您认为某评分存在恶意评价、虚假信息或不符合规范的内容时，可以提交申诉。管理员审核后将做出相应处理。"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      />
    </div>
    
    <div class="search-box">
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="申诉状态">
          <el-select v-model="queryParams.status" placeholder="申诉状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="待处理" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已驳回" value="rejected" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <el-card v-if="loading" class="loading-card">
      <el-skeleton :rows="5" animated />
    </el-card>
    
    <div v-else-if="appealList.length === 0" class="empty-content">
      <el-empty description="暂无评分申诉记录" />
    </div>
    
    <div v-else class="appeal-list">
      <el-card v-for="appeal in appealList" :key="appeal.id" class="appeal-card">
        <div class="appeal-header">
          <div class="appeal-status">
            <el-tag 
              :type="getStatusType(appeal.status)" 
              effect="dark"
            >
              {{ getStatusText(appeal.status) }}
            </el-tag>
            <span class="appeal-time">申诉时间: {{ formatDate(appeal.createTime) }}</span>
          </div>
          <div class="appeal-actions" v-if="appeal.status === 'pending'">
            <el-button type="danger" link @click="cancelAppeal(appeal.id)">
              撤销申诉
            </el-button>
          </div>
        </div>
        
        <div class="rating-info">
          <div class="rating-content">
            <div class="rating-header">
              <div class="user-info">
                <el-avatar :size="30" :src="appeal.userAvatar || '/default-avatar.png'" />
                <span class="user-name">{{ appeal.userName || '匿名用户' }}</span>
              </div>
              <div class="rating-score">
                <el-rate v-model="appeal.rating.score" disabled show-score text-color="#ff9900" />
                <span class="rating-time">{{ formatDate(appeal.rating.createTime) }}</span>
              </div>
            </div>
            <div class="rating-text">{{ appeal.rating.content }}</div>
          </div>
        </div>
        
        <div class="appeal-content">
          <div class="appeal-reason">
            <h4>申诉理由</h4>
            <div class="reason-content">{{ appeal.reason }}</div>
          </div>
          
          <div class="evidence-list" v-if="appeal.evidences && appeal.evidences.length > 0">
            <h4>申诉证据</h4>
            <div class="evidence-items">
              <div 
                v-for="(evidence, index) in appeal.evidences" 
                :key="index" 
                class="evidence-item"
                @click="viewEvidence(evidence)"
              >
                <el-image 
                  v-if="isImage(evidence)" 
                  :src="evidence" 
                  fit="cover" 
                  class="evidence-image"
                >
                  <template #error>
                    <div class="image-error">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                </el-image>
                <div v-else class="evidence-file">
                  <el-icon><Document /></el-icon>
                  <span>证据文件 {{ index + 1 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <div class="appeal-result" v-if="appeal.status !== 'pending'">
          <h4>处理结果</h4>
          <div class="result-content">
            <div class="result-time">处理时间: {{ formatDate(appeal.handleTime) }}</div>
            <div class="result-comment" v-if="appeal.comment">
              <div class="comment-label">处理意见:</div>
              <div class="comment-content">{{ appeal.comment }}</div>
            </div>
            <div class="result-action">
              <el-tag type="success" v-if="appeal.status === 'approved'">
                申诉通过，该评分已被{{ appeal.action === 'delete' ? '删除' : '修改' }}
              </el-tag>
              <el-tag type="danger" v-else-if="appeal.status === 'rejected'">
                申诉被驳回，该评分保持不变
              </el-tag>
            </div>
          </div>
        </div>
      </el-card>
    </div>
    
    <div class="pagination-container" v-if="appealList.length > 0">
      <el-pagination
        :current-page="queryParams.pageNum"
        :page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 30, 50]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 证据查看对话框 -->
    <el-dialog v-model="evidenceDialogVisible" title="查看证据" width="800px" center>
      <div class="evidence-dialog">
        <el-image 
          v-if="isImage(currentEvidence)" 
          :src="currentEvidence" 
          style="width: 100%;" 
          :preview-src-list="[currentEvidence]"
        >
          <template #error>
            <div class="image-error">
              <el-icon><Picture /></el-icon>
              <span>图片加载失败</span>
            </div>
          </template>
        </el-image>
        <div v-else class="evidence-file-preview">
          <el-icon><Document /></el-icon>
          <div class="file-actions">
            <el-button type="primary" @click="downloadEvidence">
              <el-icon><Download /></el-icon>下载文件
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Picture, 
  Document, 
  Download 
} from '@element-plus/icons-vue'
import { getRatingAppeals, cancelRatingAppeal } from '@/api/rating' // 假设这些API已实现

const loading = ref(false)
const total = ref(0)
const appealList = ref([])
const evidenceDialogVisible = ref(false)
const currentEvidence = ref('')

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: ''
})

// 获取申诉列表
const getAppealList = async () => {
  loading.value = true
  try {
    const res = await getRatingAppeals({
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize,
      status: queryParams.status
    })
    appealList.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('获取申诉列表失败:', error)
    ElMessage.error('获取申诉列表失败')
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    pending: '待处理',
    approved: '已通过',
    rejected: '已驳回'
  }
  return map[status] || '未知状态'
}

// 是否为图片
const isImage = (url) => {
  if (!url) return false
  return /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(url)
}

// 查看证据
const viewEvidence = (evidence) => {
  currentEvidence.value = evidence
  evidenceDialogVisible.value = true
}

// 下载证据
const downloadEvidence = () => {
  if (!currentEvidence.value) return
  
  // 创建一个隐形的a标签来下载文件
  const a = document.createElement('a')
  a.href = currentEvidence.value
  a.download = currentEvidence.value.split('/').pop() || 'evidence-file'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

// 撤销申诉
const cancelAppeal = (id) => {
  ElMessageBox.confirm('确定要撤销此申诉吗？撤销后不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await cancelRatingAppeal(id)
      ElMessage.success('申诉已撤销')
      getAppealList()
    } catch (error) {
      console.error('撤销申诉失败:', error)
      ElMessage.error('撤销申诉失败')
    }
  }).catch(() => {})
}

// 处理查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getAppealList()
}

// 重置查询
const resetQuery = () => {
  queryParams.status = ''
  queryParams.pageNum = 1
  getAppealList()
}

// 分页处理
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getAppealList()
}

const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getAppealList()
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 页面加载时获取数据
onMounted(() => {
  getAppealList()
})
</script>

<style lang="scss" scoped>
.rating-appeal-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0 0 15px 0;
      font-weight: bold;
    }
  }
  
  .search-box {
    margin-bottom: 20px;
    background-color: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
  }
  
  .loading-card {
    margin-bottom: 20px;
  }
  
  .empty-content {
    margin: 40px 0;
    text-align: center;
  }
  
  .appeal-list {
    .appeal-card {
      margin-bottom: 20px;
      
      .appeal-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
        
        .appeal-status {
          display: flex;
          align-items: center;
          
          .appeal-time {
            margin-left: 10px;
            color: #909399;
            font-size: 14px;
          }
        }
      }
      
      .rating-info {
        padding: 15px;
        background-color: #f5f7fa;
        border-radius: 4px;
        margin-bottom: 15px;
        
        .rating-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 10px;
          
          .user-info {
            display: flex;
            align-items: center;
            
            .user-name {
              margin-left: 8px;
              font-weight: bold;
            }
          }
          
          .rating-score {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            
            .rating-time {
              color: #909399;
              font-size: 12px;
              margin-top: 5px;
            }
          }
        }
        
        .rating-text {
          line-height: 1.6;
          color: #606266;
        }
      }
      
      .appeal-content {
        margin-bottom: 15px;
        
        h4 {
          margin: 15px 0 10px;
          font-weight: bold;
          font-size: 16px;
        }
        
        .reason-content {
          line-height: 1.6;
          color: #606266;
          padding: 10px;
          background-color: #f8f8f8;
          border-radius: 4px;
        }
        
        .evidence-items {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          
          .evidence-item {
            width: 100px;
            height: 100px;
            cursor: pointer;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            overflow: hidden;
            
            &:hover {
              border-color: #409eff;
            }
            
            .evidence-image {
              width: 100%;
              height: 100%;
            }
            
            .evidence-file {
              width: 100%;
              height: 100%;
              display: flex;
              flex-direction: column;
              justify-content: center;
              align-items: center;
              background-color: #f5f7fa;
              
              .el-icon {
                font-size: 24px;
                margin-bottom: 5px;
                color: #909399;
              }
              
              span {
                font-size: 12px;
                color: #606266;
                text-align: center;
                padding: 0 5px;
              }
            }
          }
        }
      }
      
      .appeal-result {
        background-color: #f0f9eb;
        padding: 15px;
        border-radius: 4px;
        
        h4 {
          margin: 0 0 10px;
          font-weight: bold;
          font-size: 16px;
        }
        
        .result-content {
          .result-time {
            margin-bottom: 10px;
            color: #606266;
          }
          
          .result-comment {
            margin-bottom: 10px;
            
            .comment-label {
              font-weight: bold;
              margin-bottom: 5px;
            }
            
            .comment-content {
              line-height: 1.6;
              color: #606266;
              padding: 10px;
              background-color: #fff;
              border-radius: 4px;
            }
          }
        }
      }
    }
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
    margin-top: 20px;
  }
}

.evidence-dialog {
  display: flex;
  justify-content: center;
  align-items: center;
  
  .image-error {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 300px;
    background-color: #f5f7fa;
    
    .el-icon {
      font-size: 48px;
      color: #909399;
      margin-bottom: 10px;
    }
    
    span {
      color: #606266;
    }
  }
  
  .evidence-file-preview {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 300px;
    background-color: #f5f7fa;
    
    .el-icon {
      font-size: 64px;
      color: #909399;
      margin-bottom: 20px;
    }
    
    .file-actions {
      margin-top: 20px;
    }
  }
}
</style> 