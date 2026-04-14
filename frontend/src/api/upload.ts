import { post } from './request'

export function uploadImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return post<string>('/upload/image', formData)
}
