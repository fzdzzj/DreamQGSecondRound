import request from './request'
import type { CommentAddDTO, CommentDetailVO, CommentStatVO, UnreadCountVO } from '@/types/comment'
import type { PageResult } from '@/types/common'

export function addCommentApi(data: CommentAddDTO) {
  return request.post('/comment', data)
}

export function getItemCommentsApi(itemId: number, pageNum = 1, pageSize = 10) {
  return request.get<unknown, PageResult<CommentStatVO>>(`/comment/item/${itemId}`, {
    params: { pageNum, pageSize }
  })
}

export function getCommentDetailApi(commentId: number) {
  return request.get<unknown, CommentDetailVO>(`/comment/${commentId}`)
}

export function deleteCommentApi(commentId: number) {
  return request.delete(`/comment/${commentId}`)
}

export function markCommentReadApi(commentId: number) {
  return request.put(`/comment/${commentId}/read`)
}

export function getItemUnreadCommentApi(itemId: number) {
  return request.get<unknown, UnreadCountVO>(`/comment/item/${itemId}/unread`)
}

export function getUserUnreadCommentApi() {
  return request.get<unknown, UnreadCountVO>('/comment/user/unread')
}
