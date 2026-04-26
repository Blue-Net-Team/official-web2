import { apiClient } from '../client'

import type {
  ResponseMessage,
  EquipmentDTO,
  CreateEquipmentRequestDTO,
  UpdateEquipmentRequestDTO,
} from '../schema/type'

export const adminEquipmentService = {
  /**
   * 创建设备
   * POST /admin/equipments
   */
  async create(data: CreateEquipmentRequestDTO): Promise<ResponseMessage<EquipmentDTO>> {
    const response = await apiClient.post<ResponseMessage<EquipmentDTO>>('/admin/equipments', data)
    return response.data
  },

  /**
   * 更新设备
   * PUT /admin/equipments/{id}
   */
  async update(
    id: number,
    data: UpdateEquipmentRequestDTO
  ): Promise<ResponseMessage<EquipmentDTO>> {
    const response = await apiClient.put<ResponseMessage<EquipmentDTO>>(
      `/admin/equipments/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除设备
   * DELETE /admin/equipments/{id}
   */
  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/equipments/${id}`)
    return response.data
  },

  /**
   * 更新设备图片
   * PUT /admin/equipments/{id}/image
   */
  async updateImage(id: number, imageFileId: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      `/admin/equipments/${id}/image`,
      null,
      { params: { imageFileId } }
    )
    return response.data
  },
}
