'use client'

import React, { useCallback, useEffect, useImperativeHandle, forwardRef } from 'react'
import { Upload } from 'antd'
import type { MessageInstance } from 'antd/es/message/interface'
import { PlusOutlined } from '@ant-design/icons'
import Image from 'next/image'
import { usePresignedUpload } from '@/hooks/usePresignedUpload'
import { useImageCropUpload } from '@/hooks/useImageCropUpload'

export interface AvatarUploadHandle {
  /** 清空预览与内部状态（表单提交成功后调用） */
  reset: () => void
}

interface AvatarUploadProps {
  messageApi: MessageInstance
  /** 裁剪并上传成功后回调（拿到 fileId） */
  onUploaded: (fileId: number) => void
  /** 上传中状态变化（用于禁用提交按钮等） */
  onUploadingChange?: (uploading: boolean) => void
}

const AvatarUpload = forwardRef<AvatarUploadHandle, AvatarUploadProps>(
  ({ messageApi, onUploaded, onUploadingChange }, ref) => {
    const presigned = usePresignedUpload()

    const { selectFile, cropModal, previewUrl, uploading, reset } = useImageCropUpload({
      fileType: 'AVATAR',
      upload: presigned.upload,
      cropShape: 'round',
      aspect: 1,
      outputSize: 512,
      title: '裁剪头像',
      onError: useCallback((msg: string) => messageApi.error(msg), [messageApi]),
      onUploaded,
    })

    useEffect(() => {
      onUploadingChange?.(uploading)
    }, [uploading, onUploadingChange])

    useImperativeHandle(
      ref,
      () => ({
        reset: () => {
          presigned.reset()
          reset()
        },
      }),
      [presigned, reset]
    )

    const handleUpload = useCallback(
      (file: File) => {
        selectFile(file)
        return false
      },
      [selectFile]
    )

    const uploadProgress = presigned.progress
    const uploadingAvatar = uploading

    return (
      <div className="flex flex-col items-center gap-[10px] shrink-0">
        <Upload
          accept="image/jpeg,image/png,image/webp"
          showUploadList={false}
          beforeUpload={handleUpload}
          disabled={uploadingAvatar}
        >
          <div
            className={`w-[100px] max-sm:w-[110px] h-[100px] max-sm:h-[110px] border-2 border-dashed border-[rgba(102,119,255,0.4)] rounded-full flex flex-col items-center justify-center cursor-pointer transition-all bg-[rgba(102,119,255,0.05)] relative overflow-hidden hover:border-[#6677ff] hover:shadow-[0_0_20px_rgba(102,119,255,0.4)] ${
              previewUrl ? 'border-solid border-transparent' : ''
            } ${uploadingAvatar ? 'border-[#6677ff] bg-[rgba(102,119,255,0.1)] cursor-not-allowed' : ''}`}
          >
            {uploadingAvatar ? (
              <div className="flex flex-col items-center justify-center gap-1 z-1">
                <div className="relative w-10 h-10 flex items-center justify-center">
                  <svg viewBox="0 0 36 36" className="w-full h-full -rotate-90">
                    <path
                      className="fill-none stroke-[rgba(102,119,255,0.2)]"
                      strokeWidth="3"
                      d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                    />
                    <path
                      className="fill-none stroke-[#6677ff]"
                      strokeWidth="3"
                      strokeLinecap="round"
                      strokeDasharray={`${uploadProgress || 0}, 100`}
                      d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                    />
                  </svg>
                  <span className="absolute text-[10px] text-[#6677ff] font-semibold">
                    {uploadProgress || 0}%
                  </span>
                </div>
                <span className="text-[11px] text-white/40">上传中...</span>
              </div>
            ) : previewUrl ? (
              <Image
                src={previewUrl}
                alt="avatar"
                width={120}
                height={120}
                className="absolute inset-[2px] w-[calc(100%-4px)] h-[calc(100%-4px)] object-cover rounded-full"
              />
            ) : (
              <div className="flex flex-col items-center justify-center gap-1 z-1">
                <PlusOutlined className="text-[28px] text-[#6677ff]/60" />
                <span className="text-[11px] text-white/40">点击上传</span>
              </div>
            )}
          </div>
        </Upload>
        <div className="text-xs text-white/50 font-medium">
          头像<span className="text-[#ff6b35] ml-[2px]">*</span>
        </div>
        {cropModal}
      </div>
    )
  }
)

AvatarUpload.displayName = 'AvatarUpload'

export default AvatarUpload
