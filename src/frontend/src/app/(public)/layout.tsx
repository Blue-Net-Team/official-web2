import NavBar from '@/components/PublicNavbar'
import { Layout } from 'antd'
import { ReactNode } from 'react'

export default function PublicLayout({ children }: { children: ReactNode }) {
  return (
    <Layout className="min-h-screen flex flex-col">
      <NavBar />
      {children}
    </Layout>
  )
}
