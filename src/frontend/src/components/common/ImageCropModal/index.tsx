'use client'

import { useState, useCallback } from 'react'
import { Modal, Slider } from 'antd'
import _Cropper from 'react-easy-crop'
import type { Area } from 'react-easy-crop'

// react-easy-crop exports a class component; cast to any for React 19 JSX compat
const Cropper = _Cropper as unknown as React.FC<Record<string, unknown>>

export interface ImageCropModalProps {
  open: boolean
  imageSrc: string | null
  onConfirm: (blob: Blob) => void
  onCancel: () => void
  /** 输出图片宽度（px），高度由 aspect 决定，默认 512 */
  outputSize?: number
  /** 裁剪宽高比（宽/高），默认 1（正方形） */
  aspect?: number
  /** 弹窗标题 */
  title?: string
  /** 裁剪框形状，默认矩形；头像等圆形场景传 'round' */
  cropShape?: 'rect' | 'round'
}

async function getCroppedBlob(
  imageSrc: string,
  pixelCrop: Area,
  outputSize: number,
  aspect: number
): Promise<Blob> {
  const image = new Image()
  image.src = imageSrc
  await new Promise((resolve) => {
    image.onload = resolve
  })

  // 裁剪仅裁剪：输出格式保持与原图一致（如 PNG 保留透明通道）
  const sourceBlob = await fetch(imageSrc).then((r) => r.blob())
  const mimeType = sourceBlob.type || 'image/jpeg'

  const outputHeight = Math.round(outputSize / aspect)
  const canvas = document.createElement('canvas')
  canvas.width = outputSize
  canvas.height = outputHeight

  const ctx = canvas.getContext('2d')!

  ctx.imageSmoothingQuality = 'high'

  ctx.drawImage(
    image,
    pixelCrop.x,
    pixelCrop.y,
    pixelCrop.width,
    pixelCrop.height,
    0,
    0,
    outputSize,
    outputHeight
  )

  return new Promise((resolve, reject) => {
    canvas.toBlob(
      (blob) => {
        if (blob) {
          resolve(blob)
        } else {
          reject(new Error('Canvas toBlob failed'))
        }
      },
      mimeType,
      // quality 参数仅对 JPEG 等有损格式生效
      mimeType === 'image/jpeg' ? 0.9 : undefined
    )
  })
}

export default function ImageCropModal({
  open,
  imageSrc,
  onConfirm,
  onCancel,
  outputSize = 512,
  aspect = 1,
  title = '裁剪图片',
  cropShape = 'rect',
}: ImageCropModalProps) {
  const [crop, setCrop] = useState({ x: 0, y: 0 })
  const [zoom, setZoom] = useState(1)
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area | null>(null)

  const onCropComplete = useCallback((_croppedArea: Area, croppedPixels: Area) => {
    setCroppedAreaPixels(croppedPixels)
  }, [])

  const handleConfirm = useCallback(async () => {
    if (!imageSrc || !croppedAreaPixels) return

    try {
      const blob = await getCroppedBlob(imageSrc, croppedAreaPixels, outputSize, aspect)
      onConfirm(blob)
    } catch {
      onCancel()
    }
  }, [imageSrc, croppedAreaPixels, outputSize, aspect, onConfirm, onCancel])

  const handleCancel = useCallback(() => {
    setCrop({ x: 0, y: 0 })
    setZoom(1)
    setCroppedAreaPixels(null)
    onCancel()
  }, [onCancel])

  return (
    <Modal
      title={title}
      open={open}
      onOk={handleConfirm}
      onCancel={handleCancel}
      okText="确认"
      cancelText="取消"
      width={480}
      centered
      destroyOnHidden
      afterOpenChange={() => {
        setCrop({ x: 0, y: 0 })
        setZoom(1)
      }}
    >
      <div className="relative w-full h-[360px] bg-[#1a1a1a] rounded-lg overflow-hidden mb-4">
        {imageSrc && (
          <Cropper
            image={imageSrc}
            crop={crop}
            zoom={zoom}
            aspect={aspect}
            cropShape={cropShape}
            showGrid={false}
            onCropChange={setCrop}
            onZoomChange={setZoom}
            onCropComplete={onCropComplete}
          />
        )}
      </div>
      <div className="flex items-center gap-3 px-1">
        <span className="text-[13px] text-white/50 whitespace-nowrap shrink-0">缩放</span>
        <Slider min={1} max={3} step={0.01} value={zoom} onChange={setZoom} className="flex-1" />
      </div>
    </Modal>
  )
}
