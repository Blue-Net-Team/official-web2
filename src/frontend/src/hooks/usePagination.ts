import { useState, useCallback, useRef, useEffect } from 'react'
import { ResponseMessage, PageDTO } from '@/apis/schema/type'

export interface UsePaginationReturn<T> {
  data: T[]
  total: number
  totalPages: number
  loading: boolean
  error: Error | null
  currentPage: number
  setCurrentPage: (page: number) => void
  refresh: () => void
  reset: () => void
}

/**
 * 分页数据获取 Hook
 * 自动处理分页状态和 API 调用
 *
 * @example
 * const { data, total, loading, currentPage, setCurrentPage, refresh } = usePagination(
 *   (page, size) => memberService.getMemberList({ page, size }),
 *   { pageSize: 16 }
 * )
 */
export function usePagination<T>(
  apiFn: (page: number, pageSize: number) => Promise<ResponseMessage<PageDTO<T>>>,
  options: { pageSize?: number; initialPage?: number } = {}
): UsePaginationReturn<T> {
  const { pageSize = 10, initialPage = 0 } = options
  const [data, setData] = useState<T[]>([])
  const [total, setTotal] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<Error | null>(null)
  const [currentPageState, setCurrentPageState] = useState(initialPage)
  const [fetchKey, setFetchKey] = useState(0)
  const isMounted = useRef(true)
  const apiFnRef = useRef(apiFn)
  apiFnRef.current = apiFn

  const fetchData = useCallback(
    async (page: number) => {
      setLoading(true)
      setError(null)
      try {
        const response = await apiFnRef.current(page, pageSize)
        if (response.code === 200 && response.data) {
          if (isMounted.current) {
            setData(response.data.content)
            setTotal(response.data.totalElements)
            setTotalPages(response.data.totalPages)
          }
        } else {
          const err = new Error(response.msg || '请求失败')
          if (isMounted.current) {
            setError(err)
          }
        }
      } catch (err) {
        const error = err instanceof Error ? err : new Error(String(err))
        if (isMounted.current) {
          setError(error)
        }
      } finally {
        if (isMounted.current) {
          setLoading(false)
        }
      }
    },
    [pageSize]
  )

  // Auto-fetch when currentPage changes
  useEffect(() => {
    fetchData(currentPageState)
  }, [currentPageState, fetchData, fetchKey])

  const setCurrentPage = useCallback((page: number) => {
    setCurrentPageState(page)
    setFetchKey((k) => k + 1)
  }, [])

  const refresh = useCallback(() => {
    setFetchKey((k) => k + 1)
  }, [])

  const reset = useCallback(() => {
    setData([])
    setTotal(0)
    setTotalPages(0)
    setLoading(false)
    setError(null)
    setCurrentPageState(0)
    setFetchKey((k) => k + 1)
  }, [])

  return {
    data,
    total,
    totalPages,
    loading,
    error,
    currentPage: currentPageState,
    setCurrentPage,
    refresh,
    reset,
  }
}
