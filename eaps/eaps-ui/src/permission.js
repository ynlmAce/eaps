import router from './router'
import { useUserStore } from './store/modules/user'
import { usePermissionStore } from './store/modules/permission'
import NProgress from 'nprogress'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register', '/forgot-password', '/reset-password'] // 免登录白名单

router.beforeEach(async(to, from, next) => {
  NProgress.start()
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()
  
  const hasToken = getToken()
  
  if (hasToken) {
    if (to.path === '/login') {
      // 已登录，重定向到首页
      next({ path: '/' })
      NProgress.done()
    } else {
      // 确定用户是否已获取其权限角色
      const hasRoles = userStore.roles && userStore.roles.length > 0
      if (hasRoles) {
        next()
      } else {
        try {
          // 获取用户信息
          // 注意: 角色必须是一个数组! 例如: ['admin'] 或 ['developer', 'editor']
          const { roles } = await userStore.getInfo()

          // 根据角色生成可访问路由
          const accessRoutes = await permissionStore.generateRoutes(roles)

          // 动态添加可访问路由
          accessRoutes.forEach(route => {
            router.addRoute(route)
          })

          // hack方法确保addRoutes已完成
          // 设置replace: true，这样导航就不会留下历史记录
          next({ ...to, replace: true })
        } catch (error) {
          // 清除令牌并重定向到登录页面
          await userStore.resetToken()
          ElMessage.error(error || '验证失败，请重新登录')
          next(`/login?redirect=${to.path}`)
          NProgress.done()
        }
      }
    }
  } else {
    // 没有令牌
    if (whiteList.indexOf(to.path) !== -1) {
      // 在免登录白名单中，直接进入
      next()
    } else {
      // 没有访问权限的其他页面被重定向到登录页面
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  // 完成进度条
  NProgress.done()
}) 