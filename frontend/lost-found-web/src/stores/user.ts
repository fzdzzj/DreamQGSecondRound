import { defineStore } from 'pinia'
import { clearToken, getAccessToken, setAccessToken, setRefreshToken } from '@/utils/token'
import { getPersonalInfoApi } from '@/api/user'
import { logoutApi } from '@/api/auth'
import type { LoginResponseVO } from '@/types/auth'
import type { SysUserDetailVO } from '@/types/user'

interface UserState {
  accessToken: string
  userInfo: SysUserDetailVO | null
  role: string
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    accessToken: getAccessToken(),
    userInfo: null,
    role: localStorage.getItem('USER_ROLE') || ''
  }),
  getters: {
    isLogin: (state) => Boolean(state.accessToken)
  },
  actions: {
    setLogin(data: LoginResponseVO) {
      this.accessToken = data.accessToken
      this.role = data.user.role
      setAccessToken(data.accessToken)
      setRefreshToken(data.refreshToken)
      localStorage.setItem('USER_ROLE', data.user.role)
    },
    async fetchProfile() {
      const data = await getPersonalInfoApi()
      this.userInfo = data
      this.role = data.role
      localStorage.setItem('USER_ROLE', data.role)
      return data
    },
    async doLogout() {
      try {
        await logoutApi()
      } finally {
        this.logout()
      }
    },
    logout() {
      this.accessToken = ''
      this.userInfo = null
      this.role = ''
      localStorage.removeItem('USER_ROLE')
      clearToken()
    }
  }
})
