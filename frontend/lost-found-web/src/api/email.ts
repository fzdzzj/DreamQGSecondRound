import request from './request'

export interface EmailSendCodeDTO {
  email: string
  type: string
}

export interface EmailVerifyCodeDTO {
  email: string
  code: string
  type: string
}

export function sendEmailCodeApi(data: EmailSendCodeDTO) {
  return request.post('/email/sendCode', data)
}

export function verifyEmailCodeApi(data: EmailVerifyCodeDTO) {
  return request.post('/email/verifyCode', data)
}
