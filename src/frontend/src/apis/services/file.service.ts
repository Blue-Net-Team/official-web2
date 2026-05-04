import { publicClient, apiClient } from '../client'
import { ResponseMessage, FileInfo } from '../schema/type'
import type { FileType } from '../schema/enumerate'

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
   * 统一文件上传接口
   * 对应后端 POST /api/v1/file/upload
   * @param file 文件对象
   * @param type 文件类型枚举
   * @param onProgress 上传进度回调（可选）
   */
  async upload(
    file: File,
    type: FileType,
    onProgress?: (progress: number) => void
  ): Promise<ResponseMessage<FileInfo>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)

    const client = type === 'AVATAR' ? publicClient : apiClient

    const response = await client.post<ResponseMessage<FileInfo>>('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: onProgress
        ? (event) => {
            if (event.total) {
              onProgress(Math.round((event.loaded * 100) / event.total))
            }
          }
        : undefined,
    })
    return response.data
  },

  /**
   * 更新用户头像
   * 对应后端 PUT /api/v1/user/avatar
   * @param fileId 文件ID
   */
  async updateAvatar(fileId: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>('/user/avatar', { fileId })
    return response.data
  },

  async downloadFile(fileId: number, customFilename?: string): Promise<void> {
    const response = await apiClient.get(`/file/download/${fileId}`, {
      responseType: 'blob',
    })

    const blob = response.data as Blob
    const headers = response.headers as Record<string, string>
    const originalFilename = extractFilenameFromHeaders(headers)

    let filename = customFilename ?? originalFilename
    if (customFilename && originalFilename.includes('.')) {
      const ext = originalFilename.split('.').pop()
      if (ext) {
        filename = `${customFilename}.${ext}`
      }
    }

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
