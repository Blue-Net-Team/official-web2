import { apiClient } from '../client'

import type {
  ResponseMessage,
  PageDTO,
  SoftwareResourceDTO,
  CreateSoftwareResourceRequestDTO,
  UpdateSoftwareResourceRequestDTO,
  BatchSortRequestDTO,
} from '../schema/type'

export const adminSoftwareResourceService = {
  /**
   * 查询软件资源列表
   * GET /admin/software-resources
   */
  async list(page: number, size: number): Promise<ResponseMessage<PageDTO<SoftwareResourceDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<SoftwareResourceDTO>>>(
      '/admin/software-resources',
      {
        params: { page, size },
      }
    )
    return response.data
  },

  /**
   * 创建软件资源
   * POST /admin/software-resources
   */
  async create(
    data: CreateSoftwareResourceRequestDTO
  ): Promise<ResponseMessage<SoftwareResourceDTO>> {
    const response = await apiClient.post<ResponseMessage<SoftwareResourceDTO>>(
      '/admin/software-resources',
      data
    )
    return response.data
  },

  /**
   * 更新软件资源
   * PUT /admin/software-resources/{id}
   */
  async update(
    id: number,
    data: UpdateSoftwareResourceRequestDTO
  ): Promise<ResponseMessage<SoftwareResourceDTO>> {
    const response = await apiClient.put<ResponseMessage<SoftwareResourceDTO>>(
      `/admin/software-resources/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除软件资源
   * DELETE /admin/software-resources/{id}
   */
  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(
      `/admin/software-resources/${id}`
    )
    return response.data
  },

  /**
   * 批量调整软件资源排序
   * PUT /admin/software-resources/sort
   */
  async batchUpdateSortOrder(data: BatchSortRequestDTO): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      '/admin/software-resources/sort',
      data
    )
    return response.data
  },
}
