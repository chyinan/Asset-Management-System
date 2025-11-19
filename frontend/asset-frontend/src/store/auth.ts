import { defineStore } from 'pinia'
import { jwtDecode } from 'jwt-decode'
import axios from 'axios'
import type { LoginPayload, TokenResponse } from '@/types/auth'
import {
  getAccessToken,
  getRefreshToken,
  removeAccessToken,
  removeRefreshToken,
  setAccessToken,
  setRefreshToken
} from '@/utils/storage'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
const authClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json'
  }
})

const authRequest = async <T>(url: string, data: unknown) => {
  const response = await authClient.post(url, data)
  const payload = response.data
  if (payload && payload.code === 0) {
    return payload.data as T
  }
  throw new Error(payload?.message || '请求失败')
}
interface JwtClaims {
  sub: string
  username?: string
  roles?: string[]
  permissions?: string[]
  exp?: number
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: getAccessToken(),
    refreshToken: getRefreshToken(),
    username: '',
    roles: [] as string[],
    permissions: [] as string[]
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken),
    hasPermission: (state) => (permission?: string | string[]) => {
      if (!permission) return true
      const perms = Array.isArray(permission) ? permission : [permission]
      return perms.every((p) => state.permissions.includes(p))
    }
  },
  actions: {
    parseToken(token: string) {
      const payload = jwtDecode<JwtClaims>(token)
      this.username = payload.username || ''
      this.roles = payload.roles || []
      this.permissions = payload.permissions || []
    },
    setTokens(accessToken: string, refreshToken: string) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      setAccessToken(accessToken)
      setRefreshToken(refreshToken)
      this.parseToken(accessToken)
    },
    async login(payload: LoginPayload) {
      const data = await authRequest<TokenResponse>('/auth/login', payload)
      this.setTokens(data.accessToken, data.refreshToken)
    },
    async refreshTokens() {
      if (!this.refreshToken) {
        throw new Error('No refresh token')
      }
      const data = await authRequest<TokenResponse>('/auth/refresh', { refreshToken: this.refreshToken })
      this.setTokens(data.accessToken, data.refreshToken)
    },
    async logout() {
      this.accessToken = ''
      this.refreshToken = ''
      this.username = ''
      this.roles = []
      this.permissions = []
      removeAccessToken()
      removeRefreshToken()
    }
  }
})

