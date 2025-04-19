<template>
  <div class="promotion-edit-container">
    <div class="page-header">
      <div class="back-button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </div>
      <div class="title">{{ isEdit ? '编辑宣传' : '发布宣传' }}</div>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <el-form
      v-else
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      class="promotion-form"
    >
      <el-form-item label="标题" prop="title">
        <el-input 
          v-model="formData.title" 
          placeholder="请输入宣传标题" 
          maxlength="100"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="封面图" prop="coverImage">
        <el-upload
          class="cover-upload"
          :action="uploadUrl"
          :show-file-list="false"
          :on-success="handleCoverSuccess"
          :before-upload="beforeCoverUpload"
          :on-error="handleUploadError"
        >
          <div v-if="formData.coverImage" class="cover-preview">
            <el-image :src="formData.coverImage" fit="cover" />
            <div class="cover-actions">
              <el-icon class="replace-icon"><EditPen /></el-icon>
              <span>更换封面</span>
            </div>
          </div>
          <el-button v-else type="primary">
            <el-icon><Upload /></el-icon>
            上传封面图
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              仅支持JPG/PNG格式，建议尺寸1200x630，不超过2MB
            </div>
          </template>
        </el-upload>
      </el-form-item>
      
      <el-form-item label="简介" prop="summary">
        <el-input
          v-model="formData.summary"
          type="textarea"
          :rows="3"
          placeholder="请输入宣传简介(150字以内)"
          maxlength="150"
          show-word-limit
        />
      </el-form-item>
      
      <el-form-item label="内容" prop="content">
        <div class="editor-container">
          <el-empty v-if="!isClientReady" description="编辑器加载中..." />
          <div v-else ref="editorRef" class="editor"></div>
        </div>
      </el-form-item>
      
      <el-form-item label="附件">
        <el-upload
          class="attachment-upload"
          :action="uploadUrl"
          :on-preview="handlePreview"
          :on-remove="handleRemove"
          :before-upload="beforeAttachmentUpload"
          :on-success="handleAttachmentSuccess"
          :on-error="handleUploadError"
          :file-list="attachmentList"
          multiple
          :limit="5"
        >
          <el-button type="primary">
            <el-icon><Upload /></el-icon>
            上传附件
          </el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持PDF/DOC/DOCX/XLS/XLSX/PPT/PPTX/TXT/ZIP/RAR格式，单个文件不超过10MB，最多5个附件
            </div>
          </template>
        </el-upload>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '保存修改' : '发布宣传' }}
        </el-button>
        <el-button @click="saveDraft" :loading="savingDraft">
          保存草稿
        </el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 预览对话框 -->
    <el-dialog v-model="previewDialogVisible" title="预览" width="80%" top="5vh">
      <div class="preview-container">
        <h1 class="preview-title">{{ formData.title }}</h1>
        <div v-if="formData.coverImage" class="preview-cover">
          <el-image :src="formData.coverImage" fit="cover" />
        </div>
        <div class="preview-summary">{{ formData.summary }}</div>
        <div class="preview-content" v-html="formData.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Upload, EditPen, Plus, PictureRounded
} from '@element-plus/icons-vue'
import { 
  getEnterprisePromotionDetail, 
  createEnterprisePromotion,
  updateEnterprisePromotion
} from '@/api/enterprise'
import { useUserStore } from '@/stores/user'
import E from 'wangeditor'

// 组件状态
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const editorRef = ref(null)
const editor = ref(null)
const loading = ref(false)
const submitting = ref(false)
const savingDraft = ref(false)
const previewDialogVisible = ref(false)
const isClientReady = ref(false)
const promotionId = computed(() => route.params.id)
const isEdit = computed(() => !!promotionId.value)
const uploadUrl = import.meta.env.VITE_API_BASE_URL + '/file/upload'

// 表单数据
const formData = reactive({
  title: '',
  coverImage: '',
  summary: '',
  content: '',
  attachments: [],
  isDraft: false
})

// 附件列表
const attachmentList = ref([])

// 表单校验规则
const rules = {
  title: [
    { required: true, message: '请输入宣传标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在5-100个字符之间', trigger: 'blur' }
  ],
  summary: [
    { required: true, message: '请输入宣传简介', trigger: 'blur' },
    { min: 10, max: 150, message: '简介长度在10-150个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入宣传内容', trigger: 'blur' },
    { min: 50, message: '宣传内容不能少于50个字符', trigger: 'blur' }
  ]
}

// 初始化富文本编辑器
const initEditor = () => {
  if (editorRef.value) {
    // 创建编辑器实例
    editor.value = new E(editorRef.value)
    
    // 工具栏配置
    editor.value.config.menus = [
      'head',
      'bold',
      'fontSize',
      'fontName',
      'italic',
      'underline',
      'strikeThrough',
      'indent',
      'lineHeight',
      'foreColor',
      'backColor',
      'link',
      'list',
      'justify',
      'quote',
      'emoticon',
      'image',
      'table',
      'code',
      'splitLine',
      'undo',
      'redo'
    ]
    
    // 允许上传的图片类型
    editor.value.config.uploadImgAccept = ['jpg', 'jpeg', 'png', 'gif', 'webp']
    
    // 图片上传
    editor.value.config.uploadImgServer = uploadUrl
    editor.value.config.uploadFileName = 'file'
    
    // 限制上传图片大小
    editor.value.config.uploadImgMaxSize = 5 * 1024 * 1024 // 5MB
    
    // 自定义上传图片的处理函数
    editor.value.config.uploadImgHooks = {
      customInsert: (insertImgFn, result) => {
        if (result.code === 200 && result.data && result.data.url) {
          insertImgFn(result.data.url)
        } else {
          ElMessage.error('图片上传失败')
        }
      },
      error: (xhr, editor, resData) => {
        ElMessage.error('图片上传失败')
      }
    }
    
    // 内容变化回调
    editor.value.config.onchange = () => {
      formData.content = editor.value.txt.html()
    }
    
    // 创建编辑器
    editor.value.create()
    
    // 设置初始内容
    if (formData.content) {
      editor.value.txt.html(formData.content)
    }
  }
}

// 获取宣传详情
const fetchPromotionDetail = async () => {
  if (!isEdit.value) return
  
  loading.value = true
  try {
    const res = await getEnterprisePromotionDetail(promotionId.value)
    if (res && res.data) {
      const detail = res.data
      
      // 填充表单数据
      formData.title = detail.title
      formData.coverImage = detail.coverImage
      formData.summary = detail.summary
      formData.content = detail.content
      formData.attachments = detail.attachments || []
      formData.isDraft = detail.status === 'DRAFT'
      
      // 初始化附件列表
      if (detail.attachments && detail.attachments.length > 0) {
        attachmentList.value = detail.attachments.map(item => ({
          name: item.name,
          url: item.url,
          ...item
        }))
      }
      
      // 设置编辑器内容
      if (editor.value && formData.content) {
        editor.value.txt.html(formData.content)
      }
    }
  } catch (error) {
    console.error('获取宣传详情失败:', error)
    ElMessage.error('获取宣传详情失败')
  } finally {
    loading.value = false
  }
}

// 处理封面上传成功
const handleCoverSuccess = (response, file) => {
  if (response.code === 200 && response.data && response.data.url) {
    formData.coverImage = response.data.url
  } else {
    ElMessage.error('封面上传失败')
  }
}

// 处理封面上传前检查
const beforeCoverUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png'].includes(file.type)
  const isLt2M = file.size / 1024 / 1024 < 2
  
  if (!isImage) {
    ElMessage.error('封面图片只能是JPG或PNG格式!')
    return false
  }
  
  if (!isLt2M) {
    ElMessage.error('封面图片大小不能超过2MB!')
    return false
  }
  
  return true
}

// 处理附件上传成功
const handleAttachmentSuccess = (response, file, fileList) => {
  if (response.code === 200 && response.data && response.data.url) {
    const attachment = {
      name: file.name,
      url: response.data.url,
      size: file.size,
      type: file.type
    }
    
    formData.attachments.push(attachment)
  } else {
    ElMessage.error('附件上传失败')
  }
}

// 处理附件上传前检查
const beforeAttachmentUpload = (file) => {
  const validTypes = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    'application/vnd.ms-excel',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    'application/vnd.ms-powerpoint',
    'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    'text/plain',
    'application/zip',
    'application/x-rar-compressed'
  ]
  
  const isValidType = validTypes.includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isValidType) {
    ElMessage.error('不支持的文件格式!')
    return false
  }
  
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB!')
    return false
  }
  
  return true
}

// 处理上传错误
const handleUploadError = () => {
  ElMessage.error('上传失败')
}

// 处理附件预览
const handlePreview = (file) => {
  window.open(file.url, '_blank')
}

// 处理附件移除
const handleRemove = (file, fileList) => {
  attachmentList.value = fileList
  formData.attachments = formData.attachments.filter(item => item.url !== file.url)
}

// 提交表单
const submitForm = () => {
  // 先更新内容
  if (editor.value) {
    formData.content = editor.value.txt.html()
  }
  
  formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    formData.isDraft = false
    
    try {
      if (isEdit.value) {
        // 更新宣传
        await updateEnterprisePromotion(promotionId.value, formData)
        ElMessage.success('宣传内容已更新，将在审核通过后展示')
      } else {
        // 创建宣传
        await createEnterprisePromotion(formData)
        ElMessage.success('宣传内容已提交，将在审核通过后展示')
      }
      
      // 跳转回列表页
      router.push('/enterprise/promotion')
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    } finally {
      submitting.value = false
    }
  })
}

// 保存草稿
const saveDraft = async () => {
  // 先更新内容
  if (editor.value) {
    formData.content = editor.value.txt.html()
  }
  
  savingDraft.value = true
  formData.isDraft = true
  
  try {
    if (isEdit.value) {
      // 更新草稿
      await updateEnterprisePromotion(promotionId.value, formData)
      ElMessage.success('草稿已保存')
    } else {
      // 创建草稿
      const res = await createEnterprisePromotion(formData)
      if (res && res.data && res.data.id) {
        ElMessage.success('草稿已保存')
        // 编辑模式下继续编辑
        router.replace(`/enterprise/promotion/edit/${res.data.id}`)
      }
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败')
  } finally {
    savingDraft.value = false
  }
}

// 返回上一页
const goBack = () => {
  if (editor.value && editor.value.txt.html() !== formData.content) {
    ElMessageBox.confirm('内容已修改，确定要离开吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push('/enterprise/promotion')
    }).catch(() => {})
  } else {
    router.push('/enterprise/promotion')
  }
}

// 组件挂载时初始化
onMounted(async () => {
  // 在客户端环境下才能初始化编辑器
  isClientReady.value = true
  
  await nextTick()
  // 初始化编辑器
  initEditor()
  
  // 获取宣传详情
  fetchPromotionDetail()
})

// 组件卸载前销毁编辑器
onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy()
    editor.value = null
  }
})
</script>

<style lang="scss" scoped>
.promotion-edit-container {
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
  
  .loading-container {
    padding: 30px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  
  .promotion-form {
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 30px;
    
    .cover-upload {
      .el-upload {
        display: block;
      }
      
      .cover-preview {
        position: relative;
        width: 320px;
        height: 180px;
        margin-bottom: 10px;
        overflow: hidden;
        border-radius: 4px;
        
        .el-image {
          width: 100%;
          height: 100%;
        }
        
        .cover-actions {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          background-color: rgba(0, 0, 0, 0.5);
          color: #fff;
          opacity: 0;
          transition: opacity 0.3s;
          
          .replace-icon {
            font-size: 24px;
            margin-bottom: 5px;
          }
          
          &:hover {
            opacity: 1;
          }
        }
      }
    }
    
    .editor-container {
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      
      .editor {
        height: 400px;
      }
    }
    
    .el-button {
      min-width: 100px;
    }
  }
}

.preview-container {
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
  
  .preview-title {
    font-size: 24px;
    font-weight: bold;
    text-align: center;
    margin-bottom: 20px;
  }
  
  .preview-cover {
    margin-bottom: 20px;
    text-align: center;
    
    .el-image {
      max-width: 100%;
      max-height: 400px;
    }
  }
  
  .preview-summary {
    font-size: 16px;
    line-height: 1.6;
    color: #606266;
    margin-bottom: 20px;
    padding: 15px;
    background-color: #f5f7fa;
    border-radius: 4px;
  }
  
  .preview-content {
    font-size: 16px;
    line-height: 1.8;
    
    img {
      max-width: 100%;
    }
  }
}

@media (max-width: 768px) {
  .promotion-edit-container {
    padding: 10px;
    
    .promotion-form {
      padding: 15px;
      
      .cover-preview {
        width: 100%;
        height: auto;
        aspect-ratio: 16 / 9;
      }
      
      .editor-container {
        .editor {
          height: 300px;
        }
      }
    }
  }
}

:deep(.w-e-text-container) {
  height: 400px !important;
}

:deep(.el-upload-list) {
  margin-top: 10px;
}
</style> 