'use client'

import { useEffect, ReactNode } from 'react'
import { useAuth } from '@/hooks'

interface AuthProviderProps {
  children: ReactNode
}

/**
 * 认证提供者组件
 * 在应用启动时后台检查登录状态，不阻塞渲染
 */
export default function AuthProvider({ children }: AuthProviderProps) {
  const { checkAuthStatus } = useAuth()

  useEffect(() => {
    // 后台检查登录状态，不阻塞渲染
    checkAuthStatus()
  }, [checkAuthStatus])

  return <>{children}</>
}
