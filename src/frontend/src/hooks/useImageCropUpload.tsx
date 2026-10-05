'use client'

import { useState, useCallback, useRef } from 'react'
import type { ReactNode } from 'react'
import ImageCropModal from '@/components/common/ImageCropModal'
import type { FileType } from '@/apis/schema/enumerate'

export interface UseImageCropUploadOptions {
  /** 上传通道（预签名直传或旧通道适配函数），返回 fileId；失败时抛出异常或返回 null */
  upload: (file: File, type: FileType) => Promise<number | null>
  fileType: FileType
  /** 裁剪框形状，默认 rect；头像场景传 round */
  cropShape?: 'rect' | 'round'
  /** 裁剪宽高比（宽/高），默认 1 */
  aspect?: number
  /** 输出图片宽度（px），默认 512 */
  outputSize?: number
  /** 弹窗标题，默认「裁剪图片」 */
  title?: string
  /** 允许的文件类型，默认 jpeg/png/webp（GIF 会被 Canvas 静默静态化，故默认排除） */
  allowedTypes?: string[]
  /** 最大文件体积（MB），默认 5 */
  maxSizeMB?: number
  /** 校验或上传失败时的错误提示回调 */
  onError?: (message: string) => void
  /** 上传成功后的回调（与出参 fileId 二选一或同时使用） */
  onUploaded?: (fileId: number) => void
}

export interface UseImageCropUploadReturn {
  /** 供 Upload beforeUpload / input onChange 调用：校验通过则打开裁剪弹窗 */
  selectFile: (file: File) => boolean
  /** 渲染用裁剪弹窗元素 */
  cropModal: ReactNode
  /** 裁剪后文件的预览 objectURL（reset / 重新选择时释放） */
  previewUrl: string | null
  /** 最近一次上传成功的 fileId */
  fileId: number | null
  /** 上传中（从裁剪确认到 upload 返回） */
  uploading: boolean
  /** 最近一次错误信息 */
  error: string | null
  /** 清空 previewUrl / fileId / error，释放所有 objectURL */
  reset: () => void
}

const DEFAULT_ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const DEFAULT_MAX_SIZE_MB = 5
const TYPE_LABEL = 'JPG/PNG/WEBP'

export function useImageCropUpload(options: UseImageCropUploadOptions): UseImageCropUploadReturn {
  const [cropOpen, setCropOpen] = useState(false)
  const [imageSrc, setImageSrc] = useState<string | null>(null)
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)
  const [fileId, setFileId] = useState<number | null>(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const cropObjectUrlRef = useRef<string | null>(null)
  const previewObjectUrlRef = useRef<string | null>(null)

  const revokeCropUrl = useCallback(() => {
    if (cropObjectUrlRef.current) {
      URL.revokeObjectURL(cropObjectUrlRef.current)
      cropObjectUrlRef.current = null
    }
  }, [])

  const revokePreviewUrl = useCallback(() => {
    if (previewObjectUrlRef.current) {
      URL.revokeObjectURL(previewObjectUrlRef.current)
      previewObjectUrlRef.current = null
    }
    setPreviewUrl(null)
  }, [])

  const fail = useCallback(
    (message: string) => {
      setError(message)
      options.onError?.(message)
    },
    [options.onError]
  )

  const selectFile = useCallback(
    (file: File): boolean => {
      const allowedTypes = options.allowedTypes ?? DEFAULT_ALLOWED_TYPES
      const maxSizeMB = options.maxSizeMB ?? DEFAULT_MAX_SIZE_MB

      if (!allowedTypes.includes(file.type)) {
        fail(`请选择图片文件（${TYPE_LABEL}）`)
        return false
      }
      if (file.size > maxSizeMB * 1024 * 1024) {
        fail(`图片大小不能超过 ${maxSizeMB}MB`)
        return false
      }

      // 释放上一轮裁剪弹窗的 objectURL；旧预览与 fileId 保留，
      // 直到新文件裁剪上传成功后才替换（取消裁剪不丢失已上传头像）
      revokeCropUrl()
      setError(null)

      const url = URL.createObjectURL(file)
      cropObjectUrlRef.current = url
      setImageSrc(url)
      setCropOpen(true)
      return true
    },
    [options.allowedTypes, options.maxSizeMB, fail, revokeCropUrl]
  )

  const handleCropConfirm = useCallback(
    async (blob: Blob) => {
      setCropOpen(false)
      revokeCropUrl()

      const ext = blob.type === 'image/png' ? 'png' : blob.type === 'image/webp' ? 'webp' : 'jpg'
      const croppedFile = new File([blob], `cropped.${ext}`, { type: blob.type })

      const preview = URL.createObjectURL(croppedFile)
      previewObjectUrlRef.current = preview
      setPreviewUrl(preview)

      setUploading(true)
      try {
        const id = await options.upload(croppedFile, options.fileType)
        if (id != null) {
          setFileId(id)
          options.onUploaded?.(id)
        } else {
          fail('上传失败，请重试')
        }
      } catch {
        fail('上传失败，请重试')
      } finally {
        setUploading(false)
      }
    },
    [options.upload, options.fileType, options.onUploaded, fail, revokeCropUrl]
  )

  const handleCropCancel = useCallback(() => {
    setCropOpen(false)
    revokeCropUrl()
  }, [revokeCropUrl])

  const reset = useCallback(() => {
    setCropOpen(false)
    setFileId(null)
    setError(null)
    revokeCropUrl()
    revokePreviewUrl()
  }, [revokeCropUrl, revokePreviewUrl])

  const cropModal = (
    <ImageCropModal
      open={cropOpen}
      imageSrc={imageSrc}
      title={options.title ?? '裁剪图片'}
      outputSize={options.outputSize ?? 512}
      aspect={options.aspect ?? 1}
      cropShape={options.cropShape ?? 'rect'}
      onConfirm={handleCropConfirm}
      onCancel={handleCropCancel}
    />
  )

  return {
    selectFile,
    cropModal,
    previewUrl,
    fileId,
    uploading,
    error,
    reset,
  }
}
