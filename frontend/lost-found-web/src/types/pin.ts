export interface PinAuditDTO {
  requestId?: number
  status: number
  remark?: string
}

export interface PinRequestStatVO {
  id: number
  itemId: number
  applicantId: number
  status: number
  statusDesc?: string
  createTime?: string
}

export interface PinRequestDetailVO {
  id: number
  itemId: number
  applicantId: number
  reason: string
  status: number
  statusDesc?: string
  auditAdminId?: number
  auditRemark?: string
  auditTime?: string
  createTime?: string
}

export interface PinApplyDTO {
  itemId: number
  reason: string
}

export interface PinAuditDTO {
  status: number
  remark?: string
}

