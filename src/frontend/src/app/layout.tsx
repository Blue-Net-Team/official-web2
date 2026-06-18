import type { Metadata, Viewport } from 'next'
import { AntdRegistry } from '@ant-design/nextjs-registry'
import ThemeProvider from '@/components/ThemeProvider'
import AuthProvider from '@/components/AuthProvider'
import GlobalFloatActions from '@/components/GlobalFloatActions'
import { App } from 'antd'
import './globals.css'

export const metadata: Metadata = {
  title: 'Bluenet',
  description: '蓝网官方网站',
  manifest: '/manifest.json',
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  themeColor: '#000000',
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="zh-CN">
      <body>
        <AntdRegistry>
          <ThemeProvider>
            <AuthProvider>
              <App>
                {children}
                <GlobalFloatActions />
              </App>
            </AuthProvider>
          </ThemeProvider>
        </AntdRegistry>
      </body>
    </html>
  )
}
