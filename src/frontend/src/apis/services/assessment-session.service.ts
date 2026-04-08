import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type { AssessmentSessionDTO } from '@/apis/schema/assessment.dto'

/**
 * 考核会话服务 API
 * 对应后端 /api/v1/assessment-sessions/* 接口
 */
export const assessmentSessionService = {
  /**
   * 获取当前用户的考核会话（含 deadline）
   * 对应后端 GET /api/v1/assessment-sessions/{assessmentTimeId}
   */
  async getSession(assessmentTimeId: number): Promise<ResponseMessage<AssessmentSessionDTO>> {
    const response = await apiClient.get<ResponseMessage<AssessmentSessionDTO>>(
      `/assessment-sessions/${assessmentTimeId}`
    )
    return response.data
  },
}
