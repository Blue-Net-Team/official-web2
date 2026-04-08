import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type { AssessmentAnswerDTO, CreateAnswerRequestDTO } from '@/apis/schema/assessment.dto'

/**
 * 答案服务 API
 * 对应后端 /api/v1/assessment-answers/* 接口
 */
export const assessmentAnswerService = {
  async createAnswer(
    request: CreateAnswerRequestDTO
  ): Promise<ResponseMessage<AssessmentAnswerDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentAnswerDTO>>(
      '/assessment-answers',
      request
    )
    return response.data
  },

  async updateAnswer(
    request: CreateAnswerRequestDTO
  ): Promise<ResponseMessage<AssessmentAnswerDTO>> {
    const response = await apiClient.put<ResponseMessage<AssessmentAnswerDTO>>(
      '/assessment-answers',
      request
    )
    return response.data
  },

  async getMyAnswer(questionId: number): Promise<ResponseMessage<AssessmentAnswerDTO>> {
    const response = await apiClient.get<ResponseMessage<AssessmentAnswerDTO>>(
      '/assessment-answers',
      { params: { questionId } }
    )
    return response.data
  },
}
