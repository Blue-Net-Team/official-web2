'use client'

import { Button, Result } from 'antd'

/**
 * 请求失败提示，带重试入口。
 *
 * 规范要求：任一后台接口请求失败时，页面必须展示可重试的错误提示，
 * 不得呈现空白或静默失败。
 */
export default function ErrorState({
  description = '数据加载失败，请稍后重试',
  onRetry,
}: {
  description?: string
  onRetry: () => void
}) {
  return (
    <Result
      status="error"
      title="加载失败"
      subTitle={description}
      extra={
        <Button type="primary" onClick={onRetry}>
          重试
        </Button>
      }
    />
  )
}
