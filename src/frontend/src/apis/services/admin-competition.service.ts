import { apiClient } from '../client'

import type {
  ResponseMessage,
  PageDTO,
  CompetitionResponseDTO,
  CompetitionRequestDTO,
  BatchSortRequestDTO,
  MoveCompetitionRequestDTO,
} from '../schema/type'

export const adminCompetitionService = {
  /**
   * 获取竞赛分页列表
   * GET /competitions/page
   */
  async getList(
    page: number,
    size: number
  ): Promise<ResponseMessage<PageDTO<CompetitionResponseDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<CompetitionResponseDTO>>>(
      '/competitions/page',
      { params: { page, size } }
    )
    return response.data
  },

  /**
   * 创建竞赛
   * POST /admin/competitions
   */
  async create(data: CompetitionRequestDTO): Promise<ResponseMessage<CompetitionResponseDTO>> {
    const response = await apiClient.post<ResponseMessage<CompetitionResponseDTO>>(
      '/admin/competitions',
      data
    )
    return response.data
  },

  /**
   * 更新竞赛
   * PUT /admin/competitions/{id}
   */
  async update(
    id: number,
    data: CompetitionRequestDTO
  ): Promise<ResponseMessage<CompetitionResponseDTO>> {
    const response = await apiClient.put<ResponseMessage<CompetitionResponseDTO>>(
      `/admin/competitions/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除竞赛
   * DELETE /admin/competitions/{id}
   */
  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/competitions/${id}`)
    return response.data
  },

  /**
   * 批量调整竞赛排序
   * PUT /admin/competitions/sort
   */
  async batchUpdateSortOrder(data: BatchSortRequestDTO): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>('/admin/competitions/sort', data)
    return response.data
  },

  /**
   * 移动竞赛排序（上移/下移一位）
   * PUT /admin/competitions/{id}/move
   */
  async moveCompetition(
    id: number,
    data: MoveCompetitionRequestDTO
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      `/admin/competitions/${id}/move`,
      data
    )
    return response.data
  },
}
