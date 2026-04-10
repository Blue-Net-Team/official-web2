import { ReactNode } from 'react'
import { StopOutlined } from '@ant-design/icons'
import { Empty } from 'antd'

export type ErrorPageConfig = {
  icon: ReactNode
  statusCode: number
  description: string
}

export const ERROR_CONFIGS: Record<number, ErrorPageConfig> = {
  403: {
    icon: <StopOutlined />,
    statusCode: 403,
    description: '您没有权限访问此页面',
  },
  404: {
    icon: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={false} />,
    statusCode: 404,
    description: '您访问的页面不存在',
  },
}
