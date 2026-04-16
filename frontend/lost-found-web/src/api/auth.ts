import request from './request'
import type { LoginDTO, LoginResponseVO, RegisterDTO } from '@/types/auth'
import { getRefreshToken } from '@/utils/token'
export function loginApi(data: LoginDTO) {
  return request.post<unknown, LoginResponseVO>('/auth/login', data)
}

export function registerApi(data: RegisterDTO) {
  return request.post('/auth/register', data)
}

export function logoutApi() {
  return request.post('/auth/logout', null, {
    headers: {
      'Refresh-Token': getRefreshToken()
    }
  })
}
export interface ChangePasswordDTO {
  email: string
  code: string
  newPassword: string
  confirmPassword: string
}

export function changePasswordApi(data: ChangePasswordDTO) {
  return request.post('/auth/change-password', data)
}

