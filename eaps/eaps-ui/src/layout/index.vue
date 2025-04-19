<template>
  <div class="layout">
    <el-container class="app-container">
      <el-aside width="220px" class="sidebar-container">
        <div class="logo-container">
          <img src="@/assets/logo.png" alt="Logo" class="logo" />
          <h1 class="title">就业帮扶系统</h1>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :unique-opened="true"
          class="sidebar-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router
        >
          <sidebar-item 
            v-for="route in permissionRoutes" 
            :key="route.path" 
            :item="route"
            :base-path="route.path"
          />
        </el-menu>
      </el-aside>
      <el-container class="main-container">
        <el-header height="50px" class="header">
          <div class="toggle-sidebar" @click="toggleSidebar">
            <el-icon><Fold v-if="!isCollapse" /><Expand v-else /></el-icon>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="avatar-container">
                <img src="@/assets/avatar.jpg" class="avatar" />
                <span class="username">{{ username }}</span>
                <el-icon><arrow-down /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
        <el-footer height="40px" class="footer">
          <div class="copyright">
            © {{ new Date().getFullYear() }} 大学生就业帮扶系统
          </div>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowDown, Fold, Expand } from '@element-plus/icons-vue'
import SidebarItem from './components/SidebarItem.vue'

// 用户信息
const username = ref('管理员')

// 侧边栏折叠状态
const isCollapse = ref(false)

// 路由
const router = useRouter()
const route = useRoute()

// 根据权限过滤后的路由（实际项目中应该从store中获取）
const permissionRoutes = computed(() => {
  // 模拟从store获取的路由
  return router.options.routes.filter(route => !route.hidden)
})

// 激活的菜单
const activeMenu = computed(() => {
  const { path, meta } = route
  if (meta && meta.activeMenu) {
    return meta.activeMenu
  }
  return path
})

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 处理头像下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile/index')
      break
    case 'settings':
      // 打开设置页面
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 实际项目中应该调用登出API
        router.push('/login')
      }).catch(() => {})
      break
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
  width: 100%;
}

.app-container {
  height: 100%;
}

.sidebar-container {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo-container {
  height: 50px;
  display: flex;
  align-items: center;
  padding: 0 15px;
  background-color: #2b3a4a;
}

.logo {
  width: 30px;
  height: 30px;
  margin-right: 10px;
}

.title {
  color: #fff;
  font-size: 16px;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-menu {
  border-right: none;
  height: calc(100% - 50px);
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
}

.toggle-sidebar {
  font-size: 20px;
  cursor: pointer;
}

.header-right {
  display: flex;
  align-items: center;
}

.avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  margin-right: 8px;
}

.username {
  margin-right: 4px;
}

.main-container {
  height: 100%;
  overflow: hidden;
}

.el-main {
  background-color: #f0f2f5;
  padding: 15px;
  overflow-y: auto;
}

.footer {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  border-top: 1px solid #e6e6e6;
  font-size: 12px;
  color: #666;
}
</style>