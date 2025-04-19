<template>
  <div class="promotion-edit-container">
    <div class="page-header">
      <div class="back-button" @click="confirmLeave">
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
      
      <el-form-item label="分类标签" prop="tags">
        <el-select
          v-model="formData.tags"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="请选择或创建标签"
          style="width: 100%"
        >
          <el-option
            v-for="tag in tagOptions"
            :key="tag.value"
            :label="tag.label"
            :value="tag.value"
          />
        </el-select>
        <div class="el-form-item-tip">
          选择或创建标签，最多可添加5个标签
        </div>
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
      
      <el-form-item label="内容" prop="content">
        <div class="editor-container">
          <el-empty v-if="!isClientReady" description="编辑器加载中..." />
          <div v-else ref="editorRef" class="editor"></div>
        </div>
      </el-form-item>
      
      <el-form-item label="相关图片">
        <el-upload
          class="images-upload"
          :action="uploadUrl"
          list-type="picture-card"
          :on-preview="handleImagePreview"
          :on-remove="handleImageRemove"
          :before-upload="beforeImageUpload"
          :on-success="handleImageSuccess"
          :on-error="handleUploadError"
          :file-list="imageList"
          multiple
          :limit="6"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
        <el-dialog v-model="imagePreviewVisible" width="50%">
          <img :src="imagePreviewUrl" alt="预览图片" style="width: 100%;" />
        </el-dialog>
        <div class="el-upload__tip">
          最多上传6张图片，每张不超过5MB，支持JPG/PNG/JPEG格式
        </div>
      </el-form-item>
      
      <el-form-item label="附件">
        <el-upload
          class="attachment-upload"
          :action="uploadUrl"
          :on-preview="handleAttachmentPreview"
          :on-remove="handleAttachmentRemove"
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
        <el-button @click="handlePreview">预览</el-button>
        <el-button @click="confirmLeave">取消</el-button>
        <div v-if="autoSaveStatus" class="auto-save-status">
          <el-icon v-if="autoSaveStatus === 'saving'" class="is-loading"><Loading /></el-icon>
          <el-icon v-else-if="autoSaveStatus === 'success'"><Check /></el-icon>
          <span>{{ autoSaveStatusText }}</span>
        </div>
      </el-form-item>
    </el-form>
    
    <el-dialog v-model="previewDialogVisible" title="预览" width="80%" top="5vh">
      <div class="preview-container">
        <h1 class="preview-title">{{ formData.title }}</h1>
        <div class="preview-meta">
          <span>{{ formatDate(new Date()) }}</span>
        </div>
        <div class="preview-summary">{{ formData.summary }}</div>
        <div v-if="formData.coverImage" class="preview-cover">
          <el-image :src="formData.coverImage" fit="cover" />
        </div>
        <div class="preview-content" v-html="formData.content"></div>
        
        <div v-if="imageList.length > 0" class="preview-images">
          <h2>相关图片</h2>
          <div class="image-grid">
            <div v-for="(item, index) in imageList" :key="index" class="image-item">
              <el-image :src="item.url" :preview-src-list="imageList.map(img => img.url)" fit="cover" />
            </div>
          </div>
        </div>
        
        <div v-if="attachmentList.length > 0" class="preview-attachments">
          <h2>附件资料</h2>
          <div class="attachment-list">
            <div v-for="(item, index) in attachmentList" :key="index" class="attachment-item">
              <el-icon><Document /></el-icon>
              <span>{{ item.name }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="showLeaveConfirm"
      title="提示"
      width="30%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <span>有未保存的更改，确定要离开吗？</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showLeaveConfirm = false">取消</el-button>
          <el-button type="primary" @click="leavePage">确定离开</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Upload, EditPen, Plus, Document, Check, Loading
} from '@element-plus/icons-vue'
import { 
  getPromotionDetail,
  createPromotion,
  updatePromotion,
  uploadPromotionImage,
  getPromotionTags
} from '@/api/promotion'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/format'
import E from 'wangeditor'

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
const imagePreviewVisible = ref(false)
const imagePreviewUrl = ref('')
const promotionId = computed(() => route.params.id)
const isEdit = computed(() => !!promotionId.value)
const uploadUrl = import.meta.env.VITE_API_BASE_URL + '/api/promotion/upload/image'
const hasUnsavedChanges = ref(false)
const showLeaveConfirm = ref(false)
const isLeaving = ref(false)
const autoSaveTimer = ref(null)
const autoSaveStatus = ref('')
const autoSaveLastTime = ref(null)

const tagOptions = ref([
  { label: '企业文化', value: '企业文化' },
  { label: '招聘信息', value: '招聘信息' },
  { label: '行业资讯', value: '行业资讯' },
  { label: '产品服务', value: '产品服务' },
  { label: '技术分享', value: '技术分享' },
  { label: '公司活动', value: '公司活动' },
  { label: '员工福利', value: '员工福利' }
])

const autoSaveStatusText = computed(() => {
  if (autoSaveStatus.value === 'saving') {
    return '保存中...'
  } else if (autoSaveStatus.value === 'success') {
    return `自动保存于 ${formatTime(autoSaveLastTime.value)}`
  } else if (autoSaveStatus.value === 'error') {
    return '自动保存失败'
  }
  return ''
})

const formData = reactive({
  title: '',
  coverImage: '',
  summary: '',
  content: '',
  images: [],
  attachments: [],
  isDraft: false,
  tags: []
})

const imageList = ref([])
const attachmentList = ref([])

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
  ],
  coverImage: [
    { required: true, message: '请上传封面图片', trigger: 'change' }
  ],
  tags: [
    { type: 'array', max: 5, message: '最多选择5个标签', trigger: 'change' }
  ]
}

const fetchTags = async () => {
  try {
    const res = await getPromotionTags()
    if (res && res.data && Array.isArray(res.data)) {
      const tags = res.data.map(tag => ({
        label: tag.name,
        value: tag.name
      }))
      const existingValues = tagOptions.value.map(t => t.value)
      const newTags = tags.filter(tag => !existingValues.includes(tag.value))
      tagOptions.value = [...tagOptions.value, ...newTags]
    }
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

const initEditor = () => {
  if (editorRef.value) {
    editor.value = new E(editorRef.value)
    
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
    
    editor.value.config.uploadImgAccept = ['jpg', 'jpeg', 'png', 'gif', 'webp']
    
    editor.value.config.uploadImgServer = uploadUrl
    editor.value.config.uploadFileName = 'file'
    
    editor.value.config.uploadImgMaxSize = 5 * 1024 * 1024 // 5MB
    
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
    
    editor.value.config.onchange = () => {
      formData.content = editor.value.txt.html()
    }
    
    editor.value.create()
    
    if (isEdit.value && formData.content) {
      editor.value.txt.html(formData.content)
    }
  }
}

const fetchPromotionDetail = async () => {
  if (!promotionId.value) return
  
  loading.value = true
  
  try {
    const res = await getPromotionDetail(promotionId.value)
    
    if (res && res.data) {
      const detail = res.data
      
      formData.title = detail.title
      formData.coverImage = detail.coverImage
      formData.summary = detail.summary
      formData.content = detail.content
      formData.isDraft = detail.status === 'DRAFT'
      formData.tags = detail.tags || []
      
      if (detail.images && detail.images.length > 0) {
        imageList.value = detail.images.map((url, index) => ({
          name: `图片${index + 1}`,
          url,
          uid: `img-${index}`
        }))
        formData.images = detail.images
      }
      
      if (detail.attachments && detail.attachments.length > 0) {
        attachmentList.value = detail.attachments.map((item, index) => ({
          name: item.name,
          url: item.url,
          uid: `file-${index}`
        }))
        formData.attachments = detail.attachments
      }
      
      nextTick(() => {
        if (editor.value && formData.content) {
          editor.value.txt.html(formData.content)
        }
      })
      
      hasUnsavedChanges.value = false
    }
  } catch (error) {
    console.error('获取宣传详情失败:', error)
    ElMessage.error('获取宣传详情失败')
  } finally {
    loading.value = false
  }
}

const submitForm = async () => {
  if (editor.value) {
    formData.content = editor.value.txt.html()
  }
  
  formRef.value.validate(async (valid) => {
    if (!valid) {
      return false
    }
    
    formData.images = imageList.value.map(item => item.url)
    formData.attachments = attachmentList.value.map(item => ({
      name: item.name,
      url: item.url
    }))
    
    submitting.value = true
    formData.isDraft = false
    
    try {
      let res
      
      if (isEdit.value) {
        res = await updatePromotion(promotionId.value, formData)
      } else {
        res = await createPromotion(formData)
      }
      
      if (res && res.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功，等待审核' : '发布成功，等待审核')
        goBack()
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
    } finally {
      submitting.value = false
    }
  })
}

const saveDraft = async () => {
  if (editor.value) {
    formData.content = editor.value.txt.html()
  }
  
  savingDraft.value = true
  formData.isDraft = true
  
  try {
    let res
    
    if (isEdit.value) {
      res = await updatePromotion(promotionId.value, formData)
    } else {
      res = await createPromotion(formData)
    }
    
    if (res && res.code === 200) {
      ElMessage.success('草稿保存成功')
      
      if (!isEdit.value && res.data && res.data.id) {
        router.replace(`/promotion/edit/${res.data.id}`)
      }
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败')
  } finally {
    savingDraft.value = false
  }
}

const handlePreview = () => {
  if (editor.value) {
    formData.content = editor.value.txt.html()
  }
  
  previewDialogVisible.value = true
}

const confirmLeave = () => {
  if (hasUnsavedChanges.value) {
    showLeaveConfirm.value = true
  } else {
    leavePage()
  }
}

const leavePage = () => {
  isLeaving.value = true
  showLeaveConfirm.value = false
  goBack()
}

const goBack = () => {
  router.push('/promotion')
}

const beforeCoverUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  if (!isImage) {
    ElMessage.error('封面图只能是 JPG/PNG 格式!')
    return false
  }
  
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('封面图大小不能超过 2MB!')
    return false
  }
  
  return true
}

const handleCoverSuccess = (response, file) => {
  if (response.code === 200 && response.data && response.data.url) {
    formData.coverImage = response.data.url
  } else {
    ElMessage.error('封面上传失败')
  }
}

const beforeImageUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  if (!isImage) {
    ElMessage.error('图片只能是 JPG/PNG 格式!')
    return false
  }
  
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  
  return true
}

const handleImageSuccess = (response, file, fileList) => {
  if (response.code === 200 && response.data && response.data.url) {
    imageList.value = fileList.map(item => {
      if (item.uid === file.uid) {
        return {
          name: file.name,
          url: response.data.url,
          uid: file.uid
        }
      }
      return item.response ? {
        name: item.name,
        url: item.response.data.url,
        uid: item.uid
      } : item
    })
  } else {
    ElMessage.error('图片上传失败')
  }
}

const beforeAttachmentUpload = (file) => {
  const validExtensions = ['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'zip', 'rar']
  const extension = file.name.split('.').pop().toLowerCase()
  const isValidType = validExtensions.includes(extension)
  
  if (!isValidType) {
    ElMessage.error('只支持 PDF/DOC/DOCX/XLS/XLSX/PPT/PPTX/TXT/ZIP/RAR 格式!')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('附件大小不能超过 10MB!')
    return false
  }
  
  return true
}

const handleAttachmentSuccess = (response, file, fileList) => {
  if (response.code === 200 && response.data && response.data.url) {
    attachmentList.value = fileList.map(item => {
      if (item.uid === file.uid) {
        return {
          name: file.name,
          url: response.data.url,
          uid: file.uid
        }
      }
      return item.response ? {
        name: item.name,
        url: item.response.data.url,
        uid: item.uid
      } : item
    })
  } else {
    ElMessage.error('附件上传失败')
  }
}

const handleImagePreview = (file) => {
  imagePreviewUrl.value = file.url
  imagePreviewVisible.value = true
}

const handleImageRemove = (file, fileList) => {
  imageList.value = fileList
}

const handleAttachmentPreview = (file) => {
  window.open(file.url, '_blank')
}

const handleAttachmentRemove = (file, fileList) => {
  attachmentList.value = fileList
}

const handleUploadError = () => {
  ElMessage.error('上传失败，请重试')
}

const autoSaveDraft = async () => {
  if (!hasUnsavedChanges.value || submitting.value || savingDraft.value) {
    return
  }
  
  if (editor.value) {
    formData.content = editor.value.txt.html()
  }
  
  if (!formData.title || !formData.coverImage) {
    return
  }
  
  autoSaveStatus.value = 'saving'
  
  try {
    formData.images = imageList.value.map(item => item.url)
    formData.attachments = attachmentList.value.map(item => ({
      name: item.name,
      url: item.url
    }))
    
    formData.isDraft = true
    
    let res
    
    if (isEdit.value) {
      res = await updatePromotion(promotionId.value, formData)
    } else {
      res = await createPromotion(formData)
    }
    
    if (res && res.code === 200) {
      autoSaveStatus.value = 'success'
      autoSaveLastTime.value = new Date()
      hasUnsavedChanges.value = false
      
      if (!isEdit.value && res.data && res.data.id) {
        router.replace(`/promotion/edit/${res.data.id}`)
      }
    } else {
      autoSaveStatus.value = 'error'
    }
  } catch (error) {
    console.error('自动保存草稿失败:', error)
    autoSaveStatus.value = 'error'
  }
}

const watchFormChanges = () => {
  watch(
    () => ({
      title: formData.title,
      summary: formData.summary,
      tags: [...formData.tags],
      images: [...imageList.value],
      attachments: [...attachmentList.value]
    }),
    () => {
      hasUnsavedChanges.value = true
    },
    { deep: true }
  )
}

const formatTime = (date) => {
  if (!date) return ''
  const now = new Date()
  const diff = now - date
  
  if (diff < 60 * 1000) {
    return '刚刚'
  }
  
  if (diff < 60 * 60 * 1000) {
    return `${Math.floor(diff / (60 * 1000))}分钟前`
  }
  
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${hours}:${minutes}`
}

onMounted(() => {
  isClientReady.value = true
  
  fetchTags()
  
  nextTick(() => {
    initEditor()
    
    if (isEdit.value) {
      fetchPromotionDetail()
    }
    
    watchFormChanges()
    
    autoSaveTimer.value = setInterval(autoSaveDraft, 3 * 60 * 1000)
    
    if (editor.value) {
      editor.value.config.onchange = () => {
        formData.content = editor.value.txt.html()
        hasUnsavedChanges.value = true
      }
    }
    
    window.addEventListener('beforeunload', handleBeforeUnload)
  })
})

onBeforeUnmount(() => {
  if (editor.value) {
    editor.value.destroy()
    editor.value = null
  }
  
  if (autoSaveTimer.value) {
    clearInterval(autoSaveTimer.value)
  }
  
  window.removeEventListener('beforeunload', handleBeforeUnload)
})

const handleBeforeUnload = (e) => {
  if (hasUnsavedChanges.value && !isLeaving.value) {
    e.preventDefault()
    e.returnValue = ''
    return ''
  }
}
</script>

<style lang="scss" scoped>
.promotion-edit-container {
  padding: 20px;
  
  .page-header {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    
    .back-button {
      display: flex;
      align-items: center;
      cursor: pointer;
      color: #409eff;
      font-size: 14px;
      
      .el-icon {
        margin-right: 5px;
      }
      
      &:hover {
        color: #66b1ff;
      }
    }
    
    .title {
      font-size: 22px;
      font-weight: 500;
      color: #303133;
      margin-left: 15px;
    }
  }
  
  .loading-container {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 20px;
  }
  
  .promotion-form {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    padding: 30px;
    
    .el-form-item-tip {
      font-size: 12px;
      color: #909399;
      margin-top: 5px;
    }
    
    .auto-save-status {
      display: inline-flex;
      align-items: center;
      margin-left: 15px;
      font-size: 13px;
      color: #909399;
      
      .el-icon {
        margin-right: 5px;
        font-size: 16px;
        
        &.is-loading {
          animation: rotating 2s linear infinite;
        }
      }
    }
    
    .cover-upload {
      .cover-preview {
        width: 300px;
        height: 160px;
        overflow: hidden;
        position: relative;
        border-radius: 4px;
        
        .el-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .cover-actions {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background-color: rgba(0, 0, 0, 0.5);
          display: flex;
          flex-direction: column;
          justify-content: center;
          align-items: center;
          opacity: 0;
          transition: opacity 0.3s;
          color: #fff;
          
          .replace-icon {
            font-size: 24px;
            margin-bottom: 10px;
          }
          
          &:hover {
            opacity: 1;
          }
        }
      }
      
      .el-upload__tip {
        color: #909399;
        font-size: 12px;
        margin-top: 8px;
      }
    }
    
    .editor-container {
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      overflow: hidden;
      
      .editor {
        min-height: 400px;
      }
    }
    
    .el-upload__tip {
      color: #909399;
      font-size: 12px;
      margin-top: 8px;
    }
  }
}

.preview-container {
  padding: 20px;
  
  .preview-title {
    font-size: 24px;
    font-weight: 600;
    margin-bottom: 15px;
    color: #303133;
    text-align: center;
  }
  
  .preview-meta {
    text-align: center;
    color: #909399;
    font-size: 14px;
    margin-bottom: 20px;
  }
  
  .preview-summary {
    background-color: #f5f7fa;
    border-left: 3px solid #409eff;
    padding: 15px;
    margin-bottom: 20px;
    color: #606266;
    font-size: 14px;
    line-height: 1.6;
  }
  
  .preview-cover {
    text-align: center;
    margin-bottom: 20px;
    
    .el-image {
      max-width: 100%;
      max-height: 400px;
      border-radius: 4px;
    }
  }
  
  .preview-content {
    margin-bottom: 30px;
    line-height: 1.8;
    
    :deep(img) {
      max-width: 100%;
    }
  }
  
  .preview-images {
    margin-bottom: 30px;
    
    h2 {
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 15px;
      color: #303133;
    }
    
    .image-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 15px;
      
      .image-item {
        .el-image {
          width: 100%;
          height: 150px;
          border-radius: 4px;
        }
      }
    }
  }
  
  .preview-attachments {
    h2 {
      font-size: 18px;
      font-weight: 500;
      margin-bottom: 15px;
      color: #303133;
    }
    
    .attachment-list {
      display: flex;
      flex-direction: column;
      gap: 10px;
      
      .attachment-item {
        display: flex;
        align-items: center;
        padding: 10px 15px;
        background-color: #f5f7fa;
        border-radius: 4px;
        
        .el-icon {
          margin-right: 10px;
          color: #909399;
          font-size: 18px;
        }
        
        span {
          color: #606266;
          font-size: 14px;
        }
      }
    }
  }
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style> 