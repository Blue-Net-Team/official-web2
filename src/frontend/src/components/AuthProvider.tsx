'use client'

import { useEffect, ReactNode } from 'react'
import authStore from '@/stores/authStore'

interface AuthProviderProps {
  children: ReactNode
}

/**
 * 认证提供者组件
 * 在应用启动时后台检查登录状态，不阻塞渲染
 */
export default function AuthProvider({ children }: AuthProviderProps) {
  const checkAuthStatus = authStore((state) => state.checkAuthStatus)

  useEffect(() => {
    // 后台检查登录状态，不阻塞渲染
    checkAuthStatus()
  }, [checkAuthStatus])

  return <>{children}</>
}
