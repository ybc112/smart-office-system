import { defineStore } from 'pinia'
import { login, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),

  actions: {
    // 登录
    async login(username, password) {
      try {
        const response = await login(username, password)
        if (response.code === 200 && response.data.success) {
          this.token = response.data.token
          this.userInfo = response.data.userInfo
          localStorage.setItem('token', this.token)
          localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
          return { success: true }
        } else {
          return { success: false, message: response.data.message || response.message }
        }
      } catch (error) {
        console.error('登录异常:', error)
        return { success: false, message: '登录失败，请检查网络连接' }
      }
    },

    // 登出
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    // 更新用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    }
  }
})
