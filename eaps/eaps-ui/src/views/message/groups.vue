<template>
  <div class="groups-container">
    <div class="page-header">
      <h2>查找群组</h2>
      <div class="actions">
        <el-button type="primary" @click="openCreateDialog">创建群组</el-button>
      </div>
    </div>

    <el-card class="search-card">
      <div class="search-header">
        <span class="title">筛选条件</span>
      </div>
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="群组名称">
          <el-input v-model="queryParams.name" placeholder="请输入群组名称" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="群组类型">
          <el-select v-model="queryParams.type" placeholder="请选择群组类型" clearable>
            <el-option label="全部" value="" />
            <el-option label="学生交流群" value="student" />
            <el-option label="就业指导群" value="career" />
            <el-option label="企业宣讲群" value="enterprise" />
            <el-option label="专业学习群" value="study" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="group-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabChange">
        <el-tab-pane label="推荐群组" name="recommended">
          <el-empty v-if="loading" :description="false">
            <el-skeleton :rows="6" animated />
          </el-empty>
          <div v-else class="group-list">
            <el-row :gutter="20">
              <el-col v-for="group in groupList" :key="group.id" :xs="24" :sm="12" :md="8" :lg="6">
                <el-card class="group-card" :body-style="{ padding: '0px' }">
                  <div class="group-card-header">
                    <div class="group-avatar">
                      <el-avatar :size="50" :src="group.avatar || defaultAvatar">{{ group.name.charAt(0) }}</el-avatar>
                    </div>
                    <div class="group-info">
                      <h3 class="group-name">{{ group.name }}</h3>
                      <p class="group-desc">{{ group.description }}</p>
                    </div>
                  </div>
                  <div class="group-card-body">
                    <div class="group-meta">
                      <span>类型: {{ getGroupTypeName(group.type) }}</span>
                      <span>成员: {{ group.memberCount }}人</span>
                    </div>
                    <div class="group-tags">
                      <el-tag v-for="tag in group.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
                    </div>
                  </div>
                  <div class="group-card-footer">
                    <el-button 
                      v-if="group.joined" 
                      type="success" 
                      @click="enterGroup(group.id)"
                    >进入群聊</el-button>
                    <el-button 
                      v-else-if="group.joining" 
                      type="info" 
                      disabled
                    >申请中</el-button>
                    <el-button 
                      v-else-if="group.needApproval" 
                      type="primary" 
                      @click="applyGroup(group)"
                    >申请加入</el-button>
                    <el-button 
                      v-else 
                      type="primary" 
                      @click="joinGroup(group.id)"
                    >加入群组</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        <el-tab-pane label="我的群组" name="joined">
          <el-empty v-if="loading" :description="false">
            <el-skeleton :rows="6" animated />
          </el-empty>
          <div v-else-if="myGroupList.length === 0" class="empty-data">
            <el-empty description="您还没有加入任何群组" />
          </div>
          <div v-else class="group-list">
            <el-row :gutter="20">
              <el-col v-for="group in myGroupList" :key="group.id" :xs="24" :sm="12" :md="8" :lg="6">
                <el-card class="group-card" :body-style="{ padding: '0px' }">
                  <div class="group-card-header">
                    <div class="group-avatar">
                      <el-avatar :size="50" :src="group.avatar || defaultAvatar">{{ group.name.charAt(0) }}</el-avatar>
                    </div>
                    <div class="group-info">
                      <h3 class="group-name">{{ group.name }}</h3>
                      <p class="group-desc">{{ group.description }}</p>
                    </div>
                  </div>
                  <div class="group-card-body">
                    <div class="group-meta">
                      <span>类型: {{ getGroupTypeName(group.type) }}</span>
                      <span>成员: {{ group.memberCount }}人</span>
                    </div>
                    <div class="group-tags">
                      <el-tag v-for="tag in group.tags" :key="tag" size="small" type="info">{{ tag }}</el-tag>
                    </div>
                  </div>
                  <div class="group-card-footer">
                    <el-button type="success" @click="enterGroup(group.id)">进入群聊</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:currentPage="queryParams.pageNum"
        v-model:pageSize="queryParams.pageSize"
        :page-sizes="[12, 24, 36, 48]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 申请加入群组对话框 -->
    <el-dialog
      v-model="applyDialog.visible"
      :title="`申请加入 - ${applyDialog.group?.name || ''}`"
      width="500px"
    >
      <el-form :model="applyDialog.form" label-width="80px">
        <el-form-item label="申请理由" prop="reason">
          <el-input
            v-model="applyDialog.form.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入申请加入群组的理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="applyDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="applyDialog.loading" @click="submitGroupApply">提交申请</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 创建群组对话框 -->
    <el-dialog
      v-model="createDialog.visible"
      title="创建群组"
      width="600px"
    >
      <el-form :model="createDialog.form" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="群组名称" prop="name">
          <el-input v-model="createDialog.form.name" placeholder="请输入群组名称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="群组类型" prop="type">
          <el-select v-model="createDialog.form.type" placeholder="请选择群组类型">
            <el-option label="学生交流群" value="student" />
            <el-option label="就业指导群" value="career" />
            <el-option label="企业宣讲群" value="enterprise" />
            <el-option label="专业学习群" value="study" />
          </el-select>
        </el-form-item>
        <el-form-item label="群组介绍" prop="description">
          <el-input
            v-model="createDialog.form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入群组介绍"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="群组标签">
          <el-tag
            v-for="tag in createDialog.form.tags"
            :key="tag"
            closable
            @close="removeTag(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="createDialog.tagInputVisible"
            ref="TagInputRef"
            v-model="createDialog.tagInputValue"
            class="tag-input"
            size="small"
            @keyup.enter="handleTagInputConfirm"
            @blur="handleTagInputConfirm"
          />
          <el-button v-else class="tag-button" @click="showTagInput">+ 添加标签</el-button>
        </el-form-item>
        <el-form-item label="是否需要审核" prop="needApproval">
          <el-switch v-model="createDialog.form.needApproval" />
        </el-form-item>
        <el-form-item label="群组头像">
          <el-upload
            class="avatar-uploader"
            action="/api/upload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="createDialog.form.avatar" :src="createDialog.form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="createDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="createDialog.loading" @click="submitCreateGroup">创建群组</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
// import { getGroupList, getMyGroups, joinGroup, applyToJoinGroup, createGroup } from '@/api/message'

// 默认头像
const defaultAvatar = ref('/default-avatar.png')

// 路由
const router = useRouter()

// 标签输入引用
const TagInputRef = ref(null)

// 查询参数
const queryParams = reactive({
  name: '',
  type: '',
  pageNum: 1,
  pageSize: 12
})

// 搜索条件
const activeTab = ref('recommended')
const loading = ref(false)
const total = ref(0)
const groupList = ref([])
const myGroupList = ref([])

// 申请对话框
const applyDialog = reactive({
  visible: false,
  loading: false,
  group: null,
  form: {
    reason: ''
  }
})

// 创建群组对话框
const createDialog = reactive({
  visible: false,
  loading: false,
  tagInputVisible: false,
  tagInputValue: '',
  form: {
    name: '',
    type: '',
    description: '',
    tags: [],
    needApproval: true,
    avatar: ''
  }
})

// 创建群组表单规则
const createRules = {
  name: [
    { required: true, message: '请输入群组名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择群组类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入群组介绍', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 表单引用
const createFormRef = ref(null)

// 获取群组类型名称
const getGroupTypeName = (type) => {
  const typeMap = {
    'student': '学生交流群',
    'career': '就业指导群',
    'enterprise': '企业宣讲群',
    'study': '专业学习群'
  }
  return typeMap[type] || '未知类型'
}

// 搜索群组
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchGroupList()
}

// 重置搜索条件
const resetSearch = () => {
  queryParams.name = ''
  queryParams.type = ''
  queryParams.pageNum = 1
  fetchGroupList()
}

// 处理标签页切换
const handleTabChange = () => {
  if (activeTab.value === 'joined') {
    fetchMyGroups()
  } else {
    fetchGroupList()
  }
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchGroupList()
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  queryParams.pageNum = 1
  fetchGroupList()
}

// 获取群组列表
const fetchGroupList = async () => {
  loading.value = true
  try {
    // 模拟API调用
    // const res = await getGroupList(queryParams)
    // groupList.value = res.data.list
    // total.value = res.data.total

    // 模拟数据
    setTimeout(() => {
      groupList.value = [
        {
          id: 1,
          name: '计算机科学与技术交流群',
          description: '交流计算机专业学习和就业经验',
          type: 'student',
          memberCount: 120,
          avatar: '',
          tags: ['计算机科学', '学习交流', '就业经验'],
          joined: false,
          joining: false,
          needApproval: true
        },
        {
          id: 2,
          name: '2024届毕业生就业指导',
          description: '为2024届毕业生提供就业指导和信息共享',
          type: 'career',
          memberCount: 85,
          avatar: '',
          tags: ['就业指导', '2024届', '面试技巧'],
          joined: true,
          joining: false,
          needApproval: false
        },
        {
          id: 3,
          name: '腾讯2024校招宣讲',
          description: '腾讯2024校园招聘交流和答疑',
          type: 'enterprise',
          memberCount: 245,
          avatar: '',
          tags: ['腾讯', '校园招聘', '互联网'],
          joined: false,
          joining: true,
          needApproval: true
        },
        {
          id: 4,
          name: '机器学习学习小组',
          description: '机器学习和人工智能学习交流',
          type: 'study',
          memberCount: 67,
          avatar: '',
          tags: ['机器学习', 'AI', '算法'],
          joined: false,
          joining: false,
          needApproval: false
        }
      ]
      total.value = 4
      loading.value = false
    }, 600)
  } catch (error) {
    console.error('获取群组列表失败:', error)
    loading.value = false
    ElMessage.error('获取群组列表失败')
  }
}

// 获取我的群组
const fetchMyGroups = async () => {
  loading.value = true
  try {
    // 模拟API调用
    // const res = await getMyGroups()
    // myGroupList.value = res.data

    // 模拟数据
    setTimeout(() => {
      myGroupList.value = [
        {
          id: 2,
          name: '2024届毕业生就业指导',
          description: '为2024届毕业生提供就业指导和信息共享',
          type: 'career',
          memberCount: 85,
          avatar: '',
          tags: ['就业指导', '2024届', '面试技巧']
        }
      ]
      loading.value = false
    }, 600)
  } catch (error) {
    console.error('获取我的群组失败:', error)
    loading.value = false
    ElMessage.error('获取我的群组失败')
  }
}

// 加入群组
const joinGroup = async (groupId) => {
  try {
    // 模拟API调用
    // await joinGroup(groupId)
    
    ElMessage.success('加入群组成功')
    // 刷新群组列表
    fetchGroupList()
  } catch (error) {
    console.error('加入群组失败:', error)
    ElMessage.error('加入群组失败')
  }
}

// 申请加入群组
const applyGroup = (group) => {
  applyDialog.group = group
  applyDialog.form.reason = ''
  applyDialog.visible = true
}

// 提交群组申请
const submitGroupApply = async () => {
  if (!applyDialog.form.reason.trim()) {
    ElMessage.warning('请填写申请理由')
    return
  }

  applyDialog.loading = true
  try {
    // 模拟API调用
    // await applyToJoinGroup({
    //   groupId: applyDialog.group.id,
    //   reason: applyDialog.form.reason
    // })
    
    setTimeout(() => {
      ElMessage.success('申请已提交，请等待管理员审核')
      applyDialog.visible = false
      applyDialog.loading = false
      // 刷新群组列表
      fetchGroupList()
    }, 500)
  } catch (error) {
    console.error('申请加入群组失败:', error)
    ElMessage.error('申请加入群组失败')
    applyDialog.loading = false
  }
}

// 进入群聊
const enterGroup = (groupId) => {
  router.push(`/message/group/${groupId}`)
}

// 显示标签输入框
const showTagInput = () => {
  createDialog.tagInputVisible = true
  nextTick(() => {
    TagInputRef.value?.focus()
  })
}

// 处理标签输入确认
const handleTagInputConfirm = () => {
  const inputValue = createDialog.tagInputValue
  if (inputValue && !createDialog.form.tags.includes(inputValue) && createDialog.form.tags.length < 5) {
    createDialog.form.tags.push(inputValue)
  }
  createDialog.tagInputVisible = false
  createDialog.tagInputValue = ''
}

// 移除标签
const removeTag = (tag) => {
  createDialog.form.tags.splice(createDialog.form.tags.indexOf(tag), 1)
}

// 打开创建群组对话框
const openCreateDialog = () => {
  createDialog.form = {
    name: '',
    type: '',
    description: '',
    tags: [],
    needApproval: true,
    avatar: ''
  }
  createDialog.visible = true
}

// 头像上传成功
const handleAvatarSuccess = (res) => {
  // 实际项目中应该从响应中获取图片URL
  createDialog.form.avatar = res.data
}

// 头像上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('上传头像只能是图片格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isImage && isLt2M
}

// 提交创建群组
const submitCreateGroup = async () => {
  if (!createFormRef.value) return
  
  try {
    await createFormRef.value.validate()
    
    createDialog.loading = true
    
    // 模拟API调用
    // const res = await createGroup(createDialog.form)
    
    setTimeout(() => {
      ElMessage.success('创建群组成功')
      createDialog.visible = false
      createDialog.loading = false
      // 刷新群组列表
      fetchGroupList()
    }, 800)
  } catch (error) {
    console.error('创建群组失败:', error)
    if (error !== false) { // 非表单验证错误
      ElMessage.error('创建群组失败')
    }
    createDialog.loading = false
  }
}

// 生命周期钩子
onMounted(() => {
  fetchGroupList()
})
</script>

<style scoped>
.groups-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.search-card {
  margin-bottom: 20px;
}

.search-header {
  margin-bottom: 18px;
}

.search-header .title {
  font-size: 16px;
  font-weight: bold;
}

.group-tabs {
  margin-bottom: 20px;
}

.group-list {
  margin-top: 20px;
}

.group-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.group-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.group-card-header {
  display: flex;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.group-avatar {
  margin-right: 15px;
}

.group-info {
  flex: 1;
  overflow: hidden;
}

.group-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.group-desc {
  margin: 0;
  font-size: 14px;
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.group-card-body {
  padding: 15px;
  flex: 1;
}

.group-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #909399;
  font-size: 14px;
}

.group-tags {
  margin-top: 10px;
}

.group-tags .el-tag {
  margin-right: 5px;
  margin-bottom: 5px;
}

.group-card-footer {
  padding: 10px 15px;
  border-top: 1px solid #f0f0f0;
  text-align: right;
  background-color: #f9f9f9;
}

.empty-data {
  padding: 40px 0;
}

.pagination-container {
  text-align: center;
  margin-top: 30px;
  margin-bottom: 20px;
}

.avatar-uploader {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
}

.tag-input {
  width: 90px;
  margin-left: 8px;
  vertical-align: bottom;
}

.tag-button {
  margin-left: 8px;
  height: 32px;
  font-size: 14px;
  padding-top: 0;
  padding-bottom: 0;
}
</style> 