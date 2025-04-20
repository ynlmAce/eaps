<template>
  <div class="register-container">
    <el-form
      ref="registerFormRef"
      :model="registerForm"
      :rules="registerRules"
      class="register-form"
      label-position="top"
    >
      <div class="title-container">
        <h3 class="title">用户注册</h3>
      </div>

      <!-- 用户类型选择 -->
      <el-form-item label="用户类型" prop="userType">

        <el-radio-group  v-model="registerForm.userType" style="gap: 30px;">
          <el-radio :label="0">学生</el-radio>
          <el-radio :label="3">企业</el-radio>
          <el-radio :label="4">辅导员</el-radio>
        </el-radio-group>

      </el-form-item>


      <!-- 基本信息 -->
      <el-form-item label="用户名" prop="username">
        <el-input 
          v-model="registerForm.username" 
          placeholder="请输入用户名"
          prefix-icon="User"
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input 
          v-model="registerForm.password" 
          placeholder="请输入密码" 
          type="password"
          prefix-icon="Lock" 
          show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input 
          v-model="registerForm.confirmPassword" 
          placeholder="请再次输入密码" 
          type="password"
          prefix-icon="Lock" 
          show-password
        />
      </el-form-item>

      <el-form-item label="电子邮箱" prop="email">
        <el-input 
          v-model="registerForm.email" 
          placeholder="请输入电子邮箱"
          prefix-icon="Message"
        />
      </el-form-item>

      <el-form-item label="手机号码" prop="phone">
        <el-input 
          v-model="registerForm.phone" 
          placeholder="请输入手机号码"
          prefix-icon="Phone"
        />
      </el-form-item>

      <!-- 学生特有信息 -->
      <template v-if="registerForm.userType === 0">
        <div class="section-title">学生信息</div>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="registerForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNumber">
          <el-input v-model="registerForm.studentNumber" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="学院" prop="college">
          <el-input v-model="registerForm.college" placeholder="请输入学院" />
        </el-form-item>
        <el-form-item label="专业" prop="major">
          <el-input v-model="registerForm.major" placeholder="请输入专业" />
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="registerForm.className" placeholder="请输入班级" />
        </el-form-item>
      </template>

      <!-- 企业特有信息 -->
      <template v-if="registerForm.userType === 3">
        <div class="section-title">企业信息</div>
        <el-form-item label="企业名称" prop="enterpriseName">
          <el-input v-model="registerForm.enterpriseName" placeholder="请输入企业名称" />
        </el-form-item>
        <el-form-item label="统一社会信用代码" prop="creditCode">
          <el-input v-model="registerForm.creditCode" placeholder="请输入统一社会信用代码" />
        </el-form-item>
        <el-form-item label="法定代表人" prop="legalRepresentative">
          <el-input v-model="registerForm.legalRepresentative" placeholder="请输入法定代表人姓名" />
        </el-form-item>
        <el-form-item label="企业规模" prop="scale">
          <el-select v-model="registerForm.scale" placeholder="请选择企业规模" style="width: 100%">
            <el-option label="小型企业" value="small" />
            <el-option label="中型企业" value="medium" />
            <el-option label="大型企业" value="large" />
          </el-select>
        </el-form-item>
        <el-form-item label="企业类型" prop="type">
          <el-select v-model="registerForm.type" placeholder="请选择企业类型" style="width: 100%">
            <el-option label="国有企业" value="state" />
            <el-option label="民营企业" value="private" />
            <el-option label="外资企业" value="foreign" />
            <el-option label="合资企业" value="joint" />
          </el-select>
        </el-form-item>
        <el-form-item label="企业简介" prop="introduction">
          <el-input 
            v-model="registerForm.introduction" 
            type="textarea" 
            placeholder="请输入企业简介"
            rows="4"
          />
        </el-form-item>
      </template>

      <!-- 辅导员特有信息 -->
      <template v-if="registerForm.userType === 4">
        <div class="section-title">辅导员信息</div>
        <el-form-item label="姓名" prop="counselorName">
          <el-input v-model="registerForm.counselorName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="工号" prop="employeeNumber">
          <el-input v-model="registerForm.employeeNumber" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="所属学院" prop="counselorCollege">
          <el-input v-model="registerForm.counselorCollege" placeholder="请输入所属学院" />
        </el-form-item>
        <el-form-item label="职务" prop="position">
          <el-input v-model="registerForm.position" placeholder="请输入职务" />
        </el-form-item>
      </template>

      <!-- 注册按钮 -->
      <el-button
        :loading="loading"
        type="primary"
        class="register-button"
        @click="handleRegister"
      >
        注册
      </el-button>

      <div class="login-tip">
        已有账号？<el-button type="text" @click="goToLogin">立即登录</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

// 注册表单数据
const registerForm = reactive({
  userType: 0, // 默认为学生类型
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  // 学生信息
  realName: '',
  studentNumber: '',
  college: '',
  major: '',
  className: '',
  // 企业信息
  enterpriseName: '',
  creditCode: '',
  legalRepresentative: '',
  scale: '',
  type: '',
  introduction: '',
  // 辅导员信息
  counselorName: '',
  employeeNumber: '',
  counselorCollege: '',
  position: ''
})

// 校验规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 20, message: '长度在 4 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  userType: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  // 学生信息验证规则
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  studentNumber: [
    { required: true, message: '请输入学号', trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请输入学院', trigger: 'blur' }
  ],
  major: [
    { required: true, message: '请输入专业', trigger: 'blur' }
  ],
  className: [
    { required: true, message: '请输入班级', trigger: 'blur' }
  ],
  // 企业信息验证规则
  enterpriseName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  creditCode: [
    { required: true, message: '请输入统一社会信用代码', trigger: 'blur' }
  ],
  legalRepresentative: [
    { required: true, message: '请输入法定代表人姓名', trigger: 'blur' }
  ],
  scale: [
    { required: true, message: '请选择企业规模', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择企业类型', trigger: 'change' }
  ],
  // 辅导员信息验证规则
  counselorName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  employeeNumber: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  counselorCollege: [
    { required: true, message: '请输入所属学院', trigger: 'blur' }
  ],
  position: [
    { required: true, message: '请输入职务', trigger: 'blur' }
  ]
}

// 根据用户类型动态设置验证规则
watch(() => registerForm.userType, (newType) => {
  // 可以在此处处理不同用户类型的表单验证逻辑
})

// 注册处理
function handleRegister() {
  registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 准备注册数据
        const registerData = {
          username: registerForm.username,
          password: registerForm.password,
          email: registerForm.email,
          phone: registerForm.phone,
          userType: registerForm.userType
        }

        // 根据用户类型添加特定信息
        if (registerForm.userType === 0) {
          registerData.studentInfo = {
            realName: registerForm.realName,
            studentNumber: registerForm.studentNumber,
            college: registerForm.college,
            major: registerForm.major,
            className: registerForm.className
          }
        } else if (registerForm.userType === 3) {
          registerData.enterpriseInfo = {
            enterpriseName: registerForm.enterpriseName,
            creditCode: registerForm.creditCode,
            legalRepresentative: registerForm.legalRepresentative,
            scale: registerForm.scale,
            type: registerForm.type,
            introduction: registerForm.introduction
          }
        } else if (registerForm.userType === 4) {
          registerData.counselorInfo = {
            counselorName: registerForm.counselorName,
            employeeNumber: registerForm.employeeNumber,
            counselorCollege: registerForm.counselorCollege,
            position: registerForm.position
          }
        }

        await register(registerData)
        ElMessage.success('注册成功，请等待管理员审核')
        router.push('/login')
      } catch (error) {
        ElMessage.error(error.message || '注册失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到登录页
function goToLogin() {
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  width: 100%;
  background-color: #f3f5f7;
  padding: 50px 0;
  background-image: url('@/assets/login-bg.jpg');
  background-size: cover;
  background-position: center;
  
  .register-form {
    width: 500px;
    max-width: 100%;
    padding: 35px 35px 15px 35px;
    margin: 0 auto;
    background: #ffffff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .title-container {
    margin-bottom: 30px;
    text-align: center;

    .title {
      font-size: 24px;
      color: #303133;
      font-weight: 600;
    }
  }

  .section-title {
    font-size: 18px;
    font-weight: 500;
    color: #409EFF;
    margin: 20px 0 10px;
    border-bottom: 1px solid #EBEEF5;
    padding-bottom: 10px;
  }

  .register-button {
    width: 100%;
    margin: 10px 0;
  }

  .login-tip {
    color: black;
    margin-top: 20px;
    text-align: right;
  }
}
</style> 