<template>
  <div class="reset-password-container">
    <el-form
      ref="resetPasswordFormRef"
      :model="resetPasswordForm"
      :rules="resetPasswordRules"
      class="reset-password-form"
      label-position="top"
    >
      <div class="title-container">
        <h3 class="title">重置密码</h3>
      </div>

      <el-form-item label="新密码" prop="password">
        <el-input 
          v-model="resetPasswordForm.password" 
          placeholder="请输入新密码" 
          type="password"
          prefix-icon="Lock" 
          show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input 
          v-model="resetPasswordForm.confirmPassword" 
          placeholder="请再次输入新密码" 
          type="password"
          prefix-icon="Lock" 
          show-password
        />
      </el-form-item>

      <el-button
        :loading="loading"
        type="primary"
        class="submit-button"
        @click="handleSubmit"
      >
        确认重置
      </el-button>

      <div class="tips-container">
        <div class="actions">
          <el-button type="text" @click="goToLogin">返回登录</el-button>
        </div>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resetPassword } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const resetPasswordFormRef = ref(null)
const loading = ref(false)
const token = ref('')

// 表单数据
const resetPasswordForm = reactive({
  password: '',
  confirmPassword: ''
})

// 校验规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else {
    if (resetPasswordForm.confirmPassword !== '') {
      resetPasswordFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== resetPasswordForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

// 表单验证规则
const resetPasswordRules = {
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

// 组件挂载时获取token
onMounted(() => {
  token.value = route.query.token
  if (!token.value) {
    ElMessage.error('重置密码链接无效')
    router.push('/login')
  }
})

// 提交表单
function handleSubmit() {
  resetPasswordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await resetPassword({
          token: token.value,
          password: resetPasswordForm.password
        })
        ElMessage.success('密码重置成功')
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.message || '密码重置失败，链接可能已过期')
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
.reset-password-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100%;
  background-color: #f3f5f7;
  background-image: url('@/assets/login-bg.jpg');
  background-size: cover;
  background-position: center;

  .reset-password-form {
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

    .actions {
      display: flex;
      justify-content: center;
    }
  }
}
</style> 