import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type { QuestionStatisticsDTO } from '@/apis/schema/assessment.dto'

/**
 * 考生端题目统计 API
 * 对应后端 /api/v1/assessment-statistics/* 接口，仅在后端配置开启时返回数据。
 */
export const assessmentStatisticsService = {
  async getCandidateQuestionStatistics(
    questionId: number
  ): Promise<ResponseMessage<QuestionStatisticsDTO>> {
    const response = await apiClient.get<ResponseMessage<QuestionStatisticsDTO>>(
      `/assessment-statistics/questions/${questionId}`
    )
    return response.data
  },
}
