import { Content } from 'antd/es/layout/layout'
import { ReactNode } from 'react'

export default function OtherLayout({ children }: { children: ReactNode }) {
  return (
    <div style={{ flex: 1, display: 'flex' }}>
      <Content>{children}</Content>
    </div>
  )
}
