'use client'

import AuthProvider from '@/components/AuthProvider'
import ErrorPage from '@/components/ErrorPage'
import { ERROR_CONFIGS } from '@/components/ErrorPage/configs'
import NavBar from '@/components/PublicNavbar'
import ThemeProvider from '@/components/ThemeProvider'
import { AntdRegistry } from '@ant-design/nextjs-registry'
import { App, Layout } from 'antd'

export default function NotFound() {
  return (
    <html lang="zh-CN">
      <body>
        <AntdRegistry>
          <ThemeProvider>
            <AuthProvider>
              <App>
                <Layout style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
                  <NavBar />
                  <ErrorPage config={ERROR_CONFIGS[404]} />
                </Layout>
              </App>
            </AuthProvider>
          </ThemeProvider>
        </AntdRegistry>
      </body>
    </html>
  )
}
