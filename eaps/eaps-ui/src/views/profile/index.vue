<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <!-- 基本信息 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form
            ref="userFormRef"
            :model="userForm"
            :rules="userRules"
            label-width="100px"
          >
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="userForm.username" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="昵称" prop="nickname">
                  <el-input v-model="userForm.nickname" placeholder="请输入昵称" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="userForm.email" placeholder="请输入邮箱" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="手机号" prop="phonenumber">
                  <el-input v-model="userForm.phonenumber" placeholder="请输入手机号" />
                </el-form-item>
              </el-col>
            </el-row>
            
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="性别" prop="sex">
                  <el-select v-model="userForm.sex" placeholder="请选择性别" style="width: 100%">
                    <el-option label="男" :value="0" />
                    <el-option label="女" :value="1" />
                    <el-option label="未知" :value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="头像">
                  <el-upload
                    class="avatar-uploader"
                    action="/api/user/upload-avatar"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :before-upload="beforeAvatarUpload"
                  >
                    <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
                    <el-icon v-else class="avatar-icon"><Plus /></el-icon>
                    <div class="upload-tip">点击上传</div>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
            
            <!-- 根据用户类型动态显示额外的表单项 -->
            <!-- 学生额外信息 -->
            <template v-if="userInfo.userType === 0">
              <div class="profile-section-title">学生信息</div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="姓名" prop="studentInfo.realName">
                    <el-input v-model="userForm.studentInfo.realName" placeholder="请输入真实姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="学号" prop="studentInfo.studentNumber">
                    <el-input v-model="userForm.studentInfo.studentNumber" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="学院" prop="studentInfo.college">
                    <el-input v-model="userForm.studentInfo.college" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="专业" prop="studentInfo.major">
                    <el-input v-model="userForm.studentInfo.major" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="班级" prop="studentInfo.className">
                    <el-input v-model="userForm.studentInfo.className" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
            </template>
            
            <!-- 企业额外信息 -->
            <template v-else-if="userInfo.userType === 3">
              <div class="profile-section-title">企业信息</div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="企业名称" prop="enterpriseInfo.enterpriseName">
                    <el-input v-model="userForm.enterpriseInfo.enterpriseName" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="信用代码" prop="enterpriseInfo.creditCode">
                    <el-input v-model="userForm.enterpriseInfo.creditCode" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="联系人" prop="enterpriseInfo.contactPerson">
                    <el-input v-model="userForm.enterpriseInfo.contactPerson" placeholder="请输入联系人姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="联系电话" prop="enterpriseInfo.contactPhone">
                    <el-input v-model="userForm.enterpriseInfo.contactPhone" placeholder="请输入联系电话" />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="企业简介" prop="enterpriseInfo.introduction">
                <el-input
                  v-model="userForm.enterpriseInfo.introduction"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入企业简介"
                />
              </el-form-item>
            </template>
            
            <!-- 辅导员额外信息 -->
            <template v-else-if="userInfo.userType === 4">
              <div class="profile-section-title">辅导员信息</div>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="姓名" prop="counselorInfo.counselorName">
                    <el-input v-model="userForm.counselorInfo.counselorName" placeholder="请输入姓名" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="工号" prop="counselorInfo.employeeNumber">
                    <el-input v-model="userForm.counselorInfo.employeeNumber" disabled />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="所属学院" prop="counselorInfo.counselorCollege">
                    <el-input v-model="userForm.counselorInfo.counselorCollege" disabled />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="职务" prop="counselorInfo.position">
                    <el-input v-model="userForm.counselorInfo.position" placeholder="请输入职务" />
                  </el-form-item>
                </el-col>
              </el-row>
            </template>
            
            <el-form-item>
              <el-button type="primary" @click="updateUserInfo" :loading="loading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                placeholder="请输入当前密码"
                type="password"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                placeholder="请输入新密码"
                type="password"
                show-password
              />
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                placeholder="请再次输入新密码"
                type="password"
                show-password
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="updatePassword" :loading="passwordLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { getCurrentUserInfo, updateUserInfo as updateUser, updatePassword as changePassword } from '@/api/user'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('basic')
const userFormRef = ref(null)
const passwordFormRef = ref(null)
const loading = ref(false)
const passwordLoading = ref(false)

// 用户信息
const userInfo = ref({})
const userForm = ref({
  username: '',
  nickname: '',
  email: '',
  phonenumber: '',
  sex: 0,
  avatar: '',
  // 学生信息
  studentInfo: {
    realName: '',
    studentNumber: '',
    college: '',
    major: '',
    className: ''
  },
  // 企业信息
  enterpriseInfo: {
    enterpriseName: '',
    creditCode: '',
    contactPerson: '',
    contactPhone: '',
    introduction: ''
  },
  // 辅导员信息
  counselorInfo: {
    counselorName: '',
    employeeNumber: '',
    counselorCollege: '',
    position: ''
  }
})

// 修改密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 获取用户信息
async function getUserInfo() {
  try {
    const res = await getCurrentUserInfo()
    userInfo.value = res.data
    
    // 填充表单
    const { user, ...extraInfo } = res.data
    userForm.value = { 
      ...user,
      ...extraInfo
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 更新用户信息
async function updateUserInfo() {
  userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updateUser(userForm.value)
        ElMessage.success('个人信息修改成功')
        // 刷新用户信息
        getUserInfo()
      } catch (error) {
        ElMessage.error(error.message || '修改失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 修改密码验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else {
    if (passwordForm.confirmPassword !== '') {
      passwordFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const userRules = {
  nickname: [
    { required: false, message: '请输入昵称', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phonenumber: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

// 修改密码
async function updatePassword() {
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        // 清空表单
        passwordForm.oldPassword = ''
        passwordForm.newPassword = ''
        passwordForm.confirmPassword = ''
        // 登出
        userStore.logout()
      } catch (error) {
        ElMessage.error(error.message || '修改失败，请重试')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

// 头像上传成功
function handleAvatarSuccess(response) {
  userForm.value.avatar = response.data
  ElMessage.success('头像上传成功')
}

// 头像上传前的验证
function beforeAvatarUpload(file) {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 页面加载时获取用户信息
onMounted(() => {
  getUserInfo()
})
</script>

<style lang="scss" scoped>
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-section-title {
  font-size: 16px;
  font-weight: 500;
  color: #409EFF;
  margin: 20px 0;
  padding-bottom: 10px;
  border-bottom: 1px solid #EBEEF5;
}

.avatar-uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  
  .avatar {
    width: 100px;
    height: 100px;
    border-radius: 50%;
    object-fit: cover;
  }
  
  .avatar-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    line-height: 100px;
    text-align: center;
    border: 1px dashed #d9d9d9;
    border-radius: 50%;
  }
  
  .upload-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #606266;
  }
}
</style> 