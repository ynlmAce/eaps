<template>
  <div class="position-edit-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑职位' : '发布新职位' }}</h2>
      <el-alert
        :title="isEdit ? '修改职位信息后需重新提交审核' : '填写完整的职位信息有助于吸引更多合适的求职者'"
        type="info"
        description="请填写真实、准确的职位信息，避免夸大或虚假宣传。审核通过后将对学生求职者展示。"
        show-icon
        :closable="false"
        style="margin-top: 15px; margin-bottom: 20px;"
      />
    </div>
    
    <el-card class="form-card">
      <el-form 
        ref="positionFormRef" 
        :model="positionForm" 
        :rules="positionRules" 
        label-width="100px"
        status-icon
      >
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位名称" prop="name">
              <el-input v-model="positionForm.name" placeholder="请输入职位名称" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职位类型" prop="positionType">
              <el-select v-model="positionForm.positionType" placeholder="请选择职位类型" style="width: 100%">
                <el-option label="全职" value="fulltime" />
                <el-option label="兼职" value="parttime" />
                <el-option label="实习" value="intern" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="薪资下限" prop="salaryLower">
              <el-input-number v-model="positionForm.salaryLower" :min="0" :precision="1" :step="0.5" style="width: 100%" placeholder="薪资下限(K)" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="薪资上限" prop="salaryUpper">
              <el-input-number v-model="positionForm.salaryUpper" :min="0" :precision="1" :step="0.5" style="width: 100%" placeholder="薪资上限(K)" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="薪资月数" prop="salaryCount">
              <el-input-number v-model="positionForm.salaryCount" :min="12" :max="16" :step="1" style="width: 100%" placeholder="每年薪资月数" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学历要求" prop="educationRequirement">
              <el-select v-model="positionForm.educationRequirement" placeholder="请选择学历要求" style="width: 100%">
                <el-option label="不限" value="不限" />
                <el-option label="大专" value="大专" />
                <el-option label="本科" value="本科" />
                <el-option label="硕士" value="硕士" />
                <el-option label="博士" value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经验要求" prop="experienceRequirement">
              <el-select v-model="positionForm.experienceRequirement" placeholder="请选择经验要求" style="width: 100%">
                <el-option label="不限" value="不限" />
                <el-option label="应届毕业生" value="应届毕业生" />
                <el-option label="1年以下" value="1年以下" />
                <el-option label="1-3年" value="1-3年" />
                <el-option label="3-5年" value="3-5年" />
                <el-option label="5年以上" value="5年以上" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="工作地点" prop="workLocation">
          <el-input v-model="positionForm.workLocation" placeholder="请输入工作地点，例如：北京市海淀区" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="locationDetail">
          <el-input v-model="positionForm.locationDetail" placeholder="请输入详细地址（选填）" maxlength="200" show-word-limit />
        </el-form-item>
        
        <el-form-item label="职位标签">
          <el-tag
            v-for="tag in positionForm.tags"
            :key="tag"
            closable
            :disable-transitions="false"
            @close="handleTagClose(tag)"
            class="position-tag"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="inputTagVisible"
            ref="tagInputRef"
            v-model="inputTagValue"
            class="tag-input"
            size="small"
            @keyup.enter="handleTagConfirm"
            @blur="handleTagConfirm"
          />
          <el-button v-else class="button-new-tag" size="small" @click="showTagInput">
            <el-icon><Plus /></el-icon>添加标签
          </el-button>
          <div class="tag-tip">添加职位相关标签，如"五险一金"、"弹性工作"等，最多可添加5个标签</div>
        </el-form-item>
        
        <el-divider content-position="left">详细描述</el-divider>
        
        <el-form-item label="职位描述" prop="description">
          <el-input
            v-model="positionForm.description"
            type="textarea"
            :rows="6"
            placeholder="请详细描述该职位的工作内容、职责等"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="职位要求" prop="requirements">
          <el-input
            v-model="positionForm.requirements"
            type="textarea"
            :rows="6"
            placeholder="请详细描述该职位的技能要求、素质要求等"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-divider content-position="left">其他信息</el-divider>
        
        <el-form-item label="招聘人数" prop="headcount">
          <el-input-number v-model="positionForm.headcount" :min="1" :max="999" :step="1" style="width: 200px" />
        </el-form-item>
        
        <el-form-item label="状态" v-if="isEdit">
          <el-radio-group v-model="positionForm.status">
            <el-radio 
              v-for="item in statusOptions" 
              :key="item.value" 
              :label="item.value" 
              :disabled="item.disabled"
            >
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">{{ isEdit ? '保存修改' : '提交发布' }}</el-button>
          <el-button @click="saveAsDraft" :loading="submitLoading" v-if="!isEdit">保存草稿</el-button>
          <el-button @click="cancel">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getPositionDetail,
  createPosition,
  updatePosition,
  savePositionDraft
} from '@/api/position'

const router = useRouter()
const route = useRoute()
const positionFormRef = ref(null)
const tagInputRef = ref(null)
const inputTagVisible = ref(false)
const inputTagValue = ref('')
const submitLoading = ref(false)
const draftLoading = ref(false)

// 判断是编辑还是新增
const positionId = computed(() => route.params.id)
const isEdit = computed(() => !!positionId.value)

// 状态选项
const statusOptions = reactive([
  { label: '草稿', value: 'draft', disabled: false },
  { label: '待审核', value: 'pending', disabled: false },
  { label: '已发布', value: 'active', disabled: false },
  { label: '已下线', value: 'inactive', disabled: false }
])

// 职位表单
const positionForm = reactive({
  id: '',
  name: '',
  positionType: 'fulltime',
  salaryLower: 0,
  salaryUpper: 0,
  salaryCount: 12,
  educationRequirement: '不限',
  experienceRequirement: '不限',
  workLocation: '',
  locationDetail: '',
  tags: [],
  description: '',
  requirements: '',
  headcount: 1,
  status: 'pending'  // 默认为待审核
})

// 表单验证规则
const positionRules = {
  name: [
    { required: true, message: '请输入职位名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在2到50个字符之间', trigger: 'blur' }
  ],
  positionType: [
    { required: true, message: '请选择职位类型', trigger: 'change' }
  ],
  salaryLower: [
    { required: true, message: '请输入薪资下限', trigger: 'blur' }
  ],
  salaryUpper: [
    { required: true, message: '请输入薪资上限', trigger: 'blur' },
    { validator: validateSalary, trigger: 'blur' }
  ],
  salaryCount: [
    { required: true, message: '请输入薪资月数', trigger: 'blur' }
  ],
  educationRequirement: [
    { required: true, message: '请选择学历要求', trigger: 'change' }
  ],
  experienceRequirement: [
    { required: true, message: '请选择经验要求', trigger: 'change' }
  ],
  workLocation: [
    { required: true, message: '请输入工作地点', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在2到100个字符之间', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入职位描述', trigger: 'blur' },
    { min: 10, max: 2000, message: '长度在10到2000个字符之间', trigger: 'blur' }
  ],
  requirements: [
    { required: true, message: '请输入职位要求', trigger: 'blur' },
    { min: 10, max: 2000, message: '长度在10到2000个字符之间', trigger: 'blur' }
  ],
  headcount: [
    { required: true, message: '请输入招聘人数', trigger: 'blur' }
  ]
}

// 验证薪资范围
function validateSalary(rule, value, callback) {
  if (positionForm.salaryLower > value) {
    callback(new Error('薪资上限不能小于薪资下限'))
  } else {
    callback()
  }
}

// 获取职位详情
const fetchDetail = async () => {
  if (!positionId.value) return
  submitLoading.value = true
  try {
    const res = await getPositionDetail(positionId.value)
    if (res.data) {
      // 填充表单数据
      const detail = res.data
      positionForm.name = detail.name
      positionForm.positionType = detail.type
      positionForm.salaryLower = detail.salaryMin
      positionForm.salaryUpper = detail.salaryMax
      positionForm.educationRequirement = detail.education
      positionForm.experienceRequirement = detail.experienceYears
      positionForm.workLocation = detail.workLocation
      positionForm.description = detail.description
      positionForm.requirements = detail.requirement
      
      // 处理标签
      if (detail.tags && detail.tags.length > 0) {
        positionForm.tags = detail.tags
      }
      
      positionForm.status = detail.status
    }
  } catch (error) {
    console.error('获取职位详情失败:', error)
    ElMessage.error('获取职位详情失败')
  } finally {
    submitLoading.value = false
  }
}

// 显示标签输入框
const showTagInput = () => {
  if (positionForm.tags.length >= 5) {
    ElMessage.warning('最多添加5个标签')
    return
  }
  
  inputTagVisible.value = true
  nextTick(() => {
    tagInputRef.value.focus()
  })
}

// 确认添加标签
const handleTagConfirm = () => {
  if (inputTagValue.value) {
    if (positionForm.tags.includes(inputTagValue.value)) {
      ElMessage.warning('标签已存在')
    } else if (inputTagValue.value.length > 10) {
      ElMessage.warning('标签长度不能超过10个字符')
    } else {
      positionForm.tags.push(inputTagValue.value)
    }
  }
  
  inputTagVisible.value = false
  inputTagValue.value = ''
}

// 删除标签
const handleTagClose = (tag) => {
  positionForm.tags = positionForm.tags.filter(item => item !== tag)
}

// 提交表单
const submitForm = async () => {
  if (!positionFormRef.value) return
  
  await positionFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const params = { ...positionForm }
      // 设置状态为待审核
      if (!isEdit.value || params.status === 'draft' || params.status === 'rejected') {
        params.status = 'pending'
      }
      
      if (isEdit.value) {
        await updatePosition(positionId.value, params)
        ElMessage.success('修改成功，等待审核')
      } else {
        await createPosition(params)
        ElMessage.success('发布成功，等待审核')
      }
      
      router.push('/recruit/position')
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 保存草稿
const saveAsDraft = async () => {
  if (!positionFormRef.value) return
  const valid = await positionFormRef.value.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    const params = { ...positionForm, status: 'draft' }
    // 根据是否为编辑模式决定调用哪个API
    if (isEdit.value) {
      await updatePosition(positionId.value, params)
      ElMessage.success('职位草稿已更新')
    } else {
      await savePositionDraft(params)
      ElMessage.success('职位草稿已保存')
    }
    router.push('/recruit/position')
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

// 发布职位
const publishPosition = async () => {
  if (!positionFormRef.value) return
  const valid = await positionFormRef.value.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    const params = { ...positionForm, status: 'pending' }
    // 根据是否为编辑模式决定调用哪个API
    if (isEdit.value) {
      await updatePosition(positionId.value, params)
      ElMessage.success('职位已更新并提交审核')
    } else {
      await createPosition(params)
      ElMessage.success('职位已提交审核')
    }
    router.push('/recruit/position')
  } catch (error) {
    console.error('发布职位失败:', error)
    ElMessage.error('发布职位失败，请稍后重试')
  } finally {
    submitLoading.value = false
  }
}

// 取消操作
const cancel = () => {
  ElMessageBox.confirm('确定要取消操作吗？未保存的数据将丢失', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/recruit/position')
  }).catch(() => {})
}

// 页面加载时初始化数据
onMounted(() => {
  if (isEdit.value) {
    fetchDetail()
  }
})
</script>

<style lang="scss" scoped>
.position-edit-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  
  .page-header {
    margin-bottom: 20px;
    
    h2 {
      margin: 0 0 15px 0;
      font-weight: bold;
    }
  }
  
  .form-card {
    margin-bottom: 20px;
  }
  
  .position-tag {
    margin-right: 10px;
    margin-bottom: 10px;
  }
  
  .tag-input {
    width: 120px;
    vertical-align: bottom;
    margin-right: 10px;
  }
  
  .button-new-tag {
    vertical-align: bottom;
  }
  
  .tag-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
  }
  
  .el-divider {
    margin: 20px 0;
    
    .el-divider__text {
      font-size: 16px;
      font-weight: bold;
      color: #409EFF;
    }
  }
}
</style> 