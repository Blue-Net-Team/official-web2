import { apiClient } from '../client'

import type {
  ResponseMessage,
  PageDTO,
  CompetitionResponseDTO,
  CompetitionRequestDTO,
  UpdateSortOrderRequestDTO,
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
   * 调整竞赛排序权重
   * PUT /admin/competitions/{id}/sort
   */
  async updateSortOrder(
    id: number,
    data: UpdateSortOrderRequestDTO
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      `/admin/competitions/${id}/sort`,
      data
    )
    return response.data
  },
}
