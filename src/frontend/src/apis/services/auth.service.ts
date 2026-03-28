import { publicClient, apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import { StudentIdLoginRequestDTO, UserAuthResponseDTO, AuthMeResponseDTO } from '../schema/type'

export const authService = {
  /**
   * 学号登录 - 公开接口
   * 对应后端 POST /api/v1/auth/login/student-id
   * JWT 通过 HttpOnly Cookie 自动设置，响应体返回 CSRF Token
   * @param credentials 学号和密码
   */
  async login(
    credentials: StudentIdLoginRequestDTO
  ): Promise<ResponseMessage<UserAuthResponseDTO>> {
    const response = await publicClient.post<ResponseMessage<UserAuthResponseDTO>>(
      '/auth/login/student-id',
      credentials
    )
    return response.data
  },

  /**
   * 用户登出 - 需要 Cookie 认证
   * 对应后端 POST /api/v1/auth/logout
   * Cookie 中的 JWT 会自动携带，登出后清除 Cookie
   */
  async logout(): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>('/auth/logout')
    return response.data
  },

  /**
   * 获取当前登录状态 - 公开接口
   * 对应后端 GET /api/v1/auth/me
   * 用于页面刷新后恢复登录状态和获取 CSRF Token
   */
  async getAuthMe(): Promise<ResponseMessage<AuthMeResponseDTO>> {
    const response = await publicClient.get<ResponseMessage<AuthMeResponseDTO>>('/auth/me')
    return response.data
  },
}
