'use client'

import React from 'react'
import {
  DIRECTION_LABELS,
  GENDER_LABELS,
  ROLE_LABELS,
  getRoleTagColor,
} from '@/apis/schema/enumerate'
import { Tag } from 'antd'

/**
 * 统一的只读个人信息展示数据接口
 * 适配 UserInfo（个人主页）和 MemberDetailDTO（成员详情页）
 */
export interface ProfileDisplayData {
  username: string
  nickname: string | null
  grade: string | null
  college: string | null
  major: string | null
  direction: string | null
  gender: string | null
  roleName: string | null
  bio: string | null
}

interface ProfileInfoDisplayProps {
  profile: ProfileDisplayData
}

export const ProfileInfoDisplay: React.FC<ProfileInfoDisplayProps> = ({ profile }) => {
  const directionLabel = profile.direction
    ? (DIRECTION_LABELS as Record<string, string>)[profile.direction] || profile.direction
    : '未知'
  const genderLabel = profile.gender
    ? (GENDER_LABELS as Record<string, string>)[profile.gender] || '未知'
    : '未知'

  const fields = [
    { label: '用户名', value: profile.username },
    { label: '昵称', value: profile.nickname || profile.username },
    { label: '年级', value: profile.grade || '未知' },
    { label: '学院', value: profile.college || '未设置' },
    { label: '专业', value: profile.major || '未设置' },
    { label: '报名方向', value: directionLabel },
    { label: '性别', value: genderLabel },
  ]

  return (
    <div className="bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-8 animate-[fadeIn_0.3s_ease] max-[480px]:p-4">
      <div className="text-lg font-semibold text-white mb-6 flex items-center gap-2.5 [&_svg]:w-5 [&_svg]:h-5 [&_svg]:text-[#6677ff]">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
        <h2>基本信息</h2>
      </div>

      <div className="grid grid-cols-2 gap-5 max-md:grid-cols-1">
        {fields.map(({ label, value }) => (
          <div key={label} className="flex flex-col gap-1.5">
            <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
              {label}
            </span>
            <span className="text-sm text-white">{value}</span>
          </div>
        ))}
        <div className="flex flex-col gap-1.5">
          <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
            角色
          </span>
          <span className="text-sm text-white">
            {profile.roleName ? (
              <Tag color={getRoleTagColor(profile.roleName)}>
                {ROLE_LABELS[profile.roleName] || profile.roleName}
              </Tag>
            ) : (
              '-'
            )}
          </span>
        </div>
        {profile.bio && (
          <div className="col-span-1 md:col-span-2 flex flex-col gap-1.5">
            <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
              个人简介
            </span>
            <span className="text-sm text-white">{profile.bio}</span>
          </div>
        )}
      </div>
    </div>
  )
}

export default ProfileInfoDisplay
