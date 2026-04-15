import request from './request'

export function uploadFileApi(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  return request.post<unknown, string>('/api/file/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
