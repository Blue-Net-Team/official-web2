import { publicClient, apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import {
  StudentIdLoginRequestDTO,
  UserAuthResponseDTO,
  AuthMeResponseDTO,
  EmailLoginRequestDTO,
  SendVerificationCodeRequestDTO,
} from '../schema/type'

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
   * 邮箱验证码登录 - 公开接口
   * 对应后端 POST /api/v1/auth/login/email
   * JWT 通过 HttpOnly Cookie 自动设置，响应体返回 CSRF Token
   * @param credentials 邮箱和验证码
   */
  async loginWithEmail(
    credentials: EmailLoginRequestDTO
  ): Promise<ResponseMessage<UserAuthResponseDTO>> {
    const response = await publicClient.post<ResponseMessage<UserAuthResponseDTO>>(
      '/auth/login/email',
      credentials
    )
    return response.data
  },

  /**
   * 发送邮箱验证码 - 公开接口
   * 对应后端 POST /api/v1/auth/verification-code/send
   * @param data 邮箱
   */
  async sendVerificationCode(data: SendVerificationCodeRequestDTO): Promise<ResponseMessage<void>> {
    const response = await publicClient.post<ResponseMessage<void>>(
      '/auth/verification-code/send',
      data
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

  // ==================== 密码重置 ====================

  /**
   * 密码重置 - 验证学号 - 公开接口
   * 对应后端 POST /api/v1/auth/reset-password/verify-student
   */
  async resetPasswordVerifyStudent(studentId: string): Promise<ResponseMessage<string>> {
    const response = await publicClient.post<ResponseMessage<string>>(
      '/auth/reset-password/verify-student',
      { studentId }
    )
    return response.data
  },

  /**
   * 密码重置 - 验证邮箱 - 公开接口
   * 对应后端 POST /api/v1/auth/reset-password/verify-email
   */
  async resetPasswordVerifyEmail(
    resetToken: string,
    email: string
  ): Promise<ResponseMessage<string>> {
    const response = await publicClient.post<ResponseMessage<string>>(
      '/auth/reset-password/verify-email',
      { resetToken, email }
    )
    return response.data
  },

  /**
   * 密码重置 - 发送验证码 - 公开接口
   * 对应后端 POST /api/v1/auth/reset-password/send-code
   */
  async resetPasswordSendCode(resetToken: string): Promise<ResponseMessage<void>> {
    const response = await publicClient.post<ResponseMessage<void>>(
      '/auth/reset-password/send-code',
      { resetToken }
    )
    return response.data
  },

  /**
   * 密码重置 - 验证验证码 - 公开接口
   * 对应后端 POST /api/v1/auth/reset-password/verify-code
   */
  async resetPasswordVerifyCode(resetToken: string, code: string): Promise<ResponseMessage<void>> {
    const response = await publicClient.post<ResponseMessage<void>>(
      '/auth/reset-password/verify-code',
      { resetToken, code }
    )
    return response.data
  },

  /**
   * 密码重置 - 重置密码 - 公开接口
   * 对应后端 POST /api/v1/auth/reset-password/reset
   */
  async resetPassword(
    resetToken: string,
    newPassword: string,
    confirmPassword: string
  ): Promise<ResponseMessage<void>> {
    const response = await publicClient.post<ResponseMessage<void>>('/auth/reset-password/reset', {
      resetToken,
      newPassword,
      confirmPassword,
    })
    return response.data
  },
}
