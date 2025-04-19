<template>
  <div class="promotion-detail-container">
    <div class="back-link" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>返回宣传列表</span>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton animated :rows="15" />
    </div>
    
    <div v-else-if="!promotionDetail" class="empty-container">
      <el-empty description="未找到相关宣传内容" />
      <el-button type="primary" @click="goBack">返回列表</el-button>
    </div>
    
    <template v-else>
      <div class="promotion-header">
        <h1 class="promotion-title">{{ promotionDetail.title }}</h1>
        
        <div class="promotion-meta">
          <div class="enterprise-info" @click="viewEnterpriseDetail(promotionDetail.enterprise.id)">
            <el-avatar :size="40" :src="promotionDetail.enterprise.logo">
              {{ promotionDetail.enterprise.name.substr(0, 1) }}
            </el-avatar>
            <div class="enterprise-meta">
              <div class="enterprise-name">{{ promotionDetail.enterprise.name }}</div>
              <div class="enterprise-info-text">
                <span>{{ promotionDetail.enterprise.industry }}</span>
                <el-divider direction="vertical" />
                <span>{{ promotionDetail.enterprise.size }}</span>
              </div>
            </div>
          </div>
          
          <div class="info-tags">
            <div class="info-tag">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDate(promotionDetail.createTime) }}</span>
            </div>
            <div class="info-tag">
              <el-icon><View /></el-icon>
              <span>{{ promotionDetail.viewCount || 0 }} 浏览</span>
            </div>
            <div class="share-btn" @click="showShareOptions = true">
              <el-icon><Share /></el-icon>
              <span>分享</span>
            </div>
          </div>
        </div>
      </div>
      
      <div class="promotion-summary" v-if="promotionDetail.summary">
        {{ promotionDetail.summary }}
      </div>
      
      <div class="promotion-cover" v-if="promotionDetail.coverImage">
        <el-image 
          :src="promotionDetail.coverImage" 
          fit="contain" 
          :preview-src-list="[promotionDetail.coverImage]"
        />
      </div>
      
      <div class="promotion-content">
        <div class="html-content" v-html="promotionDetail.content"></div>
      </div>
      
      <div class="promotion-images" v-if="promotionDetail.images && promotionDetail.images.length > 0">
        <h2 class="section-title">宣传图片</h2>
        <el-row :gutter="16">
          <el-col 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6" 
            v-for="(image, index) in promotionDetail.images" 
            :key="index"
          >
            <div class="image-item">
              <el-image 
                :src="image" 
                :preview-src-list="promotionDetail.images"
                fit="cover"
                class="gallery-image"
              />
            </div>
          </el-col>
        </el-row>
      </div>
      
      <div class="promotion-attachments" v-if="promotionDetail.attachments && promotionDetail.attachments.length > 0">
        <h2 class="section-title">附件资料</h2>
        <div class="attachments-list">
          <div 
            v-for="(file, index) in promotionDetail.attachments" 
            :key="index"
            class="attachment-item"
            @click="downloadAttachment(file)"
          >
            <div class="file-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="file-info">
              <div class="file-name">{{ file.name }}</div>
              <div class="file-meta">
                <span class="file-size">{{ formatFileSize(file.size) }}</span>
                <span class="file-type">{{ getFileType(file.name) }}</span>
              </div>
            </div>
            <div class="download-icon">
              <el-icon><Download /></el-icon>
            </div>
          </div>
        </div>
      </div>
      
      <div class="enterprise-card">
        <h2 class="section-title">企业信息</h2>
        <div class="enterprise-detail-card" @click="viewEnterpriseDetail(promotionDetail.enterprise.id)">
          <div class="enterprise-logo">
            <el-avatar :size="64" :src="promotionDetail.enterprise.logo">
              {{ promotionDetail.enterprise.name.substr(0, 1) }}
            </el-avatar>
          </div>
          <div class="enterprise-detail">
            <div class="enterprise-name">{{ promotionDetail.enterprise.name }}</div>
            <div class="enterprise-info-row">
              <div class="info-item">
                <el-icon><OfficeBuilding /></el-icon>
                <span>{{ promotionDetail.enterprise.industry }}</span>
              </div>
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span>{{ promotionDetail.enterprise.size }}</span>
              </div>
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span>{{ promotionDetail.enterprise.location }}</span>
              </div>
            </div>
            <div class="enterprise-description">
              {{ promotionDetail.enterprise.description || '暂无企业简介' }}
            </div>
            <div class="view-more">
              查看企业详情 <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
      
      <div class="related-promotions" v-if="relatedPromotions.length > 0">
        <h2 class="section-title">相关宣传</h2>
        <el-row :gutter="20">
          <el-col 
            v-for="item in relatedPromotions" 
            :key="item.id" 
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6"
          >
            <div class="promotion-card" @click="viewDetail(item.id)">
              <div class="card-cover" v-if="item.coverImage">
                <el-image :src="item.coverImage" fit="cover" />
              </div>
              <div class="card-content">
                <div class="card-title">{{ item.title }}</div>
                <div class="card-meta">
                  <div class="enterprise-info">
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
    </template>
    
    <!-- 分享对话框 -->
    <el-dialog v-model="showShareOptions" title="分享" width="360px" center>
      <div class="share-options">
        <div class="share-option" @click="shareViaWechat">
          <div class="share-icon wechat">
            <i class="fab fa-weixin"></i>
          </div>
          <div class="share-name">微信</div>
        </div>
        <div class="share-option" @click="shareViaWeibo">
          <div class="share-icon weibo">
            <i class="fab fa-weibo"></i>
          </div>
          <div class="share-name">微博</div>
        </div>
        <div class="share-option" @click="copyLink">
          <div class="share-icon link">
            <el-icon><Link /></el-icon>
          </div>
          <div class="share-name">复制链接</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, ArrowRight, Calendar, View, Share, Document, 
  Download, User, Location, OfficeBuilding, Link
} from '@element-plus/icons-vue'
import { 
  getPromotionDetail, 
  getPromotionList
} from '@/api/promotion'
import { formatDate, formatTimeAgo } from '@/utils/format'

// 获取路由信息和响应式状态
const route = useRoute()
const router = useRouter()
const promotionId = route.params.id
const loading = ref(true)
const promotionDetail = ref(null)
const relatedPromotions = ref([])
const showShareOptions = ref(false)

// 获取宣传详情
const fetchPromotionDetail = async () => {
  loading.value = true
  
  try {
    const res = await getPromotionDetail(promotionId)
    
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
  if (!promotionDetail.value || !promotionDetail.value.enterprise) return
  
  try {
    const params = {
      pageNum: 1,
      pageSize: 4,
      enterpriseId: promotionDetail.value.enterprise.id,
      status: 'APPROVED',
      excludeId: promotionId
    }
    
    const res = await getPromotionList(params)
    
    if (res && res.data) {
      relatedPromotions.value = res.data.records || []
    }
  } catch (error) {
    console.error('获取相关宣传失败:', error)
  }
}

// 返回列表
const goBack = () => {
  router.push('/promotion')
}

// 查看企业详情
const viewEnterpriseDetail = (id) => {
  router.push(`/enterprise/${id}`)
}

// 查看宣传详情
const viewDetail = (id) => {
  router.push(`/promotion/${id}`)
  // 页面切换后刷新数据
  setTimeout(() => {
    fetchPromotionDetail()
  }, 100)
}

// 下载附件
const downloadAttachment = (file) => {
  if (!file || !file.url) {
    ElMessage.error('下载链接不存在')
    return
  }
  
  const a = document.createElement('a')
  a.style.display = 'none'
  a.href = file.url
  a.download = file.name
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes) return '0 B'
  
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 获取文件类型
const getFileType = (filename) => {
  if (!filename) return ''
  
  const extension = filename.split('.').pop().toLowerCase()
  
  const fileTypes = {
    pdf: 'PDF文档',
    doc: 'Word文档',
    docx: 'Word文档',
    xls: 'Excel表格',
    xlsx: 'Excel表格',
    ppt: 'PPT演示文稿',
    pptx: 'PPT演示文稿',
    txt: '文本文件',
    zip: '压缩文件',
    rar: '压缩文件',
    jpg: '图片',
    jpeg: '图片',
    png: '图片',
    gif: '图片'
  }
  
  return fileTypes[extension] || '未知类型'
}

// 分享功能
const shareViaWechat = () => {
  ElMessage.info('请使用微信扫描二维码分享')
  showShareOptions.value = false
}

const shareViaWeibo = () => {
  const url = encodeURIComponent(window.location.href)
  const title = encodeURIComponent(promotionDetail.value?.title || '企业宣传')
  
  window.open(`http://service.weibo.com/share/share.php?url=${url}&title=${title}`, '_blank')
  showShareOptions.value = false
}

const copyLink = () => {
  const input = document.createElement('input')
  input.value = window.location.href
  document.body.appendChild(input)
  input.select()
  document.execCommand('copy')
  document.body.removeChild(input)
  
  ElMessage.success('链接已复制到剪贴板')
  showShareOptions.value = false
}

// 初始化
onMounted(() => {
  if (!promotionId) {
    router.push('/promotion')
    return
  }
  
  fetchPromotionDetail()
})
</script>

<style lang="scss" scoped>
.promotion-detail-container {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  
  .back-link {
    display: inline-flex;
    align-items: center;
    color: #409eff;
    cursor: pointer;
    margin-bottom: 20px;
    
    .el-icon {
      margin-right: 6px;
    }
    
    &:hover {
      color: #66b1ff;
    }
  }
  
  .loading-container {
    background-color: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .empty-container {
    background-color: #fff;
    border-radius: 8px;
    padding: 40px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .el-button {
      margin-top: 20px;
    }
  }
  
  .promotion-header {
    background-color: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
    
    .promotion-title {
      font-size: 28px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 20px;
      line-height: 1.4;
      
      @media (max-width: 768px) {
        font-size: 22px;
      }
    }
    
    .promotion-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 20px;
      
      .enterprise-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        
        &:hover .enterprise-name {
          color: #66b1ff;
        }
        
        .el-avatar {
          margin-right: 12px;
        }
        
        .enterprise-meta {
          .enterprise-name {
            font-size: 16px;
            font-weight: 500;
            color: #409eff;
            margin-bottom: 4px;
          }
          
          .enterprise-info-text {
            font-size: 14px;
            color: #909399;
          }
        }
      }
      
      .info-tags {
        display: flex;
        align-items: center;
        gap: 16px;
        
        .info-tag, .share-btn {
          display: flex;
          align-items: center;
          color: #606266;
          font-size: 14px;
          
          .el-icon {
            margin-right: 5px;
            font-size: 16px;
          }
        }
        
        .share-btn {
          cursor: pointer;
          color: #409eff;
          
          &:hover {
            color: #66b1ff;
          }
        }
      }
    }
  }
  
  .promotion-summary {
    background-color: #f9f9f9;
    border-left: 4px solid #409eff;
    padding: 15px 20px;
    margin-bottom: 20px;
    color: #606266;
    font-size: 16px;
    line-height: 1.6;
    border-radius: 0 4px 4px 0;
  }
  
  .promotion-cover {
    margin-bottom: 20px;
    text-align: center;
    
    .el-image {
      max-width: 100%;
      max-height: 500px;
      border-radius: 8px;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    }
  }
  
  .promotion-content {
    background-color: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
    
    .html-content {
      font-size: 16px;
      line-height: 1.8;
      color: #303133;
      
      :deep(img) {
        max-width: 100%;
        height: auto;
        margin: 15px 0;
        border-radius: 4px;
      }
      
      :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
        margin-top: 1.5em;
        margin-bottom: 0.5em;
        font-weight: 500;
        color: #303133;
      }
      
      :deep(p) {
        margin-bottom: 1em;
      }
      
      :deep(ul), :deep(ol) {
        padding-left: 2em;
        margin-bottom: 1em;
      }
      
      :deep(a) {
        color: #409eff;
        text-decoration: none;
        
        &:hover {
          text-decoration: underline;
        }
      }
      
      :deep(blockquote) {
        border-left: 4px solid #e9e9eb;
        padding-left: 1em;
        color: #606266;
        font-style: italic;
        margin: 1em 0;
      }
      
      :deep(table) {
        width: 100%;
        border-collapse: collapse;
        margin: 1em 0;
        
        th, td {
          border: 1px solid #e9e9eb;
          padding: 0.5em;
          text-align: left;
        }
        
        th {
          background-color: #f5f7fa;
        }
      }
    }
  }
  
  .section-title {
    font-size: 20px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 20px;
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
  
  .promotion-images {
    background-color: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
    
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
  
  .promotion-attachments {
    background-color: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
    
    .attachments-list {
      display: flex;
      flex-direction: column;
      gap: 15px;
      
      .attachment-item {
        display: flex;
        align-items: center;
        padding: 15px;
        border-radius: 8px;
        background-color: #f5f7fa;
        cursor: pointer;
        transition: background-color 0.3s;
        
        &:hover {
          background-color: #ecf5ff;
        }
        
        .file-icon {
          margin-right: 15px;
          font-size: 24px;
          color: #909399;
        }
        
        .file-info {
          flex: 1;
          
          .file-name {
            font-size: 16px;
            color: #303133;
            margin-bottom: 5px;
          }
          
          .file-meta {
            font-size: 14px;
            color: #909399;
            
            .file-size {
              margin-right: 10px;
            }
          }
        }
        
        .download-icon {
          color: #409eff;
          font-size: 20px;
        }
      }
    }
  }
  
  .enterprise-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
    
    .enterprise-detail-card {
      display: flex;
      background-color: #f5f7fa;
      border-radius: 8px;
      padding: 20px;
      cursor: pointer;
      transition: background-color 0.3s;
      
      &:hover {
        background-color: #ecf5ff;
      }
      
      .enterprise-logo {
        margin-right: 20px;
      }
      
      .enterprise-detail {
        flex: 1;
        
        .enterprise-name {
          font-size: 20px;
          font-weight: 500;
          color: #303133;
          margin-bottom: 10px;
        }
        
        .enterprise-info-row {
          display: flex;
          flex-wrap: wrap;
          gap: 15px;
          margin-bottom: 15px;
          
          .info-item {
            display: flex;
            align-items: center;
            color: #606266;
            font-size: 14px;
            
            .el-icon {
              margin-right: 5px;
              font-size: 16px;
            }
          }
        }
        
        .enterprise-description {
          color: #606266;
          font-size: 14px;
          line-height: 1.6;
          margin-bottom: 15px;
          display: -webkit-box;
          -webkit-line-clamp: 3;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .view-more {
          color: #409eff;
          font-size: 14px;
          display: flex;
          align-items: center;
          
          .el-icon {
            margin-left: 5px;
          }
        }
      }
    }
  }
  
  .related-promotions {
    background-color: #fff;
    border-radius: 8px;
    padding: 30px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    
    .promotion-card {
      background-color: #f5f7fa;
      border-radius: 8px;
      overflow: hidden;
      margin-bottom: 20px;
      transition: transform 0.3s, box-shadow 0.3s;
      cursor: pointer;
      
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
          transition: transform 0.6s;
          
          &:hover {
            transform: scale(1.05);
          }
        }
      }
      
      .card-content {
        padding: 15px;
        
        .card-title {
          font-size: 16px;
          font-weight: 500;
          color: #303133;
          margin-bottom: 10px;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
          line-height: 1.5;
          height: 48px;
        }
        
        .card-meta {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .enterprise-info {
            display: flex;
            align-items: center;
            
            .el-avatar {
              margin-right: 8px;
            }
            
            .enterprise-name {
              font-size: 14px;
              color: #606266;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              max-width: 100px;
            }
          }
          
          .meta-data {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            color: #909399;
            font-size: 12px;
          }
        }
      }
    }
  }
}

.share-options {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
  
  .share-option {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    
    .share-icon {
      width: 50px;
      height: 50px;
      border-radius: 50%;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 24px;
      color: #fff;
      margin-bottom: 10px;
      
      &.wechat {
        background-color: #07c160;
      }
      
      &.weibo {
        background-color: #e6162d;
      }
      
      &.link {
        background-color: #909399;
      }
    }
    
    .share-name {
      font-size: 14px;
      color: #606266;
    }
    
    &:hover .share-name {
      color: #409eff;
    }
  }
}
</style> 