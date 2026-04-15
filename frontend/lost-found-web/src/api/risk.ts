import request from './request'
import type { BizRiskEvent, RiskHandleDTO } from '@/types/risk'
import type { PageResult } from '@/types/common'

export function getRiskPageApi(params: Record<string, unknown>) {
  return request.get<unknown, PageResult<BizRiskEvent>>('/admin/risk', { params })
}

export function getRiskDetailApi(id: number) {
  return request.get<unknown, BizRiskEvent>(`/admin/risk/${id}`)
}

export function handleRiskApi(id: number, data: RiskHandleDTO) {
  return request.put(`/admin/risk/${id}/handle`, data)
}
