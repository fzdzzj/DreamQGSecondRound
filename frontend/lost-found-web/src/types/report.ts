export interface ReportListVO {
  id: number
  itemId: number
  reporterId: number
  content: string
  status: number
  statusDesc: string
  createTime: string
}

export interface ReportPageQueryDTO {
  pageNum: number
  pageSize: number
  status?: number
  startTime?: string
  endTime?: string
  reporterId?: number
  itemId?: number
}export interface ReportListVO {
  id: number
  itemId: number
  reporterId: number
  content: string
  status: number
  statusDesc: string
  createTime: string
}

export interface ReportPageQueryDTO {
  pageNum: number
  pageSize: number
  status?: number
  startTime?: string
  endTime?: string
  reporterId?: number
  itemId?: number
}export interface ReportDTO {
  itemId: number
  reason: number
  detail?: string
}

export interface ReportAuditDTO {
  reportId?: number
  status: number
  remark?: string
}

export interface ReportListVO {
  id: number
  itemId: number
  reporterId: number
  status: number
  statusDesc?: string
  createTime?: string
}

export interface ReportDetailVO {
  id: number
  itemId: number
  reporterId: number
  reason: number
  detail?: string
  status: number
  statusDesc?: string
  adminId?: number
  auditRemark?: string
  auditTime?: string
  createTime?: string
}

export interface ReportDTO {
  itemId: number
  reason: number
  detail?: string
}

export interface ReportAuditDTO {
  status: number
  remark?: string
}