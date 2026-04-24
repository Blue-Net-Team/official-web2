import { useState, useCallback, useRef, useEffect } from 'react'
import { ResponseMessage } from '@/apis/schema/type'

export interface UseListApiReturn<T, Args extends unknown[] = unknown[]> {
  data: T[]
  loading: boolean
  error: Error | null
  execute: (...args: Args) => Promise<T[]>
  refresh: () => void
  reset: () => void
}

/**
 * 列表数据获取 Hook（不分页）
 * 自动将 API 返回数据转为数组
 *
 * @example
 * const { data, loading, execute } = useListApi(assessmentTimeService.getAssessmentTimes)
 * // data 为 AssessmentTimeDTO[]
 */
export function useListApi<T, Args extends unknown[] = unknown[]>(
  apiFn: (...args: Args) => Promise<ResponseMessage<T[]>>,
  options: { immediate?: boolean; defaultArgs?: Args } = {}
): UseListApiReturn<T, Args> {
  const { immediate = true, defaultArgs = [] as unknown as Args } = options
  const [data, setData] = useState<T[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)
  const isMounted = useRef(true)

  const execute = useCallback(
    async (...args: Args): Promise<T[]> => {
      setLoading(true)
      setError(null)
      try {
        const response = await apiFn(...args)
        if (response.code === 200 && response.data) {
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

  const refresh = useCallback(() => {
    execute(...defaultArgs)
  }, [execute, defaultArgs])

  const reset = useCallback(() => {
    setData([])
    setLoading(false)
    setError(null)
  }, [])

  useEffect(() => {
    if (immediate) {
      execute(...defaultArgs)
    }
  }, [execute, immediate, defaultArgs])

  return { data, loading, error, execute, refresh, reset }
}
