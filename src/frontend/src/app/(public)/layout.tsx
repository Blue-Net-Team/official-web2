import NavBar from '@/components/PublicNavbar'
import { Layout } from 'antd'
import { ReactNode } from 'react'

export default function PublicLayout({ children }: { children: ReactNode }) {
  return (
    <Layout style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <NavBar />
      {children}
    </Layout>
  )
}
