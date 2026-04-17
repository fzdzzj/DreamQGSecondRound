import request from './request'
import type { AdminStatisticsVO } from '@/types/admin'
import type { PageResult } from '@/types/common'
import type { SysUserDetailVO, SysUserStatVO } from '@/types/user'

export function getAdminUserPageApi(params: Record<string, unknown>) {
  return request.post<unknown, PageResult<SysUserStatVO>>('/admin/user', params)
}

export function getAdminUserDetailApi(id: number) {
  return request.get<unknown, SysUserDetailVO>(`/admin/user/${id}`)
}

export function disableAdminUserApi(id: number) {
  return request.put(`/admin/user/${id}/disable`)
}

export function enableAdminUserApi(id: number) {
  return request.put(`/admin/user/${id}/enable`)
}

export function adminDeleteItemApi(id: number) {
  return request.delete(`/item/${id}`)
}

export function getAdminStatisticsApi(params?: { startTime?: string; endTime?: string }) {
  return request.post<unknown, AdminStatisticsVO>('/admin/statistics', params || {})
}