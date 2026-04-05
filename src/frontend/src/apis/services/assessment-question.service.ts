import { apiClient } from '../client'
import { ResponseMessage, PageDTO } from '../schema/type'
import type { AssessmentQuestionDTO } from '@/types/assessment'

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
  ): Promise<ResponseMessage<PageDTO<AssessmentQuestionDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<AssessmentQuestionDTO>>>(
      '/assessment-questions',
      { params: { assessmentTimeId, page, size } }
    )
    return response.data
  },
}
