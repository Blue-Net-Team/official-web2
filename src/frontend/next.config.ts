import type { NextConfig } from 'next'

/**
 * Next.js 图片优化器内部用 new URL() 解析图片地址。
 * 对于 https://host:443/...，Node.js 会将默认端口 443 标准化为空字符串，
 * 导致 remotePattern 中显式的 port: '443' 无法匹配。
 * 默认端口时传空字符串，让 Next.js 跳过端口检查。
 */
function normalizePort(port: string | undefined): string {
  if (!port) return '8080'
  if (port === '443' || port === '80') return ''
  return port
}

const nextConfig: NextConfig = {
  output: 'standalone',
  images: {
    remotePatterns: [
      {
        protocol: (process.env.SSL_ENABLED === 'true' ? 'https' : 'http') as 'http' | 'https',
        hostname: process.env.BACKEND_HOST || 'localhost',
        port: normalizePort(process.env.BACKEND_PORT),
        pathname: '/api/**',
      },
      {
        protocol: (process.env.NEXT_PUBLIC_SSL_ENABLED === 'true' ? 'https' : 'http') as 'http' | 'https',
        hostname: process.env.NEXT_PUBLIC_BACKEND_HOST || 'localhost',
        port: normalizePort(process.env.NEXT_PUBLIC_BACKEND_PORT),
        pathname: '/api/**',
      },
    ],
  },
}

export default nextConfig
