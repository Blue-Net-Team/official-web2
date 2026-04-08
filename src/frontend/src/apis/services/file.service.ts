import { publicClient, apiClient } from '../client'
import { ResponseMessage, FileInfo } from '../schema/type'

function extractFilenameFromHeaders(headers: Record<string, string>): string {
  const disposition = headers['content-disposition'] || ''
  const utf8Match = disposition.match(/filename\*=UTF-8''(.+?)(?:;|$)/)
  if (utf8Match) {
    return decodeURIComponent(utf8Match[1])
  }
  const asciiMatch = disposition.match(/filename="?(.+?)"?(?:;|$)/)
  if (asciiMatch) {
    return decodeURIComponent(asciiMatch[1])
  }
  return 'download'
}

export const fileService = {
  /**
   * 上传头像 - 公开接口，无需认证头
   * 对应后端 POST /api/v1/file/upload/avatar
   * 未登录用户上传会作为报名头像处理
   * @param file 文件对象
   */
  async uploadAvatar(file: File): Promise<ResponseMessage<FileInfo>> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await publicClient.post<ResponseMessage<FileInfo>>(
      '/file/upload/avatar',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    )
    return response.data
  },

  /**
   * 上传考题作品 - 需要登录
   * 对应后端 POST /api/v1/file/upload/assessment/work
   * @param file 文件对象
   * @param questionId 题目ID
   * @param onProgress 上传进度回调
   */
  async uploadWork(
    file: File,
    questionId: number,
    onProgress?: (progress: number) => void
  ): Promise<ResponseMessage<FileInfo>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('questionId', String(questionId))

    const response = await apiClient.post<ResponseMessage<FileInfo>>(
      '/file/upload/assessment/work',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        onUploadProgress: (event) => {
          if (event.total && onProgress) {
            onProgress(Math.round((event.loaded * 100) / event.total))
          }
        },
      }
    )
    return response.data
  },

  async downloadFile(fileId: number): Promise<void> {
    const response = await apiClient.get(`/file/download/${fileId}`, {
      responseType: 'blob',
    })

    const blob = response.data as Blob
    const headers = response.headers as Record<string, string>
    const filename = extractFilenameFromHeaders(headers)

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  },
}
