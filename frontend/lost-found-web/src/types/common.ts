export interface Result<T> {
  code: number
  message: string
  data: T
  success: boolean
  timestamp: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface OptionItem<T = string | number> {
  label: string
  value: T
}
export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  pageNum: number
  pageSize: number
  total: number
}

