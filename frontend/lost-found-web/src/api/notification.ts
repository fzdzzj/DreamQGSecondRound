import request from './request'
import type { NotificationVO } from '@/types/notification'
import type { UnreadCountVO } from '@/types/comment'
import type { PageResult } from '@/types/common'

export function getNotificationUnreadApi() {
  return request.get<unknown, UnreadCountVO>('/notification/user/unread')
}

export function getNotificationPageApi(pageNum = 1, pageSize = 10) {
  return request.get<unknown, PageResult<NotificationVO>>('/notification/user', {
    params: { pageNum, pageSize }
  })
}

export function markNotificationReadApi(id: number) {
  return request.put(`/notification/${id}/read`)
}

export function deleteNotificationApi(id: number) {
  return request.delete(`/notification/${id}`)
}
