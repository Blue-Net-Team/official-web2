'use client'

import React, { useState } from 'react'
import Image from 'next/image'
import Link from 'next/link'
import { MemberBriefDTO } from '@/apis/schema/type'
import { Role } from '@/apis/schema/enumerate'
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
    <Link href={`/members/${member.id}`} className="no-underline text-inherit block">
      <div
        className="relative overflow-hidden rounded-[20px] border border-white/[0.08] bg-white/[0.03] p-7 cursor-pointer transition-all duration-400 ease-[cubic-bezier(0.4,0,0.2,1)] max-md:p-5 before:content-[''] before:absolute before:top-0 before:left-0 before:right-0 before:h-[3px] before:bg-gradient-to-r before:from-[#6677ff] before:to-[#ff6b35] before:opacity-0 before:transition-opacity before:duration-300 hover:bg-white/[0.06] hover:border-white/[0.15] hover:-translate-y-2 hover:shadow-[0_20px_40px_rgba(0,0,0,0.4),0_0_60px_rgba(102,119,255,0.1)] hover:before:opacity-100 animate-[fadeInUp_0.5s_ease_forwards] opacity-0"
        style={{ animationDelay: `${index * 0.05}s` }}
      >
        <div className="flex items-center gap-4 mb-5">
          <div className="w-[72px] h-[72px] rounded-full relative shrink-0 max-md:w-[60px] max-md:h-[60px]">
            <div className="absolute -inset-[3px] rounded-full bg-gradient-to-br from-[#6677ff] via-[#ff6b35] to-[#2f27b0] p-[3px]" />
            <div className="w-full h-full rounded-full bg-gradient-to-br from-[#1a1a2e] to-[#16213e] flex items-center justify-center text-[28px] font-semibold text-white relative z-1 overflow-hidden max-md:text-2xl">
              {avatarImageUrl && !avatarError ? (
                <Image
                  src={avatarImageUrl}
                  alt={member.nickname}
                  fill
                  className="object-cover"
                  onError={() => setAvatarError(true)}
                />
              ) : (
                <UserOutlined />
              )}
            </div>
          </div>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-1.5 mb-1">
              <h3 className="text-xl font-semibold text-white whitespace-nowrap overflow-hidden text-ellipsis max-md:text-lg">
                {member.username}
              </h3>
              {gender.icon && (
                <div className="flex items-center justify-center shrink-0">
                  <Image src={gender.icon} alt={gender.label} width={16} height={16} />
                </div>
              )}
            </div>
            <span className="text-[13px] text-white/40">{member.nickname}</span>
          </div>
        </div>

        <div className="flex items-center justify-start mb-4 gap-2">
          <div
            className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium ${direction.className}`}
          >
            <Image src={direction.iconImg} alt={direction.label} width={14} height={14} />
            {direction.label}
          </div>
          {member.roleName !== 'MEMBER' && member.roleName !== 'CANDIDATE' && (
            <span
              className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium ${role.className}`}
            >
              <Image src={AdminIcon} alt="role" width={14} height={14} />
              {role.label}
            </span>
          )}
        </div>

        <div className="flex gap-4 pt-4 border-t border-white/[0.06]">
          <div className="flex items-center gap-1.5 text-[13px] text-white/50">
            <svg
              className="w-4 h-4 opacity-60"
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
          <div className="flex items-center gap-1.5 text-[13px] text-white/50">
            <svg
              className="w-4 h-4 opacity-60"
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
