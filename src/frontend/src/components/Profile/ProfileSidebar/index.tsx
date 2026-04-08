'use client'

import { useState, useRef, useCallback } from 'react'
import type { UserInfo, UserStats } from '@/types/profile'
import { DIRECTION_LABELS, Direction } from '@/apis/schema/enumerate'
import { API_BASE_URL } from '@/apis/config'
import { fileService } from '@/apis/services/file.service'
import {
  DesktopOutlined,
  BookOutlined,
  CalendarOutlined,
  CameraOutlined,
  LoadingOutlined,
} from '@ant-design/icons'
import { message } from 'antd'
import Image from 'next/image'
import AvatarCropModal from '../AvatarCropModal'
import cvIcon from '@/assets/icon/direction/cv_icon.png'
import structIcon from '@/assets/icon/direction/struct_icon.png'
import embedIcon from '@/assets/icon/direction/embed_icon.png'

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
const MAX_SIZE = 5 * 1024 * 1024

interface ProfileSidebarProps {
  profile: UserInfo
  stats: UserStats
  onAvatarUpdate?: () => void
}

const directionIconMap: Record<Direction, string> = {
  COMPUTER_VISION: cvIcon,
  STRUCTURAL_DESIGN: structIcon,
  EMBEDDED: embedIcon,
}

const directionThemeMap: Record<Direction, 'computerVision' | 'structuralDesign' | 'embedded'> = {
  COMPUTER_VISION: 'computerVision',
  STRUCTURAL_DESIGN: 'structuralDesign',
  EMBEDDED: 'embedded',
}

export default function ProfileSidebar({ profile, stats, onAvatarUpdate }: ProfileSidebarProps) {
  const [uploading, setUploading] = useState(false)
  const [cropModalOpen, setCropModalOpen] = useState(false)
  const [cropImageSrc, setCropImageSrc] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const directionLabel = DIRECTION_LABELS[profile.direction] || profile.direction || '-'
  const displayName = profile.nickname || profile.username
  const directionIcon = directionIconMap[profile.direction as Direction]
  const directionTheme = directionThemeMap[profile.direction as Direction]

  const handleAvatarClick = useCallback(() => {
    if (uploading) return
    fileInputRef.current?.click()
  }, [uploading])

  const handleFileChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    e.target.value = ''

    if (!ALLOWED_TYPES.includes(file.type)) {
      message.error('请选择图片文件（JPG/PNG/GIF/WEBP）')
      return
    }

    if (file.size > MAX_SIZE) {
      message.error('图片大小不能超过 5MB')
      return
    }

    const url = URL.createObjectURL(file)
    setCropImageSrc(url)
    setCropModalOpen(true)
  }, [])

  const handleCropConfirm = useCallback(
    async (blob: Blob) => {
      setUploading(true)
      setCropModalOpen(false)
      try {
        const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })
        const res = await fileService.uploadAvatar(file)
        if (res.code === 200) {
          message.success('头像更新成功')
          onAvatarUpdate?.()
        } else {
          message.error(res.msg || '头像上传失败，请重试')
        }
      } catch {
        message.error('头像上传失败，请重试')
      } finally {
        setUploading(false)
        if (cropImageSrc) {
          URL.revokeObjectURL(cropImageSrc)
          setCropImageSrc(null)
        }
      }
    },
    [onAvatarUpdate, cropImageSrc]
  )

  const handleCropCancel = useCallback(() => {
    setCropModalOpen(false)
    if (cropImageSrc) {
      URL.revokeObjectURL(cropImageSrc)
      setCropImageSrc(null)
    }
  }, [cropImageSrc])

  return (
    <aside className="w-[340px] shrink-0 max-lg:w-full max-lg:shrink">
      <div className="sticky top-[104px] bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-8 transition-all duration-300 max-lg:relative max-lg:top-auto max-[640px]:p-5">
        <div className="text-center mb-6">
          <div className="relative inline-block mb-5">
            <div
              className={`relative w-[120px] h-[120px] rounded-full bg-[linear-gradient(135deg,#6677ff_0%,#ff6b35_100%)] p-1 overflow-hidden group ${uploading ? '' : 'cursor-pointer'}`}
              onClick={handleAvatarClick}
            >
              <div className="w-[112px] h-[112px] rounded-full bg-[linear-gradient(135deg,#1a1a1a_0%,#2a2a2a_100%)] flex items-center justify-center text-[48px] font-bold text-white overflow-hidden [&>img]:w-full [&>img]:h-full [&>img]:object-cover">
                {uploading ? (
                  <LoadingOutlined className="text-[28px] text-[#6677ff]" />
                ) : profile.avatarFileId ? (
                  <Image
                    src={`${API_BASE_URL}/file/download/${profile.avatarFileId}`}
                    alt={displayName}
                    width={120}
                    height={120}
                  />
                ) : (
                  displayName.charAt(0)
                )}
              </div>
              {!uploading && (
                <div className="absolute inset-1 rounded-full bg-black/55 flex flex-col items-center justify-center gap-1 opacity-0 transition-opacity duration-250 pointer-events-none group-hover:opacity-100">
                  <CameraOutlined className="text-[20px] text-white" />
                  <span className="text-[11px] text-white/85 whitespace-nowrap">更换头像</span>
                </div>
              )}
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              onChange={handleFileChange}
              className="hidden"
            />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white mb-1.5">{displayName}</h1>
            {profile.nickname && (
              <span className="text-sm text-[rgba(140,140,141,1)]">@{profile.username}</span>
            )}
          </div>
          <span
            className={`inline-flex items-center gap-1.5 px-[14px] py-1.5 rounded-[20px] text-xs font-semibold mt-3 ${
              profile.roleName === 'candidate'
                ? 'bg-[linear-gradient(135deg,#ff6b35_0%,#ff8c42_100%)] text-white'
                : 'bg-[linear-gradient(135deg,#6677ff_0%,#2f27b0_100%)] text-white'
            }`}
          >
            {profile.roleName === 'candidate' ? '考生' : '成员'}
          </span>
        </div>

        {profile.bio && (
          <div className="mb-6 pb-6 border-b border-white/[0.05]">
            <p className="text-sm leading-[1.7] text-white/70 text-center">{profile.bio}</p>
          </div>
        )}

        <div className="mb-6">
          {profile.college && (
            <div className="flex items-center gap-3 py-3 text-sm text-white/70 border-b border-white/[0.03] [&>svg]:w-[18px] [&>svg]:h-[18px] [&>svg]:text-[#6677ff] [&>svg]:shrink-0">
              <DesktopOutlined />
              <span>{profile.college}</span>
            </div>
          )}
          {profile.major && (
            <div className="flex items-center gap-3 py-3 text-sm text-white/70 border-b border-white/[0.03] [&>svg]:w-[18px] [&>svg]:h-[18px] [&>svg]:text-[#6677ff] [&>svg]:shrink-0">
              <BookOutlined />
              <span>{profile.major}</span>
            </div>
          )}
          {profile.grade && (
            <div className="flex items-center gap-3 py-3 text-sm text-white/70 [&>svg]:w-[18px] [&>svg]:h-[18px] [&>svg]:text-[#6677ff] [&>svg]:shrink-0">
              <CalendarOutlined />
              <span>{profile.grade}</span>
            </div>
          )}
        </div>

        {profile.direction && (
          <div className="mb-6 py-6 border-t border-b border-white/[0.05]">
            <div className="text-xs font-semibold text-[rgba(140,140,141,1)] uppercase tracking-[0.5px] mb-4">
              报名方向
            </div>
            <div className="flex items-center gap-3 p-3 rounded-[10px] bg-white/[0.02] mb-2.5 transition-all duration-300 hover:bg-[rgba(102,119,255,0.08)]">
              <div
                className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 overflow-hidden ${
                  directionTheme === 'computerVision'
                    ? 'bg-gradient-to-br from-[rgba(102,119,255,0.3)] to-[rgba(47,39,176,0.3)] shadow-[0_0_20px_rgba(102,119,255,0.3)]'
                    : directionTheme === 'structuralDesign'
                      ? 'bg-gradient-to-br from-[rgba(255,107,53,0.3)] to-[rgba(255,140,66,0.3)] shadow-[0_0_20px_rgba(255,107,53,0.3)]'
                      : 'bg-gradient-to-br from-[rgba(46,204,113,0.3)] to-[rgba(39,174,96,0.3)] shadow-[0_0_20px_rgba(46,204,113,0.3)]'
                }`}
              >
                {directionIcon && (
                  <Image src={directionIcon} alt={directionLabel} width={44} height={44} />
                )}
              </div>
              <div className="flex-1">
                <div className="text-sm font-medium text-white">{directionLabel}</div>
              </div>
            </div>
          </div>
        )}

        <div className="grid grid-cols-3 gap-3 pt-6 border-t border-white/[0.05]">
          <div className="text-center py-3 px-2 rounded-[10px] bg-white/[0.02] transition-all duration-300 hover:bg-[rgba(102,119,255,0.08)]">
            <div className="text-xl font-bold text-[#6677ff] mb-1">{stats.assessmentCount}</div>
            <div className="text-xs text-[rgba(140,140,141,1)]">考核轮次</div>
          </div>
          <div className="text-center py-3 px-2 rounded-[10px] bg-white/[0.02] transition-all duration-300 hover:bg-[rgba(102,119,255,0.08)]">
            <div className="text-xl font-bold text-[#6677ff] mb-1">{stats.completedCount}</div>
            <div className="text-xs text-[rgba(140,140,141,1)]">已完成</div>
          </div>
          <div className="text-center py-3 px-2 rounded-[10px] bg-white/[0.02] transition-all duration-300 hover:bg-[rgba(102,119,255,0.08)]">
            <div className="text-xl font-bold text-[#6677ff] mb-1">{stats.averageScore}</div>
            <div className="text-xs text-[rgba(140,140,141,1)]">平均分</div>
          </div>
        </div>
      </div>

      <AvatarCropModal
        open={cropModalOpen}
        imageSrc={cropImageSrc}
        onConfirm={handleCropConfirm}
        onCancel={handleCropCancel}
      />
    </aside>
  )
}
