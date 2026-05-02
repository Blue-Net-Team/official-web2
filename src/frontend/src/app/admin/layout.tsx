'use client'

import AdminNav from '@/components/Admin/AdminNav'
import AdminHeadBar from '@/components/Admin/AdminNav/AdminHeadBar'
import AdminSideBar from '@/components/Admin/AdminNav/AdminSideBar'
import ErrorPage from '@/components/ErrorPage'
import { ERROR_CONFIGS } from '@/components/ErrorPage/configs'
import { Layout, Spin } from 'antd'
import { Content } from 'antd/es/layout/layout'
import { ReactNode, useEffect, useState } from 'react'
import { useAuth } from '@/hooks'
import { getRoleLevel } from '@/utils/RoleUtils'

export default function AdminLayout({ children }: { children: ReactNode }) {
  const { userInfo } = useAuth()
  const [hydrated, setHydrated] = useState(false)
  const roleLevel = getRoleLevel(userInfo?.roleName || '')

  useEffect(() => {
    setHydrated(true)
  }, [])

  if (!hydrated) {
    return (
      <AdminNav>
        <Layout className="min-h-screen flex items-center justify-center">
          <Spin size="large" />
        </Layout>
      </AdminNav>
    )
  }

  if (roleLevel < 1) {
    return (
      <AdminNav>
        <Layout className="min-h-screen">
          <AdminHeadBar />
          <ErrorPage config={ERROR_CONFIGS[403]} />
        </Layout>
      </AdminNav>
    )
  }

  return (
    <AdminNav>
      <Layout className="h-screen overflow-hidden">
        <AdminHeadBar />
        <Layout className="flex-1 overflow-hidden">
          <AdminSideBar />
          <Content className="p-6 flex-1 overflow-auto">{children}</Content>
        </Layout>
      </Layout>
    </AdminNav>
  )
}
