<template>
  <div class="position-detail-container">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="!positionData" class="not-found">
      <el-empty description="职位不存在或已下线" />
      <el-button type="primary" @click="router.push('/job/positions')">返回职位列表</el-button>
    </div>
    <div v-else>
      <!-- 返回按钮 -->
      <div class="back-btn">
        <el-button @click="goBack" plain>
          <el-icon><ArrowLeft /></el-icon>返回
        </el-button>
      </div>

      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
          <!-- 职位基本信息 -->
          <el-card class="position-card">
            <div class="position-header">
              <h1 class="position-title">{{ positionData.name }}</h1>
              <div class="position-salary">{{ positionData.salaryLower }}k-{{ positionData.salaryUpper }}k·{{ positionData.salaryCount }}薪</div>
            </div>
            
            <div class="position-meta">
              <div class="meta-item">
                <el-icon><Location /></el-icon>
                <span>{{ positionData.workLocation }}</span>
              </div>
              <div class="meta-item">
                <el-icon><User /></el-icon>
                <span>经验{{ positionData.experienceRequirement }}</span>
              </div>
              <div class="meta-item">
                <el-icon><School /></el-icon>
                <span>{{ positionData.educationRequirement }}</span>
              </div>
              <div class="meta-item">
                <el-icon><Time /></el-icon>
                <span>{{ formatDate(positionData.publishTime) }}发布</span>
              </div>
            </div>
            
            <div class="position-tags" v-if="positionData.tags && positionData.tags.length > 0">
              <el-tag v-for="tag in positionData.tags" :key="tag" size="small">{{ tag }}</el-tag>
            </div>
            
            <div class="apply-btn-desktop">
              <el-button type="primary" size="large" @click="applyPosition" :disabled="positionData.status !== 'active'">
                {{ positionData.status === 'active' ? '立即申请' : '该职位已关闭' }}
              </el-button>
              <el-button size="large" @click="collectPosition">
                <el-icon><Star /></el-icon>收藏职位
              </el-button>
            </div>
          </el-card>

          <!-- 职位描述 -->
          <el-card class="description-card">
            <template #header>
              <div class="card-header">
                <h2>职位描述</h2>
              </div>
            </template>
            <div v-if="positionData.description" class="position-description">
              <div v-html="formatContent(positionData.description)"></div>
            </div>
            <el-empty v-else description="暂无职位描述" />
          </el-card>

          <!-- 职位要求 -->
          <el-card class="requirements-card">
            <template #header>
              <div class="card-header">
                <h2>职位要求</h2>
              </div>
            </template>
            <div v-if="positionData.requirements" class="position-requirements">
              <div v-html="formatContent(positionData.requirements)"></div>
            </div>
            <el-empty v-else description="暂无职位要求" />
          </el-card>

          <!-- 工作地点 -->
          <el-card class="location-card" v-if="positionData.workLocation">
            <template #header>
              <div class="card-header">
                <h2>工作地点</h2>
              </div>
            </template>
            <div class="location-info">
              <div class="location-address">{{ positionData.workLocation }}</div>
              <div class="location-detail" v-if="positionData.locationDetail">{{ positionData.locationDetail }}</div>
              <!-- 可以在这里集成地图组件 -->
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <!-- 申请职位固定卡片（移动端显示在顶部，桌面端显示在右侧固定） -->
          <div class="apply-card-wrapper" :class="{ 'is-fixed': isApplyCardFixed }">
            <el-card class="apply-card">
              <div class="apply-card-content">
                <div class="position-info">
                  <div class="position-name">{{ positionData.name }}</div>
                  <div class="position-salary">{{ positionData.salaryLower }}k-{{ positionData.salaryUpper }}k</div>
                </div>
                <div class="apply-buttons">
                  <el-button type="primary" @click="applyPosition" :disabled="positionData.status !== 'active'" block>
                    {{ positionData.status === 'active' ? '立即申请' : '该职位已关闭' }}
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>

          <!-- 企业信息 -->
          <el-card class="enterprise-card">
            <template #header>
              <div class="card-header">
                <h2>企业信息</h2>
                <el-button type="primary" link @click="viewEnterpriseDetail">查看详情</el-button>
              </div>
            </template>
            <div class="enterprise-info">
              <div class="enterprise-header">
                <el-avatar :size="60" :src="positionData.enterpriseLogo || '/default-logo.png'" class="enterprise-logo" />
                <div class="enterprise-name-type">
                  <div class="enterprise-name" @click="viewEnterpriseDetail">{{ positionData.enterpriseName }}</div>
                  <div class="enterprise-type">{{ positionData.enterpriseType }}</div>
                </div>
              </div>
              <div class="enterprise-meta">
                <div class="meta-row">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>{{ positionData.enterpriseIndustry }}</span>
                </div>
                <div class="meta-row">
                  <el-icon><User /></el-icon>
                  <span>{{ positionData.enterpriseScale }}</span>
                </div>
                <div class="meta-row" v-if="positionData.enterpriseLocation">
                  <el-icon><Location /></el-icon>
                  <span>{{ positionData.enterpriseLocation }}</span>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 相似职位 -->
          <el-card class="similar-positions-card" v-if="similarPositions.length > 0">
            <template #header>
              <div class="card-header">
                <h2>相似职位</h2>
                <el-button type="primary" link @click="viewMorePositions">查看更多</el-button>
              </div>
            </template>
            <div class="similar-positions-list">
              <div 
                v-for="position in similarPositions" 
                :key="position.id" 
                class="similar-position-item"
                @click="viewPositionDetail(position.id)"
              >
                <div class="position-info">
                  <div class="position-name">{{ position.name }}</div>
                  <div class="position-salary">{{ position.salaryLower }}k-{{ position.salaryUpper }}k</div>
                </div>
                <div class="position-company">{{ position.enterpriseName }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 申请表单对话框 -->
      <el-dialog v-model="applyDialogVisible" title="申请职位" width="500px">
        <el-form :model="applyForm" :rules="applyRules" ref="applyFormRef" label-width="100px">
          <el-form-item label="选择简历" prop="resumeId">
            <el-select v-model="applyForm.resumeId" placeholder="请选择您的简历" style="width: 100%">
              <el-option 
                v-for="resume in resumeList" 
                :key="resume.id" 
                :label="resume.name" 
                :value="resume.id"
              >
                <div class="resume-option">
                  <span>{{ resume.name }}</span>
                  <el-tag size="small" type="success" v-if="resume.isDefault">默认</el-tag>
                </div>
              </el-option>
            </el-select>
            <div class="form-tip" v-if="resumeList.length === 0">
              您还没有创建简历，请先<el-link type="primary" @click="createResume">创建简历</el-link>
            </div>
          </el-form-item>
          
          <el-form-item label="求职信" prop="coverLetter">
            <el-input 
              v-model="applyForm.coverLetter" 
              type="textarea" 
              :rows="5" 
              placeholder="请简要介绍您为什么适合这个职位..."
              maxlength="2000"
              show-word-limit
            />
          </el-form-item>
          
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="applyForm.phone" placeholder="请输入联系电话" />
          </el-form-item>
          
          <el-form-item label="联系邮箱" prop="email">
            <el-input v-model="applyForm.email" placeholder="请输入联系邮箱" />
          </el-form-item>
        </el-form>
        
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="applyDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitApplication" :loading="submitting">提交申请</el-button>
          </span>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Location, User, School, Time, Star, OfficeBuilding } from '@element-plus/icons-vue'
import { getJobPositionDetail, applyJobPosition } from '@/api/job'
import { getResumeList } from '@/api/resume'
import { getUserInfo } from '@/utils/auth'

const router = useRouter()
const route = useRoute()
const positionId = ref(route.params.id)
const loading = ref(true)
const positionData = ref(null)
const similarPositions = ref([])
const resumeList = ref([])
const applyDialogVisible = ref(false)
const submitting = ref(false)
const applyFormRef = ref(null)
const isApplyCardFixed = ref(false)

// 申请表单
const applyForm = reactive({
  resumeId: '',
  coverLetter: '',
  phone: '',
  email: ''
})

// 表单验证规则
const applyRules = {
  resumeId: [
    { required: true, message: '请选择简历', trigger: 'change' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入联系邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 获取职位详情
const fetchPositionDetail = async () => {
  try {
    const res = await getJobPositionDetail(positionId.value)
    positionData.value = res
    
    // 预填写用户信息
    const userInfo = getUserInfo()
    if (userInfo) {
      applyForm.phone = userInfo.phone || ''
      applyForm.email = userInfo.email || ''
    }
    
    // 获取相似职位
    fetchSimilarPositions()
  } catch (error) {
    console.error('获取职位详情失败:', error)
    ElMessage.error('获取职位详情失败')
  } finally {
    loading.value = false
  }
}

// 获取相似职位
const fetchSimilarPositions = async () => {
  try {
    // 实际应该调用接口获取相似职位
    // 模拟数据
    similarPositions.value = [
      {
        id: '2',
        name: '前端开发工程师',
        salaryLower: 15,
        salaryUpper: 25,
        enterpriseName: '某科技有限公司'
      },
      {
        id: '3',
        name: 'Web前端开发',
        salaryLower: 18,
        salaryUpper: 30,
        enterpriseName: '某网络科技公司'
      },
      {
        id: '4',
        name: '高级前端工程师',
        salaryLower: 25,
        salaryUpper: 40,
        enterpriseName: '某互联网公司'
      }
    ]
  } catch (error) {
    console.error('获取相似职位失败:', error)
  }
}

// 获取简历列表
const fetchResumeList = async () => {
  try {
    const res = await getResumeList()
    resumeList.value = res || []
    
    // 如果有默认简历，自动选择
    const defaultResume = resumeList.value.find(item => item.isDefault)
    if (defaultResume) {
      applyForm.resumeId = defaultResume.id
    } else if (resumeList.value.length > 0) {
      applyForm.resumeId = resumeList.value[0].id
    }
  } catch (error) {
    console.error('获取简历列表失败:', error)
    ElMessage.error('获取简历列表失败')
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

// 查看企业详情
const viewEnterpriseDetail = () => {
  if (!positionData.value || !positionData.value.enterpriseId) return
  router.push(`/enterprise/detail/${positionData.value.enterpriseId}`)
}

// 查看职位详情
const viewPositionDetail = (id) => {
  router.push(`/job/position/${id}`)
}

// 查看更多职位
const viewMorePositions = () => {
  if (!positionData.value || !positionData.value.enterpriseId) return
  router.push(`/job/positions?enterpriseId=${positionData.value.enterpriseId}`)
}

// 申请职位
const applyPosition = async () => {
  // 检查用户是否登录
  const userInfo = getUserInfo()
  if (!userInfo) {
    ElMessageBox.confirm('您需要登录才能申请职位', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push(`/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
    }).catch(() => {})
    return
  }
  
  // 打开申请对话框
  await fetchResumeList()
  applyDialogVisible.value = true
}

// 创建简历
const createResume = () => {
  applyDialogVisible.value = false
  router.push('/resume/create')
}

// 收藏职位
const collectPosition = () => {
  // 检查用户是否登录
  const userInfo = getUserInfo()
  if (!userInfo) {
    ElMessageBox.confirm('您需要登录才能收藏职位', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push(`/login?redirect=${encodeURIComponent(router.currentRoute.value.fullPath)}`)
    }).catch(() => {})
    return
  }
  
  // TODO: 实现收藏职位功能
  ElMessage.success('收藏成功')
}

// 提交申请
const submitApplication = async () => {
  if (!applyFormRef.value) return
  
  await applyFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const params = {
        positionId: positionId.value,
        resumeId: applyForm.resumeId,
        coverLetter: applyForm.coverLetter,
        phone: applyForm.phone,
        email: applyForm.email
      }
      
      await applyJobPosition(params)
      ElMessage.success('申请提交成功')
      applyDialogVisible.value = false
      
      // 提示用户查看申请状态
      ElMessageBox.confirm('您可以在"我的申请"中查看申请状态', '申请成功', {
        confirmButtonText: '查看我的申请',
        cancelButtonText: '继续浏览',
        type: 'success'
      }).then(() => {
        router.push('/job/my-applications')
      }).catch(() => {})
    } catch (error) {
      console.error('提交申请失败:', error)
      ElMessage.error('提交申请失败')
    } finally {
      submitting.value = false
    }
  })
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 处理页面滚动，固定申请卡片
const handleScroll = () => {
  const scrollTop = window.scrollY || document.documentElement.scrollTop
  isApplyCardFixed.value = scrollTop > 300
}

// 页面加载
onMounted(() => {
  if (!positionId.value) {
    ElMessage.error('职位ID不能为空')
    router.push('/job/positions')
    return
  }
  
  fetchPositionDetail()
  
  // 监听页面滚动
  window.addEventListener('scroll', handleScroll)
})

// 页面卸载
onUnmounted(() => {
  // 移除滚动监听
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style lang="scss" scoped>
.position-detail-container {
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

.position-card {
  margin-bottom: 20px;
  
  .position-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 15px;
    
    .position-title {
      margin: 0;
      font-size: 24px;
      font-weight: bold;
      color: #303133;
    }
    
    .position-salary {
      color: #f56c6c;
      font-size: 20px;
      font-weight: bold;
    }
    
    @media (max-width: 576px) {
      flex-direction: column;
      
      .position-salary {
        margin-top: 10px;
      }
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
    display: flex;
    flex-wrap: wrap;
    gap: 5px;
    margin-bottom: 20px;
  }
  
  .apply-btn-desktop {
    display: flex;
    gap: 10px;
    
    @media (max-width: 768px) {
      display: none;
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

.description-card, .requirements-card, .location-card {
  margin-bottom: 20px;
  
  .position-description, .position-requirements {
    line-height: 1.8;
    color: #606266;
    
    :deep(ul) {
      padding-left: 20px;
    }
  }
  
  .location-info {
    color: #606266;
    
    .location-address {
      font-weight: bold;
      margin-bottom: 10px;
    }
    
    .location-detail {
      line-height: 1.6;
    }
  }
}

.apply-card-wrapper {
  margin-bottom: 20px;
  
  &.is-fixed {
    .apply-card {
      position: sticky;
      top: 20px;
    }
  }
  
  .apply-card {
    .apply-card-content {
      .position-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;
        
        .position-name {
          font-size: 16px;
          font-weight: bold;
        }
        
        .position-salary {
          color: #f56c6c;
          font-weight: bold;
        }
      }
      
      .apply-buttons {
        margin-top: 10px;
      }
    }
  }
  
  @media (max-width: 768px) {
    .apply-card {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      z-index: 100;
      border-radius: 0;
      box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
    }
  }
}

.enterprise-card {
  margin-bottom: 20px;
  
  .enterprise-info {
    .enterprise-header {
      display: flex;
      align-items: center;
      margin-bottom: 15px;
      
      .enterprise-logo {
        margin-right: 15px;
      }
      
      .enterprise-name-type {
        .enterprise-name {
          font-size: 16px;
          font-weight: bold;
          color: #409EFF;
          margin-bottom: 5px;
          cursor: pointer;
          
          &:hover {
            text-decoration: underline;
          }
        }
        
        .enterprise-type {
          color: #909399;
          font-size: 14px;
        }
      }
    }
    
    .enterprise-meta {
      color: #606266;
      
      .meta-row {
        display: flex;
        align-items: center;
        margin-bottom: 8px;
        
        .el-icon {
          margin-right: 5px;
        }
      }
    }
  }
}

.similar-positions-card {
  .similar-positions-list {
    .similar-position-item {
      padding: 12px 0;
      border-bottom: 1px solid #EBEEF5;
      cursor: pointer;
      
      &:last-child {
        border-bottom: none;
      }
      
      &:hover {
        .position-info .position-name {
          color: #409EFF;
        }
      }
      
      .position-info {
        display: flex;
        justify-content: space-between;
        margin-bottom: 5px;
        
        .position-name {
          font-weight: bold;
          transition: color 0.3s;
        }
        
        .position-salary {
          color: #f56c6c;
        }
      }
      
      .position-company {
        color: #909399;
        font-size: 14px;
      }
    }
  }
}

.resume-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  margin-top: 5px;
  color: #909399;
  font-size: 14px;
}

/* 移动端样式调整 */
@media (max-width: 768px) {
  .position-detail-container {
    padding-bottom: 80px; /* 为底部固定的申请按钮留出空间 */
  }
}
</style> 