import axios, {
  AxiosInstance,
  AxiosError,
  InternalAxiosRequestConfig,
  AxiosRequestConfig,
} from 'axios'
import { API_BASE_URL } from './config'
import { ResponseMessage } from './schema/type'

/** 获取 CSRF Token（从内存/store） */
let csrfToken: string | null = null

/** 设置 CSRF Token */
export const setCsrfToken = (token: string | null) => {
  csrfToken = token
}

/** 获取 CSRF Token */
export const getCsrfToken = () => csrfToken

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

/** 请求拦截器 - 自动添加 CSRF Token */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 对 POST/PUT/DELETE/PATCH 请求添加 CSRF Token
    const method = config.method?.toLowerCase()
    if (method && CSRF_METHODS.includes(method) && csrfToken && config.headers) {
      config.headers['X-CSRF-Token'] = csrfToken
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
      // 401 时清除 CSRF Token
      csrfToken = null
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

/** 公开客户端请求拦截器 - 自动添加 CSRF Token（登录后可能需要） */
publicClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const method = config.method?.toLowerCase()
    if (method && CSRF_METHODS.includes(method) && csrfToken && config.headers) {
      config.headers['X-CSRF-Token'] = csrfToken
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
