<template>
  <div class="error-container">
    <div class="error-content">
      <div class="error-image">
        <img src="@/assets/404.png" alt="404" />
      </div>
      <div class="error-text">
        <h1>404</h1>
        <h2>哎呀！页面找不到了</h2>
        <p>您访问的页面不存在或已被删除</p>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

function goHome() {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.error-container {
  height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7f9;
}

.error-content {
  display: flex;
  align-items: center;
  justify-content: center;
  max-width: 1200px;
  margin: 0 auto;
  
  @media (max-width: 768px) {
    flex-direction: column;
    text-align: center;
  }
}

.error-image {
  flex: 0 0 50%;
  max-width: 400px;
  
  img {
    width: 100%;
  }
}

.error-text {
  flex: 0 0 50%;
  padding: 0 30px;
  
  h1 {
    font-size: 72px;
    color: #409EFF;
    margin: 0;
    line-height: 1;
  }
  
  h2 {
    font-size: 28px;
    color: #303133;
    margin: 20px 0;
  }
  
  p {
    font-size: 16px;
    color: #606266;
    margin-bottom: 30px;
  }
}
</style> 