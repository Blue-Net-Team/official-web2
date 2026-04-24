import { Spin } from 'antd'

interface LoadingSpinnerProps {
  tip?: string
  className?: string
}

/**
 * 通用加载状态组件
 */
export default function LoadingSpinner({ tip, className = '' }: LoadingSpinnerProps) {
  return (
    <div className={`flex items-center justify-center ${className}`}>
      <Spin size="large" tip={tip} />
    </div>
  )
}
