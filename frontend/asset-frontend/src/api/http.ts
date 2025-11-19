import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { useAuthStore } from '@/store/auth'
import type { ApiResponse } from '@/types/api'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

interface HttpInstance extends AxiosInstance {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
  post<T = any>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
  put<T = any>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T>
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T>
}

const http = axios.create({
  baseURL,
  timeout: 15000
}) as HttpInstance

declare module 'axios' {
  export interface AxiosRequestConfig {
    _retry?: boolean
  }
}

http.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

http.interceptors.response.use(
  (response): any => {
    const data: ApiResponse<unknown> | AxiosResponse['data'] = response.data
    if (data && typeof (data as ApiResponse<unknown>).code === 'number') {
      const apiData = data as ApiResponse<unknown>
      if (apiData.code === 0) {
        return apiData.data
      }
      return Promise.reject(new Error(apiData.message || '请求失败'))
    }
    return data
  },
  async (error) => {
    const auth = useAuthStore()
    const originalRequest: AxiosRequestConfig = error.config || {}
    if (error.response?.status === 401 && !originalRequest._retry && auth.refreshToken) {
      originalRequest._retry = true
      try {
        await auth.refreshTokens()
        originalRequest.headers = originalRequest.headers || {}
        originalRequest.headers.Authorization = `Bearer ${auth.accessToken}`
        return http(originalRequest)
      } catch (refreshError) {
        await auth.logout()
        return Promise.reject(refreshError)
      }
    }
    return Promise.reject(error)
  }
)

export default http

