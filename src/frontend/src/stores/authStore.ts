import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { UserInfo, StudentIdLoginRequestDTO, UserAuthResponseDTO } from '@/apis/schema/type'
import { authService } from '@/apis/services/auth.service'
import { AxiosError } from 'axios'

interface AuthState {
  /** CSRF Token（内存存储，不持久化） */
  csrfToken: string | null
  /** 用户信息 */
  userInfo: UserInfo | null
  /** 是否已认证 */
  isAuthenticated: boolean
  /** 是否正在加载（登录/登出操作） */
  isLoading: boolean
  /** 学号登录 */
  login: (credentials: StudentIdLoginRequestDTO) => Promise<UserAuthResponseDTO>
  /** 邮箱验证码登录 */
  loginWithEmail: (email: string, verifyCode: string) => Promise<UserAuthResponseDTO>
  /** 发送邮箱验证码 */
  sendVerificationCode: (email: string) => Promise<void>
  /** 登出 */
  logout: () => Promise<void>
  /** 检查登录状态（页面刷新后调用） */
  checkAuthStatus: () => Promise<boolean>
  /** 设置加载状态 */
  setLoading: (loading: boolean) => void
}

const authStore = create<AuthState>()(
  persist(
    (set) => ({
      csrfToken: null,
      userInfo: null,
      isAuthenticated: false,
      isLoading: false,

      login: async (credentials: StudentIdLoginRequestDTO) => {
        set({ isLoading: true })
        try {
          const response = await authService.login({
            studentId: credentials.studentId,
            password: credentials.password,
          })

          if (response.code === 200 && response.data) {
            set({
              csrfToken: response.data.csrfToken,
              userInfo: response.data.userInfo,
              isAuthenticated: true,
              isLoading: false,
            })
            return response.data
          } else {
            set({ isLoading: false })
            throw new Error(response.msg || '登录失败')
          }
        } catch (error) {
          set({ isLoading: false })
          throw error
        }
      },

      loginWithEmail: async (email: string, verifyCode: string) => {
        set({ isLoading: true })
        try {
          const response = await authService.loginWithEmail({
            email,
            verifyCode,
          })

          if (response.code === 200 && response.data) {
            set({
              csrfToken: response.data.csrfToken,
              userInfo: response.data.userInfo,
              isAuthenticated: true,
              isLoading: false,
            })
            return response.data
          } else {
            set({ isLoading: false })
            throw new Error(response.msg || '登录失败')
          }
        } catch (error) {
          set({ isLoading: false })
          if (error instanceof AxiosError && error.response?.data?.msg) {
            throw new Error(error.response.data.msg)
          }
          throw error
        }
      },

      sendVerificationCode: async (email: string) => {
        try {
          await authService.sendVerificationCode({ email })
        } catch (error) {
          if (error instanceof AxiosError && error.response?.data?.msg) {
            throw new Error(error.response.data.msg)
          }
          throw error
        }
      },

      logout: async () => {
        try {
          await authService.logout()
        } catch {
          // 即使 API 调用失败，也清除本地状态
        } finally {
          set({
            csrfToken: null,
            userInfo: null,
            isAuthenticated: false,
          })
        }
      },

      checkAuthStatus: async () => {
        try {
          const response = await authService.getAuthMe()

          if (response.code === 200 && response.data?.authenticated) {
            set({
              csrfToken: response.data.csrfToken,
              userInfo: response.data.userInfo,
              isAuthenticated: true,
            })
            return true
          } else {
            set({
              csrfToken: null,
              userInfo: null,
              isAuthenticated: false,
            })
            return false
          }
        } catch {
          set({
            csrfToken: null,
            userInfo: null,
            isAuthenticated: false,
          })
          return false
        }
      },

      setLoading: (loading: boolean) => {
        set({ isLoading: loading })
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        userInfo: state.userInfo,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)

export default authStore
