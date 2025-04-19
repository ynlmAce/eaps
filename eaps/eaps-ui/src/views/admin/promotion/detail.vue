<template>
  <div class="promotion-detail-container" v-loading="loading">
    <div class="page-header">
      <div class="left">
        <el-button @click="goBack" icon="ArrowLeft">返回列表</el-button>
        <div class="title">宣传详情</div>
      </div>
      
      <div class="actions" v-if="promotionDetail">
        <el-button 
          v-if="promotionDetail.status === 'PENDING'" 
          type="success" 
          @click="handleApprove"
        >
          <el-icon><Check /></el-icon>
          通过审核
        </el-button>
        
        <el-button 
          v-if="promotionDetail.status === 'PENDING'" 
          type="danger" 
          @click="handleReject"
        >
          <el-icon><Close /></el-icon>
          拒绝审核
        </el-button>
        
        <el-button 
          v-if="promotionDetail.status === 'APPROVED'" 
          type="primary" 
          @click="handleSetTop(!promotionDetail.isTop)"
        >
          <el-icon><Top /></el-icon>
          {{ promotionDetail.isTop ? '取消置顶' : '设为置顶' }}
        </el-button>
      </div>
    </div>
    
    <div class="detail-content" v-if="promotionDetail">
      <div class="basic-info-section">
        <div class="section-header">
          <el-divider content-position="left">
            <span class="section-title">基本信息</span>
          </el-divider>
        </div>
        
        <el-descriptions border :column="2" size="large">
          <el-descriptions-item label="宣传ID">{{ promotionDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(promotionDetail.status)">
              {{ getStatusText(promotionDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="宣传标题">{{ promotionDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="浏览量">{{ promotionDetail.viewCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(promotionDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatDateTime(promotionDetail.updateTime) }}</el-descriptions-item>
          <el-descriptions-item label="置顶状态">
            <el-tag v-if="promotionDetail.isTop" type="danger">已置顶</el-tag>
            <el-tag v-else type="info">未置顶</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="审核时间" v-if="promotionDetail.reviewTime">
            {{ formatDateTime(promotionDetail.reviewTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="拒绝原因" v-if="promotionDetail.status === 'REJECTED'" :span="2">
            {{ promotionDetail.rejectReason || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="enterprise-info-section">
        <div class="section-header">
          <el-divider content-position="left">
            <span class="section-title">企业信息</span>
          </el-divider>
        </div>
        
        <div class="enterprise-card" v-if="promotionDetail.enterprise">
          <div class="enterprise-header">
            <el-avatar 
              :size="50" 
              :src="promotionDetail.enterprise.logo"
              @click="viewEnterpriseDetail(promotionDetail.enterprise.id)"
            />
            <div class="enterprise-meta">
              <div class="enterprise-name" @click="viewEnterpriseDetail(promotionDetail.enterprise.id)">
                {{ promotionDetail.enterprise.name }}
              </div>
              <div class="enterprise-info">
                <span>{{ promotionDetail.enterprise.industry }}</span>
                <el-divider direction="vertical" />
                <span>{{ promotionDetail.enterprise.size }}</span>
                <el-divider direction="vertical" />
                <span>{{ promotionDetail.enterprise.location }}</span>
              </div>
            </div>
          </div>
          
          <div class="enterprise-contact" v-if="promotionDetail.enterprise.contactPerson">
            <div class="contact-item">
              <el-icon><User /></el-icon>
              <span>联系人: {{ promotionDetail.enterprise.contactPerson }}</span>
            </div>
            <div class="contact-item">
              <el-icon><Phone /></el-icon>
              <span>电话: {{ promotionDetail.enterprise.contactPhone }}</span>
            </div>
            <div class="contact-item">
              <el-icon><Message /></el-icon>
              <span>邮箱: {{ promotionDetail.enterprise.contactEmail }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="content-section">
        <div class="section-header">
          <el-divider content-position="left">
            <span class="section-title">宣传内容</span>
          </el-divider>
        </div>
        
        <div class="promotion-cover" v-if="promotionDetail.coverImage">
          <div class="cover-label">封面图片：</div>
          <el-image 
            :src="promotionDetail.coverImage" 
            :preview-src-list="[promotionDetail.coverImage]"
            fit="contain"
            style="max-width: 100%; max-height: 400px;"
          />
        </div>
        
        <div class="promotion-content">
          <div class="content-label">详细内容：</div>
          <div class="html-content" v-html="promotionDetail.content"></div>
        </div>
      </div>
      
      <div class="images-section" v-if="promotionDetail.images && promotionDetail.images.length > 0">
        <div class="section-header">
          <el-divider content-position="left">
            <span class="section-title">宣传图片</span>
          </el-divider>
        </div>
        
        <el-row :gutter="16">
          <el-col 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6" 
            v-for="(image, index) in promotionDetail.images" 
            :key="index"
            class="image-item"
          >
            <el-image 
              :src="image" 
              :preview-src-list="promotionDetail.images"
              fit="cover"
              class="gallery-image"
            />
          </el-col>
        </el-row>
      </div>
    </div>
    
    <div class="empty-state" v-else-if="!loading">
      <el-empty description="未找到宣传详情数据" />
      <el-button type="primary" @click="goBack">返回列表</el-button>
    </div>
    
    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-form :model="rejectForm" ref="rejectFormRef" :rules="rejectRules">
        <el-form-item label="拒绝原因" prop="reason" label-width="80px">
          <el-input
            v-model="rejectForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因，将通知企业"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject" :loading="submitting">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Check, Close, Top, User, Phone, Message } from '@element-plus/icons-vue'
import { 
  getPromotionDetail, 
  reviewPromotion,
  recommendPromotion,
  cancelRecommendPromotion
} from '@/api/promotion'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const promotionId = route.params.id
const loading = ref(true)
const promotionDetail = ref(null)
const rejectDialogVisible = ref(false)
const submitting = ref(false)

const rejectForm = reactive({
  reason: ''
})

const rejectRules = {
  reason: [
    { required: true, message: '请输入拒绝原因', trigger: 'blur' },
    { min: 5, max: 200, message: '拒绝原因长度应在5-200个字符之间', trigger: 'blur' }
  ]
}

// 获取宣传详情
const fetchPromotionDetail = async () => {
  loading.value = true
  
  try {
    const res = await getPromotionDetail(promotionId)
    promotionDetail.value = res.data
  } catch (error) {
    console.error('获取宣传详情失败:', error)
    ElMessage.error('获取宣传详情失败')
  } finally {
    loading.value = false
  }
}

// 返回列表
const goBack = () => {
  router.back()
}

// 查看企业详情
const viewEnterpriseDetail = (id) => {
  router.push(`/enterprise/${id}`)
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
    case 'APPROVED': return '已通过'
    case 'PENDING': return '待审核'
    case 'REJECTED': return '已拒绝'
    default: return '未知状态'
  }
}

// 审核通过
const handleApprove = async () => {
  try {
    await ElMessageBox.confirm('确定要通过该宣传吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await reviewPromotion(promotionId, {
      approved: true
    })
    
    ElMessage.success('审核通过成功')
    fetchPromotionDetail()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  }
}

// 打开拒绝对话框
const handleReject = () => {
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

// 确认拒绝
const confirmReject = async () => {
  submitting.value = true
  
  try {
    await reviewPromotion(promotionId, {
      approved: false,
      reason: rejectForm.reason
    })
    
    ElMessage.success('已拒绝该宣传')
    rejectDialogVisible.value = false
    fetchPromotionDetail()
  } catch (error) {
    console.error('拒绝失败:', error)
    ElMessage.error('拒绝失败')
  } finally {
    submitting.value = false
  }
}

// 设置置顶状态
const handleSetTop = async (isTop) => {
  try {
    if (isTop) {
      await recommendPromotion(promotionId)
    } else {
      await cancelRecommendPromotion(promotionId)
    }
    
    ElMessage.success(isTop ? '设置置顶成功' : '取消置顶成功')
    fetchPromotionDetail()
  } catch (error) {
    console.error('设置置顶状态失败:', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  if (!promotionId) {
    ElMessage.error('宣传ID不能为空')
    router.push('/admin/promotion')
    return
  }
  
  fetchPromotionDetail()
})
</script>

<style lang="scss" scoped>
.promotion-detail-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .left {
      display: flex;
      align-items: center;
      
      .title {
        font-size: 22px;
        font-weight: 500;
        color: #303133;
        margin-left: 20px;
      }
    }
    
    .actions {
      display: flex;
      gap: 10px;
    }
  }
  
  .detail-content {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 20px;
    
    .section-header {
      margin: 20px 0;
      
      .section-title {
        font-size: 18px;
        font-weight: 500;
        color: #303133;
      }
    }
    
    .enterprise-card {
      background-color: #f9f9f9;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      
      .enterprise-header {
        display: flex;
        align-items: center;
        margin-bottom: 15px;
        
        .el-avatar {
          margin-right: 15px;
          cursor: pointer;
        }
        
        .enterprise-meta {
          .enterprise-name {
            font-size: 18px;
            font-weight: 500;
            color: #409eff;
            margin-bottom: 8px;
            cursor: pointer;
            
            &:hover {
              text-decoration: underline;
            }
          }
          
          .enterprise-info {
            color: #606266;
            font-size: 14px;
          }
        }
      }
      
      .enterprise-contact {
        display: flex;
        flex-wrap: wrap;
        gap: 15px;
        
        .contact-item {
          display: flex;
          align-items: center;
          color: #606266;
          
          .el-icon {
            margin-right: 8px;
            color: #909399;
          }
        }
      }
    }
    
    .promotion-cover, .promotion-content {
      margin-bottom: 30px;
      
      .cover-label, .content-label {
        font-size: 16px;
        font-weight: 500;
        color: #303133;
        margin-bottom: 15px;
      }
      
      .html-content {
        background-color: #f9f9f9;
        border-radius: 8px;
        padding: 20px;
        line-height: 1.6;
        
        :deep(img) {
          max-width: 100%;
        }
        
        :deep(p) {
          margin-bottom: 1em;
        }
        
        :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
          margin-top: 1.5em;
          margin-bottom: 0.5em;
        }
        
        :deep(ul), :deep(ol) {
          padding-left: 2em;
          margin-bottom: 1em;
        }
      }
    }
    
    .images-section {
      .image-item {
        margin-bottom: 16px;
        
        .gallery-image {
          width: 100%;
          height: 200px;
          border-radius: 8px;
          transition: transform 0.3s;
          
          &:hover {
            transform: scale(1.02);
          }
        }
      }
    }
  }
  
  .empty-state {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 40px;
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .el-button {
      margin-top: 20px;
    }
  }
}
</style> 