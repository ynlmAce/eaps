<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

onMounted(() => {
  // 获取token或其他登录状态标识
  const token = localStorage.getItem('token')
  // 获取当前路径
  const currentPath = router.currentRoute.value.path
  // 允许访问的无需登录的路径
  const allowedPaths = ['/login', '/register', '/forgot-password', '/reset-password']
  
  // 如果未登录且当前路径不在允许列表中，重定向到登录页面
  if (!token && !allowedPaths.includes(currentPath)) {
    router.push('/login')
  }
})
</script>

<template>
  <router-view />
</template>

<style>
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

* {
  box-sizing: border-box;
}
</style>
