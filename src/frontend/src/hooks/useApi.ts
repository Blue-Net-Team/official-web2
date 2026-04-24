import { useState, useCallback, useRef } from 'react'
import { ResponseMessage } from '@/apis/schema/type'

export interface UseApiReturn<T, Args extends unknown[] = unknown[]> {
  data: T | null
  loading: boolean
  error: Error | null
  execute: (...args: Args) => Promise<T | null>
  reset: () => void
}

/**
 * 通用 API 调用 Hook
 * @param apiFn 返回 Promise<ResponseMessage<T>> 的 API 函数
 * @returns { data, loading, error, execute, reset }
 *
 * @example
 * const { data, loading, error, execute } = useApi(memberService.getMemberById)
 * // 调用
 * execute(1)
 */
export function useApi<T, Args extends unknown[] = unknown[]>(
  apiFn: (...args: Args) => Promise<ResponseMessage<T>>
): UseApiReturn<T, Args> {
  const [data, setData] = useState<T | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)
  const isMounted = useRef(true)

  const execute = useCallback(
    async (...args: Args): Promise<T | null> => {
      setLoading(true)
      setError(null)
      try {
        const response = await apiFn(...args)
        if (response.code === 200 && response.data !== undefined) {
          if (isMounted.current) {
            setData(response.data)
          }
          return response.data
        } else {
          const err = new Error(response.msg || '请求失败')
          if (isMounted.current) {
            setError(err)
          }
          throw err
        }
      } catch (err) {
        const error = err instanceof Error ? err : new Error(String(err))
        if (isMounted.current) {
          setError(error)
        }
        throw error
      } finally {
        if (isMounted.current) {
          setLoading(false)
        }
      }
    },
    [apiFn]
  )

  const reset = useCallback(() => {
    setData(null)
    setLoading(false)
    setError(null)
  }, [])

  return { data, loading, error, execute, reset }
}
