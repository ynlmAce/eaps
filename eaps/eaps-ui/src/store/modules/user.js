import { defineStore } from 'pinia'
import { login, logout, getInfo } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import router, { resetRouter } from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    name: '',
    avatar: '',
    introduction: '',
    roles: [],
    permissions: []
  }),
  
  getters: {
    userInfo(state) {
      return {
        name: state.name,
        avatar: state.avatar,
        introduction: state.introduction,
        roles: state.roles,
        permissions: state.permissions
      }
    }
  },
  
  actions: {
    // 用户登录
    async login(userInfo) {
      const { username, password } = userInfo
      try {
        const response = await login({ username: username.trim(), password: password })
        const { data } = response
        this.setToken(data.token)
        return Promise.resolve()
      } catch (error) {
        return Promise.reject(error)
      }
    },
    
    // 获取用户信息
    async getInfo() {
      try {
        const response = await getInfo()
        const { data } = response
        if (!data) {
          return Promise.reject('Verification failed, please login again.')
        }
        
        const { roles, name, avatar, introduction, permissions } = data
        
        // 角色必须是非空数组
        if (!roles || roles.length <= 0) {
          return Promise.reject('getInfo: roles must be a non-null array!')
        }
        
        this.roles = roles
        this.name = name
        this.avatar = avatar
        this.introduction = introduction
        this.permissions = permissions
        
        return Promise.resolve(data)
      } catch (error) {
        return Promise.reject(error)
      }
    },
    
    // 用户登出
    async logout() {
      try {
        await logout()
        this.resetToken()
        resetRouter()
        return Promise.resolve()
      } catch (error) {
        return Promise.reject(error)
      }
    },
    
    // 重置令牌
    resetToken() {
      removeToken()
      this.token = ''
      this.roles = []
      this.permissions = []
    },
    
    // 设置令牌
    setToken(token) {
      this.token = token
      setToken(token)
    }
  },
  
  persist: {
    key: 'user',
    storage: localStorage,
    paths: ['token']
  }
}) 