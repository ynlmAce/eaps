<template>
  <div class="enterprise-detail-container">
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
        <el-button @click="router.push('/enterprise/list')" plain>
          <el-icon><ArrowLeft /></el-icon>返回企业列表
        </el-button>
      </div>

      <!-- 企业基本信息 -->
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
          <el-card class="info-card">
            <div class="enterprise-header">
              <div class="enterprise-logo">
                <el-avatar :size="100" :src="enterpriseData.logo || '/default-logo.png'" />
              </div>
              <div class="enterprise-info">
                <div class="name-status">
                  <h1 class="enterprise-name">{{ enterpriseData.name }}</h1>
                  <el-tag v-if="enterpriseData.status === 'verified'" type="success">已认证</el-tag>
                </div>
                <div class="enterprise-meta">
                  <span>{{ enterpriseData.industry }}</span>
                  <el-divider direction="vertical" />
                  <span>{{ enterpriseData.scale }}</span>
                  <el-divider direction="vertical" />
                  <span>{{ enterpriseData.type }}</span>
                </div>
                <div class="enterprise-contact" v-if="enterpriseData.location || enterpriseData.website">
                  <div class="contact-item" v-if="enterpriseData.location">
                    <el-icon><Location /></el-icon>
                    <span>{{ enterpriseData.location }}</span>
                  </div>
                  <div class="contact-item" v-if="enterpriseData.website">
                    <el-icon><Link /></el-icon>
                    <a :href="addHttpPrefix(enterpriseData.website)" target="_blank">{{ enterpriseData.website }}</a>
                  </div>
                </div>
              </div>
              <div class="enterprise-actions">
                <el-button type="primary" @click="startChat">
                  <el-icon><Chat /></el-icon>在线咨询
                </el-button>
                <el-button type="success" @click="router.push(`/enterprise/rating/${enterpriseId}`)">
                  <el-icon><Star /></el-icon>评价企业
                </el-button>
              </div>
            </div>
          </el-card>

          <!-- 企业描述信息 -->
          <el-card class="description-card">
            <template #header>
              <div class="card-header">
                <h2>企业介绍</h2>
              </div>
            </template>
            <div v-if="enterpriseData.description" class="description-content">
              {{ enterpriseData.description }}
            </div>
            <el-empty v-else description="暂无企业介绍" />
          </el-card>

          <!-- 招聘职位 -->
          <el-card class="jobs-card">
            <template #header>
              <div class="card-header">
                <h2>招聘职位</h2>
                <el-button type="primary" link @click="showAllJobs">查看全部</el-button>
              </div>
            </template>
            <div v-if="jobList.length > 0">
              <div v-for="job in jobList" :key="job.id" class="job-item" @click="viewJobDetail(job.id)">
                <div class="job-header">
                  <div class="job-title">{{ job.name }}</div>
                  <div class="job-salary">{{ job.salaryLower }}-{{ job.salaryUpper }}K·{{ job.salaryCount }}薪</div>
                </div>
                <div class="job-tags">
                  <el-tag size="small">{{ job.workLocation }}</el-tag>
                  <el-tag size="small">{{ job.experienceRequirement }}</el-tag>
                  <el-tag size="small">{{ job.educationRequirement }}</el-tag>
                </div>
                <div class="job-meta">
                  <span class="publish-time">{{ formatDate(job.publishTime) }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无招聘职位" />
          </el-card>

          <!-- 宣传内容 -->
          <el-card class="promotion-card">
            <template #header>
              <div class="card-header">
                <h2>企业动态</h2>
                <el-button type="primary" link @click="showAllPromotions">查看全部</el-button>
              </div>
            </template>
            <div v-if="promotionList.length > 0">
              <div v-for="promotion in promotionList" :key="promotion.id" class="promotion-item" @click="viewPromotion(promotion.id)">
                <h3 class="promotion-title">{{ promotion.title }}</h3>
                <div class="promotion-content" v-if="promotion.content">
                  {{ promotion.content.length > 150 ? promotion.content.substring(0, 150) + '...' : promotion.content }}
                </div>
                <div class="promotion-meta">
                  <span class="publish-time">{{ formatDate(promotion.publishTime) }}</span>
                  <span class="view-count"><el-icon><View /></el-icon>{{ promotion.viewCount }}</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无企业动态" />
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <!-- 企业评分 -->
          <el-card class="rating-card">
            <template #header>
              <div class="card-header">
                <h2>企业评分</h2>
                <el-button type="primary" link @click="router.push(`/enterprise/rating/${enterpriseId}`)">查看全部</el-button>
              </div>
            </template>
            <div class="rating-overview">
              <div class="rating-score">
                <div class="score-value">{{ enterpriseData.avgRating || 0 }}</div>
                <div class="score-stars">
                  <el-rate
                    v-model="enterpriseData.avgRating"
                    disabled
                    show-score
                    text-color="#ff9900"
                  />
                </div>
                <div class="rating-count">{{ enterpriseData.ratingCount || 0 }}条评价</div>
              </div>
            </div>
            <el-divider />
            <div v-if="ratingList.length > 0" class="rating-list">
              <div v-for="rating in ratingList" :key="rating.id" class="rating-item">
                <div class="rating-header">
                  <div class="user-info">
                    <el-avatar :size="32" :src="rating.anonymous ? '/anonymous-avatar.png' : (rating.userAvatar || '/default-avatar.png')" />
                    <span class="user-name">{{ rating.anonymous ? '匿名用户' : rating.userName }}</span>
                  </div>
                  <div class="rating-value">
                    <el-rate 
                      v-model="rating.score" 
                      disabled 
                      size="small"
                    />
                  </div>
                </div>
                <div class="rating-content">{{ rating.content }}</div>
                <div class="rating-time">{{ formatDate(rating.createTime) }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无评价" />
          </el-card>

          <!-- 快捷操作 -->
          <el-card class="actions-card">
            <template #header>
              <div class="card-header">
                <h2>快捷操作</h2>
              </div>
            </template>
            <div class="quick-actions">
              <el-button type="primary" @click="router.push(`/job/positions?enterpriseId=${enterpriseId}`)">
                <el-icon><Promotion /></el-icon>查看所有职位
              </el-button>
              <el-button type="success" @click="router.push(`/enterprise/rating/${enterpriseId}`)">
                <el-icon><Star /></el-icon>我要评价
              </el-button>
              <el-button @click="startChat">
                <el-icon><ChatLineRound /></el-icon>在线咨询
              </el-button>
              <el-button @click="reportEnterprise">
                <el-icon><Warning /></el-icon>举报企业
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 举报对话框 -->
      <el-dialog v-model="reportDialogVisible" title="举报企业" width="500px">
        <el-form :model="reportForm" label-width="80px">
          <el-form-item label="举报原因" required>
            <el-select v-model="reportForm.reason" placeholder="请选择举报原因" style="width: 100%">
              <el-option label="虚假信息" value="fake" />
              <el-option label="违法内容" value="illegal" />
              <el-option label="骚扰行为" value="harassment" />
              <el-option label="欺诈行为" value="fraud" />
              <el-option label="其他原因" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="详细说明" required>
            <el-input 
              v-model="reportForm.description" 
              type="textarea" 
              :rows="4" 
              placeholder="请详细描述举报原因..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item label="上传证据" tip="可上传截图等证据材料">
            <el-upload
              class="evidence-uploader"
              action="/api/file/upload"
              :auto-upload="false"
              :limit="3"
              list-type="picture-card"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="reportDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitReport">提交举报</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, Link, Chat, Star, View, Promotion, ChatLineRound, Warning, Plus } from '@element-plus/icons-vue'
import { getEnterpriseDetail, getEnterprisePositions, getEnterprisePromotions, getEnterpriseRatings } from '@/api/enterprise'
import { createChatSession } from '@/api/message'

const router = useRouter()
const route = useRoute()
const enterpriseId = ref(route.params.id)
const loading = ref(true)
const enterpriseData = ref(null)
const jobList = ref([])
const promotionList = ref([])
const ratingList = ref([])
const reportDialogVisible = ref(false)

// 举报表单
const reportForm = reactive({
  reason: '',
  description: '',
  evidence: []
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

// 获取企业职位
const fetchEnterpriseJobs = async () => {
  try {
    const params = {
      pageNum: 1,
      pageSize: 5
    }
    const res = await getEnterprisePositions(enterpriseId.value, params)
    jobList.value = res.list || []
  } catch (error) {
    console.error('获取企业职位失败:', error)
    ElMessage.error('获取企业职位失败')
  }
}

// 获取企业宣传内容
const fetchEnterprisePromotions = async () => {
  try {
    const params = {
      pageNum: 1,
      pageSize: 3
    }
    const res = await getEnterprisePromotions(enterpriseId.value, params)
    promotionList.value = res.list || []
  } catch (error) {
    console.error('获取企业宣传内容失败:', error)
    ElMessage.error('获取企业宣传内容失败')
  }
}

// 获取企业评分
const fetchEnterpriseRatings = async () => {
  try {
    const params = {
      pageNum: 1,
      pageSize: 3,
      sortBy: 'time'
    }
    const res = await getEnterpriseRatings(enterpriseId.value, params)
    ratingList.value = res.list || []
  } catch (error) {
    console.error('获取企业评分失败:', error)
    ElMessage.error('获取企业评分失败')
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 添加http前缀
const addHttpPrefix = (url) => {
  if (!url) return ''
  return url.startsWith('http') ? url : `http://${url}`
}

// 查看职位详情
const viewJobDetail = (jobId) => {
  router.push(`/job/position/${jobId}`)
}

// 查看宣传内容详情
const viewPromotion = (promotionId) => {
  router.push(`/enterprise/promotion/${promotionId}`)
}

// 查看所有职位
const showAllJobs = () => {
  router.push(`/job/positions?enterpriseId=${enterpriseId.value}`)
}

// 查看所有宣传内容
const showAllPromotions = () => {
  router.push(`/enterprise/promotions/${enterpriseId.value}`)
}

// 开始聊天
const startChat = async () => {
  try {
    // 创建会话
    const session = await createChatSession({
      targetType: 'enterprise',
      targetId: enterpriseId.value
    })
    
    // 跳转到聊天页面
    router.push(`/message/chat/${session.id}`)
  } catch (error) {
    console.error('创建聊天会话失败:', error)
    ElMessage.error('创建聊天会话失败')
  }
}

// 举报企业
const reportEnterprise = () => {
  reportForm.reason = ''
  reportForm.description = ''
  reportForm.evidence = []
  reportDialogVisible.value = true
}

// 提交举报
const submitReport = () => {
  if (!reportForm.reason) {
    ElMessage.warning('请选择举报原因')
    return
  }
  
  if (!reportForm.description) {
    ElMessage.warning('请填写详细说明')
    return
  }
  
  // 这里实际上应该调用API提交举报
  ElMessage.success('举报已提交，我们会尽快处理')
  reportDialogVisible.value = false
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
      await Promise.all([
        fetchEnterpriseJobs(),
        fetchEnterprisePromotions(),
        fetchEnterpriseRatings()
      ])
    }
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.enterprise-detail-container {
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
    flex-wrap: wrap;
    
    .enterprise-logo {
      margin-right: 20px;
    }
    
    .enterprise-info {
      flex: 1;
      
      .name-status {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        
        .enterprise-name {
          margin: 0 10px 0 0;
          font-size: 24px;
          font-weight: bold;
        }
      }
      
      .enterprise-meta {
        margin-bottom: 10px;
        color: #606266;
      }
      
      .enterprise-contact {
        color: #606266;
        
        .contact-item {
          display: flex;
          align-items: center;
          margin-bottom: 5px;
          
          .el-icon {
            margin-right: 5px;
          }
          
          a {
            color: #409EFF;
            text-decoration: none;
            
            &:hover {
              text-decoration: underline;
            }
          }
        }
      }
    }
    
    .enterprise-actions {
      display: flex;
      gap: 10px;
      margin-top: 20px;
      
      @media (max-width: 768px) {
        width: 100%;
        justify-content: space-between;
      }
    }
    
    @media (max-width: 576px) {
      flex-direction: column;
      align-items: center;
      
      .enterprise-logo {
        margin-right: 0;
        margin-bottom: 15px;
      }
      
      .enterprise-info {
        width: 100%;
        text-align: center;
        margin-bottom: 15px;
        
        .name-status {
          justify-content: center;
        }
        
        .enterprise-meta {
          justify-content: center;
        }
        
        .enterprise-contact {
          .contact-item {
            justify-content: center;
          }
        }
      }
      
      .enterprise-actions {
        width: 100%;
      }
    }
  }
}

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

.description-card, .jobs-card, .promotion-card {
  margin-bottom: 20px;
  
  .description-content {
    white-space: pre-line;
    line-height: 1.6;
  }
}

.job-item {
  padding: 15px 0;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background-color: #F5F7FA;
  }
  
  .job-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .job-title {
      font-size: 16px;
      font-weight: bold;
      color: #409EFF;
    }
    
    .job-salary {
      color: #F56C6C;
      font-weight: bold;
    }
  }
  
  .job-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    margin-bottom: 8px;
  }
  
  .job-meta {
    color: #909399;
    font-size: 12px;
  }
}

.promotion-item {
  padding: 15px 0;
  border-bottom: 1px solid #EBEEF5;
  cursor: pointer;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background-color: #F5F7FA;
  }
  
  .promotion-title {
    font-size: 16px;
    font-weight: bold;
    margin: 0 0 10px 0;
    color: #303133;
  }
  
  .promotion-content {
    margin-bottom: 10px;
    color: #606266;
    line-height: 1.6;
  }
  
  .promotion-meta {
    display: flex;
    justify-content: space-between;
    color: #909399;
    font-size: 12px;
    
    .view-count {
      display: flex;
      align-items: center;
      
      .el-icon {
        margin-right: 5px;
      }
    }
  }
}

.rating-card {
  margin-bottom: 20px;
  
  .rating-overview {
    display: flex;
    justify-content: center;
    padding: 20px 0;
    
    .rating-score {
      text-align: center;
      
      .score-value {
        font-size: 36px;
        font-weight: bold;
        color: #FF9900;
      }
      
      .score-stars {
        margin: 10px 0;
      }
      
      .rating-count {
        color: #909399;
      }
    }
  }
  
  .rating-list {
    .rating-item {
      margin-bottom: 15px;
      padding-bottom: 15px;
      border-bottom: 1px solid #EBEEF5;
      
      &:last-child {
        margin-bottom: 0;
        padding-bottom: 0;
        border-bottom: none;
      }
      
      .rating-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .user-info {
          display: flex;
          align-items: center;
          
          .user-name {
            margin-left: 8px;
            font-weight: bold;
          }
        }
      }
      
      .rating-content {
        margin-bottom: 8px;
        line-height: 1.6;
        color: #606266;
      }
      
      .rating-time {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.actions-card {
  .quick-actions {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }
}

.evidence-uploader {
  :deep(.el-upload--picture-card) {
    width: 100px;
    height: 100px;
    line-height: 100px;
  }
}
</style> 