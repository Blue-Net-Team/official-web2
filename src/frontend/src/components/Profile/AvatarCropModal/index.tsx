'use client'

import { useState, useCallback } from 'react'
import { Modal, Slider } from 'antd'
import _Cropper from 'react-easy-crop'
import type { Area } from 'react-easy-crop'

// react-easy-crop exports a class component; cast to any for React 19 JSX compat
const Cropper = _Cropper as unknown as React.FC<Record<string, unknown>>

interface AvatarCropModalProps {
  open: boolean
  imageSrc: string | null
  onConfirm: (blob: Blob) => void
  onCancel: () => void
}

async function getCroppedBlob(imageSrc: string, pixelCrop: Area): Promise<Blob> {
  const image = new Image()
  image.src = imageSrc
  await new Promise((resolve) => {
    image.onload = resolve
  })

  const canvas = document.createElement('canvas')
  const maxSize = 512
  canvas.width = maxSize
  canvas.height = maxSize

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
    maxSize,
    maxSize
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
      'image/jpeg',
      0.9
    )
  })
}

export default function AvatarCropModal({
  open,
  imageSrc,
  onConfirm,
  onCancel,
}: AvatarCropModalProps) {
  const [crop, setCrop] = useState({ x: 0, y: 0 })
  const [zoom, setZoom] = useState(1)
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area | null>(null)

  const onCropComplete = useCallback((_croppedArea: Area, croppedPixels: Area) => {
    setCroppedAreaPixels(croppedPixels)
  }, [])

  const handleConfirm = useCallback(async () => {
    if (!imageSrc || !croppedAreaPixels) return

    try {
      const blob = await getCroppedBlob(imageSrc, croppedAreaPixels)
      onConfirm(blob)
    } catch {
      onCancel()
    }
  }, [imageSrc, croppedAreaPixels, onConfirm, onCancel])

  const handleCancel = useCallback(() => {
    setCrop({ x: 0, y: 0 })
    setZoom(1)
    setCroppedAreaPixels(null)
    onCancel()
  }, [onCancel])

  return (
    <Modal
      title="裁剪头像"
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
            aspect={1}
            cropShape="round"
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
