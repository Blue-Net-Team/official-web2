'use client'

import { useEffect } from 'react'
import { Button, Result } from 'antd'
import { useRouter } from 'next/navigation'

export default function AdminError({
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
    <div className="min-h-screen flex items-center justify-center">
      <Result
        status="error"
        title="管理后台出错"
        subTitle={error.message || '页面加载时发生错误，请稍后重试'}
        extra={[
          <Button key="retry" type="primary" onClick={reset}>
            重试
          </Button>,
          <Button key="home" onClick={() => router.push('/admin')}>
            返回管理首页
          </Button>,
        ]}
      />
    </div>
  )
}
