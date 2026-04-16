export interface LostBizItemDTO {
  title: string
  description: string
  location: string
  happenTime: string
  contactMethod: string
  imageUrls: string[]
}

export interface UpdateBizItemDTO {
  title: string
  description: string
  location: string
  happenTime: string
  contactMethod: string
  status?: number
  imageUrls: string[]
}

export interface ItemPageQueryDTO {
  pageNum: number
  pageSize: number
  type?: number
  location?: string
  startTime?: string
  endTime?: string
  keyword?: string
  aiCategory?: string
}

export interface BizItemStatVO {
  id: number
  title: string
  location: string
  happenTime: string
  status: string
  statusDesc?: string
  description: string
  aiCategory?: string
  contactMethod?: string
}

export interface BizItemDetailVO {
  id: number
  type: string
  title: string
  description: string
  location: string
  happenTime: string
  status: string
  statusDesc?: string
  imageUrls: string[]
  aiStatus?: string
  aiCategory?: string
  aiTags?: string[]
  aiDescription?: string
  contactMethod?: string
}

export interface PinApplyDTO {
  itemId: number
  reason: string
}


export interface BizItemDetailVO extends BizItemStatVO {
  description: string
  imageUrls: string[]
  aiCategory?: string
  aiDescription?: string
}

export interface ItemPageQueryDTO {
  pageNum: number
  pageSize: number
  keyword?: string
  location?: string
}