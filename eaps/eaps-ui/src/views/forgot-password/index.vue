<template>
  <div class="forgot-password-container">
    <el-form
      ref="forgotPasswordFormRef"
      :model="forgotPasswordForm"
      :rules="forgotPasswordRules"
      class="forgot-password-form"
      label-position="top"
    >
      <div class="title-container">
        <h3 class="title">找回密码</h3>
      </div>

      <!-- 步骤条 -->
      <el-steps :active="currentStep" finish-status="success" simple style="margin-bottom: 30px">
        <el-step title="输入用户名" />
        <el-step title="回答密保问题" />
        <el-step title="重置密码" />
      </el-steps>

      <!-- 步骤1：输入用户名 -->
      <template v-if="currentStep === 0">
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="forgotPasswordForm.username" 
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-button
          :loading="loading"
          type="primary"
          class="submit-button"
          @click="handleGetSecurityQuestion"
        >
          下一步
        </el-button>
      </template>

      <!-- 步骤2：回答密保问题 -->
      <template v-if="currentStep === 1">
        <el-form-item label="密保问题">
          <div class="security-question">{{ securityQuestion }}</div>
        </el-form-item>

        <el-form-item label="密保答案" prop="securityAnswer">
          <el-input 
            v-model="forgotPasswordForm.securityAnswer" 
            placeholder="请输入密保答案"
            prefix-icon="Lock"
          />
        </el-form-item>

        <el-button
          :loading="loading"
          type="primary"
          class="submit-button"
          @click="handleVerifyAnswer"
        >
          下一步
        </el-button>
      </template>

      <!-- 步骤3：重置密码 -->
      <template v-if="currentStep === 2">
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="forgotPasswordForm.newPassword" 
            type="password"
            placeholder="请输入新密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="forgotPasswordForm.confirmPassword" 
            type="password"
            placeholder="请再次输入新密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-button
          :loading="loading"
          type="primary"
          class="submit-button"
          @click="handleResetPassword"
        >
          重置密码
        </el-button>
      </template>

      <div class="tips-container">
        <div class="actions">
          <el-button type="text" @click="goToLogin">返回登录</el-button>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSecurityQuestion, verifySecurityAnswer, resetPassword } from '@/api/auth'

const router = useRouter()
const forgotPasswordFormRef = ref(null)
const loading = ref(false)
const currentStep = ref(0)
const securityQuestion = ref('')
const resetToken = ref('')

// 表单数据
const forgotPasswordForm = reactive({
  username: '',
  securityAnswer: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const forgotPasswordRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  securityAnswer: [
    { required: true, message: '请输入密保答案', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取密保问题
async function handleGetSecurityQuestion() {
  forgotPasswordFormRef.value.validateField('username', async (valid) => {
    if (!valid) {
      loading.value = true
      try {
        const question = await getSecurityQuestion(forgotPasswordForm.username)
        securityQuestion.value = question
        currentStep.value = 1
      } catch (error) {
        ElMessage.error(error.message || '获取密保问题失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 验证密保答案
async function handleVerifyAnswer() {
  forgotPasswordFormRef.value.validateField('securityAnswer', async (valid) => {
    if (!valid) {
      loading.value = true
      try {
        const token = await verifySecurityAnswer({
          username: forgotPasswordForm.username,
          answer: forgotPasswordForm.securityAnswer
        })
        resetToken.value = token
        currentStep.value = 2
      } catch (error) {
        ElMessage.error(error.message || '密保答案验证失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 重置密码
async function handleResetPassword() {
  forgotPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await resetPassword({
          token: resetToken.value,
          password: forgotPasswordForm.newPassword
        })
        ElMessage.success('密码重置成功')
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.message || '密码重置失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 返回登录页
function goToLogin() {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.forgot-password-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100%;
  background-color: #f3f5f7;
  background-image: url('@/assets/login-bg.jpg');
  background-size: cover;
  background-position: center;

  .forgot-password-form {
    width: 400px;
    max-width: 100%;
    padding: 35px 35px 15px 35px;
    margin: 0 auto;
    background: #ffffff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .title-container {
    position: relative;
    margin-bottom: 30px;

    .title {
      font-size: 24px;
      color: #303133;
      text-align: center;
      font-weight: 600;
    }
  }

  .security-question {
    font-size: 16px;
    color: #409EFF;
    padding: 10px;
    background-color: #ecf5ff;
    border-radius: 4px;
    margin-bottom: 10px;
  }

  .submit-button {
    width: 100%;
    margin-bottom: 20px;
  }

  .tips-container {
    margin-top: 20px;
    text-align: center;

    .actions {
      display: flex;
      justify-content: center;
    }
  }
}
</style> 