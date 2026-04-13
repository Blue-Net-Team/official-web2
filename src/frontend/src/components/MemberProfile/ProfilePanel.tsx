'use client'

import React from 'react'
import { MemberDetailDTO } from '@/apis/schema/type'
import {
  DIRECTION_LABELS,
  GENDER_LABELS,
  ROLE_LABELS,
  getRoleTagColor,
} from '@/apis/schema/enumerate'
import { Tag } from 'antd'

interface ProfilePanelProps {
  member: MemberDetailDTO
}

export const ProfilePanel: React.FC<ProfilePanelProps> = ({ member }) => {
  const directionLabel = DIRECTION_LABELS[member.direction] || member.direction
  const genderLabel = GENDER_LABELS[member.gender] || '未知'

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
        <div className="flex flex-col gap-1.5">
          <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
            用户名
          </span>
          <span className="text-sm text-white">{member.username}</span>
        </div>
        <div className="flex flex-col gap-1.5">
          <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
            昵称
          </span>
          <span className="text-sm text-white">{member.nickname || member.username}</span>
        </div>
        <div className="flex flex-col gap-1.5">
          <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
            年级
          </span>
          <span className="text-sm text-white">{member.grade || '未知'}</span>
        </div>
        <div className="flex flex-col gap-1.5">
          <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
            学院
          </span>
          <span className="text-sm text-white">{member.college || '未设置'}</span>
        </div>
        <div className="flex flex-col gap-1.5">
          <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
            专业
          </span>
          <span className="text-sm text-white">{member.major || '未设置'}</span>
        </div>
        <div className="flex flex-col gap-1.5">
          <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
            报名方向
          </span>
          <span className="text-sm text-white">{directionLabel}</span>
        </div>
        <div className="flex flex-col gap-1.5">
          <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
            性别
          </span>
          <span className="text-sm text-white">{genderLabel}</span>
        </div>
        <div className="flex flex-col gap-1.5">
          <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
            角色
          </span>
          <span className="text-sm text-white">
            <Tag color={getRoleTagColor(member.role)}>
              {ROLE_LABELS[member.role] || member.role}
            </Tag>
          </span>
        </div>
        {member.bio && (
          <div className="col-span-1 md:col-span-2 flex flex-col gap-1.5">
            <span className="text-xs font-medium text-[#8c8c8d] uppercase tracking-[0.5px]">
              个人简介
            </span>
            <span className="text-sm text-white">{member.bio}</span>
          </div>
        )}
      </div>
    </div>
  )
}
