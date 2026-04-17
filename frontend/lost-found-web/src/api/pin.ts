import request from './request'
import type { PinApplyDTO, PinAuditDTO, PinRequestDetailVO, PinRequestStatVO } from '@/types/pin'
import type { PageResult } from '@/types/common'

export function applyPinApi(data: PinApplyDTO) {
  return request.post('/pin/apply', data)
}

export function getPinPageApi(params: Record<string, unknown>) {
  return request.get<unknown, PageResult<PinRequestStatVO>>('/pin/page', { params })
}

export function getPinDetailApi(id: number) {
  return request.get<unknown, PinRequestDetailVO>(`/pin/${id}`)
}

export function auditPinApi(data: PinAuditDTO) {
  return request.put(`/pin/audit`, data)
}

export function cancelPinApi(id: number) {
  return request.put(`/pin/${id}/cancel`)
}

export function getMyPinListApi() {
  return request.get<unknown, PinRequestStatVO[]>('/pin/mylist')
}