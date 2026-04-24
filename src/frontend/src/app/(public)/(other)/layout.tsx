import { Content } from 'antd/es/layout/layout'
import { ReactNode } from 'react'

export default function OtherLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex-1 flex">
      <Content>{children}</Content>
    </div>
  )
}
