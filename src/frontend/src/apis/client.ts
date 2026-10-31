import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { API_BASE_URL } from './config'
import { ResponseMessage } from './schema/type'
import { message } from 'antd'

declare module 'axios' {
  interface AxiosRequestConfig {
    /** 仅用于明确需要静默的后台请求，不影响错误传播或 401 跳转。 */
    skipErrorMessage?: boolean
  }
}

/** 获取 CSRF Token（从 cookie 读取，保证与 cookie 始终一致） */
function getCsrfTokenFromCookie(): string | null {
  if (typeof document === 'undefined') return null
  const match = document.cookie.match(/(?:^|;\s*)csrf_token=([^;]*)/)
  return match ? decodeURIComponent(match[1]) : null
}

/** 需要添加 CSRF Token 的请求方法 */
const CSRF_METHODS = ['post', 'put', 'delete', 'patch']

const HTTP_ERROR_MESSAGES: Record<number, string> = {
  400: '请求参数有误，请检查后重试',
  401: '登录状态已失效，请重新登录',
  403: '没有权限执行此操作',
  404: '请求的资源不存在或已被删除',
  405: '当前操作不被支持',
  408: '请求超时，请稍后重试',
  409: '数据状态已发生变化，请刷新后重试',
  413: '上传内容过大，请压缩后重试',
  415: '不支持的内容格式',
  422: '提交内容未通过校验，请检查后重试',
  429: '操作过于频繁，请稍后重试',
  500: '服务器内部错误，请稍后重试',
  502: '服务暂时不可用，请稍后重试',
  503: '服务正在维护或暂时不可用',
  504: '服务响应超时，请稍后重试',
}

const DEFAULT_ERROR_MESSAGE = '请求失败，请稍后重试'

function getResponseMessage(data: unknown): string | null {
  if (!data || typeof data !== 'object' || !('msg' in data)) return null
  const msg = data.msg
  return typeof msg === 'string' && msg.trim() ? msg.trim() : null
}

function showErrorMessage(content: string): void {
  if (typeof window !== 'undefined') {
    message.error(content)
  }
}

function getHttpErrorMessage(error: AxiosError<ResponseMessage<unknown>>): string {
  const responseMessage = getResponseMessage(error.response?.data)
  if (responseMessage) return responseMessage

  const status = error.response?.status
  if (status !== undefined) {
    return HTTP_ERROR_MESSAGES[status] ?? DEFAULT_ERROR_MESSAGE
  }

  if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
    return HTTP_ERROR_MESSAGES[408]
  }

  return '网络连接异常，请检查网络后重试'
}

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

  // 响应拦截器 - 统一展示业务错误和 HTTP/网络错误
  client.interceptors.response.use(
    (response) => {
      const data = response.data as ResponseMessage<unknown>
      if (data && data.code >= 400 && !response.config.skipErrorMessage) {
        showErrorMessage(
          getResponseMessage(data) ?? HTTP_ERROR_MESSAGES[data.code] ?? DEFAULT_ERROR_MESSAGE
        )
      }
      return response
    },
    (error: AxiosError<ResponseMessage<unknown>>) => {
      if (!error.config?.skipErrorMessage) {
        showErrorMessage(getHttpErrorMessage(error))
      }
      return Promise.reject(error)
    }
  )

  return client
}

/** 需要认证的 API 客户端 */
const apiClient: AxiosInstance = createBaseClient()

// apiClient 额外处理 401 登录跳转
apiClient.interceptors.response.use(
  (response) => response,
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
