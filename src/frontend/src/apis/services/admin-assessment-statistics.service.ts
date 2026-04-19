import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type { QuestionStatisticsDTO } from '@/apis/schema/assessment.dto'

/**
 * 管理端考题统计 API
 * 对应后端 /api/v1/admin/assessment-statistics/* 接口
 */
export const adminAssessmentStatisticsService = {
  async getQuestionStatistics(questionId: number): Promise<ResponseMessage<QuestionStatisticsDTO>> {
    const response = await apiClient.get<ResponseMessage<QuestionStatisticsDTO>>(
      `/admin/assessment-statistics/questions/${questionId}`
    )
    return response.data
  },
}
