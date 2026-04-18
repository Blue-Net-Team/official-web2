import { apiClient } from '../client'
import { ResponseMessage, PageDTO } from '../schema/type'
import type { AssessmentTimeDTO } from '@/apis/schema/assessment.dto'
import type { Direction } from '@/apis/schema/enumerate'
import type {
  CreateAssessmentTimeRequestDTO,
  UpdateAssessmentTimeRequestDTO,
} from '@/apis/schema/assessment.dto'

/**
 * 管理端考核时间 API
 * 对应后端 /api/v1/admin/assessment-times/* 接口
 */
export const adminAssessmentTimeService = {
  /**
   * 分页查询考核时间列表
   * GET /api/v1/admin/assessment-times
   */
  async getList(page = 0, size = 20): Promise<ResponseMessage<PageDTO<AssessmentTimeDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<AssessmentTimeDTO>>>(
      '/admin/assessment-times',
      { params: { page, size } }
    )
    return response.data
  },

  /**
   * 创建考核时间
   * POST /api/v1/admin/assessment-times
   */
  async create(data: CreateAssessmentTimeRequestDTO): Promise<ResponseMessage<AssessmentTimeDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentTimeDTO>>(
      '/admin/assessment-times',
      data
    )
    return response.data
  },

  /**
   * 更新考核时间
   * PUT /api/v1/admin/assessment-times/{id}
   */
  async update(
    id: number,
    data: UpdateAssessmentTimeRequestDTO
  ): Promise<ResponseMessage<AssessmentTimeDTO>> {
    const response = await apiClient.put<ResponseMessage<AssessmentTimeDTO>>(
      `/admin/assessment-times/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除考核时间
   * DELETE /api/v1/admin/assessment-times/{id}
   */
  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/assessment-times/${id}`)
    return response.data
  },
}
