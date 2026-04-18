// src/api/log.ts
import request from './request'

export interface OperationLog {
  id: number
  userId: number
  userName: string
  operation: string
  targetType: string
  targetId: number
  remark?: string
  createTime: string
}

export interface OperationLogPageDTO {
  pageNum?: number
  pageSize?: number
  userName?: string
  targetType?: string
  startTime?: string
  endTime?: string
}

export function getOperationLogPageApi(params: OperationLogPageDTO) {
  return request.post('/log/page', params)
}
