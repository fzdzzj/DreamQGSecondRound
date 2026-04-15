export interface BizRiskEvent {
  id: number
  riskType: number
  riskLevel: number
  title: string
  content: string
  relatedItemId?: number
  relatedUserId?: number
  location?: string
  timeWindow?: string
  evidenceJson?: string
  notifyStatus?: number
  handleStatus?: number
  handleRemark?: string
  handledBy?: number
  handledTime?: string
  createTime?: string
  updateTime?: string
}

export interface RiskHandleDTO {
  riskEventId?: number
  handleStatus: number
  handleRemark?: string
}
export interface BizRiskEvent {
  id: number
  title: string
  content: string
  riskLevel: number
}
