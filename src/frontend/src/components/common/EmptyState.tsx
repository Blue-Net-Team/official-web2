import { Empty } from 'antd'

interface EmptyStateProps {
  description?: string
  className?: string
}

/**
 * 通用空数据状态组件
 */
export default function EmptyState({ description = '暂无数据', className = '' }: EmptyStateProps) {
  return (
    <div className={`flex justify-center items-center min-h-[300px] ${className}`}>
      <Empty description={description} />
    </div>
  )
}
