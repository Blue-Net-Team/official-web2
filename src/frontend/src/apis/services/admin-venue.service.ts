import { apiClient } from '../client'

import type {
  ResponseMessage,
  VenueDTO,
  CreateVenueRequestDTO,
  UpdateVenueRequestDTO,
} from '../schema/type'

export const adminVenueService = {
  /**
   * 创建场地
   * POST /admin/venues
   */
  async create(data: CreateVenueRequestDTO): Promise<ResponseMessage<VenueDTO>> {
    const response = await apiClient.post<ResponseMessage<VenueDTO>>('/admin/venues', data)
    return response.data
  },

  /**
   * 更新场地
   * PUT /admin/venues/{id}
   */
  async update(id: number, data: UpdateVenueRequestDTO): Promise<ResponseMessage<VenueDTO>> {
    const response = await apiClient.put<ResponseMessage<VenueDTO>>(`/admin/venues/${id}`, data)
    return response.data
  },

  /**
   * 删除场地
   * DELETE /admin/venues/{id}
   */
  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/venues/${id}`)
    return response.data
  },

  /**
   * 更新场地图片
   * PUT /admin/venues/{id}/image
   */
  async updateImage(id: number, imageFileId: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(`/admin/venues/${id}/image`, null, {
      params: { imageFileId },
    })
    return response.data
  },
}
