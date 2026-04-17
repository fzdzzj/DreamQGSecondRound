import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { getAccessToken, getRefreshToken, setAccessToken, setRefreshToken, clearToken } from '@/utils/token'
import { showError } from '@/utils/message'
import type { Result } from '@/types/common'

// 🔥 关键修复：baseURL 改成 /api，走 Vite 代理
const service = axios.create({
  baseURL: import.meta.env.VITE_BASE_API,
  timeout: 15000
})


let refreshing = false
let requestQueue: Array<(token: string) => void> = []

service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // 设置Content-Type请求头
  if (config.method === 'post' || config.method === 'put' || config.method === 'patch') {
    config.headers['Content-Type'] = 'application/json'
  }
  // 添加调试信息
  console.log('请求配置:', {
    url: config.url,
    method: config.method,
    headers: config.headers,
    params: config.params,
    data: config.data
  })
  return config
})

service.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    const res = response.data
    if (res.code !== 200) {
      showError(res.message || '请求失败')
      return Promise.reject(res)
    }
    return res.data as unknown as AxiosResponse['data']
  },
  async (error: AxiosError<Result<unknown>>) => {
    const status = error.response?.status
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true

      if (!refreshing) {
        refreshing = true
        try {
          const refreshToken = getRefreshToken()
          if (!refreshToken) {
            clearToken()
            location.href = '/login'
            return Promise.reject(error)
          }

          // 🔥 第二个关键修复：刷新 token 也走代理，不写死 8080
          const refreshResponse = await service.post<Result<{ accessToken: string; refreshToken?: string }>>(
            '/auth/refresh',
            null,
            {
              headers: {
                'Refresh-Token': refreshToken
              }
            }
          )

          const refreshData = refreshResponse.data.data
          setAccessToken(refreshData.accessToken)
          if (refreshData.refreshToken) {
            setRefreshToken(refreshData.refreshToken)
          }

          requestQueue.forEach((cb) => cb(refreshData.accessToken))
          requestQueue = []

          originalRequest.headers.Authorization = `Bearer ${refreshData.accessToken}`
          return service(originalRequest)
        } catch (refreshError) {
          clearToken()
          location.href = '/login'
          return Promise.reject(refreshError)
        } finally {
          refreshing = false
        }
      }

      return new Promise((resolve) => {
        requestQueue.push((token: string) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          resolve(service(originalRequest))
        })
      })
    }

    showError(error.response?.data?.message || '网络异常')
    return Promise.reject(error)
  }
)

export default service