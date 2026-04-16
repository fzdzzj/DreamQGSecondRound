import request from './request'
import type { ApproveRequestDTO, BizClaimRequestDTO, BizClaimRequestVO } from '@/types/claim'

export function createClaimApi(data: BizClaimRequestDTO) {
  return request.post('/biz/claim', data)
}

export function getPendingClaimApi() {
  return request.get<unknown, BizClaimRequestVO[]>('/biz/claim/pending')
}

export function auditClaimApi(id: number, data: ApproveRequestDTO) {
  return request.put(`/biz/claim/${id}/audit`, data)
}