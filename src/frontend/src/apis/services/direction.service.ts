import { publicClient } from '../client'
import { DirectionLearningPathResponse } from '../schema/direction.dto'

/**
 * 方向相关 API 服务
 */
export const directionService = {
  /**
   * 获取方向学习路径数据
   * @param slug 方向标识（cv/embed/struct）
   * @returns 学习路径数据，包含视频链接
   */
  async getLearningPath(slug: string): Promise<DirectionLearningPathResponse> {
    const response = await publicClient.get<DirectionLearningPathResponse>(
      `/directions/${slug}/learning-path`
    )
    return response.data
  },
}
