'use client'

import { Button } from 'antd'
import { useRouter } from 'next/navigation'
import { loginButtonStyle } from './constants'

interface LoginButtonProps {
  isMobile?: boolean
}

export const LoginButton = ({ isMobile = false }: LoginButtonProps) => {
  const router = useRouter()
  const style = isMobile ? { ...loginButtonStyle, width: '100%' } : loginButtonStyle

  return (
    <Button
      type="primary"
      style={style}
      onMouseEnter={(e) => {
        e.currentTarget.style.backgroundColor = '#ff8a5c'
        e.currentTarget.style.borderColor = '#ff8a5c'
        e.currentTarget.style.color = '#fff'
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.backgroundColor = '#ff6f3c'
        e.currentTarget.style.borderColor = '#ff6f3c'
        e.currentTarget.style.color = '#ffe4c5'
      }}
      onClick={() => router.push('/login')}
    >
      登录
    </Button>
  )
}
