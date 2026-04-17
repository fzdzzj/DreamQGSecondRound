import request from './request'
import type { BizItemDetailVO, BizItemStatVO, ItemPageQueryDTO, LostBizItemDTO, UpdateBizItemDTO } from '@/types/item'
import type { PageResult } from '@/types/common'

export function publishLostItemApi(data: LostBizItemDTO) {
  return request.post('/item/lost', data)
}

export function publishFoundItemApi(data: LostBizItemDTO) {
  return request.post('/item/found', data)
}

export function updateItemApi(id: number, data: UpdateBizItemDTO) {
  return request.put(`/item/${id}`, data)
}

export function getItemDetailApi(id: number) {
  return request.get<unknown, BizItemDetailVO>(`/item/${id}`)
}

export function deleteItemApi(id: number) {
  return request.delete(`/item/${id}`)
}

export function getItemPageApi(data: ItemPageQueryDTO) {
  return request.post<unknown, PageResult<BizItemStatVO>>('/item', data)
}

export function getMyItemPageApi(params: ItemPageQueryDTO) {
  return request.get<unknown, PageResult<BizItemStatVO>>('/item/my', { params })
}

export function closeItemApi(id: number) {
  return request.put(`/item/${id}/close`)
}
