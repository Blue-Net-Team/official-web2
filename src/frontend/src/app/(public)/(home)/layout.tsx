import AppFooter from '@/components/Footer'
import { Content } from 'antd/es/layout/layout'
import { ReactNode } from 'react'

export default function HomeLayout({ children }: { children: ReactNode }) {
  return (
    <>
      <Content>{children}</Content>
      <AppFooter />
    </>
  )
}
