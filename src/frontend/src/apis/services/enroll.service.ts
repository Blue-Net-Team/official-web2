import { publicClient } from '../client'
import { ResponseMessage, CreateEnrollmentRequestDTO, EnrollmentBriefDTO } from '../schema/type'

export const enrollService = {
  /**
   * 提交报名 - 公开接口，无需认证头
   * 对应后端 POST /api/v1/enrollments
   * 成功返回 201，学号冲突返回 409
   * @param data 报名信息
   */
  async submitEnrollment(
    data: CreateEnrollmentRequestDTO
  ): Promise<ResponseMessage<EnrollmentBriefDTO>> {
    const response = await publicClient.post<ResponseMessage<EnrollmentBriefDTO>>(
      '/enrollments',
      data
    )
    return response.data
  },

  /**
   * 更新报名 - 公开接口，无需认证头
   * 对应后端 POST /api/v1/enrollments (forceUpdate=true)
   * 成功返回 200
   * @param data 报名信息
   */
  async updateEnrollment(
    data: CreateEnrollmentRequestDTO
  ): Promise<ResponseMessage<EnrollmentBriefDTO>> {
    const response = await publicClient.post<ResponseMessage<EnrollmentBriefDTO>>('/enrollments', {
      ...data,
      forceUpdate: true,
    })
    return response.data
  },
}
