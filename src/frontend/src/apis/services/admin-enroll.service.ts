import { apiClient } from '../client'
import type {
  ResponseMessage,
  PageDTO,
  EnrollmentBriefDTO,
  EnrollmentDetailDTO,
  EnrollmentStatisticsDTO,
  EnrollmentApprovalResultDTO,
  RejectEnrollmentRequestDTO,
  EnrollmentListQueryDTO,
} from '../schema/type'

export const adminEnrollService = {
  /**
   * 获取报名列表（分页）
   * GET /admin/enrollments
   */
  async getList(
    params: EnrollmentListQueryDTO = {}
  ): Promise<ResponseMessage<PageDTO<EnrollmentBriefDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<EnrollmentBriefDTO>>>(
      '/admin/enrollments',
      { params }
    )
    return response.data
  },

  /**
   * 获取报名详情
   * GET /admin/enrollments/{id}
   */
  async getDetail(id: number): Promise<ResponseMessage<EnrollmentDetailDTO>> {
    const response = await apiClient.get<ResponseMessage<EnrollmentDetailDTO>>(
      `/admin/enrollments/${id}`
    )
    return response.data
  },

  /**
   * 通过报名
   * PUT /admin/enrollments/{id}/approve
   */
  async approve(id: number): Promise<ResponseMessage<EnrollmentApprovalResultDTO>> {
    const response = await apiClient.put<ResponseMessage<EnrollmentApprovalResultDTO>>(
      `/admin/enrollments/${id}/approve`
    )
    return response.data
  },

  /**
   * 拒绝报名
   * PUT /admin/enrollments/{id}/reject
   */
  async reject(
    id: number,
    data: RejectEnrollmentRequestDTO = {}
  ): Promise<ResponseMessage<EnrollmentApprovalResultDTO>> {
    const response = await apiClient.put<ResponseMessage<EnrollmentApprovalResultDTO>>(
      `/admin/enrollments/${id}/reject`,
      data
    )
    return response.data
  },

  /**
   * 获取报名统计数据
   * GET /admin/enrollments/statistics
   */
  async getStatistics(): Promise<ResponseMessage<EnrollmentStatisticsDTO>> {
    const response = await apiClient.get<ResponseMessage<EnrollmentStatisticsDTO>>(
      '/admin/enrollments/statistics'
    )
    return response.data
  },
}
