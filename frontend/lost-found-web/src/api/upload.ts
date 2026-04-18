import request from './request'

export function uploadFileApi(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post<unknown, string>('/file/upload', formData)
}