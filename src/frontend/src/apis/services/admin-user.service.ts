import { apiClient } from '../client'
import type {
  ResponseMessage,
  PageDTO,
  AdminUserListItemDTO,
  AdminUserDetailDTO,
  AdminUserListQueryDTO,
  AdminUserUpdateRequestDTO,
  AdminUserResetPasswordRequestDTO,
  AdminUserBatchOperateRequestDTO,
  AdminUserBatchUpdateRoleRequestDTO,
  AdminUserCreateRequestDTO,
  AdminUserCreateResponseDTO,
} from '../schema/type'

export const adminUserService = {
  async getList(
    params: AdminUserListQueryDTO = {}
  ): Promise<ResponseMessage<PageDTO<AdminUserListItemDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<AdminUserListItemDTO>>>(
      '/admin/users',
      { params }
    )
    return response.data
  },

  async getDetail(id: number): Promise<ResponseMessage<AdminUserDetailDTO>> {
    const response = await apiClient.get<ResponseMessage<AdminUserDetailDTO>>(`/admin/users/${id}`)
    return response.data
  },

  async update(id: number, data: AdminUserUpdateRequestDTO): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(`/admin/users/${id}`, data)
    return response.data
  },

  async resetPassword(
    id: number,
    data: AdminUserResetPasswordRequestDTO
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(`/admin/users/${id}/password`, data)
    return response.data
  },

  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/users/${id}`)
    return response.data
  },

  async batchDelete(data: AdminUserBatchOperateRequestDTO): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>('/admin/users/batch-delete', data)
    return response.data
  },

  async batchDisable(data: AdminUserBatchOperateRequestDTO): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>('/admin/users/batch-disable', data)
    return response.data
  },

  async batchEnable(data: AdminUserBatchOperateRequestDTO): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>('/admin/users/batch-enable', data)
    return response.data
  },

  async batchUpdateRole(data: AdminUserBatchUpdateRoleRequestDTO): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>('/admin/users/batch-role', data)
    return response.data
  },

  async create(
    data: AdminUserCreateRequestDTO
  ): Promise<ResponseMessage<AdminUserCreateResponseDTO>> {
    const response = await apiClient.post<ResponseMessage<AdminUserCreateResponseDTO>>(
      '/admin/users',
      data
    )
    return response.data
  },
}
