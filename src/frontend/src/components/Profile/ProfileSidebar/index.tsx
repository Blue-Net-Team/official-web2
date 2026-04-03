'use client'

import { useState, useRef, useCallback } from 'react'
import type { UserInfo, UserStats } from '@/types/profile'
import { DirectionLabels } from '@/types/profile'
import { API_BASE_URL } from '@/apis/config'
import { fileService } from '@/apis/services/file.service'
import styles from './styles.module.css'
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

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
const MAX_SIZE = 5 * 1024 * 1024

interface ProfileSidebarProps {
  profile: UserInfo
  stats: UserStats
  onAvatarUpdate?: () => void
}

const directionAbbrMap: Record<string, string> = {
  computer_vision: 'CV',
  embedded: 'EM',
  structural_design: 'SD',
}

export default function ProfileSidebar({ profile, stats, onAvatarUpdate }: ProfileSidebarProps) {
  const [uploading, setUploading] = useState(false)
  const [cropModalOpen, setCropModalOpen] = useState(false)
  const [cropImageSrc, setCropImageSrc] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const directionAbbr =
    directionAbbrMap[profile.direction] ||
    (profile.direction ? profile.direction.slice(0, 2).toUpperCase() : '-')
  const directionLabel = DirectionLabels[profile.direction] || profile.direction || '-'
  const displayName = profile.nickname || profile.username

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
    <aside className={styles.sidebar}>
      <div className={styles.sidebarContent}>
        <div className={styles.avatarSection}>
          <div className={styles.avatarContainer}>
            <div
              className={`${styles.avatarRing} ${uploading ? '' : styles.avatarRingClickable}`}
              onClick={handleAvatarClick}
            >
              <div className={styles.avatarImg}>
                {uploading ? (
                  <LoadingOutlined className={styles.avatarLoading} />
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
                <div className={styles.avatarOverlay}>
                  <CameraOutlined className={styles.avatarOverlayIcon} />
                  <span className={styles.avatarOverlayText}>更换头像</span>
                </div>
              )}
            </div>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/gif,image/webp"
              onChange={handleFileChange}
              className={styles.hiddenInput}
            />
          </div>
          <div>
            <h1 className={styles.memberName}>{displayName}</h1>
            {profile.nickname && <span className={styles.memberNickname}>@{profile.username}</span>}
          </div>
          <span
            className={`${styles.roleBadge} ${
              profile.roleName === 'candidate' ? styles.roleBadgeCandidate : styles.roleBadgeMember
            }`}
          >
            {profile.roleName === 'candidate' ? '考生' : '成员'}
          </span>
        </div>

        {profile.bio && (
          <div className={styles.bioSection}>
            <p className={styles.bioText}>{profile.bio}</p>
          </div>
        )}

        <div className={styles.infoList}>
          {profile.college && (
            <div className={styles.infoItem}>
              <DesktopOutlined />
              <span>{profile.college}</span>
            </div>
          )}
          {profile.major && (
            <div className={styles.infoItem}>
              <BookOutlined />
              <span>{profile.major}</span>
            </div>
          )}
          {profile.grade && (
            <div className={styles.infoItem}>
              <CalendarOutlined />
              <span>{profile.grade}</span>
            </div>
          )}
        </div>

        {profile.direction && (
          <div className={styles.directionSection}>
            <div className={styles.sectionLabel}>报名方向</div>
            <div className={styles.directionItem}>
              <div className={styles.directionIcon}>{directionAbbr}</div>
              <div className={styles.directionInfo}>
                <div className={styles.directionName}>{directionLabel}</div>
              </div>
            </div>
          </div>
        )}

        <div className={styles.statsSection}>
          <div className={styles.statBox}>
            <div className={styles.statNumber}>{stats.assessmentCount}</div>
            <div className={styles.statLabel}>考核轮次</div>
          </div>
          <div className={styles.statBox}>
            <div className={styles.statNumber}>{stats.completedCount}</div>
            <div className={styles.statLabel}>已完成</div>
          </div>
          <div className={styles.statBox}>
            <div className={styles.statNumber}>{stats.averageScore}</div>
            <div className={styles.statLabel}>平均分</div>
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
