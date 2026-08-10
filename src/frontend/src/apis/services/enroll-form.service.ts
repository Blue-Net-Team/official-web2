import { apiClient, publicClient } from '../client'
import { ResponseMessage } from '../schema/type'

/**
 * 当前报名表 DTO
 */
export interface EnrollFormDTO {
  /** 文件ID，用于下载报名表 */
  fileId: number
  /** 上传时间 */
  createdAt: string
}

export const enrollFormService = {
  /**
   * 获取当前报名表 - 公开接口，无需认证
   * 对应后端 GET /api/v1/enroll-form
   * 无报名表时 data 为 null
   */
  async getCurrent(): Promise<ResponseMessage<EnrollFormDTO | null>> {
    const response = await publicClient.get<ResponseMessage<EnrollFormDTO | null>>('/enroll-form')
    return response.data
  },

  /**
   * 设置或更新报名表 - 管理端接口
   * 对应后端 POST /api/v1/admin/enroll-form?fileId=
   * 设置成功后旧报名表文件将被删除
   */
  async setEnrollForm(fileId: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>(
      `/admin/enroll-form?fileId=${fileId}`
    )
    return response.data
  },

  /**
   * 删除报名表 - 管理端接口
   * 对应后端 DELETE /api/v1/admin/enroll-form
   */
  async deleteEnrollForm(): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>('/admin/enroll-form')
    return response.data
  },
}
