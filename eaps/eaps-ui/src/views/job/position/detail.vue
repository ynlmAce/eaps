<template>
  <div class="position-detail-container" v-loading="loading">
    <template v-if="!loading && !notFound">
      <!-- 顶部返回按钮 -->
      <div class="back-btn">
        <el-button icon="Back" @click="goBack">返回列表</el-button>
      </div>
      
      <!-- 主要内容区域 -->
      <el-row :gutter="20">
        <!-- 左侧内容区域 -->
        <el-col :span="16">
          <!-- 职位信息卡片 -->
          <el-card class="position-card" shadow="never">
            <div class="position-header">
              <div class="position-title-container">
                <h1 class="position-title">{{ position.positionName }}</h1>
                <el-tag v-if="position.urgent" type="danger">急聘</el-tag>
              </div>
              <div class="position-salary">{{ position.salaryRange }}</div>
            </div>
            
            <div class="position-meta">
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>{{ position.location }}</span>
              </div>
              <div class="meta-item">
                <el-icon><OfficeBuilding /></el-icon>
                <span>{{ position.enterpriseName }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Briefcase /></el-icon>
                <span>{{ position.experience }}</span>
              </div>
              <div class="meta-item">
                <el-icon><GraduationCap /></el-icon>
                <span>{{ position.education }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Calendar /></el-icon>
                <span>发布于 {{ position.publishTime }}</span>
              </div>
            </div>
            
            <div class="position-tags">
              <el-tag v-for="(tag, index) in position.tags" :key="index" class="tag-item" effect="plain">{{ tag }}</el-tag>
            </div>
            
            <el-divider />
            
            <div class="action-container">
              <el-button type="primary" size="large" @click="handleApply">申请职位</el-button>
              <el-button type="default" size="large" :icon="isCollected ? 'Star' : 'StarFilled'" 
                       :class="{ 'is-collected': isCollected }"
                       @click="toggleCollect">
                {{ isCollected ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </el-card>
          
          <!-- 职位详情卡片 -->
          <el-card class="detail-card" shadow="never">
            <template #header>
              <div class="card-header">
                <h2>职位描述</h2>
              </div>
            </template>
            
            <div class="section-title">岗位职责</div>
            <div class="section-content" v-html="position.responsibilities"></div>
            
            <div class="section-title">任职要求</div>
            <div class="section-content" v-html="position.requirements"></div>
            
            <div v-if="position.benefits" class="section-title">福利待遇</div>
            <div v-if="position.benefits" class="section-content" v-html="position.benefits"></div>
            
            <div v-if="position.additionalInfo" class="section-title">其他信息</div>
            <div v-if="position.additionalInfo" class="section-content" v-html="position.additionalInfo"></div>
          </el-card>
          
          <!-- 工作地点卡片 -->
          <el-card class="location-card" shadow="never">
            <template #header>
              <div class="card-header">
                <h2>工作地点</h2>
              </div>
            </template>
            
            <div class="location-text">{{ position.detailedLocation }}</div>
            <div id="map-container" class="map-container"></div>
          </el-card>
        </el-col>
        
        <!-- 右侧企业信息区域 -->
        <el-col :span="8">
          <!-- 企业信息卡片 -->
          <el-card class="enterprise-card" shadow="never">
            <div class="enterprise-header">
              <el-avatar :size="60" :src="enterprise.logo || defaultLogo"></el-avatar>
              <div class="enterprise-info">
                <h3 class="enterprise-name">{{ enterprise.enterpriseName }}</h3>
                <div class="enterprise-meta">
                  <span>{{ enterprise.industry }}</span>
                  <el-divider direction="vertical" />
                  <span>{{ enterprise.scale }}</span>
                  <el-divider direction="vertical" />
                  <span>{{ enterprise.type }}</span>
                </div>
              </div>
            </div>
            
            <el-divider />
            
            <div class="enterprise-about">
              <h4>企业简介</h4>
              <p class="enterprise-description">{{ enterprise.description }}</p>
            </div>
            
            <el-divider />
            
            <div class="enterprise-rating">
              <h4>企业评分</h4>
              <div class="rating-content">
                <div class="rating-stars">
                  <el-rate v-model="enterprise.rating" disabled text-color="#ff9900" />
                  <span class="rating-value">{{ enterprise.rating }}</span>
                </div>
                <div class="rating-count">{{ enterprise.ratingCount }}人评价</div>
              </div>
            </div>
            
            <el-divider />
            
            <div class="enterprise-actions">
              <el-button type="primary" plain @click="viewEnterpriseDetail">查看企业主页</el-button>
              <el-button type="info" plain @click="showEnterpriseJobs">查看该企业全部职位</el-button>
            </div>
          </el-card>
          
          <!-- 相似职位推荐卡片 -->
          <el-card class="similar-jobs-card" shadow="never">
            <template #header>
              <div class="card-header">
                <h2>相似职位推荐</h2>
              </div>
            </template>
            
            <div v-if="similarJobs.length === 0" class="empty-data">
              暂无相似职位
            </div>
            <div v-else class="similar-jobs-list">
              <div v-for="job in similarJobs" :key="job.id" class="similar-job-item" @click="viewJobDetail(job.id)">
                <div class="job-info">
                  <div class="job-title">{{ job.positionName }}</div>
                  <div class="job-company">{{ job.enterpriseName }}</div>
                </div>
                <div class="job-salary">{{ job.salaryRange }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
    
    <!-- 职位不存在提示 -->
    <el-empty v-if="notFound" description="该职位不存在或已下线">
      <el-button type="primary" @click="goToJobList">返回职位列表</el-button>
    </el-empty>
    
    <!-- 申请职位对话框 -->
    <el-dialog v-model="dialogVisible" title="申请职位" width="600px">
      <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="applyForm.name" placeholder="请输入姓名" />
        </el-form-item>
        
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="applyForm.phone" placeholder="请输入手机号码" />
        </el-form-item>
        
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="applyForm.email" placeholder="请输入电子邮箱" />
        </el-form-item>
        
        <el-form-item label="简历" prop="resume">
          <el-upload
            class="resume-uploader"
            action="/api/upload/resume"
            :on-success="handleResumeSuccess"
            :on-error="handleResumeError"
            :before-upload="beforeResumeUpload"
            :limit="1"
            :file-list="resumeFileList"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">只能上传PDF或DOC/DOCX文件，且不超过10MB</div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="求职信" prop="coverLetter">
          <el-input
            v-model="applyForm.coverLetter"
            type="textarea"
            :rows="4"
            placeholder="请简要介绍您为什么适合这个职位（选填）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApplication" :loading="submitting">提交申请</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, OfficeBuilding, Briefcase, GraduationCap, Calendar, Star, StarFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const defaultLogo = '@/assets/default-logo.png'

// 页面状态
const loading = ref(true)
const notFound = ref(false)
const isCollected = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)

// 职位信息
const position = ref({
  id: 0,
  positionName: '',
  enterpriseName: '',
  salaryRange: '',
  location: '',
  experience: '',
  education: '',
  publishTime: '',
  tags: [],
  urgent: false,
  responsibilities: '',
  requirements: '',
  benefits: '',
  additionalInfo: '',
  detailedLocation: ''
})

// 企业信息
const enterprise = ref({
  id: 0,
  enterpriseName: '',
  logo: '',
  industry: '',
  scale: '',
  type: '',
  description: '',
  rating: 0,
  ratingCount: 0
})

// 相似职位
const similarJobs = ref([])

// 申请表单
const applyFormRef = ref(null)
const applyForm = reactive({
  name: '',
  phone: '',
  email: '',
  resumeId: '',
  coverLetter: ''
})
const resumeFileList = ref([])

// 申请表单校验规则
const applyRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的电子邮箱', trigger: 'blur' }
  ],
  resumeId: [
    { required: true, message: '请上传简历', trigger: 'change' }
  ]
}

// 获取职位详情
async function getPositionDetail() {
  loading.value = true
  try {
    // 这里应调用实际的API，暂时使用模拟数据
    setTimeout(() => {
      // 模拟数据
      position.value = {
        id: 1,
        positionName: '前端开发工程师',
        enterpriseName: '科技有限公司',
        salaryRange: '15k-20k',
        location: '北京市 海淀区',
        experience: '3-5年',
        education: '本科及以上',
        publishTime: '2023-05-01',
        tags: ['React', 'Vue', 'JavaScript', 'HTML5', 'CSS3', 'TypeScript'],
        urgent: true,
        responsibilities: `<p>1. 负责公司产品的前端开发工作，实现产品UI和交互方面的开发需求；</p>
                          <p>2. 参与产品需求分析，提出合理化建议，并参与技术方案设计；</p>
                          <p>3. 根据产品需求完成高质量的前端开发和页面重构工作；</p>
                          <p>4. 优化前端架构，提升用户体验，改进代码质量；</p>
                          <p>5. 与后端开发、产品经理协同工作，推动工作进展；</p>
                          <p>6. 研究主流前端框架和技术，有能力进行技术选型。</p>`,
        requirements: `<p>1. 本科及以上学历，计算机相关专业，3-5年前端开发经验；</p>
                       <p>2. 熟练掌握HTML、CSS、JavaScript等前端技术，熟悉W3C标准；</p>
                       <p>3. 熟练掌握Vue.js、React等主流前端框架，有实际项目经验；</p>
                       <p>4. 熟悉Webpack、Vite等构建工具，了解前端工程化；</p>
                       <p>5. 熟悉RESTful API设计规范，了解前后端分离开发模式；</p>
                       <p>6. 对前端性能优化、浏览器兼容性、用户体验有深入理解；</p>
                       <p>7. 有良好的编码习惯，注重代码质量和可维护性；</p>
                       <p>8. 具有良好的团队合作精神和沟通能力，能够主动解决问题。</p>`,
        benefits: `<p>1. 具有竞争力的薪资和年终奖；</p>
                   <p>2. 完善的五险一金、补充商业保险；</p>
                   <p>3. 弹性工作时间、免费午餐；</p>
                   <p>4. 定期团建、节日福利；</p>
                   <p>5. 技术分享会、培训机会；</p>
                   <p>6. 扁平化管理、开放的工作环境。</p>`,
        additionalInfo: `<p>我们正在寻找热爱技术、积极上进的前端开发工程师加入我们的团队。如果你热爱挑战，愿意与优秀的人共事，期待你的加入！</p>`,
        detailedLocation: '北京市海淀区西二旗创业路29号院3号楼'
      }
      
      enterprise.value = {
        id: 1,
        enterpriseName: '科技有限公司',
        logo: '',
        industry: '互联网',
        scale: '500-1000人',
        type: '民营企业',
        description: '科技有限公司成立于2015年，是一家专注于人工智能和大数据领域的科技企业。公司致力于为企业和个人提供智能化解决方案，帮助客户提升效率、降低成本。目前已获得多轮融资，估值超过10亿元，处于快速发展阶段。',
        rating: 4.5,
        ratingCount: 127
      }
      
      similarJobs.value = [
        {
          id: 10,
          positionName: '高级前端开发工程师',
          enterpriseName: '互联网科技有限公司',
          salaryRange: '20k-30k'
        },
        {
          id: 11,
          positionName: '前端架构师',
          enterpriseName: '数字科技有限公司',
          salaryRange: '30k-40k'
        },
        {
          id: 12,
          positionName: 'Web前端开发工程师',
          enterpriseName: '创新科技有限公司',
          salaryRange: '15k-25k'
        },
        {
          id: 13,
          positionName: '全栈开发工程师',
          enterpriseName: '智能科技有限公司',
          salaryRange: '18k-28k'
        }
      ]
      
      loading.value = false
    }, 1000)
  } catch (error) {
    console.error('获取职位详情失败:', error)
    notFound.value = true
    loading.value = false
  }
}

// 收藏/取消收藏职位
function toggleCollect() {
  isCollected.value = !isCollected.value
  if (isCollected.value) {
    ElMessage.success('收藏成功')
  } else {
    ElMessage.success('已取消收藏')
  }
}

// 返回上一页
function goBack() {
  router.back()
}

// 跳转到职位列表页
function goToJobList() {
  router.push('/job/position')
}

// 查看企业详情
function viewEnterpriseDetail() {
  router.push(`/enterprise/detail/${enterprise.value.id}`)
}

// 查看企业全部职位
function showEnterpriseJobs() {
  router.push({
    path: '/job/position',
    query: { enterpriseId: enterprise.value.id }
  })
}

// 查看相似职位详情
function viewJobDetail(id) {
  router.push(`/job/position/${id}`)
}

// 申请职位
function handleApply() {
  // 检查用户是否登录
  const isLoggedIn = true // 实际应检查登录状态
  
  if (!isLoggedIn) {
    ElMessageBox.confirm(
      '申请职位需要先登录，是否前往登录页面？',
      '提示',
      {
        confirmButtonText: '前往登录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push({
        path: '/login',
        query: { redirect: route.fullPath }
      })
    }).catch(() => {})
    return
  }
  
  // 初始化申请表单，实际应用中可能需要从用户信息中预填充
  applyForm.name = '张三' // 示例
  applyForm.phone = '13800138000' // 示例
  applyForm.email = 'zhangsan@example.com' // 示例
  applyForm.resumeId = ''
  applyForm.coverLetter = ''
  resumeFileList.value = []
  
  dialogVisible.value = true
}

// 简历上传相关方法
function handleResumeSuccess(response) {
  applyForm.resumeId = response.data.resumeId
  ElMessage.success('简历上传成功')
}

function handleResumeError() {
  ElMessage.error('简历上传失败，请重试')
}

function beforeResumeUpload(file) {
  const validTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  const isValidType = validTypes.includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isValidType) {
    ElMessage.error('简历只能是PDF或Word格式！')
  }
  if (!isLt10M) {
    ElMessage.error('简历大小不能超过10MB！')
  }
  
  return isValidType && isLt10M
}

// 提交申请
function submitApplication() {
  applyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      
      // 这里应调用实际的API提交申请
      setTimeout(() => {
        submitting.value = false
        dialogVisible.value = false
        ElMessage.success('申请提交成功！')
      }, 1500)
    }
  })
}

// 页面加载时获取职位详情
onMounted(() => {
  const positionId = route.params.id
  if (!positionId) {
    notFound.value = true
    loading.value = false
    return
  }
  
  getPositionDetail()
  
  // 这里可以初始化地图，实际应用中可能需要引入地图SDK
})
</script>

<style lang="scss" scoped>
.position-detail-container {
  padding: 20px;
}

.back-btn {
  margin-bottom: 20px;
}

.position-card {
  margin-bottom: 20px;
  
  .position-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 15px;
    
    .position-title-container {
      display: flex;
      align-items: center;
      
      .position-title {
        margin: 0;
        font-size: 24px;
        font-weight: bold;
        margin-right: 10px;
      }
    }
    
    .position-salary {
      color: #f56c6c;
      font-size: 20px;
      font-weight: bold;
    }
  }
  
  .position-meta {
    display: flex;
    flex-wrap: wrap;
    margin-bottom: 15px;
    
    .meta-item {
      display: flex;
      align-items: center;
      margin-right: 20px;
      margin-bottom: 10px;
      color: #606266;
      
      .el-icon {
        margin-right: 5px;
      }
    }
  }
  
  .position-tags {
    margin-bottom: 15px;
    
    .tag-item {
      margin-right: 10px;
      margin-bottom: 10px;
    }
  }
  
  .action-container {
    display: flex;
    gap: 15px;
    
    .is-collected {
      background-color: #FEF6E9;
      color: #E6A23C;
      border-color: #E6A23C;
    }
  }
}

.detail-card, .location-card {
  margin-bottom: 20px;
  
  .card-header {
    h2 {
      margin: 0;
      font-size: 18px;
      font-weight: bold;
    }
  }
  
  .section-title {
    font-size: 16px;
    font-weight: bold;
    margin: 15px 0 10px;
    color: #303133;
  }
  
  .section-content {
    margin-bottom: 20px;
    color: #606266;
    line-height: 1.6;
    
    &:deep(p) {
      margin: 8px 0;
    }
  }
}

.location-card {
  .location-text {
    margin-bottom: 15px;
    color: #606266;
  }
  
  .map-container {
    height: 300px;
    background-color: #f5f7fa;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #909399;
    font-size: 14px;
  }
}

.enterprise-card {
  margin-bottom: 20px;
  
  .enterprise-header {
    display: flex;
    margin-bottom: 15px;
    
    .enterprise-info {
      margin-left: 15px;
      
      .enterprise-name {
        margin: 0 0 8px;
        font-size: 18px;
        font-weight: bold;
      }
      
      .enterprise-meta {
        color: #606266;
        font-size: 14px;
      }
    }
  }
  
  .enterprise-about, .enterprise-rating {
    margin-bottom: 15px;
    
    h4 {
      margin: 0 0 10px;
      font-size: 16px;
      font-weight: bold;
    }
    
    .enterprise-description {
      color: #606266;
      line-height: 1.6;
      margin: 0;
    }
    
    .rating-content {
      display: flex;
      align-items: center;
      justify-content: space-between;
      
      .rating-stars {
        display: flex;
        align-items: center;
        
        .rating-value {
          margin-left: 8px;
          font-size: 16px;
          font-weight: bold;
          color: #ff9900;
        }
      }
      
      .rating-count {
        color: #909399;
        font-size: 14px;
      }
    }
  }
  
  .enterprise-actions {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
}

.similar-jobs-card {
  .similar-jobs-list {
    .similar-job-item {
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
        background-color: #F5F7FA;
      }
      
      .job-info {
        .job-title {
          font-weight: bold;
          margin-bottom: 5px;
          color: #303133;
        }
        
        .job-company {
          font-size: 13px;
          color: #909399;
        }
      }
      
      .job-salary {
        color: #f56c6c;
        font-weight: bold;
      }
    }
  }
}

.empty-data {
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 14px;
}

.resume-uploader {
  :deep(.el-upload-list__item) {
    transition: all 0.3s;
  }
}
</style> 