<template>
  <div class="promotion-detail-container">
    <div class="page-header">
      <div class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </div>
      <div class="title">企业宣传详情</div>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="!promotionDetail.id" class="empty-container">
      <el-empty description="未找到该宣传内容" />
      <el-button type="primary" @click="goBack">返回列表</el-button>
    </div>
    
    <div v-else class="promotion-content">
      <div class="promotion-header">
        <h1 class="promotion-title">{{ promotionDetail.title }}</h1>
        <div class="promotion-meta">
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDateTime(promotionDetail.createTime) }}</span>
          </div>
          <div class="meta-item">
            <el-icon><View /></el-icon>
            <span>{{ promotionDetail.viewCount }} 浏览</span>
          </div>
          <div class="meta-item" v-if="promotionDetail.isTop">
            <el-tag size="small" type="danger">置顶</el-tag>
          </div>
          <div class="meta-item">
            <el-tag size="small" :type="getStatusType(promotionDetail.status)">{{ getStatusText(promotionDetail.status) }}</el-tag>
          </div>
        </div>
        
        <div class="enterprise-info" v-if="promotionDetail.enterprise">
          <div class="enterprise-avatar">
            <el-avatar 
              :size="50" 
              :src="promotionDetail.enterprise.logo" 
              @click="viewEnterpriseDetail(promotionDetail.enterprise.id)"
            />
          </div>
          <div class="enterprise-meta">
            <div class="enterprise-name" @click="viewEnterpriseDetail(promotionDetail.enterprise.id)">
              {{ promotionDetail.enterprise.name }}
            </div>
            <div class="enterprise-industry">{{ promotionDetail.enterprise.industry }}</div>
          </div>
        </div>
      </div>
      
      <el-divider />
      
      <div class="promotion-body">
        <div v-if="promotionDetail.coverImage" class="cover-image">
          <el-image 
            :src="promotionDetail.coverImage" 
            fit="cover"
            :preview-src-list="[promotionDetail.coverImage]"
          />
        </div>
        
        <div class="content-html" v-html="promotionDetail.content"></div>
      </div>
      
      <el-divider />
      
      <div class="promotion-actions">
        <div class="action-btns">
          <el-button type="primary" @click="viewEnterpriseDetail(promotionDetail.enterprise.id)">
            <el-icon><OfficeBuilding /></el-icon>
            查看企业主页
          </el-button>
          <el-button @click="startChat(promotionDetail.enterprise.id)">
            <el-icon><ChatDotRound /></el-icon>
            联系企业
          </el-button>
          <el-dropdown trigger="click" @command="handleMoreAction">
            <el-button>
              <el-icon><MoreFilled /></el-icon>
              更多操作
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="favorite">
                  <el-icon><Star /></el-icon> 收藏
                </el-dropdown-item>
                <el-dropdown-item command="share">
                  <el-icon><Share /></el-icon> 分享
                </el-dropdown-item>
                <el-dropdown-item command="report" divided>
                  <el-icon><Warning /></el-icon> 举报
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <div class="related-promotions" v-if="relatedPromotions.length > 0">
        <h3 class="section-title">相关宣传</h3>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" v-for="item in relatedPromotions" :key="item.id">
            <div class="promotion-card" @click="viewPromotionDetail(item.id)">
              <div class="card-cover" v-if="item.coverImage">
                <el-image :src="item.coverImage" fit="cover" />
              </div>
              <div class="card-content">
                <div class="card-title">{{ item.title }}</div>
                <div class="card-meta">
                  <span>{{ formatTimeAgo(item.createTime) }}</span>
                  <span>{{ item.viewCount }} 浏览</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    
    <!-- 分享对话框 -->
    <el-dialog v-model="shareDialogVisible" title="分享宣传" width="400px">
      <div class="share-container">
        <div class="share-qrcode">
          <div ref="qrcodeRef" class="qrcode"></div>
          <div class="share-tips">扫描二维码分享</div>
        </div>
        <div class="share-link">
          <el-input v-model="shareLink" readonly>
            <template #append>
              <el-button @click="copyShareLink">复制</el-button>
            </template>
          </el-input>
        </div>
        <div class="share-platforms">
          <div class="platform-icons">
            <div class="platform-icon" v-for="platform in sharePlatforms" :key="platform.name" @click="shareTo(platform.name)">
              <el-icon>
                <component :is="platform.icon"></component>
              </el-icon>
              <span>{{ platform.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- 举报对话框 -->
    <el-dialog v-model="reportDialogVisible" title="举报内容" width="500px">
      <el-form :model="reportForm" label-width="80px" :rules="reportRules" ref="reportFormRef">
        <el-form-item label="举报原因" prop="reason">
          <el-select v-model="reportForm.reason" placeholder="请选择举报原因" style="width: 100%">
            <el-option v-for="item in reportReasons" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细说明" prop="description">
          <el-input 
            v-model="reportForm.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请详细描述问题，有助于我们快速处理"
          />
        </el-form-item>
        <el-form-item label="证据上传">
          <el-upload
            :action="uploadUrl"
            :on-preview="handlePreview"
            :on-remove="handleRemove"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            multiple
            :limit="3"
            list-type="picture"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">支持jpg/png/pdf文件，不超过5MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="reportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReport" :loading="submittingReport">
            提交
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Calendar, View, OfficeBuilding, ChatDotRound, 
  MoreFilled, Star, Share, Warning
} from '@element-plus/icons-vue'
import { 
  getEnterprisePromotionDetail, 
  getRelatedPromotions, 
  favoritePromotion,
  reportPromotion 
} from '@/api/enterprise'
import { createChatSession } from '@/api/message'
import { formatDateTime, formatTimeAgo } from '@/utils/format'
import QRCode from 'qrcodejs2'

const route = useRoute()
const router = useRouter()
const promotionId = computed(() => route.params.id)
const loading = ref(false)
const promotionDetail = ref({})
const relatedPromotions = ref([])
const shareDialogVisible = ref(false)
const reportDialogVisible = ref(false)
const qrcodeRef = ref(null)
const shareLink = ref('')
const reportFormRef = ref(null)
const submittingReport = ref(false)

const reportForm = ref({
  reason: '',
  description: '',
  evidence: []
})

const reportRules = {
  reason: [
    { required: true, message: '请选择举报原因', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请填写详细说明', trigger: 'blur' },
    { min: 10, message: '详细说明不少于10个字符', trigger: 'blur' }
  ]
}

const reportReasons = [
  { label: '虚假信息', value: 'FAKE_INFO' },
  { label: '垃圾广告', value: 'SPAM' },
  { label: '违法内容', value: 'ILLEGAL' },
  { label: '骚扰信息', value: 'HARASSMENT' },
  { label: '侵犯权益', value: 'INFRINGEMENT' },
  { label: '其他问题', value: 'OTHER' }
]

const sharePlatforms = [
  { name: 'wechat', label: '微信', icon: 'ChatDotRound' },
  { name: 'weibo', label: '微博', icon: 'Position' },
  { name: 'qq', label: 'QQ', icon: 'Bell' },
  { name: 'copy', label: '复制链接', icon: 'CopyDocument' }
]

const uploadUrl = import.meta.env.VITE_API_BASE_URL + '/file/upload'

// 获取宣传详情
const fetchPromotionDetail = async () => {
  loading.value = true
  try {
    const res = await getEnterprisePromotionDetail(promotionId.value)
    if (res && res.data) {
      promotionDetail.value = res.data
      // 获取相关宣传
      fetchRelatedPromotions()
    }
  } catch (error) {
    console.error('获取宣传详情失败:', error)
    ElMessage.error('获取宣传详情失败')
  } finally {
    loading.value = false
  }
}

// 获取相关宣传
const fetchRelatedPromotions = async () => {
  if (!promotionDetail.value.id || !promotionDetail.value.enterprise?.id) return
  
  try {
    const res = await getRelatedPromotions({
      enterpriseId: promotionDetail.value.enterprise.id,
      currentId: promotionDetail.value.id,
      limit: 3
    })
    if (res && res.data) {
      relatedPromotions.value = res.data
    }
  } catch (error) {
    console.error('获取相关宣传失败:', error)
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

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 查看企业详情
const viewEnterpriseDetail = (enterpriseId) => {
  if (!enterpriseId) return
  router.push(`/enterprise/${enterpriseId}`)
}

// 查看宣传详情
const viewPromotionDetail = (id) => {
  if (id === promotionId.value) return
  router.push(`/enterprise/promotion/${id}`)
}

// 发起聊天
const startChat = async (enterpriseId) => {
  if (!enterpriseId) return
  
  try {
    const res = await createChatSession({
      targetId: enterpriseId,
      targetType: 'ENTERPRISE'
    })
    
    if (res && res.data) {
      router.push(`/message/session/${res.data.id}`)
    }
  } catch (error) {
    console.error('创建聊天会话失败:', error)
    ElMessage.error('创建聊天会话失败')
  }
}

// 处理更多操作
const handleMoreAction = (command) => {
  switch (command) {
    case 'favorite':
      handleFavorite()
      break
    case 'share':
      openShareDialog()
      break
    case 'report':
      openReportDialog()
      break
  }
}

// 收藏宣传
const handleFavorite = async () => {
  try {
    await favoritePromotion(promotionId.value)
    ElMessage.success('收藏成功')
  } catch (error) {
    console.error('收藏失败:', error)
    ElMessage.error('收藏失败')
  }
}

// 打开分享对话框
const openShareDialog = () => {
  shareDialogVisible.value = true
  shareLink.value = window.location.href
  
  setTimeout(() => {
    if (qrcodeRef.value) {
      new QRCode(qrcodeRef.value, {
        text: shareLink.value,
        width: 128,
        height: 128,
        colorDark: '#000000',
        colorLight: '#ffffff',
        correctLevel: QRCode.CorrectLevel.H
      })
    }
  }, 100)
}

// 复制分享链接
const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制')
  }).catch(err => {
    console.error('复制失败:', err)
    ElMessage.error('复制失败')
  })
}

// 分享到平台
const shareTo = (platform) => {
  // 实际项目中应该使用各平台的分享SDK
  ElMessage.info(`分享到${platform}功能开发中`)
  shareDialogVisible.value = false
}

// 打开举报对话框
const openReportDialog = () => {
  reportDialogVisible.value = true
  reportForm.value = {
    reason: '',
    description: '',
    evidence: []
  }
}

// 提交举报
const submitReport = () => {
  reportFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submittingReport.value = true
    try {
      await reportPromotion(promotionId.value, reportForm.value)
      ElMessage.success('举报已提交，我们会尽快处理')
      reportDialogVisible.value = false
    } catch (error) {
      console.error('举报提交失败:', error)
      ElMessage.error('举报提交失败')
    } finally {
      submittingReport.value = false
    }
  })
}

// 处理图片预览
const handlePreview = (file) => {
  console.log(file)
}

// 处理图片移除
const handleRemove = (file, fileList) => {
  reportForm.value.evidence = fileList.map(item => item.response?.data?.url || item.url)
}

// 文件上传前检查
const beforeUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/png', 'application/pdf'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isValidType) {
    ElMessage.error('只能上传JPG/PNG/PDF文件!')
    return false
  }
  
  if (!isLt5M) {
    ElMessage.error('文件大小不能超过5MB!')
    return false
  }
  
  return true
}

// 上传成功回调
const handleUploadSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    reportForm.value.evidence = fileList.map(item => item.response?.data?.url || item.url)
  } else {
    ElMessage.error('文件上传失败')
  }
}

// 组件挂载时获取数据
onMounted(() => {
  if (promotionId.value) {
    fetchPromotionDetail()
  }
})
</script>

<style lang="scss" scoped>
.promotion-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    .back-button {
      display: flex;
      align-items: center;
      cursor: pointer;
      color: #606266;
      margin-right: 20px;
      
      .el-icon {
        margin-right: 5px;
      }
      
      &:hover {
        color: #409eff;
      }
    }
    
    .title {
      font-size: 20px;
      font-weight: 500;
      color: #303133;
    }
  }
  
  .loading-container, .empty-container {
    padding: 30px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .el-button {
      margin-top: 20px;
    }
  }
  
  .promotion-content {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 30px;
    
    .promotion-header {
      .promotion-title {
        font-size: 24px;
        font-weight: bold;
        color: #303133;
        margin: 0 0 15px 0;
      }
      
      .promotion-meta {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        color: #909399;
        font-size: 14px;
        margin-bottom: 20px;
        
        .meta-item {
          display: flex;
          align-items: center;
          margin-right: 20px;
          margin-bottom: 10px;
          
          .el-icon {
            margin-right: 5px;
          }
        }
      }
      
      .enterprise-info {
        display: flex;
        align-items: center;
        padding: 15px;
        background-color: #f5f7fa;
        border-radius: 4px;
        
        .enterprise-avatar {
          margin-right: 15px;
          cursor: pointer;
        }
        
        .enterprise-meta {
          .enterprise-name {
            font-size: 16px;
            font-weight: 500;
            color: #303133;
            cursor: pointer;
            
            &:hover {
              color: #409eff;
            }
          }
          
          .enterprise-industry {
            font-size: 14px;
            color: #909399;
            margin-top: 5px;
          }
        }
      }
    }
    
    .promotion-body {
      .cover-image {
        margin-bottom: 20px;
        border-radius: 4px;
        overflow: hidden;
        max-height: 400px;
        
        .el-image {
          width: 100%;
          height: 100%;
        }
      }
      
      .content-html {
        font-size: 16px;
        line-height: 1.8;
        color: #303133;
        
        img {
          max-width: 100%;
          height: auto;
        }
        
        p {
          margin-bottom: 16px;
        }
        
        h1, h2, h3, h4, h5, h6 {
          font-weight: 500;
          margin-top: 1.5em;
          margin-bottom: 0.5em;
        }
      }
    }
    
    .promotion-actions {
      padding: 20px 0;
      
      .action-btns {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        
        .el-button {
          .el-icon {
            margin-right: 5px;
          }
        }
      }
    }
    
    .related-promotions {
      margin-top: 20px;
      
      .section-title {
        font-size: 18px;
        font-weight: 500;
        margin-bottom: 20px;
        position: relative;
        padding-left: 15px;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 16px;
          background-color: #409eff;
          border-radius: 2px;
        }
      }
      
      .promotion-card {
        background-color: #fff;
        border-radius: 4px;
        overflow: hidden;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        margin-bottom: 20px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          transform: translateY(-5px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
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
          
          .card-meta {
            display: flex;
            justify-content: space-between;
            color: #909399;
            font-size: 12px;
            
            span {
              display: inline-flex;
              align-items: center;
            }
          }
        }
      }
    }
  }
}

.share-container {
  .share-qrcode {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 20px;
    
    .qrcode {
      margin-bottom: 10px;
      
      img {
        display: block;
        margin: 0 auto;
      }
    }
    
    .share-tips {
      color: #909399;
      font-size: 14px;
    }
  }
  
  .share-link {
    margin-bottom: 20px;
  }
  
  .share-platforms {
    .platform-icons {
      display: flex;
      justify-content: space-around;
      flex-wrap: wrap;
      
      .platform-icon {
        display: flex;
        flex-direction: column;
        align-items: center;
        cursor: pointer;
        margin: 0 10px 10px;
        
        .el-icon {
          font-size: 24px;
          color: #409eff;
          margin-bottom: 5px;
        }
        
        span {
          font-size: 12px;
        }
        
        &:hover {
          color: #409eff;
        }
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .promotion-detail-container {
    padding: 10px;
    
    .promotion-content {
      padding: 15px;
      
      .promotion-header {
        .promotion-title {
          font-size: 20px;
          margin-bottom: 10px;
        }
        
        .enterprise-info {
          flex-direction: column;
          align-items: flex-start;
          
          .enterprise-avatar {
            margin-bottom: 10px;
          }
        }
      }
      
      .promotion-actions {
        .action-btns {
          justify-content: space-between;
          
          .el-button {
            flex: 1;
            margin-right: 0;
            
            &:last-child {
              margin-right: 0;
            }
          }
        }
      }
    }
  }
}
</style> 