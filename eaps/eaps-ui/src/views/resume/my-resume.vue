<template>
  <div class="my-resume-container">
    <el-card shadow="never" class="header-card">
      <div class="page-header">
        <h2 class="page-title">我的简历</h2>
        <div class="action-buttons">
          <el-button type="primary" @click="showUploadDialog">上传新简历</el-button>
          <el-button @click="showCreateDialog">在线创建简历</el-button>
        </div>
      </div>
    </el-card>

    <div v-loading="loading">
      <!-- 简历列表 -->
      <div v-if="resumeList.length > 0" class="resume-list">
        <el-card v-for="resume in resumeList" :key="resume.id" shadow="hover" class="resume-item">
          <div class="resume-header">
            <div class="resume-info">
              <div class="resume-title">
                <span class="name">{{ resume.name }}</span>
                <el-tag v-if="resume.isDefault" type="success" size="small">默认简历</el-tag>
                <el-tag v-if="resume.uploadType === 'system'" type="info" size="small">系统简历</el-tag>
                <el-tag v-else type="warning" size="small">上传简历</el-tag>
              </div>
              <div class="resume-meta">
                <span class="meta-item">
                  <el-icon><Calendar /></el-icon>更新时间：{{ formatDate(resume.updateTime) }}
                </span>
                <span class="meta-item">
                  <el-icon><Document /></el-icon>类型：{{ resume.fileType ? resume.fileType.toUpperCase() : '系统简历' }}
                </span>
                <span class="meta-item" v-if="resume.fileSize">
                  <el-icon><Files /></el-icon>大小：{{ formatFileSize(resume.fileSize) }}
                </span>
              </div>
            </div>
            <div class="resume-preview">
              <el-image
                :src="resume.previewUrl || '/resume-default.png'"
                fit="cover"
                :preview-src-list="resume.previewUrl ? [resume.previewUrl] : []"
                :initial-index="0"
                class="preview-image"
              >
                <template #error>
                  <div class="preview-placeholder">
                    <el-icon><Document /></el-icon>
                    <span>预览不可用</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
          
          <div class="resume-content" v-if="resume.uploadType === 'system'">
            <div class="info-section">
              <h4>基本信息</h4>
              <div class="info-grid">
                <div class="info-item">
                  <span class="label">姓名：</span>
                  <span class="value">{{ resume.basicInfo.name }}</span>
                </div>
                <div class="info-item">
                  <span class="label">性别：</span>
                  <span class="value">{{ resume.basicInfo.gender }}</span>
                </div>
                <div class="info-item">
                  <span class="label">年龄：</span>
                  <span class="value">{{ resume.basicInfo.age }}</span>
                </div>
                <div class="info-item">
                  <span class="label">电话：</span>
                  <span class="value">{{ resume.basicInfo.phone }}</span>
                </div>
                <div class="info-item">
                  <span class="label">邮箱：</span>
                  <span class="value">{{ resume.basicInfo.email }}</span>
                </div>
                <div class="info-item">
                  <span class="label">居住地：</span>
                  <span class="value">{{ resume.basicInfo.location }}</span>
                </div>
              </div>
            </div>
            
            <div class="info-section">
              <h4>教育经历</h4>
              <div v-if="resume.education && resume.education.length > 0">
                <div v-for="(edu, index) in resume.education" :key="index" class="education-item">
                  <div class="edu-header">
                    <span class="edu-school">{{ edu.school }}</span>
                    <span class="edu-time">{{ edu.startTime }} - {{ edu.endTime }}</span>
                  </div>
                  <div class="edu-detail">
                    <span>{{ edu.degree }}</span> | <span>{{ edu.major }}</span>
                  </div>
                </div>
              </div>
              <div v-else class="empty-section">暂无教育经历</div>
            </div>
            
            <div class="info-section">
              <h4>工作/实习经历</h4>
              <div v-if="resume.experience && resume.experience.length > 0">
                <div v-for="(exp, index) in resume.experience" :key="index" class="experience-item">
                  <div class="exp-header">
                    <span class="exp-company">{{ exp.company }}</span>
                    <span class="exp-time">{{ exp.startTime }} - {{ exp.endTime }}</span>
                  </div>
                  <div class="exp-position">{{ exp.position }}</div>
                  <div class="exp-description">{{ exp.description }}</div>
                </div>
              </div>
              <div v-else class="empty-section">暂无工作/实习经历</div>
            </div>
            
            <div class="info-section">
              <h4>项目经历</h4>
              <div v-if="resume.projects && resume.projects.length > 0">
                <div v-for="(project, index) in resume.projects" :key="index" class="project-item">
                  <div class="project-header">
                    <span class="project-name">{{ project.name }}</span>
                    <span class="project-time">{{ project.startTime }} - {{ project.endTime }}</span>
                  </div>
                  <div class="project-role">{{ project.role }}</div>
                  <div class="project-description">{{ project.description }}</div>
                </div>
              </div>
              <div v-else class="empty-section">暂无项目经历</div>
            </div>
            
            <div class="info-section">
              <h4>技能与证书</h4>
              <div v-if="resume.skills || resume.certificates">
                <div v-if="resume.skills" class="skills-section">
                  <strong>技能：</strong>{{ resume.skills }}
                </div>
                <div v-if="resume.certificates" class="certificates-section">
                  <strong>证书：</strong>{{ resume.certificates }}
                </div>
              </div>
              <div v-else class="empty-section">暂无技能与证书</div>
            </div>
            
            <div class="info-section" v-if="resume.selfDescription">
              <h4>自我评价</h4>
              <div class="self-description">{{ resume.selfDescription }}</div>
            </div>
          </div>
          
          <div class="resume-actions">
            <el-button type="primary" @click="viewResume(resume)">查看</el-button>
            <el-button type="success" @click="downloadResumeFile(resume.id)" v-if="resume.uploadType === 'upload'">下载</el-button>
            <el-button @click="editResume(resume)" v-if="resume.uploadType === 'system'">编辑</el-button>
            <el-dropdown trigger="click">
              <el-button type="info">
                更多操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="setAsDefault(resume.id)" v-if="!resume.isDefault">设为默认</el-dropdown-item>
                  <el-dropdown-item @click="renameResumeItem(resume)">重命名</el-dropdown-item>
                  <el-dropdown-item @click="deleteResumeItem(resume.id)" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-card>
      </div>
      
      <!-- 无简历提示 -->
      <el-empty v-else description="您还没有创建或上传简历" class="empty-resume">
        <el-button type="primary" @click="showUploadDialog">上传简历</el-button>
        <el-button @click="showCreateDialog">创建简历</el-button>
      </el-empty>
    </div>
    
    <!-- 上传简历对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传简历" width="500px">
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="简历名称" required>
          <el-input v-model="uploadForm.name" placeholder="请输入简历名称" />
        </el-form-item>
        
        <el-form-item label="简历文件" required>
          <el-upload
            class="resume-uploader"
            action="/api/resume/upload"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :limit="1"
            :file-list="fileList"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
                只能上传PDF/DOC/DOCX文件，且不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="设为默认">
          <el-switch v-model="uploadForm.isDefault" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUpload" :loading="uploading">上传</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 重命名对话框 -->
    <el-dialog v-model="renameDialogVisible" title="重命名简历" width="400px">
      <el-form :model="renameForm" label-width="80px">
        <el-form-item label="新名称" required>
          <el-input v-model="renameForm.name" placeholder="请输入新名称" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="renameDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRename">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar, Document, Files, ArrowDown } from '@element-plus/icons-vue'
import { getResumeList, deleteResume, setDefaultResume, renameResume, uploadResumeFile, downloadResume as downloadResumeAPI } from '@/api/resume'

const router = useRouter()
const loading = ref(false)
const uploadDialogVisible = ref(false)
const renameDialogVisible = ref(false)
const fileList = ref([])
const uploading = ref(false)
const resumeList = ref([])

// 上传表单
const uploadForm = reactive({
  name: '',
  file: null,
  isDefault: false
})

// 重命名表单
const renameForm = reactive({
  id: null,
  name: ''
})

// 获取简历列表
const fetchResumeList = async () => {
  loading.value = true
  try {
    const res = await getResumeList()
    resumeList.value = res || []
  } catch (error) {
    console.error('获取简历列表失败:', error)
    ElMessage.error('获取简历列表失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (size < 1024) {
    return size + ' B'
  } else if (size < 1024 * 1024) {
    return (size / 1024).toFixed(2) + ' KB'
  } else if (size < 1024 * 1024 * 1024) {
    return (size / (1024 * 1024)).toFixed(2) + ' MB'
  }
  return (size / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

// 显示上传对话框
const showUploadDialog = () => {
  uploadForm.name = ''
  uploadForm.file = null
  uploadForm.isDefault = false
  fileList.value = []
  uploadDialogVisible.value = true
}

// 显示创建对话框
const showCreateDialog = () => {
  router.push('/resume/create')
}

// 文件选择处理
const handleFileChange = (file) => {
  uploadForm.file = file.raw
  if (!uploadForm.name && file.name) {
    // 自动设置简历名称为文件名（不带扩展名）
    const lastDot = file.name.lastIndexOf('.')
    if (lastDot > 0) {
      uploadForm.name = file.name.substring(0, lastDot)
    } else {
      uploadForm.name = file.name
    }
  }
}

// 文件移除处理
const handleFileRemove = () => {
  uploadForm.file = null
}

// 提交上传
const submitUpload = async () => {
  if (!uploadForm.name.trim()) {
    ElMessage.warning('请输入简历名称')
    return
  }
  
  if (!uploadForm.file) {
    ElMessage.warning('请选择简历文件')
    return
  }
  
  // 验证文件类型和大小
  const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document']
  if (!allowedTypes.includes(uploadForm.file.type)) {
    ElMessage.error('只能上传PDF或Word文件')
    return
  }
  
  if (uploadForm.file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }
  
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('name', uploadForm.name)
    formData.append('file', uploadForm.file)
    formData.append('isDefault', uploadForm.isDefault)
    
    await uploadResumeFile(formData)
    
    uploading.value = false
    uploadDialogVisible.value = false
    ElMessage.success('简历上传成功')
    await fetchResumeList()
  } catch (error) {
    console.error('上传简历失败:', error)
    ElMessage.error('上传简历失败')
    uploading.value = false
  }
}

// 查看简历
const viewResume = (resume) => {
  if (resume.uploadType === 'system') {
    router.push(`/resume/view/${resume.id}`)
  } else {
    // 对于上传的简历，可能需要在新窗口打开或下载查看
    if (resume.downloadUrl) {
      window.open(resume.downloadUrl, '_blank')
    } else {
      ElMessage.info('正在准备预览...')
      downloadResumeFile(resume.id)
    }
  }
}

// 下载简历
const downloadResumeFile = async (resumeId) => {
  try {
    const res = await downloadResumeAPI(resumeId)
    // 处理文件下载
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = resumeList.value.find(item => item.id === resumeId)?.name || '简历.pdf'
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    console.error('下载简历失败:', error)
    ElMessage.error('下载简历失败')
  }
}

// 编辑简历
const editResume = (resume) => {
  router.push(`/resume/edit/${resume.id}`)
}

// 设为默认简历
const setAsDefault = async (id) => {
  try {
    await setDefaultResume(id)
    
    resumeList.value.forEach(item => {
      item.isDefault = item.id === id
    })
    ElMessage.success('已设为默认简历')
  } catch (error) {
    console.error('设置默认简历失败:', error)
    ElMessage.error('设置默认简历失败')
  }
}

// 重命名简历
const renameResumeItem = (resume) => {
  renameForm.id = resume.id
  renameForm.name = resume.name
  renameDialogVisible.value = true
}

// 提交重命名
const submitRename = async () => {
  if (!renameForm.name.trim()) {
    ElMessage.warning('请输入简历名称')
    return
  }
  
  try {
    await renameResume(renameForm.id, renameForm.name)
    
    const resume = resumeList.value.find(item => item.id === renameForm.id)
    if (resume) {
      resume.name = renameForm.name
    }
    renameDialogVisible.value = false
    ElMessage.success('简历重命名成功')
  } catch (error) {
    console.error('重命名简历失败:', error)
    ElMessage.error('重命名简历失败')
  }
}

// 删除简历
const deleteResumeItem = async (id) => {
  ElMessageBox.confirm('确定要删除该简历吗？删除后不可恢复', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteResume(id)
      
      resumeList.value = resumeList.value.filter(item => item.id !== id)
      ElMessage.success('简历已删除')
    } catch (error) {
      console.error('删除简历失败:', error)
      ElMessage.error('删除简历失败')
    }
  }).catch(() => {})
}

// 页面加载时获取简历列表
onMounted(() => {
  fetchResumeList()
})
</script>

<style lang="scss" scoped>
.my-resume-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.header-card {
  margin-bottom: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .page-title {
      margin: 0;
      font-size: 20px;
      font-weight: bold;
    }
    
    .action-buttons {
      display: flex;
      gap: 10px;
    }
  }
}

.resume-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.resume-item {
  .resume-header {
    display: flex;
    margin-bottom: 15px;
    
    .resume-info {
      flex: 1;
      
      .resume-title {
        display: flex;
        align-items: center;
        margin-bottom: 10px;
        
        .name {
          font-size: 18px;
          font-weight: bold;
          margin-right: 10px;
        }
      }
      
      .resume-meta {
        color: #606266;
        
        .meta-item {
          display: inline-flex;
          align-items: center;
          margin-right: 15px;
          
          .el-icon {
            margin-right: 5px;
          }
        }
      }
    }
    
    .resume-preview {
      width: 100px;
      height: 140px;
      margin-left: 20px;
      
      .preview-image {
        width: 100%;
        height: 100%;
        border: 1px solid #e0e0e0;
        border-radius: 4px;
      }
      
      .preview-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        background-color: #f5f7fa;
        color: #909399;
        border: 1px solid #e0e0e0;
        border-radius: 4px;
        
        .el-icon {
          font-size: 24px;
          margin-bottom: 5px;
        }
      }
    }
  }
  
  .resume-content {
    margin-bottom: 20px;
    
    .info-section {
      margin-bottom: 15px;
      
      h4 {
        font-size: 16px;
        margin: 0 0 10px 0;
        padding-bottom: 5px;
        border-bottom: 1px solid #ebeef5;
      }
      
      .info-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 10px;
        
        .info-item {
          .label {
            color: #606266;
            font-weight: bold;
          }
        }
      }
      
      .education-item, .experience-item, .project-item {
        margin-bottom: 15px;
        
        .edu-header, .exp-header, .project-header {
          display: flex;
          justify-content: space-between;
          margin-bottom: 5px;
          
          .edu-school, .exp-company, .project-name {
            font-weight: bold;
          }
          
          .edu-time, .exp-time, .project-time {
            color: #909399;
          }
        }
        
        .edu-detail, .exp-position, .project-role {
          margin-bottom: 5px;
          color: #303133;
        }
        
        .exp-description, .project-description {
          color: #606266;
          line-height: 1.6;
        }
      }
      
      .skills-section, .certificates-section {
        margin-bottom: 10px;
        line-height: 1.6;
      }
      
      .self-description {
        line-height: 1.6;
        color: #606266;
      }
      
      .empty-section {
        color: #909399;
        font-style: italic;
        padding: 10px 0;
      }
    }
  }
  
  .resume-actions {
    display: flex;
    gap: 10px;
  }
}

.empty-resume {
  padding: 50px 0;
  
  :deep(.el-empty__description) {
    margin-bottom: 20px;
  }
}

.resume-uploader {
  width: 100%;
  
  :deep(.el-upload-list__item) {
    transition: all 0.3s;
  }
}
</style> 