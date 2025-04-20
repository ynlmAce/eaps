<template>
  <div class="login-container">
    <div class="left-banner">  </div>
    <div class="login-box">
      <!-- 登录表单区域 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        autocomplete="on"
        label-position="left"
      >
        <div class="title-container">
          <h3 class="title">大学生就业帮扶平台</h3>
        </div>

        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="密码"
            prefix-icon="Lock"
          >
            <template #suffix>
              <el-icon class="el-input__icon" @click="passwordVisible = !passwordVisible">
                <component :is="passwordVisible ? 'View' : 'Hide'" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <el-button type="text" @click="goToForgotPassword">忘记密码?</el-button>
        </div>

        <el-button
          type="primary"
          class="login-button"
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>

        <div class="register-container">
          <span>还没有账号? </span>
          <el-button type="text" @click="goToRegister">立即注册</el-button>
        </div>
      </el-form>
    </div>
  </div>

</template>



<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const passwordVisible = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 检测是否为移动设备
const isMobile = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
}

// 页面加载时检测设备类型
onMounted(() => {
  if (isMobile()) {
    ElMessage.warning('建议使用电脑访问以获得更好的体验')
  }
})

function handleLogin() {
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await userStore.login(loginForm)
        router.push({ path: '/' })
      } catch (error) {
        ElMessage.error(error.message || '登录失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

function goToRegister() {
  router.push('/register')
}

function goToForgotPassword() {
  router.push('/forgot-password')
}
</script>

<style lang="scss" scoped>
/* 页面初始化，移除默认 margin/padding */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  overflow: hidden; // 阻止滚动条
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}

body{
  display: unset !important;
}

.login-container {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

/* 关键：防止 left-banner 撑破父容器宽度 */
.left-banner {
  flex-grow: 1;
  min-width: 0;
  background-image: url('@/assets/login-bg.jpg');
  background-size: cover;
  background-position: center;
  height: 100vh;
}

/* 登录区域固定宽度，不超过视口 */
.login-box {
  width: 480px;
  max-width: 100vw;
  height: 100%;
  margin-left: auto;
  background-color: #fff;
  padding: 60px 50px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-shadow: -8px 0 20px rgba(0, 0, 0, 0.05);
}

/* 标题部分 */
.title-container {
  text-align: center;
  margin-bottom: 40px;

  .title {
    font-size: 28px;
    font-weight: bold;
    color: #1f2d3d;
  }
}

/* 表单按钮等 */
.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 20px 0;
  font-size: 14px;
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: bold;
  letter-spacing: 1px;
  margin-bottom: 20px;
}

.register-container {
  color: black;
  text-align: right;
  font-size: 14px;
}
@media (max-width: 768px) {
  .left-banner {
    display: none; // ✅ 可选：移动端不显示背景图
  }

  .login-container {
    flex-direction: column;
  }

  .login-box {
    width: 100%;
    height: 100%;
  }
}
</style>



