'use client'

import { useCallback, useRef, useState, useEffect } from 'react'
import { fileService, calculateFileMd5 } from '@/apis/services/file.service'
import type { FileType } from '@/apis/schema/enumerate'

export type UploadPhase = 'idle' | 'preparing' | 'uploading' | 'verifying' | 'completed' | 'error'

export interface UsePresignedUploadReturn {
  phase: UploadPhase
  progress: number
  error: Error | null
  fileId: number | null
  upload: (file: File, type: FileType) => Promise<number | null>
  cancel: () => void
  reset: () => void
}

const PREPARING_DURATION = 800
const UPLOADING_OFFSET = 15
const UPLOADING_SCALE = 0.7
const VERIFYING_PROGRESS = 85
const MAX_PUT_ATTEMPTS = 3
const CONFIRM_RETRY_DELAYS = [1000, 2000, 4000]

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

export function usePresignedUpload(): UsePresignedUploadReturn {
  const [phase, setPhase] = useState<UploadPhase>('idle')
  const [progress, setProgress] = useState(0)
  const [error, setError] = useState<Error | null>(null)
  const [fileId, setFileId] = useState<number | null>(null)

  const xhrRef = useRef<XMLHttpRequest | null>(null)
  const cancelledRef = useRef(false)
  const retryingRef = useRef(false)
  const phaseRef = useRef<UploadPhase>('idle')
  const lastProgressTimeRef = useRef<number>(0)

  const setPhaseInternal = useCallback((p: UploadPhase) => {
    phaseRef.current = p
    setPhase(p)
  }, [])

  const setProgressInternal = useCallback((p: number) => {
    setProgress(p)
    if (p > 0) {
      lastProgressTimeRef.current = Date.now()
    }
  }, [])

  const runFakeProgress = useCallback(
    async (from: number, to: number, duration: number) => {
      const start = Date.now()
      while (!cancelledRef.current && phaseRef.current !== 'error') {
        const elapsed = Date.now() - start
        const ratio = Math.min(elapsed / duration, 1)
        setProgressInternal(from + (to - from) * ratio)
        if (ratio >= 1) break
        await sleep(16)
      }
    },
    [setProgressInternal]
  )

  const doPutUpload = useCallback(
    (file: File, uploadUrl: string): Promise<void> => {
      return new Promise((resolve, reject) => {
        if (cancelledRef.current && !retryingRef.current) {
          reject(new Error('UPLOAD_ABORTED'))
          return
        }

        const xhr = new XMLHttpRequest()
        xhrRef.current = xhr

        xhr.upload.onprogress = (event) => {
          if (event.lengthComputable) {
            const raw = (event.loaded * 100) / event.total
            const mapped = UPLOADING_OFFSET + raw * UPLOADING_SCALE
            setProgressInternal(mapped)
          }
        }

        xhr.onload = () => {
          xhrRef.current = null
          if (xhr.status >= 200 && xhr.status < 300) {
            resolve()
          } else if (xhr.status === 403) {
            reject(new Error('UPLOAD_URL_EXPIRED'))
          } else {
            reject(new Error(`上传失败: HTTP ${xhr.status}`))
          }
        }

        xhr.onerror = () => {
          xhrRef.current = null
          reject(new Error('网络错误，上传失败'))
        }

        xhr.ontimeout = () => {
          xhrRef.current = null
          reject(new Error('上传超时'))
        }

        xhr.onabort = () => {
          xhrRef.current = null
          if (retryingRef.current) {
            retryingRef.current = false
            reject(new Error('NETWORK_ERROR'))
          } else {
            reject(new Error('UPLOAD_ABORTED'))
          }
        }

        xhr.open('PUT', uploadUrl)
        xhr.setRequestHeader('Content-Type', file.type || 'application/octet-stream')
        xhr.send(file)
      })
    },
    [setProgressInternal]
  )

  const doConfirmWithRetry = useCallback(
    async (dto: {
      fileId: number
      callbackToken: string
      md5: string
      size: number
    }): Promise<number | null> => {
      for (let attempt = 0; attempt <= CONFIRM_RETRY_DELAYS.length; attempt++) {
        if (cancelledRef.current) return null
        try {
          const res = await fileService.confirmUpload(dto)
          if (res.code === 200 && res.data) {
            if (res.data.status === 'ACTIVE') {
              return res.data.fileId
            } else {
              throw new Error('文件校验未通过')
            }
          } else {
            throw new Error(res.msg || '确认上传失败')
          }
        } catch (err) {
          if (cancelledRef.current) return null
          if (attempt < CONFIRM_RETRY_DELAYS.length) {
            await sleep(CONFIRM_RETRY_DELAYS[attempt])
          } else {
            throw err
          }
        }
      }
      return null
    },
    []
  )

  const upload = useCallback(
    async (file: File, type: FileType): Promise<number | null> => {
      cancelledRef.current = false
      retryingRef.current = false
      setError(null)
      setFileId(null)
      setPhaseInternal('preparing')
      setProgressInternal(0)

      let prepareResult: {
        fileId: number
        uploadUrl: string
        callbackToken: string
        filename: string
        type: FileType
      }

      try {
        // 1. 计算 MD5 + fake progress 动画
        const md5Promise = calculateFileMd5(file)
        const fakeProgressPromise = runFakeProgress(0, UPLOADING_OFFSET, PREPARING_DURATION)
        const [md5] = await Promise.all([md5Promise, fakeProgressPromise])

        if (cancelledRef.current) return null

        // 2. prepareUpload
        const prepareRes = await fileService.prepareUpload({
          filename: file.name,
          type,
          size: file.size,
          contentType: file.type || 'application/octet-stream',
        })

        if (cancelledRef.current) return null

        if (prepareRes.code !== 200 || !prepareRes.data) {
          throw new Error(prepareRes.msg || '准备上传失败')
        }

        prepareResult = prepareRes.data

        // 3. PUT 到 OSS
        setPhaseInternal('uploading')
        setProgressInternal(UPLOADING_OFFSET)

        let putSuccess = false
        let putAttempts = 0

        while (!putSuccess && putAttempts < MAX_PUT_ATTEMPTS) {
          putAttempts++
          try {
            await doPutUpload(file, prepareResult.uploadUrl)
            putSuccess = true
          } catch (err) {
            const msg = err instanceof Error ? err.message : ''
            if (msg === 'UPLOAD_ABORTED' || cancelledRef.current) return null

            if (msg === 'UPLOAD_URL_EXPIRED') {
              // 重新 prepareUpload，不消耗尝试次数
              putAttempts--
              const newPrepareRes = await fileService.prepareUpload({
                filename: file.name,
                type,
                size: file.size,
                contentType: file.type || 'application/octet-stream',
              })
              if (cancelledRef.current) return null
              if (newPrepareRes.code !== 200 || !newPrepareRes.data) {
                throw new Error(newPrepareRes.msg || '重新准备上传失败')
              }
              prepareResult = newPrepareRes.data
            } else if (putAttempts >= MAX_PUT_ATTEMPTS) {
              throw err
            } else {
              await sleep(1000)
            }
          }
        }

        if (cancelledRef.current) return null

        // 4. confirmUpload（带重试）
        setPhaseInternal('verifying')
        setProgressInternal(VERIFYING_PROGRESS)

        const confirmedFileId = await doConfirmWithRetry({
          fileId: prepareResult.fileId,
          callbackToken: prepareResult.callbackToken,
          md5,
          size: file.size,
        })

        if (cancelledRef.current) return null
        if (confirmedFileId == null) {
          throw new Error('上传确认失败')
        }

        setPhaseInternal('completed')
        setProgressInternal(100)
        setFileId(confirmedFileId)
        return confirmedFileId
      } catch (err) {
        if (cancelledRef.current) return null
        const errorObj = err instanceof Error ? err : new Error(String(err))
        setError(errorObj)
        setPhaseInternal('error')
        throw errorObj
      }
    },
    [setPhaseInternal, setProgressInternal, runFakeProgress, doPutUpload, doConfirmWithRetry]
  )

  const cancel = useCallback(() => {
    cancelledRef.current = true
    retryingRef.current = false
    if (xhrRef.current) {
      xhrRef.current.abort()
      xhrRef.current = null
    }
    setPhaseInternal('idle')
    setProgressInternal(0)
    setError(null)
    setFileId(null)
  }, [setPhaseInternal, setProgressInternal])

  const reset = useCallback(() => {
    cancelledRef.current = true
    retryingRef.current = false
    if (xhrRef.current) {
      xhrRef.current.abort()
      xhrRef.current = null
    }
    setPhaseInternal('idle')
    setProgressInternal(0)
    setError(null)
    setFileId(null)
  }, [setPhaseInternal, setProgressInternal])

  // visibilitychange 监听：切回前台时若 uploading 且进度停滞，触发重试
  useEffect(() => {
    const handler = () => {
      if (
        document.visibilityState === 'visible' &&
        phaseRef.current === 'uploading' &&
        xhrRef.current
      ) {
        const stalled = Date.now() - lastProgressTimeRef.current > 5000
        if (stalled) {
          retryingRef.current = true
          xhrRef.current.abort()
          xhrRef.current = null
        }
      }
    }
    document.addEventListener('visibilitychange', handler)
    return () => {
      document.removeEventListener('visibilitychange', handler)
    }
  }, [])

  // 组件卸载时清理
  useEffect(() => {
    return () => {
      cancelledRef.current = true
      if (xhrRef.current) {
        xhrRef.current.abort()
        xhrRef.current = null
      }
    }
  }, [])

  return {
    phase,
    progress,
    error,
    fileId,
    upload,
    cancel,
    reset,
  }
}
