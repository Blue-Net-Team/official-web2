import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  output: 'standalone',
  images: {
    remotePatterns: [
      {
        protocol: (process.env.SSL_ENABLED === 'true' ? 'https' : 'http') as 'http' | 'https',
        hostname: process.env.BACKEND_HOST || 'localhost',
        port: process.env.BACKEND_PORT || '8080',
        pathname: '/api/**',
      },
      {
        protocol: (process.env.NEXT_PUBLIC_SSL_ENABLED === 'true' ? 'https' : 'http') as 'http' | 'https',
        hostname: process.env.NEXT_PUBLIC_BACKEND_HOST || 'localhost',
        port: process.env.NEXT_PUBLIC_BACKEND_PORT || '8080',
        pathname: '/api/**',
      },
    ],
  },
}

export default nextConfig
