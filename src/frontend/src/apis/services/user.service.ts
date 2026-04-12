import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type { UserInfo, TabCounts, UserExperience } from '../schema/type'
import type { ExperienceType } from '../schema/enumerate'
import type {
  UpdateProfileRequestDTO,
  CreateExperienceRequestDTO,
  UpdateExperienceRequestDTO,
} from '../schema/profile.dto'

/**
 * 用户服务 API
 * 对应后端 /api/v1/user/* 接口
 */
export const userService = {
  /**
   * 获取当前用户信息
   * 对应后端 GET /api/v1/user/info
   */
  async getUserInfo(): Promise<ResponseMessage<UserInfo>> {
    const response = await apiClient.get<ResponseMessage<UserInfo>>('/user/info')
    return response.data
  },

  /**
   * 更新用户信息
   * 对应后端 PUT /api/v1/user/info
   * @param data 更新请求
   */
  async updateProfile(data: UpdateProfileRequestDTO): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>('/user/info', data)
    return response.data
  },

  /**
   * 获取 Tab 计数
   * 对应后端 GET /api/v1/user/tab-counts
   */
  async getTabCounts(): Promise<ResponseMessage<TabCounts>> {
    const response = await apiClient.get<ResponseMessage<TabCounts>>('/user/tab-counts')
    return response.data
  },

  /**
   * 获取经历列表
   * 对应后端 GET /api/v1/user/experiences
   * @param type 经历类型过滤（可选）
   */
  async getExperiences(type?: ExperienceType): Promise<ResponseMessage<UserExperience[]>> {
    const params = type ? { type } : {}
    const response = await apiClient.get<ResponseMessage<UserExperience[]>>('/user/experiences', {
      params,
    })
    return response.data
  },

  /**
   * 创建经历
   * 对应后端 POST /api/v1/user/experiences
   * @param data 创建请求
   */
  async createExperience(
    data: CreateExperienceRequestDTO
  ): Promise<ResponseMessage<UserExperience>> {
    const response = await apiClient.post<ResponseMessage<UserExperience>>(
      '/user/experiences',
      data
    )
    return response.data
  },

  /**
   * 更新经历
   * 对应后端 PUT /api/v1/user/experiences/{id}
   * @param id 经历 ID
   * @param data 更新请求
   */
  async updateExperience(
    id: string,
    data: UpdateExperienceRequestDTO
  ): Promise<ResponseMessage<UserExperience>> {
    const response = await apiClient.put<ResponseMessage<UserExperience>>(
      `/user/experiences/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除经历
   * 对应后端 DELETE /api/v1/user/experiences/{id}
   * @param id 经历 ID
   */
  async deleteExperience(id: string): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/user/experiences/${id}`)
    return response.data
  },

  async sendEmailVerificationCode(email: string, scene: string): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>(
      '/user/email/verification-code/send',
      { email, scene }
    )
    return response.data
  },

  async changeEmail(data: {
    originalEmailVerifyCode: string
    newEmail: string
    newEmailVerifyCode: string
  }): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>('/user/email', data)
    return response.data
  },

  /**
   * 验证当前密码
   * 对应后端 POST /api/v1/user/password/verify
   * @param currentPassword 当前密码
   * @returns 验证令牌
   */
  async verifyPassword(currentPassword: string): Promise<ResponseMessage<string>> {
    const response = await apiClient.post<ResponseMessage<string>>('/user/password/verify', {
      currentPassword,
    })
    return response.data
  },

  /**
   * 修改密码
   * 对应后端 PUT /api/v1/user/password
   * @param token 验证令牌
   * @param newPassword 新密码
   * @param confirmPassword 确认新密码
   */
  async changePassword(
    token: string,
    newPassword: string,
    confirmPassword: string
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>('/user/password', {
      token,
      newPassword,
      confirmPassword,
    })
    return response.data
  },
}
