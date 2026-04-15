export interface BizClaimRequestDTO {
  itemId: number
  verificationAnswer: string
}

export interface ApproveRequestDTO {
  requestId?: number
  status: number
  remark?: string
}

export interface BizClaimRequestVO {
  id: number
  itemId: number
  applicantId: number
  verificationAnswer: string
  status: string | number
  pickupCode?: string
  createTime?: string
  updateTime?: string
}
export interface BizClaimRequestDTO {
  itemId: number
  verificationAnswer: string
}

export interface ApproveRequestDTO {
  status: number
}
