import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { API_BASE_URL } from './config'
import { ResponseMessage } from './schema/type'

/** 获取 CSRF Token（从 cookie 读取，保证与 cookie 始终一致） */
function getCsrfTokenFromCookie(): string | null {
  if (typeof document === 'undefined') return null
  const match = document.cookie.match(/(?:^|;\s*)csrf_token=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : null
}

/** 设置 CSRF Token（保留兼容，但实际从 cookie 读取） */
export const setCsrfToken = (_token: string | null) => {
  // 不再需要存储到内存变量
}

/** 获取 CSRF Token */
export const getCsrfToken = () => getCsrfTokenFromCookie()

/** 需要添加 CSRF Token 的请求方法 */
const CSRF_METHODS = ['post', 'put', 'delete', 'patch']

/** 创建 axios 实例 */
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  withCredentials: true, // 携带 Cookie
  headers: {
    'Content-Type': 'application/json',
  },
})

/** 请求拦截器 - 自动从 cookie 读取 CSRF Token */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const method = config.method?.toLowerCase()
    if (method && CSRF_METHODS.includes(method) && config.headers) {
      const token = getCsrfTokenFromCookie()
      if (token) {
        config.headers['X-CSRF-Token'] = token
      }
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

/** 响应拦截器 - 统一处理错误 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ResponseMessage<unknown>>) => {
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      return Promise.resolve({
        data: { code: 408, msg: '请求超时', data: null } as ResponseMessage<null>,
        status: 408,
        statusText: 'Request Timeout',
        headers: {},
        config: error.config!,
      })
    }
    if (error.response?.status === 401) {
      // 401 时跳转登录
      if (typeof window !== 'undefined') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

/** 不带认证头的客户端（用于公开接口） */
const publicClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  withCredentials: true, // 携带 Cookie（登录接口需要设置 Cookie）
  headers: {
    'Content-Type': 'application/json',
  },
})

/** 公开客户端请求拦截器 - 自动从 cookie 读取 CSRF Token */
publicClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const method = config.method?.toLowerCase()
    if (method && CSRF_METHODS.includes(method) && config.headers) {
      const token = getCsrfTokenFromCookie()
      if (token) {
        config.headers['X-CSRF-Token'] = token
      }
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

/** 响应拦截器 - 统一处理超时 */
publicClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ResponseMessage<unknown>>) => {
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      return Promise.resolve({
        data: { code: 408, msg: '请求超时', data: null } as ResponseMessage<null>,
        status: 408,
        statusText: 'Request Timeout',
        headers: {},
        config: error.config!,
      })
    }
    return Promise.reject(error)
  }
)

export { apiClient, publicClient }
