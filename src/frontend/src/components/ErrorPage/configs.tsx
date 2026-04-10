import { ReactNode } from 'react'
import { FileSearchOutlined, StopOutlined } from '@ant-design/icons'

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
    icon: <FileSearchOutlined />,
    statusCode: 404,
    description: '您访问的页面不存在',
  },
}
