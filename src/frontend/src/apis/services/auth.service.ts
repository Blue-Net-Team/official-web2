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

  /**
   * 发起 GitHub OAuth 登录 - 公开接口
   * 对应后端 GET /api/v1/auth/github
   * 返回 GitHub 授权页面 URL，前端应重定向到该 URL
   */
  async getGithubAuthorizeUrl(): Promise<ResponseMessage<string>> {
    const response = await publicClient.get<ResponseMessage<string>>('/auth/github')
    return response.data
  },

  /**
   * 发起 GitHub 账号绑定 - 需要认证
   * 对应后端 GET /api/v1/auth/github/bind
   * 返回 GitHub 授权页面 URL，前端应重定向到该 URL
   */
  async getGithubBindUrl(): Promise<ResponseMessage<string>> {
    const response = await apiClient.get<ResponseMessage<string>>('/auth/github/bind')
    return response.data
  },

  /**
   * 查询 GitHub 绑定状态 - 需要认证
   * 对应后端 GET /api/v1/auth/github/status
   * 返回 GitHub 用户名（未绑定返回 null）
   */
  async getGithubBindingStatus(): Promise<ResponseMessage<string | null>> {
    const response = await apiClient.get<ResponseMessage<string | null>>('/auth/github/status')
    return response.data
  },

  /**
   * 解绑 GitHub 账号 - 需要认证
   * 对应后端 DELETE /api/v1/auth/github/bind
   */
  async unbindGithub(): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>('/auth/github/bind')
    return response.data
  },
}
