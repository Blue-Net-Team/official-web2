import { apiClient } from '../client'
import { ResponseMessage, PageDTO } from '../schema/type'
import type {
  AssessmentQuestionDTO,
  CreateQuestionRequestDTO,
  UpdateQuestionRequestDTO,
} from '@/apis/schema/assessment.dto'

/**
 * 管理端考题 API
 * 对应后端 /api/v1/admin/assessment-questions/* 接口
 */
export const adminAssessmentQuestionService = {
  /**
   * 分页查询考题列表（需指定考核时间ID）
   * GET /api/v1/admin/assessment-questions
   */
  async getList(
    assessmentTimeId: number,
    page = 0,
    size = 20
  ): Promise<ResponseMessage<PageDTO<AssessmentQuestionDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<AssessmentQuestionDTO>>>(
      '/admin/assessment-questions',
      { params: { assessmentTimeId, page, size } }
    )
    return response.data
  },

  /**
   * 创建考题
   * POST /api/v1/admin/assessment-questions
   */
  async create(data: CreateQuestionRequestDTO): Promise<ResponseMessage<AssessmentQuestionDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentQuestionDTO>>(
      '/admin/assessment-questions',
      data
    )
    return response.data
  },

  /**
   * 更新考题
   * PUT /api/v1/admin/assessment-questions/{id}
   */
  async update(
    id: number,
    data: UpdateQuestionRequestDTO
  ): Promise<ResponseMessage<AssessmentQuestionDTO>> {
    const response = await apiClient.put<ResponseMessage<AssessmentQuestionDTO>>(
      `/admin/assessment-questions/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除考题
   * DELETE /api/v1/admin/assessment-questions/{id}
   */
  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(
      `/admin/assessment-questions/${id}`
    )
    return response.data
  },
}
