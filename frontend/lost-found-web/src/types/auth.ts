export interface LoginDTO {
  identifier: string
  password?: string
  loginType: 'PASSWORD' | 'EMAIL_CODE'
  code?: string
}

export interface RegisterDTO {
  username: string
  email: string
  phone?: string
  password: string
  passwordConfirm: string
  nickname: string
  code: string
}

export interface UserLoginVO {
  id: number
  username: string
  role: string
  nickname: string
  avatar?: string
}

export interface LoginResponseVO {
  accessToken: string
  refreshToken: string
  user: UserLoginVO
}
