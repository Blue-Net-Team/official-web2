'use client'

import AdminHeadBar from '@/components/Admin/AdminHeadBar'
import AdminSideBar from '@/components/Admin/AdminSideBar'
import NavBar from '@/components/PublicNavbar'
import ErrorPage from '@/components/ErrorPage'
import { ERROR_CONFIGS } from '@/components/ErrorPage/configs'
import { Layout } from 'antd'
import { Content } from 'antd/es/layout/layout'
import React from 'react'
import { ReactNode } from 'react'
import authStore from '@/stores/authStore'
import { getRoleLevel } from '@/utils/RoleUtils'

export default function AdminLayout({ children }: { children: ReactNode }) {
  const userInfo = authStore((state) => state.userInfo)
  const roleLevel = getRoleLevel(userInfo?.roleName || '')

  if (roleLevel < 1) {
    return (
      <Layout style={{ minHeight: '100vh' }}>
        <AdminHeadBar />
        <ErrorPage config={ERROR_CONFIGS[403]} />
      </Layout>
    )
  }

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
