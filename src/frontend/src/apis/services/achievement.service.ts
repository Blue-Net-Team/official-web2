import { publicClient } from '../client'
import { AchievementDTO, AchievementStatsDTO, PageDTO, ResponseMessage } from '../schema/type'

export interface AchievementQueryParams {
  type?: string
  awardLevel?: string
  year?: number
  page?: number
  size?: number
}

export const AchievementService = {
  /**
   * 获取成就列表（分页）
   */
  getAchievements: async (params?: AchievementQueryParams) => {
    const response = await publicClient.get<ResponseMessage<PageDTO<AchievementDTO>>>(
      '/achievements',
      {
        params,
      }
    )
    return response.data
  },

  /**
   * 获取成就统计
   */
  getAchievementStats: async () => {
    const response =
      await publicClient.get<ResponseMessage<AchievementStatsDTO>>('/achievements/stats')
    return response.data
  },
}
