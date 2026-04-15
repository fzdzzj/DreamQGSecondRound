import axios, { AxiosError, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { getAccessToken, getRefreshToken, setAccessToken, setRefreshToken, clearToken } from '@/utils/token'
import { showError } from '@/utils/message'
import type { Result } from '@/types/common'

const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 15000
})

let refreshing = false
let requestQueue: Array<(token: string) => void> = []

service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 修复点：明确响应拦截器返回的是解包后的数据 T，而不是 AxiosResponse
// 这里使用 any 作为临时泛型占位，实际使用时 service.get<T> 会推断出正确类型
service.interceptors.response.use(
  (response: AxiosResponse<Result<unknown>>) => {
    const res = response.data
    if (res.code !== 200) {
      showError(res.message || '请求失败')
      return Promise.reject(res)
    }
    // 返回业务数据，而非整个 response 对象
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

          const refreshResponse = await axios.post<Result<{ accessToken: string; refreshToken?: string }>>(
            `http://localhost:8080/auth/refresh`,
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