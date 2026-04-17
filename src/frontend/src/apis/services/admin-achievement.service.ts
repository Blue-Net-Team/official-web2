import { apiClient } from '../client'

import type {
  ResponseMessage,
  PageDTO,
  AchievementDTO,
  CreateAchievementRequestDTO,
  UpdateAchievementRequestDTO,
} from '../schema/type'

export const adminAchievementService = {
  /**
   * 获取成就分页列表
   * GET /admin/achievements?page=&size=
   */
  async getList(
    page: number,
    size: number,
    type?: string,
    awardLevel?: string,
    year?: number
  ): Promise<ResponseMessage<PageDTO<AchievementDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<AchievementDTO>>>(
      '/admin/achievements',
      { params: { page, size, type, awardLevel, year } }
    )
    return response.data
  },

  /**
   * 创建成就
   * POST /admin/achievements
   */
  async create(data: CreateAchievementRequestDTO): Promise<ResponseMessage<AchievementDTO>> {
    const response = await apiClient.post<ResponseMessage<AchievementDTO>>(
      '/admin/achievements',
      data
    )
    return response.data
  },

  /**
   * 更新成就
   * PUT /admin/achievements/{id}
   */
  async update(
    id: number,
    data: UpdateAchievementRequestDTO
  ): Promise<ResponseMessage<AchievementDTO>> {
    const response = await apiClient.put<ResponseMessage<AchievementDTO>>(
      `/admin/achievements/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除成就
   * DELETE /admin/achievements/{id}
   */
  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/achievements/${id}`)
    return response.data
  },
}
