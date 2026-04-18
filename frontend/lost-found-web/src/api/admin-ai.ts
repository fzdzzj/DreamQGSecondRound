import request from './request'

export interface AdminAiStatisticsQueryDTO {
  statDate?: string
  statType?: string
  pageNum?: number
  pageSize?: number
}

export interface AdminAiStatisticsVO {
  aiSummary: string
  modelName: string
  createTime: string
}

export function getAdminAiStatisticsApi(data: AdminAiStatisticsQueryDTO) {
  return request({
    url: '/admin/aiStatistics',
    method: 'post',
    data
  })
}
