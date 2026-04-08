import { apiClient } from '../client'
import { ResponseMessage, PageDTO } from '../schema/type'
import type { AssessmentTimeDTO, AssessmentProgressDTO } from '@/apis/schema/assessment.dto'

/**
 * 考核时间服务 API
 * 对应后端 /api/v1/assessment-times/* 接口
 */
export const assessmentTimeService = {
  /**
   * 查询考核时间列表（用户端，带进度数据）
   * 对应后端 GET /api/v1/assessment-times
   */
  async getAssessmentTimes(
    page = 0,
    size = 20
  ): Promise<ResponseMessage<PageDTO<AssessmentTimeDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<AssessmentTimeDTO>>>(
      '/assessment-times',
      { params: { page, size } }
    )
    return response.data
  },

  /**
   * 查询考核进度
   * 对应后端 GET /api/v1/assessment-times/{id}/progress
   */
  async getAssessmentProgress(id: number): Promise<ResponseMessage<AssessmentProgressDTO>> {
    const response = await apiClient.get<ResponseMessage<AssessmentProgressDTO>>(
      `/assessment-times/${id}/progress`
    )
    return response.data
  },
}
