import { apiClient } from '../client'

import type {
  ResponseMessage,
  PageDTO,
  PermissionDTO,
  PermissionQueryDTO,
  PermissionTreeDTO,
  RolePermissionBatchRequestDTO,
  RolePermissionResponseDTO,
  PermissionRoleBatchRequestDTO,
  PermissionRoleResponseDTO,
} from '../schema/type'

export const adminPermissionService = {
  async getPermissions(
    query: PermissionQueryDTO
  ): Promise<ResponseMessage<PageDTO<PermissionDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<PermissionDTO>>>(
      '/admin/permissions',
      { params: query }
    )
    return response.data
  },

  async getPermissionDetail(id: number): Promise<ResponseMessage<PermissionDTO>> {
    const response = await apiClient.get<ResponseMessage<PermissionDTO>>(`/admin/permissions/${id}`)
    return response.data
  },

  async getPermissionTree(): Promise<ResponseMessage<PermissionTreeDTO[]>> {
    const response =
      await apiClient.get<ResponseMessage<PermissionTreeDTO[]>>('/admin/permissions/tree')
    return response.data
  },

  async getRolePermissions(roleName: string): Promise<ResponseMessage<string[]>> {
    const response = await apiClient.get<ResponseMessage<string[]>>(
      `/admin/roles/${roleName}/permissions`
    )
    return response.data
  },

  async assignPermissionsToRole(
    roleName: string,
    data: RolePermissionBatchRequestDTO
  ): Promise<ResponseMessage<RolePermissionResponseDTO>> {
    const response = await apiClient.post<ResponseMessage<RolePermissionResponseDTO>>(
      `/admin/roles/${roleName}/permissions/batch`,
      data
    )
    return response.data
  },

  async removePermissionsFromRole(
    roleName: string,
    data: RolePermissionBatchRequestDTO
  ): Promise<ResponseMessage<RolePermissionResponseDTO>> {
    const response = await apiClient.delete<ResponseMessage<RolePermissionResponseDTO>>(
      `/admin/roles/${roleName}/permissions/batch`,
      { data }
    )
    return response.data
  },

  async getPermissionRoles(permissionId: number): Promise<ResponseMessage<string[]>> {
    const response = await apiClient.get<ResponseMessage<string[]>>(
      `/admin/permissions/${permissionId}/roles`
    )
    return response.data
  },

  async assignRolesToPermission(
    permissionId: number,
    data: PermissionRoleBatchRequestDTO
  ): Promise<ResponseMessage<PermissionRoleResponseDTO>> {
    const response = await apiClient.post<ResponseMessage<PermissionRoleResponseDTO>>(
      `/admin/permissions/${permissionId}/roles/batch`,
      data
    )
    return response.data
  },

  async removeRolesFromPermission(
    permissionId: number,
    data: PermissionRoleBatchRequestDTO
  ): Promise<ResponseMessage<PermissionRoleResponseDTO>> {
    const response = await apiClient.delete<ResponseMessage<PermissionRoleResponseDTO>>(
      `/admin/permissions/${permissionId}/roles/batch`,
      { data }
    )
    return response.data
  },
}
