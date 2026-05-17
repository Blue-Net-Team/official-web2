import { publicClient, apiClient } from '../client'
import { API_BASE_URL } from '../config'
import {
  ResponseMessage,
  FileInfo,
  PrepareUploadResponse,
  ConfirmUploadResponse,
} from '../schema/type'
import type { FileType } from '../schema/enumerate'
import SparkMD5 from 'spark-md5'

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

/**
 * 分片计算文件 MD5，每 10MB 让出事件循环避免阻塞 UI
 * @param file 目标文件
 * @param onProgress 计算进度回调（0-100）
 */
export async function calculateFileMd5(
  file: File,
  onProgress?: (progress: number) => void
): Promise<string> {
  return new Promise((resolve, reject) => {
    const chunkSize = 10 * 1024 * 1024 // 10MB
    const chunks = Math.ceil(file.size / chunkSize)
    const spark = new SparkMD5.ArrayBuffer()
    let currentChunk = 0

    const fileReader = new FileReader()

    fileReader.onload = (e) => {
      const result = e.target?.result as ArrayBuffer
      if (!result) {
        reject(new Error('读取文件失败'))
        return
      }
      spark.append(result)
      currentChunk++

      if (onProgress) {
        onProgress(Math.round((currentChunk / chunks) * 100))
      }

      if (currentChunk < chunks) {
        setTimeout(() => loadNext(), 0)
      } else {
        resolve(spark.end())
      }
    }

    fileReader.onerror = () => {
      reject(new Error('读取文件失败'))
    }

    function loadNext() {
      const start = currentChunk * chunkSize
      const end = Math.min(start + chunkSize, file.size)
      fileReader.readAsArrayBuffer(file.slice(start, end))
    }

    loadNext()
  })
}

export const fileService = {
  /**
   * 统一文件上传接口（已废弃，保留作为回滚备选）
   * 对应后端 POST /api/v1/file/upload
   * @deprecated 请使用预签名直传流程
   */
  async upload(
    file: File,
    type: FileType,
    onProgress?: (progress: number) => void
  ): Promise<ResponseMessage<FileInfo>> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)

    const client = type === 'AVATAR' || type === 'NORMAL_IMG' ? publicClient : apiClient

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
   * 预签名上传准备
   * 对应后端 POST /api/v1/file/prepare-upload
   * AVATAR/NORMAL_IMG 走 publicClient（允许匿名），其他类型走 apiClient
   */
  async prepareUpload(dto: {
    filename: string
    type: FileType
    size: number
    contentType: string
  }): Promise<ResponseMessage<PrepareUploadResponse>> {
    const client = dto.type === 'AVATAR' || dto.type === 'NORMAL_IMG' ? publicClient : apiClient
    const response = await client.post<ResponseMessage<PrepareUploadResponse>>(
      '/file/prepare-upload',
      dto
    )
    return response.data
  },

  /**
   * 预签名上传确认
   * 对应后端 POST /api/v1/file/confirm-upload
   */
  async confirmUpload(dto: {
    fileId: number
    callbackToken: string
    md5: string
    size: number
  }): Promise<ResponseMessage<ConfirmUploadResponse>> {
    const response = await apiClient.post<ResponseMessage<ConfirmUploadResponse>>(
      '/file/confirm-upload',
      dto
    )
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

  async downloadFile(fileId: number): Promise<void> {
    window.open(`${API_BASE_URL}/file/download/${fileId}`, '_blank')
  },

  async downloadBatch(
    entries: { fileId: number; filename: string }[],
    zipName: string
  ): Promise<void> {
    const response = await apiClient.post(
      '/file/download/batch',
      {
        entries,
        zipName,
      },
      {
        responseType: 'blob',
      }
    )

    const blob = response.data as Blob
    const headers = response.headers as Record<string, string>
    const originalFilename = extractFilenameFromHeaders(headers)
    const filename = originalFilename || zipName

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
