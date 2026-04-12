'use client'

import React, { useState } from 'react'
import Image from 'next/image'
import { MemberDetailDTO, TabCounts } from '@/apis/schema/type'
import { DIRECTION_LABELS, ROLE_LABELS, getRoleTagColor } from '@/apis/schema/enumerate'
import { API_BASE_URL } from '@/apis/config'
import { Tag } from 'antd'

interface MemberProfileSidebarProps {
  member: MemberDetailDTO
  activeTab: string
  onTabChange: (tab: string) => void
  tabCounts: TabCounts
}

const DIRECTION_ABBR: Record<string, string> = {
  COMPUTER_VISION: 'CV',
  STRUCTURAL_DESIGN: 'SD',
  EMBEDDED: 'EM',
}

const GRADE_LABELS: Record<number, string> = {
  1: '大一',
  2: '大二',
  3: '大三',
  4: '大四',
  5: '研一',
  6: '研二',
}

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
    <aside className="w-[340px] shrink-0 max-[1024px]:w-full max-[1024px]:shrink">
      <div className="sticky top-[104px] bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-8 transition-all duration-300 max-[1024px]:relative max-[1024px]:top-auto max-[480px]:p-4">
        <div className="text-center mb-6">
          <div className="relative inline-block mb-5">
            <div className="w-[120px] h-[120px] rounded-full bg-gradient-to-br from-[#6677ff] to-[#ff6b35] p-1 max-[480px]:w-[100px] max-[480px]:h-[100px]">
              <div className="w-[112px] h-[112px] rounded-full bg-gradient-to-br from-[#1a1a1a] to-[#2a2a2a] flex items-center justify-center text-[48px] font-bold text-white overflow-hidden relative max-[480px]:w-[92px] max-[480px]:h-[92px] max-[480px]:text-[36px]">
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
            <h1 className="text-2xl font-bold text-white mb-1.5 max-[480px]:text-xl">
              {displayName}
            </h1>
            {member.nickname && <span className="text-sm text-[#8c8c8d]">@{member.username}</span>}
          </div>
          <Tag
            color={getRoleTagColor(member.role)}
            className="mt-3"
          >
            {ROLE_LABELS[member.role] || member.role}
          </Tag>
        </div>

        {member.bio && (
          <div className="mb-6 pb-6 border-b border-white/[0.05]">
            <p className="text-sm leading-[1.7] text-white/70 text-center">{member.bio}</p>
          </div>
        )}

        <div className="mb-6">
          {member.college && (
            <div className="flex items-center gap-3 py-3 text-sm text-white/70 border-b border-white/[0.03]">
              <svg
                className="w-[18px] h-[18px] text-[#6677ff] shrink-0"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
              >
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2" />
                <line x1="8" y1="21" x2="16" y2="21" />
                <line x1="12" y1="17" x2="12" y2="21" />
              </svg>
              <span>{member.college}</span>
            </div>
          )}
          {member.major && (
            <div className="flex items-center gap-3 py-3 text-sm text-white/70 border-b border-white/[0.03]">
              <svg
                className="w-[18px] h-[18px] text-[#6677ff] shrink-0"
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
          )}
          <div className="flex items-center gap-3 py-3 text-sm text-white/70">
            <svg
              className="w-[18px] h-[18px] text-[#6677ff] shrink-0"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
              <line x1="16" y1="2" x2="16" y2="6" />
              <line x1="8" y1="2" x2="8" y2="6" />
              <line x1="3" y1="10" x2="21" y2="10" />
            </svg>
            <span>{gradeLabel}</span>
          </div>
        </div>

        {member.direction && (
          <div className="mb-6 py-6 border-t border-b border-white/[0.05]">
            <div className="text-xs font-semibold text-[#8c8c8d] uppercase tracking-[0.5px] mb-4">
              报名方向
            </div>
            <div className="flex items-center gap-3 p-3 rounded-[10px] bg-white/[0.02] mb-2.5 transition-all duration-300 hover:bg-[#6677ff]/[0.08]">
              <div className="w-10 h-10 rounded-[10px] bg-gradient-to-br from-[#6677ff] to-[#2f27b0] flex items-center justify-center text-sm font-semibold text-white">
                {directionAbbr}
              </div>
              <div className="flex-1">
                <div className="text-sm font-medium text-white">{directionLabel}</div>
              </div>
            </div>
          </div>
        )}

        <div className="grid grid-cols-3 gap-3 pt-6 border-t border-white/[0.05]">
          <div
            className={`text-center py-3 px-2 rounded-[10px] bg-white/[0.02] transition-all duration-300 cursor-pointer hover:bg-[#6677ff]/[0.08] ${activeTab === 'projects' ? 'bg-[#6677ff]/[0.1]' : ''}`}
            onClick={() => handleStatClick('projects')}
          >
            <div className="text-xl font-bold text-[#6677ff] mb-1">{tabCounts.projects}</div>
            <div className="text-xs text-[#8c8c8d]">项目经历</div>
          </div>
          <div
            className={`text-center py-3 px-2 rounded-[10px] bg-white/[0.02] transition-all duration-300 cursor-pointer hover:bg-[#6677ff]/[0.08] ${activeTab === 'competitions' ? 'bg-[#6677ff]/[0.1]' : ''}`}
            onClick={() => handleStatClick('competitions')}
          >
            <div className="text-xl font-bold text-[#6677ff] mb-1">{tabCounts.competitions}</div>
            <div className="text-xs text-[#8c8c8d]">竞赛经历</div>
          </div>
          <div
            className={`text-center py-3 px-2 rounded-[10px] bg-white/[0.02] transition-all duration-300 cursor-pointer hover:bg-[#6677ff]/[0.08] ${activeTab === 'internships' ? 'bg-[#6677ff]/[0.1]' : ''}`}
            onClick={() => handleStatClick('internships')}
          >
            <div className="text-xl font-bold text-[#6677ff] mb-1">{tabCounts.internships}</div>
            <div className="text-xs text-[#8c8c8d]">实习经历</div>
          </div>
        </div>
      </div>
    </aside>
  )
}
