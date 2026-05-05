import { apiClient } from '../client'

import type {
  ResponseMessage,
  CollegeDTO,
  CreateCollegeRequestDTO,
  UpdateCollegeRequestDTO,
} from '../schema/type'

export const adminCollegeService = {
  async create(data: CreateCollegeRequestDTO): Promise<ResponseMessage<CollegeDTO>> {
    const response = await apiClient.post<ResponseMessage<CollegeDTO>>('/admin/colleges', data)
    return response.data
  },

  async update(
    id: number,
    data: UpdateCollegeRequestDTO
  ): Promise<ResponseMessage<CollegeDTO>> {
    const response = await apiClient.put<ResponseMessage<CollegeDTO>>(
      `/admin/colleges/${id}`,
      data
    )
    return response.data
  },

  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/colleges/${id}`)
    return response.data
  },
}
