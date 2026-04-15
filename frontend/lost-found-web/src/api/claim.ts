import request from './request'
import type { ApproveRequestDTO, BizClaimRequestDTO, BizClaimRequestVO } from '@/types/claim'

export function createClaimApi(data: BizClaimRequestDTO) {
  return request.post('/claim', data)
}

export function getPendingClaimApi() {
  return request.get<unknown, BizClaimRequestVO[]>('/claim/pending')
}

export function auditClaimApi(id: number, data: ApproveRequestDTO) {
  return request.put(`/claim/${id}/audit`, data)
}
