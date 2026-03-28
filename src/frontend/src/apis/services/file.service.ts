import { publicClient } from '../client'
import { ResponseMessage, FileInfo } from '../schema/type'

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
}
