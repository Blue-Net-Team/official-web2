'use client'

import { useState, useRef, useCallback } from 'react'
import type { TabCounts } from '@/apis/schema/type'
import { DIRECTION_LABELS, ROLE_LABELS, getRoleTagColor, Direction } from '@/apis/schema/enumerate'
import { API_BASE_URL } from '@/apis/config'
import { fileService } from '@/apis/services/file.service'
import {
  DesktopOutlined,
  BookOutlined,
  CalendarOutlined,
  CameraOutlined,
  LoadingOutlined,
} from '@ant-design/icons'
import { App, Tag, Modal } from 'antd'
import { QrcodeOutlined } from '@ant-design/icons'
import Image, { type StaticImageData } from 'next/image'
import AvatarCropModal from '../AvatarCropModal'
import cvIcon from '@/assets/icon/direction/cv_icon.png'
import structIcon from '@/assets/icon/direction/struct_icon.png'
import embedIcon from '@/assets/icon/direction/embed_icon.png'

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
const MAX_SIZE = 5 * 1024 * 1024

/**
 * 统一的侧边栏用户数据接口
 * 适配 UserInfo（个人主页）和 MemberDetailDTO（成员详情页）
 */
export interface SidebarProfile {
  username: string
  nickname: string
  college: string
  major: string
  grade: string
  bio: string
  avatarFileId: number | null
  wechatQrcode: string | null
  roleName: string
  direction: Direction | null
}

interface ProfileSidebarProps {
  profile: SidebarProfile
  /** 是否允许上传头像（个人主页为 true，查看他人为 false） */
  allowAvatarUpload?: boolean
  /** 经历统计 + 点击可切换 tab（成员详情页需要） */
  tabCounts?: TabCounts
  onTabChange?: (tab: string) => void
  activeTab?: string
  onAvatarUpdate?: () => void
}

const directionIconMap: Record<Direction, StaticImageData> = {
  COMPUTER_VISION: cvIcon,
  STRUCTURAL_DESIGN: structIcon,
  EMBEDDED: embedIcon,
}

const directionThemeMap: Record<Direction, 'computerVision' | 'structuralDesign' | 'embedded'> = {
  COMPUTER_VISION: 'computerVision',
  STRUCTURAL_DESIGN: 'structuralDesign',
  EMBEDDED: 'embedded',
}

export default function ProfileSidebar({
  profile,
  allowAvatarUpload = true,
  tabCounts,
  onTabChange,
  activeTab,
  onAvatarUpdate,
}: ProfileSidebarProps) {
  const { message: messageApi } = App.useApp()
  const [uploading, setUploading] = useState(false)
  const [cropModalOpen, setCropModalOpen] = useState(false)
  const [cropImageSrc, setCropImageSrc] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const directionLabel = profile.direction ? DIRECTION_LABELS[profile.direction] : '-'
  const displayName = profile.nickname || profile.username
  const directionIcon = profile.direction ? directionIconMap[profile.direction] : undefined
  const directionTheme = profile.direction ? directionThemeMap[profile.direction] : undefined

  const handleAvatarClick = useCallback(() => {
    if (uploading) return
    fileInputRef.current?.click()
  }, [uploading])

  const handleFileChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    e.target.value = ''

    if (!ALLOWED_TYPES.includes(file.type)) {
      messageApi.error('请选择图片文件（JPG/PNG/GIF/WEBP）')
      return
    }

    if (file.size > MAX_SIZE) {
      messageApi.error('图片大小不能超过 5MB')
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
        const res = await fileService.upload(file, 'AVATAR')
        if (res.code === 200 && res.data) {
          await fileService.updateAvatar(res.data.id)
          messageApi.success('头像更新成功')
          onAvatarUpdate?.()
        } else {
          messageApi.error(res.msg || '头像上传失败，请重试')
        }
      } catch {
        messageApi.error('头像上传失败，请重试')
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

  const handleStatClick = (tab: string) => {
    if (tabCounts && tabCounts[tab as keyof TabCounts] > 0) {
      onTabChange?.(tab)
    }
  }

  const [qrcodeModalOpen, setQrcodeModalOpen] = useState(false)

  return (
    <aside className="w-[340px] shrink-0 max-lg:w-full max-lg:shrink">
      <div className="sticky top-[104px] bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-8 transition-all duration-300 max-lg:relative max-lg:top-auto max-[640px]:p-5">
        <div className="text-center mb-6">
          <div className="relative inline-block mb-5">
            <div
              className={`relative w-[120px] h-[120px] rounded-full bg-[linear-gradient(135deg,#6677ff_0%,#ff6b35_100%)] p-1 overflow-hidden ${allowAvatarUpload ? 'group' : ''} ${uploading ? '' : allowAvatarUpload ? 'cursor-pointer' : ''}`}
              onClick={allowAvatarUpload ? handleAvatarClick : undefined}
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
              {allowAvatarUpload && !uploading && (
                <div className="absolute inset-1 rounded-full bg-black/55 flex flex-col items-center justify-center gap-1 opacity-0 transition-opacity duration-250 pointer-events-none group-hover:opacity-100">
                  <CameraOutlined className="text-[20px] text-white" />
                  <span className="text-[11px] text-white/85 whitespace-nowrap">更换头像</span>
                </div>
              )}
            </div>
            {allowAvatarUpload && (
              <input
                title="点击上传头像"
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/gif,image/webp"
                onChange={handleFileChange}
                className="hidden"
              />
            )}
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white mb-1.5">{displayName}</h1>
            {profile.nickname && (
              <span className="text-sm text-[rgba(140,140,141,1)]">@{profile.username}</span>
            )}
          </div>
          <Tag color={getRoleTagColor(profile.roleName)} className="mt-3">
            {ROLE_LABELS[profile.roleName as keyof typeof ROLE_LABELS] ?? profile.roleName}
          </Tag>
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

        <div className="mb-6 py-6 border-t border-b border-white/[0.05]">
          <div className="text-xs font-semibold text-[rgba(140,140,141,1)] uppercase tracking-[0.5px] mb-4">
            微信二维码
          </div>
          {profile.wechatQrcode ? (
            <div
              className="w-[140px] h-[140px] mx-auto rounded-[10px] bg-white/[0.02] p-2 cursor-pointer transition-all duration-300 hover:bg-[rgba(102,119,255,0.08)]"
              onClick={() => setQrcodeModalOpen(true)}
            >
              <Image
                src={profile.wechatQrcode}
                alt="微信二维码"
                width={140}
                height={140}
                className="w-full h-full object-contain rounded-[6px]"
              />
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center gap-2 py-4 px-3 rounded-[10px] bg-white/[0.02] text-white/40">
              <QrcodeOutlined className="text-[24px]" />
              <span className="text-xs">暂无微信二维码</span>
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

        {tabCounts && (
          <div className="grid grid-cols-3 gap-3 pt-6 border-t border-white/[0.05]">
            {(['projects', 'competitions', 'internships'] as const).map((tab) => {
              const labelMap = {
                projects: '项目经历',
                competitions: '竞赛经历',
                internships: '实习经历',
              }
              return (
                <div
                  key={tab}
                  className={`text-center py-3 px-2 rounded-[10px] bg-white/[0.02] transition-all duration-300 cursor-pointer hover:bg-[#6677ff]/[0.08] ${activeTab === tab ? 'bg-[#6677ff]/[0.1]' : ''}`}
                  onClick={() => handleStatClick(tab)}
                >
                  <div className="text-xl font-bold text-[#6677ff] mb-1">{tabCounts[tab]}</div>
                  <div className="text-xs text-[#8c8c8d]">{labelMap[tab]}</div>
                </div>
              )
            })}
          </div>
        )}
      </div>

      {allowAvatarUpload && (
        <AvatarCropModal
          open={cropModalOpen}
          imageSrc={cropImageSrc}
          onConfirm={handleCropConfirm}
          onCancel={handleCropCancel}
        />
      )}

      <Modal
        open={qrcodeModalOpen}
        footer={null}
        onCancel={() => setQrcodeModalOpen(false)}
        centered
        width={320}
        className="[&_.ant-modal-content]:bg-[#1a1a1a] [&_.ant-modal-content]:border [&_.ant-modal-content]:border-white/10"
      >
        {profile.wechatQrcode && (
          <Image
            src={profile.wechatQrcode}
            alt="微信二维码"
            width={280}
            height={280}
            className="w-full h-auto object-contain"
          />
        )}
      </Modal>
    </aside>
  )
}
