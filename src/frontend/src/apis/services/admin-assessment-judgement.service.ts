import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type {
  AssessmentDecisionDTO,
  AssessmentDecisionRequestDTO,
  AssessmentJudgementDTO,
  ManualReviewRequestDTO,
} from '@/apis/schema/assessment.dto'

/**
 * 管理端评判 API
 * 对应后端 /api/v1/admin/assessment-judgements/* 接口
 */
export const adminAssessmentJudgementService = {
  async getByQuestion(questionId: number): Promise<ResponseMessage<AssessmentJudgementDTO[]>> {
    const response = await apiClient.get<ResponseMessage<AssessmentJudgementDTO[]>>(
      '/admin/assessment-judgements',
      { params: { questionId } }
    )
    return response.data
  },

  async manualReview(
    request: ManualReviewRequestDTO
  ): Promise<ResponseMessage<AssessmentJudgementDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentJudgementDTO>>(
      '/admin/assessment-judgements/manual-review',
      request
    )
    return response.data
  },

  async decide(
    request: AssessmentDecisionRequestDTO
  ): Promise<ResponseMessage<AssessmentDecisionDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentDecisionDTO>>(
      '/admin/assessment-judgements/decisions',
      request
    )
    return response.data
  },
}
