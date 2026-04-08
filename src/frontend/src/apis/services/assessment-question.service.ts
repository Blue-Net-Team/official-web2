import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type {
  AssessmentQuestionDTO,
  UserQuestionListResponseDTO,
} from '@/apis/schema/assessment.dto'

/**
 * 考题服务 API
 * 对应后端 /api/v1/assessment-questions/* 接口
 */
export const assessmentQuestionService = {
  /**
   * 查询考题目录（用户端，分页）
   * 对应后端 GET /api/v1/assessment-questions
   */
  async getQuestions(
    assessmentTimeId: number,
    page = 0,
    size = 10
  ): Promise<ResponseMessage<UserQuestionListResponseDTO>> {
    const response = await apiClient.get<ResponseMessage<UserQuestionListResponseDTO>>(
      '/assessment-questions',
      { params: { assessmentTimeId, page, size } }
    )
    return response.data
  },

  /**
   * 查询题目详情（含content）
   * 对应后端 GET /api/v1/assessment-questions/:id
   */
  async getQuestionDetail(id: number): Promise<ResponseMessage<AssessmentQuestionDTO>> {
    const response = await apiClient.get<ResponseMessage<AssessmentQuestionDTO>>(
      `/assessment-questions/${id}`
    )
    return response.data
  },
}
