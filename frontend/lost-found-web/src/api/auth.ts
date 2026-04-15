import request from './request'
import type { LoginDTO, LoginResponseVO, RegisterDTO } from '@/types/auth'

export function loginApi(data: LoginDTO) {
  return request.post<unknown, LoginResponseVO>('/auth/login', data)
}

export function registerApi(data: RegisterDTO) {
  return request.post('/auth/register', data)
}

export function logoutApi() {
  return request.post('/auth/logout', null)
}
