import { useCallback } from 'react'
import authStore from '@/stores/authStore'

/**
 * 认证状态 Hook
 * 封装 authStore，提供便捷的认证相关操作
 */
export function useAuth() {
  const userInfo = authStore((state) => state.userInfo)
  const isAuthenticated = authStore((state) => state.isAuthenticated)
  const isLoading = authStore((state) => state.isLoading)
  const login = authStore((state) => state.login)
  const loginWithEmail = authStore((state) => state.loginWithEmail)
  const logout = authStore((state) => state.logout)
  const checkAuthStatus = authStore((state) => state.checkAuthStatus)
  const sendVerificationCode = authStore((state) => state.sendVerificationCode)

  const isAdmin = userInfo?.roleName === 'ADMIN' || userInfo?.roleName === 'SUPER_ADMIN'
  const isSuperAdmin = userInfo?.roleName === 'SUPER_ADMIN'

  const refreshAuth = useCallback(async () => {
    return checkAuthStatus()
  }, [checkAuthStatus])

  return {
    userInfo,
    isAuthenticated,
    isLoading,
    isAdmin,
    isSuperAdmin,
    login,
    loginWithEmail,
    logout,
    checkAuthStatus,
    refreshAuth,
    sendVerificationCode,
  }
}
