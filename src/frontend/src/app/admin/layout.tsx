'use client'

import AdminNav from '@/components/Admin/AdminNav'
import AdminHeadBar from '@/components/Admin/AdminNav/AdminHeadBar'
import AdminSideBar from '@/components/Admin/AdminNav/AdminSideBar'
import ErrorPage from '@/components/ErrorPage'
import { ERROR_CONFIGS } from '@/components/ErrorPage/configs'
import { Layout } from 'antd'
import { Content } from 'antd/es/layout/layout'
import { ReactNode } from 'react'
import authStore from '@/stores/authStore'
import { getRoleLevel } from '@/utils/RoleUtils'

export default function AdminLayout({ children }: { children: ReactNode }) {
  const userInfo = authStore((state) => state.userInfo)
  const roleLevel = getRoleLevel(userInfo?.roleName || '')

  if (roleLevel < 1) {
    return (
      <AdminNav>
        <Layout style={{ minHeight: '100vh' }}>
          <AdminHeadBar />
          <ErrorPage config={ERROR_CONFIGS[403]} />
        </Layout>
      </AdminNav>
    )
  }

  return (
    <AdminNav>
      <Layout style={{ height: '100vh', overflow: 'hidden' }}>
        <AdminHeadBar />
        <Layout style={{ flex: 1, overflow: 'hidden' }}>
          <AdminSideBar />
          <Content style={{ padding: 24, flex: 1, overflow: 'auto' }}>{children}</Content>
        </Layout>
      </Layout>
    </AdminNav>
  )
}
