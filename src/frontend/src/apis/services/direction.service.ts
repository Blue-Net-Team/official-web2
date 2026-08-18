import { apiClient, publicClient } from '../client'
import {
  DirectionLearningPathResponse,
  LearningStepDTO,
  LearningStepRequestDTO,
} from '../schema/direction.dto'
import { ResponseMessage } from '../schema/type'

/**
 * 方向相关 API 服务
 */
export const directionService = {
  /**
   * 获取方向学习路径数据
   * @param slug 方向标识（cv/embed/struct）
   * @returns 学习路径数据，包含相关链接
   */
  async getLearningPath(slug: string): Promise<DirectionLearningPathResponse> {
    const response = await publicClient.get<DirectionLearningPathResponse>(
      `/directions/${slug}/learning-path`
    )
    return response.data
  },
}

/**
 * 方向学习路径管理 API 服务（需方向管理员及以上权限）
 */
export const adminDirectionService = {
  /**
   * 获取方向学习路径数据（管理端复用公开查询接口）
   * @param slug 方向标识（cv/embed/struct）
   */
  async getLearningPath(slug: string): Promise<DirectionLearningPathResponse> {
    const response = await apiClient.get<DirectionLearningPathResponse>(
      `/directions/${slug}/learning-path`
    )
    return response.data
  },

  /**
   * 创建学习步骤
   * POST /admin/directions/{slug}/learning-steps
   */
  async createStep(
    slug: string,
    data: LearningStepRequestDTO
  ): Promise<ResponseMessage<LearningStepDTO>> {
    const response = await apiClient.post<ResponseMessage<LearningStepDTO>>(
      `/admin/directions/${slug}/learning-steps`,
      data
    )
    return response.data
  },

  /**
   * 更新学习步骤
   * PUT /admin/directions/learning-steps/{id}
   */
  async updateStep(
    id: number,
    data: LearningStepRequestDTO
  ): Promise<ResponseMessage<LearningStepDTO>> {
    const response = await apiClient.put<ResponseMessage<LearningStepDTO>>(
      `/admin/directions/learning-steps/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除学习步骤
   * DELETE /admin/directions/learning-steps/{id}
   */
  async deleteStep(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(
      `/admin/directions/learning-steps/${id}`
    )
    return response.data
  },
}
