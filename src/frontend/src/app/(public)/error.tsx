'use client'

import { useEffect } from 'react'
import { Button, Result } from 'antd'
import { useRouter } from 'next/navigation'

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string }
  reset: () => void
}) {
  const router = useRouter()

  useEffect(() => {
    console.error(error)
  }, [error])

  return (
    <div className="min-h-screen bg-[#0a0a0a] flex items-center justify-center">
      <Result
        status="error"
        title="出错了"
        subTitle={error.message || '页面加载时发生错误，请稍后重试'}
        extra={[
          <Button
            key="retry"
            type="primary"
            onClick={reset}
            className="!bg-gradient-to-br !from-[#6677ff] !to-[#2f27b0]"
          >
            重试
          </Button>,
          <Button key="home" onClick={() => router.push('/')}>
            返回首页
          </Button>,
        ]}
      />
    </div>
  )
}
