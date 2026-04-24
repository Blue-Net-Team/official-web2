import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { API_BASE_URL } from './config'
import { ResponseMessage } from './schema/type'
import { message } from 'antd'

/** 获取 CSRF Token（从 cookie 读取，保证与 cookie 始终一致） */
function getCsrfTokenFromCookie(): string | null {
  if (typeof document === 'undefined') return null
  const match = document.cookie.match(/(?:^|;\s*)csrf_token=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : null
}

/** 需要添加 CSRF Token 的请求方法 */
const CSRF_METHODS = ['post', 'put', 'delete', 'patch']

/** 创建基础 axios 实例的工厂函数 */
function createBaseClient(): AxiosInstance {
  const client = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    withCredentials: true,
    headers: {
      'Content-Type': 'application/json',
    },
  })

  // 请求拦截器 - 自动从 cookie 读取 CSRF Token
  client.interceptors.request.use(
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

  // 响应拦截器 - 统一超时处理
  client.interceptors.response.use(
    (response) => response,
    (error: AxiosError<ResponseMessage<unknown>>) => {
      if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
        return Promise.reject(new Error('请求超时，请稍后重试'))
      }
      return Promise.reject(error)
    }
  )

  return client
}

/** 需要认证的 API 客户端 */
const apiClient: AxiosInstance = createBaseClient()

// apiClient 额外处理 401 和服务器错误响应
apiClient.interceptors.response.use(
  (response) => {
    // 统一处理业务错误响应（code >= 400）
    const data = response.data as ResponseMessage<unknown>
    if (data && data.code >= 400 && typeof window !== 'undefined') {
      message.error(data.msg || '请求失败')
    }
    return response
  },
  (error: AxiosError<ResponseMessage<unknown>>) => {
    if (error.response?.status === 401) {
      if (typeof window !== 'undefined') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

/** 不带认证头的公开客户端（用于公开接口） */
const publicClient: AxiosInstance = createBaseClient()

export { apiClient, publicClient }
