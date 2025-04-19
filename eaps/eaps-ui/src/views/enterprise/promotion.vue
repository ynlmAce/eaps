<template>
  <div class="promotion-detail-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="!promotionData" class="not-found">
      <el-empty description="内容不存在或已被删除" />
      <el-button type="primary" @click="goBack">返回</el-button>
    </div>
    <div v-else>
      <!-- 返回按钮 -->
      <div class="back-btn">
        <el-button @click="goBack" plain>
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
      </div>

      <!-- 宣传内容详情 -->
      <el-card class="promotion-card">
        <div class="promotion-header">
          <h1 class="promotion-title">{{ promotionData.title }}</h1>
          <div class="promotion-meta">
            <div class="enterprise-info" @click="viewEnterpriseDetail">
              <el-avatar :size="30" :src="promotionData.enterpriseLogo || '/default-logo.png'" class="enterprise-logo" />
              <span class="enterprise-name">{{ promotionData.enterpriseName }}</span>
            </div>
            <div class="publish-info">
              <span class="publish-time">{{ formatDate(promotionData.publishTime) }}</span>
              <el-divider direction="vertical" />
              <span class="view-count"><el-icon><View /></el-icon>{{ promotionData.viewCount }}</span>
            </div>
          </div>
        </div>
        
        <el-divider />
        
        <div class="promotion-content" v-if="promotionData.content">
          <div v-html="formatContent(promotionData.content)" class="content-html"></div>
        </div>
        
        <div v-if="promotionData.attachments && promotionData.attachments.length > 0" class="promotion-attachments">
          <h3>附件</h3>
          <div class="attachment-list">
            <div v-for="(attachment, index) in promotionData.attachments" :key="index" class="attachment-item">
              <el-button type="primary" link @click="downloadAttachment(attachment)">
                <el-icon><Document /></el-icon>{{ attachment.name }}
              </el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 操作卡片 -->
      <el-card class="action-card">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-button type="primary" block @click="viewEnterpriseDetail">
              <el-icon><OfficeBuilding /></el-icon>查看企业详情
            </el-button>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-button type="success" block @click="viewEnterpriseJobs">
              <el-icon><Promotion /></el-icon>查看企业职位
            </el-button>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
            <el-button type="info" block @click="startChat">
              <el-icon><ChatLineRound /></el-icon>咨询企业
            </el-button>
          </el-col>
        </el-row>
      </el-card>

      <!-- 其他宣传内容 -->
      <el-card class="other-promotions-card" v-if="otherPromotions.length > 0">
        <template #header>
          <div class="card-header">
            <h2>其他宣传内容</h2>
            <el-button type="primary" link @click="viewAllPromotions">查看全部</el-button>
          </div>
        </template>
        <div class="other-promotions-list">
          <div v-for="item in otherPromotions" :key="item.id" class="other-promotion-item" @click="viewPromotion(item.id)">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-time">{{ formatDate(item.publishTime) }}</div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, View, Document, OfficeBuilding, Promotion, ChatLineRound } from '@element-plus/icons-vue'
import { getEnterprisePromotions } from '@/api/enterprise'
import { createChatSession } from '@/api/message'

const router = useRouter()
const route = useRoute()
const promotionId = ref(route.params.id)
const loading = ref(true)
const promotionData = ref(null)
const otherPromotions = ref([])

// 获取宣传详情
const fetchPromotionDetail = async () => {
  try {
    // 此处应调用API获取宣传详情，这里使用模拟数据
    // 实际实现应该是类似: const res = await getEnterprisePromotion(promotionId.value)
    
    // 模拟数据
    const mockData = {
      id: promotionId.value,
      title: '2023年校园招聘计划发布',
      content: '我司将于2023年9月开启全国高校校园招聘活动，涵盖计算机科学、人工智能、数据分析等多个专业方向。\n\n本次招聘计划招收应届毕业生共计200名，将提供有竞争力的薪资待遇和完善的培训体系。\n\n招聘流程包括：\n1. 网申筛选\n2. 在线笔试\n3. 初面\n4. 复试\n5. HR面试\n6. 发放offer\n\n欢迎各位同学踊跃报名！',
      publishTime: '2023-08-15',
      viewCount: 2560,
      enterpriseId: '12345',
      enterpriseName: '示例科技有限公司',
      enterpriseLogo: '/default-logo.png',
      attachments: [
        { name: '招聘职位详情.pdf', url: '#' },
        { name: '应聘流程说明.docx', url: '#' }
      ]
    }
    
    // 延迟模拟网络请求
    setTimeout(() => {
      promotionData.value = mockData
    }, 500)
    
  } catch (error) {
    console.error('获取宣传详情失败:', error)
    ElMessage.error('获取宣传详情失败')
  }
}

// 获取其他宣传内容
const fetchOtherPromotions = async () => {
  try {
    if (!promotionData.value || !promotionData.value.enterpriseId) return
    
    // 此处应调用API获取同一企业的其他宣传内容
    // 实际实现应该是类似：
    // const res = await getEnterprisePromotions(promotionData.value.enterpriseId, {
    //   pageNum: 1,
    //   pageSize: 5,
    //   excludeId: promotionId.value
    // })
    
    // 模拟数据
    const mockData = [
      { id: '2', title: '公司年度技术峰会圆满举行', publishTime: '2023-07-20' },
      { id: '3', title: '我司荣获"年度创新企业"称号', publishTime: '2023-06-10' },
      { id: '4', title: '新一代AI产品发布会预告', publishTime: '2023-05-15' }
    ]
    
    // 延迟模拟网络请求
    setTimeout(() => {
      otherPromotions.value = mockData
    }, 500)
    
  } catch (error) {
    console.error('获取其他宣传内容失败:', error)
    ElMessage.error('获取其他宣传内容失败')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 格式化内容（替换换行符为<br>）
const formatContent = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 下载附件
const downloadAttachment = (attachment) => {
  // 此处应调用API下载附件
  ElMessage.info('开始下载：' + attachment.name)
}

// 查看企业详情
const viewEnterpriseDetail = () => {
  if (!promotionData.value || !promotionData.value.enterpriseId) return
  router.push(`/enterprise/detail/${promotionData.value.enterpriseId}`)
}

// 查看企业职位
const viewEnterpriseJobs = () => {
  if (!promotionData.value || !promotionData.value.enterpriseId) return
  router.push(`/job/positions?enterpriseId=${promotionData.value.enterpriseId}`)
}

// 查看所有宣传内容
const viewAllPromotions = () => {
  if (!promotionData.value || !promotionData.value.enterpriseId) return
  router.push(`/enterprise/promotions/${promotionData.value.enterpriseId}`)
}

// 查看特定宣传内容
const viewPromotion = (id) => {
  router.push(`/enterprise/promotion/${id}`)
}

// 开始聊天
const startChat = async () => {
  if (!promotionData.value || !promotionData.value.enterpriseId) return
  
  try {
    // 创建会话
    const session = await createChatSession({
      targetType: 'enterprise',
      targetId: promotionData.value.enterpriseId
    })
    
    // 跳转到聊天页面
    router.push(`/message/chat/${session.id}`)
  } catch (error) {
    console.error('创建聊天会话失败:', error)
    ElMessage.error('创建聊天会话失败')
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 页面加载
onMounted(async () => {
  if (!promotionId.value) {
    ElMessage.error('内容ID不能为空')
    router.push('/enterprise/list')
    return
  }
  
  loading.value = true
  try {
    await fetchPromotionDetail()
    
    if (promotionData.value) {
      await fetchOtherPromotions()
    }
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.promotion-detail-container {
  max-width: 1000px;
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

.promotion-card {
  margin-bottom: 20px;
  
  .promotion-header {
    .promotion-title {
      margin: 0 0 20px 0;
      font-size: 24px;
      font-weight: bold;
      color: #303133;
    }
    
    .promotion-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      
      .enterprise-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        
        .enterprise-logo {
          margin-right: 10px;
        }
        
        .enterprise-name {
          color: #409EFF;
          
          &:hover {
            text-decoration: underline;
          }
        }
      }
      
      .publish-info {
        color: #909399;
        font-size: 14px;
        
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
        align-items: flex-start;
        
        .enterprise-info {
          margin-bottom: 10px;
        }
      }
    }
  }
  
  .promotion-content {
    margin-bottom: 30px;
    color: #606266;
    line-height: 1.8;
    
    .content-html {
      overflow-wrap: break-word;
    }
  }
  
  .promotion-attachments {
    h3 {
      font-size: 16px;
      margin-bottom: 15px;
    }
    
    .attachment-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
      
      .attachment-item {
        .el-icon {
          margin-right: 5px;
        }
      }
    }
  }
}

.action-card {
  margin-bottom: 20px;
  
  .el-row {
    .el-col {
      margin-bottom: 15px;
    }
  }
}

.other-promotions-card {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: bold;
    }
  }
  
  .other-promotions-list {
    .other-promotion-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid #EBEEF5;
      cursor: pointer;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        .item-title {
          color: #409EFF;
        }
      }
      
      .item-title {
        font-size: 16px;
        color: #303133;
        transition: color 0.3s;
      }
      
      .item-time {
        color: #909399;
        font-size: 14px;
      }
      
      @media (max-width: 576px) {
        flex-direction: column;
        align-items: flex-start;
        
        .item-time {
          margin-top: 5px;
        }
      }
    }
  }
}
</style> 