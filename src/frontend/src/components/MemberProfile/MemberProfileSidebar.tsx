'use client'

import React, { useState } from 'react'
import Image from 'next/image'
import { MemberDetailDTO, TabCounts } from '@/apis/schema/type'
import { DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { API_BASE_URL } from '@/apis/config'
import styles from './MemberProfile.module.css'

interface MemberProfileSidebarProps {
  member: MemberDetailDTO
  activeTab: string
  onTabChange: (tab: string) => void
  tabCounts: TabCounts
}

// 方向缩写映射
const DIRECTION_ABBR: Record<string, string> = {
  COMPUTER_VISION: 'CV',
  STRUCTURAL_DESIGN: 'SD',
  EMBEDDED: 'EM',
}

// 角色样式映射
const getRoleBadgeClass = (roleName: string): string => {
  switch (roleName) {
    case 'CANDIDATE':
      return styles.roleBadgeCandidate
    case 'MEMBER':
      return styles.roleBadgeMember
    default:
      return styles.roleBadgeAdmin
  }
}

// 角色标签映射
const ROLE_LABELS: Record<string, string> = {
  CANDIDATE: '考生',
  MEMBER: '成员',
  SUPER_ADMIN: '超级管理员',
  DIRECTION_ADMIN: '方向管理员',
}

// 年级映射
const GRADE_LABELS: Record<number, string> = {
  1: '大一',
  2: '大二',
  3: '大三',
  4: '大四',
  5: '研一',
  6: '研二',
}

// 计算年级
const calculateGrade = (enrollmentYear: number): string => {
  const currentYear = new Date().getFullYear()
  const grade = currentYear - enrollmentYear + 1
  return GRADE_LABELS[grade] || `${enrollmentYear}级`
}

export const MemberProfileSidebar: React.FC<MemberProfileSidebarProps> = ({
  member,
  activeTab,
  onTabChange,
  tabCounts,
}) => {
  const [avatarError, setAvatarError] = useState(false)

  const avatarImageUrl = member.avatarFileId
    ? `${API_BASE_URL}/file/download/${member.avatarFileId}`
    : null

  const displayName = member.nickname || member.username
  const directionLabel = DIRECTION_LABELS[member.direction] || member.direction
  const directionAbbr =
    DIRECTION_ABBR[member.direction] || member.direction.slice(0, 2).toUpperCase()
  const gradeLabel = calculateGrade(member.enrollmentYear)

  const handleStatClick = (tab: string) => {
    if (tabCounts[tab as keyof typeof tabCounts] > 0) {
      onTabChange(tab)
    }
  }

  return (
    <aside className={styles.sidebar}>
      <div className={styles.sidebarContent}>
        {/* 头像区域 */}
        <div className={styles.avatarSection}>
          <div className={styles.avatarContainer}>
            <div className={styles.avatarRing}>
              <div className={styles.avatarImg}>
                {avatarImageUrl && !avatarError ? (
                  <Image
                    src={avatarImageUrl}
                    alt={displayName}
                    fill
                    style={{ objectFit: 'cover' }}
                    onError={() => setAvatarError(true)}
                  />
                ) : (
                  displayName.charAt(0)
                )}
              </div>
            </div>
          </div>
          <div>
            <h1 className={styles.memberName}>{displayName}</h1>
            {member.nickname && <span className={styles.memberNickname}>@{member.username}</span>}
          </div>
          <span className={`${styles.roleBadge} ${getRoleBadgeClass(member.role)}`}>
            {ROLE_LABELS[member.role] || member.role}
          </span>
        </div>

        {/* 个人简介 */}
        {member.bio && (
          <div className={styles.bioSection}>
            <p className={styles.bioText}>{member.bio}</p>
          </div>
        )}

        {/* 基本信息列表 */}
        <div className={styles.infoList}>
          {member.college && (
            <div className={styles.infoItem}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2" />
                <line x1="8" y1="21" x2="16" y2="21" />
                <line x1="12" y1="17" x2="12" y2="21" />
              </svg>
              <span>{member.college}</span>
            </div>
          )}
          {member.major && (
            <div className={styles.infoItem}>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
              </svg>
              <span>{member.major}</span>
            </div>
          )}
          <div className={styles.infoItem}>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
            <span>{gradeLabel}</span>
          </div>
        </div>

        {/* 报名方向 */}
        {member.direction && (
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

        {/* 统计数据 */}
        <div className={styles.statsSection}>
          <div
            className={`${styles.statBox} ${activeTab === 'projects' ? styles.statBoxActive : ''}`}
            onClick={() => handleStatClick('projects')}
          >
            <div className={styles.statNumber}>{tabCounts.projects}</div>
            <div className={styles.statLabel}>项目经历</div>
          </div>
          <div
            className={`${styles.statBox} ${activeTab === 'competitions' ? styles.statBoxActive : ''}`}
            onClick={() => handleStatClick('competitions')}
          >
            <div className={styles.statNumber}>{tabCounts.competitions}</div>
            <div className={styles.statLabel}>竞赛经历</div>
          </div>
          <div
            className={`${styles.statBox} ${activeTab === 'internships' ? styles.statBoxActive : ''}`}
            onClick={() => handleStatClick('internships')}
          >
            <div className={styles.statNumber}>{tabCounts.internships}</div>
            <div className={styles.statLabel}>实习经历</div>
          </div>
        </div>
      </div>
    </aside>
  )
}
