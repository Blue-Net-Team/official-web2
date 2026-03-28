'use client'

import React, { useState } from 'react'
import Image from 'next/image'
import Link from 'next/link'
import { MemberBriefDTO } from '@/apis/schema/type'
import { Role } from '@/apis/schema/enumerate'
import styles from './MemberCard.module.css'
import { DIRECTION_CONFIG, ROLE_CONFIG, GENDER_CONFIG } from './MemberCard.config'
import { API_BASE_URL } from '@/apis/config'
import { UserOutlined } from '@ant-design/icons'
import AdminIcon from '@/assets/icon/admin.svg'

interface MemberCardProps {
  member: MemberBriefDTO
  index: number
}

export const MemberCard: React.FC<MemberCardProps> = ({ member, index }) => {
  const [avatarError, setAvatarError] = useState(false)
  const direction = DIRECTION_CONFIG[member.direction]
  const role = ROLE_CONFIG[member.roleName as Role]
  const gender = GENDER_CONFIG[member.gender]
  const avatarImageUrl = member.avatarFileId
    ? `${API_BASE_URL}/file/download/${member.avatarFileId}`
    : null

  return (
    <Link href={`/members/${member.id}`} className={styles.memberCardLink}>
      <div className={styles.memberCard} style={{ animationDelay: `${index * 0.05}s` }}>
        <div className={styles.cardHeader}>
          <div className={styles.memberAvatar}>
            <div className={styles.avatarRing} />
            <div className={styles.avatarImg}>
              {avatarImageUrl && !avatarError ? (
                <Image
                  src={avatarImageUrl}
                  alt={member.nickname}
                  fill
                  style={{ objectFit: 'cover' }}
                  onError={() => setAvatarError(true)}
                />
              ) : (
                <UserOutlined />
              )}
            </div>
          </div>
          <div className={styles.memberBasicInfo}>
            <div className={styles.memberNameRow}>
              <h3 className={styles.memberName}>{member.username}</h3>
              {gender.icon && (
                <div className={styles.genderIcon}>
                  <Image src={gender.icon} alt={gender.label} width={16} height={16} />
                </div>
              )}
            </div>
            <span className={styles.memberNickname}>{member.nickname}</span>
          </div>
        </div>

        <div className={styles.tagRow}>
          <div className={`${styles.directionTag} ${direction.className}`}>
            <Image src={direction.iconImg} alt={direction.label} width={14} height={14} />
            {direction.label}
          </div>
          {member.roleName !== 'MEMBER' && member.roleName !== 'CANDIDATE' && (
            <span className={`${styles.roleBadge} ${role.className}`}>
              <Image src={AdminIcon} alt="role" width={14} height={14} />
              {role.label}
            </span>
          )}
        </div>

        <div className={styles.memberStats}>
          <div className={styles.statItem}>
            <svg
              className={styles.statIcon}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M22 10v6M2 10l10-5 10 5-10 5z" />
              <path d="M6 12v5c3 3 9 3 12 0v-5" />
            </svg>
            <span>{member.enrollmentYear}级</span>
          </div>
          <div className={styles.statItem}>
            <svg
              className={styles.statIcon}
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
              <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
            </svg>
            <span>{member.major}</span>
          </div>
        </div>
      </div>
    </Link>
  )
}
