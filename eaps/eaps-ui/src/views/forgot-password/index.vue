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

      <el-form-item label="用户名" prop="username">
        <el-input 
          v-model="forgotPasswordForm.username" 
          placeholder="请输入用户名"
          prefix-icon="User"
        />
      </el-form-item>

      <el-form-item label="电子邮箱" prop="email">
        <el-input 
          v-model="forgotPasswordForm.email" 
          placeholder="请输入注册时使用的电子邮箱"
          prefix-icon="Message"
        />
      </el-form-item>

      <el-button
        :loading="loading"
        type="primary"
        class="submit-button"
        @click="handleSubmit"
      >
        发送重置密码链接
      </el-button>

      <div class="tips-container">
        <p class="tip">系统将发送重置密码链接到您的邮箱，请注意查收</p>
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
import { forgotPassword } from '@/api/auth'

const router = useRouter()
const forgotPasswordFormRef = ref(null)
const loading = ref(false)

// 表单数据
const forgotPasswordForm = reactive({
  username: '',
  email: ''
})

// 表单验证规则
const forgotPasswordRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 提交表单
function handleSubmit() {
  forgotPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await forgotPassword({
          username: forgotPasswordForm.username,
          email: forgotPasswordForm.email
        })
        ElMessage.success('重置密码链接已发送至您的邮箱，请查收')
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.message || '操作失败，请检查用户名和邮箱是否匹配')
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

  .submit-button {
    width: 100%;
    margin-bottom: 20px;
  }

  .tips-container {
    margin-top: 20px;
    text-align: center;

    .tip {
      font-size: 14px;
      color: #606266;
      margin-bottom: 15px;
    }

    .actions {
      display: flex;
      justify-content: center;
    }
  }
}
</style> 