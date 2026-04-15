import type { PageResult } from './common'

export interface CommentAddDTO {
  itemId: number
  content: string
  parentId?: number
}

export interface CommentStatVO {
  id: number
  itemId: number
  userId: number
  parentId?: number
  content: string
  nickname?: string
  avatar?: string
  isRead: number
  deleted?: number
  createTime?: string
  children?: CommentStatVO[]
}

export interface CommentDetailVO {
  id: number
  itemId: number
  userId: number
  content: string
  parentId?: number
  isRead: number
  nickname?: string
  avatar?: string
  deleted?: number
  createTime?: string
  updateTime?: string
}

export interface UnreadCountVO {
  count: number
}

export type CommentPageResult = PageResult<CommentStatVO>


export interface UnreadCountVO {
  count: number
}