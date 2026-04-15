export interface AdminStatisticsQueryRangeVO {
  startTime?: string
  endTime?: string
}

export interface AdminStatisticsVO {
  publishCount: number
  foundCount: number
  activeUserCount: number
  range?: AdminStatisticsQueryRangeVO
}

export interface AdminStatisticsVO {
  publishCount: number
  foundCount: number
  activeUserCount: number
}

