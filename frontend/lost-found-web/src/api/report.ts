import request from './request'
import type { ReportAuditDTO, ReportDTO, ReportDetailVO, ReportListVO } from '@/types/report'
import type { PageResult } from '@/types/common'

export function createReportApi(data: ReportDTO) {
  return request.post('/report', data)
}

export function getReportPageApi(params: Record<string, unknown>) {
  return request.get<unknown, PageResult<ReportListVO>>('/report', { params })
}

export function getReportDetailApi(id: number) {
  return request.get<unknown, ReportDetailVO>(`/report/${id}`)
}

export function auditReportApi(id: number, data: ReportAuditDTO) {
  return request.put(`/report/${id}/audit`, data)
}
