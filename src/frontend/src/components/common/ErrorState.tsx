import { Button, Result } from 'antd'

interface ErrorStateProps {
  title?: string
  message?: string
  onRetry?: () => void
}

/**
 * 通用错误状态组件
 */
export default function ErrorState({
  title = '加载失败',
  message = '数据加载失败，请稍后重试',
  onRetry,
}: ErrorStateProps) {
  return (
    <Result
      status="error"
      title={title}
      subTitle={message}
      extra={
        onRetry
          ? [
              <Button
                key="retry"
                type="primary"
                onClick={onRetry}
                className="!bg-gradient-to-br !from-[#6677ff] !to-[#2f27b0]"
              >
                重试
              </Button>,
            ]
          : undefined
      }
    />
  )
}
