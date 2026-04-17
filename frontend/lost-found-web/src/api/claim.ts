import request from './request'
import type { ApproveRequestDTO, BizClaimRequestDTO, BizClaimRequestVO } from '@/types/claim'

export function createClaimApi(data: BizClaimRequestDTO) {
  return request.post('/biz/claim', data)
}

// 查询当前用户所有待审批申请
export function getPendingClaimRequestsApi(status: string) {
  return request.get(`/biz/claim?status=${status}`)
}

// 审批单条认领申请
export function approveClaimRequestApi(id: number, data: ApproveRequestDTO) {
  return request.post(`/biz/claim/${id}/audit`, data)
}