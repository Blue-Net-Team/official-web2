import AdminHeadBar from '@/components/Admin/AdminHeadBar'
import AdminSideBar from '@/components/Admin/AdminSideBar'
import { Layout } from 'antd'
import { Content } from 'antd/es/layout/layout'
import React from 'react'
import { ReactNode } from 'react'

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <AdminHeadBar />
      <Layout style={{ flex: 1 }}>
        <AdminSideBar />
        <Content style={{ padding: 24, flex: 1 }}>{children}</Content>
      </Layout>
    </Layout>
  )
}
