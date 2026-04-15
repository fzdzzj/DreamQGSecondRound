import request from './request'
import type { ChangePasswordByCodeDTO, ChangePasswordDTO, SysUserDetailVO, UpdateUserDTO } from '@/types/user'

export function getPersonalInfoApi() {
  return request.get<unknown, SysUserDetailVO>('/common/personal-info')
}

export function updatePersonalInfoApi(data: UpdateUserDTO) {
  return request.put('/common/personal-info', data)
}

export function changePasswordApi(data: ChangePasswordDTO) {
  return request.put('/common/password', data)
}

export function changePasswordByCodeApi(data: ChangePasswordByCodeDTO) {
  return request.put('/common/password/by-code', data)
}
