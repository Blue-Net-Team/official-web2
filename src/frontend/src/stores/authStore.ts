import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import { UserInfo, StudentIdLoginRequestDTO, UserAuthResponseDTO } from '@/apis/schema/type'
import { authService } from '@/apis/services/auth.service'
import { hashPassword } from '@/utils/passwordHash'
import { setCsrfToken } from '@/apis/client'

interface AuthState {
  /** CSRF Token（内存存储，不持久化） */
  csrfToken: string | null
  /** 用户信息 */
  userInfo: UserInfo | null
  /** 是否已认证 */
  isAuthenticated: boolean
  /** 是否正在加载（登录/登出操作） */
  isLoading: boolean
  /** 登录 */
  login: (credentials: StudentIdLoginRequestDTO) => Promise<UserAuthResponseDTO>
  /** 登出 */
  logout: () => Promise<void>
  /** 检查登录状态（页面刷新后调用） */
  checkAuthStatus: () => Promise<boolean>
  /** 设置加载状态 */
  setLoading: (loading: boolean) => void
}

const authStore = create<AuthState>()(
  persist(
    (set, get) => ({
      csrfToken: null,
      userInfo: null,
      isAuthenticated: false,
      isLoading: false,

      login: async (credentials: StudentIdLoginRequestDTO) => {
        set({ isLoading: true })
        try {
          const hashedPassword = await hashPassword(credentials.password)
          const response = await authService.login({
            studentId: credentials.studentId,
            password: hashedPassword,
          })

          if (response.code === 200 && response.data) {
            // 更新全局 CSRF Token
            setCsrfToken(response.data.csrfToken)
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

      logout: async () => {
        try {
          await authService.logout()
        } catch {
          // 即使 API 调用失败，也清除本地状态
        } finally {
          // 清除全局 CSRF Token
          setCsrfToken(null)
          set({
            csrfToken: null,
            userInfo: null,
            isAuthenticated: false,
          })
        }
      },

      checkAuthStatus: async () => {
        // 如果已经有用户信息和 CSRF Token，跳过检查
        const { userInfo, csrfToken } = get()
        if (userInfo && csrfToken) {
          return true
        }

        try {
          const response = await authService.getAuthMe()

          if (response.code === 200 && response.data?.authenticated) {
            // 更新全局 CSRF Token
            setCsrfToken(response.data.csrfToken)
            set({
              csrfToken: response.data.csrfToken,
              userInfo: response.data.userInfo,
              isAuthenticated: true,
            })
            return true
          } else {
            // 未登录状态
            setCsrfToken(null)
            set({
              csrfToken: null,
              userInfo: null,
              isAuthenticated: false,
            })
            return false
          }
        } catch {
          // 请求失败，清除状态
          setCsrfToken(null)
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
      // 仅持久化 userInfo，不持久化 csrfToken（需要通过 /auth/me 获取）
      partialize: (state) => ({
        userInfo: state.userInfo,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)

export default authStore
