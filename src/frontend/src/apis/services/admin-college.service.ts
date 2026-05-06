import { apiClient } from '../client'

import type {
  ResponseMessage,
  CollegeDTO,
  CreateCollegeRequestDTO,
  UpdateCollegeRequestDTO,
} from '../schema/type'

export const adminCollegeService = {
  /** 创建学院 POST /admin/colleges */
  async create(data: CreateCollegeRequestDTO): Promise<ResponseMessage<CollegeDTO>> {
    const response = await apiClient.post<ResponseMessage<CollegeDTO>>('/admin/colleges', data)
    return response.data
  },

  /** 更新学院 PUT /admin/colleges/{id} */
  async update(id: number, data: UpdateCollegeRequestDTO): Promise<ResponseMessage<CollegeDTO>> {
    const response = await apiClient.put<ResponseMessage<CollegeDTO>>(`/admin/colleges/${id}`, data)
    return response.data
  },

  /** 删除学院 DELETE /admin/colleges/{id} */
  async delete(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/colleges/${id}`)
    return response.data
  },
}
