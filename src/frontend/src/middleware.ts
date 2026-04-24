import { NextResponse } from 'next/server'
import type { NextRequest } from 'next/server'

/**
 * 受保护的路由前缀列表
 * 访问这些路由需要登录状态
 */
const PROTECTED_ROUTES = ['/admin', '/profile', '/assessment']

/**
 * 公开可访问的路由（不需要登录）
 */
const PUBLIC_ROUTES = ['/login', '/forgot-password', '/change-password', '/enroll']

/**
 * 静态资源和 API 路由不需要中间件处理
 */
const STATIC_PATHS = ['/_next', '/static', '/api', '/favicon.ico', '/robots.txt', '/sitemap.xml']

/**
 * Next.js Middleware - 路由守卫
 *
 * 说明：
 * - Middleware 在 Edge Runtime 中运行，只能访问 Cookie（不能访问 localStorage）
 * - JWT Token 是 HttpOnly Cookie，此处无法直接验证其有效性
 * - 因此 middleware 只能做粗粒度的 Cookie 存在性检查
 * - 完整的 Token 验证仍由客户端 authStore.checkAuthStatus() 负责
 */
export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl

  // 跳过静态资源和 API 路由
  if (STATIC_PATHS.some((path) => pathname.startsWith(path))) {
    return NextResponse.next()
  }

  // 检查是否是受保护路由
  const isProtected = PROTECTED_ROUTES.some((route) => pathname.startsWith(route))
  if (!isProtected) {
    return NextResponse.next()
  }

  // 检查是否有认证标记（CSRF Token 存在说明用户可能已登录）
  // 注意：这不是严格的身份验证，仅用于快速拦截明显未登录的请求
  const hasCsrfToken = request.cookies.has('csrf_token')

  if (!hasCsrfToken) {
    const loginUrl = new URL('/login', request.url)
    loginUrl.searchParams.set('redirect', pathname)
    return NextResponse.redirect(loginUrl)
  }

  return NextResponse.next()
}

/**
 * Matcher 配置
 * 排除静态文件和 API 路由，仅对页面路由运行中间件
 */
export const config = {
  matcher: [
    /*
     * 匹配所有路径，除了：
     * - /api (API 路由)
     * - /_next (Next.js 内部文件)
     * - /static (静态文件)
     * - /favicon.ico, /sitemap.xml, /robots.txt
     */
    '/((?!api|_next|static|favicon.ico|sitemap.xml|robots.txt).*)',
  ],
}
