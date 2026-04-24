'use client'

import React, { useCallback } from 'react'
import { Upload } from 'antd'
import type { MessageInstance } from 'antd/es/message/interface'
import { PlusOutlined } from '@ant-design/icons'
import Image from 'next/image'

interface AvatarUploadProps {
  previewUrl?: string
  uploading?: boolean
  uploadProgress?: number
  onFileSelect?: (file: File) => void
  messageApi: MessageInstance
}

const AvatarUpload: React.FC<AvatarUploadProps> = ({
  previewUrl,
  uploading,
  uploadProgress,
  onFileSelect,
  messageApi,
}) => {
  const handleUpload = useCallback(
    (file: File) => {
      const isImage = file.type.startsWith('image/')
      if (!isImage) {
        messageApi.error('请选择图片文件')
        return false
      }

      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isLt2M) {
        messageApi.error('图片大小不能超过2MB')
        return false
      }

      onFileSelect?.(file)
      return false
    },
    [onFileSelect, messageApi]
  )

  return (
    <div className="flex flex-col items-center gap-[10px] shrink-0">
      <Upload
        accept="image/*"
        showUploadList={false}
        beforeUpload={handleUpload}
        disabled={uploading}
      >
        <div
          className={`w-[100px] max-sm:w-[110px] h-[100px] max-sm:h-[110px] border-2 border-dashed border-[rgba(102,119,255,0.4)] rounded-full flex flex-col items-center justify-center cursor-pointer transition-all bg-[rgba(102,119,255,0.05)] relative overflow-hidden hover:border-[#6677ff] hover:shadow-[0_0_20px_rgba(102,119,255,0.4)] ${
            previewUrl ? 'border-solid border-transparent' : ''
          } ${uploading ? 'border-[#6677ff] bg-[rgba(102,119,255,0.1)] cursor-not-allowed' : ''}`}
        >
          {uploading ? (
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
              <PlusOutlined style={{ fontSize: '28px', color: 'rgba(102, 119, 255, 0.6)' }} />
              <span className="text-[11px] text-white/40">点击上传</span>
            </div>
          )}
        </div>
      </Upload>
      <div className="text-xs text-white/50 font-medium">
        头像<span className="text-[#ff6b35] ml-[2px]">*</span>
      </div>
    </div>
  )
}

export default AvatarUpload
