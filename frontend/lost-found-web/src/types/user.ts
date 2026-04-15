export interface SysUserDetailVO {
  id: number
  username: string
  email?: string
  phone?: string
  nickname: string
  avatar?: string
  role: string
  roleDesc?: string
  status: number
  statusDesc?: string
  lastLoginIp?: string
  lastLoginTime?: string
  createTime?: string
  updateTime?: string
}

export interface SysUserStatVO {
  id: number
  username: string
  nickname: string
  email?: string
  phone?: string
  role: string
  roleDesc?: string
  status: number
  statusDesc?: string
  createTime?: string
}

export interface UpdateUserDTO {
  email?: string
  phone?: string
  nickname?: string
  avatar?: string
}

export interface ChangePasswordDTO {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export interface ChangePasswordByCodeDTO {
  email: string
  code: string
  newPassword: string
}


