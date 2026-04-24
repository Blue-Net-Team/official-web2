'use client'

import React from 'react'
import { UserExperience } from '@/apis/schema/type'
import type { InternshipStatus } from '@/apis/schema/enumerate'
import {
  FolderOutlined,
  TrophyOutlined,
  SolutionOutlined,
  LinkOutlined,
  FileTextOutlined,
} from '@ant-design/icons'

interface ExperienceCardProps {
  experience: UserExperience
  actions?: React.ReactNode
}

const ICON_CLASS_MAP: Record<string, string> = {
  PROJECT: 'bg-gradient-to-br from-[#6677ff] to-[#2f27b0]',
  COMPETITION: 'bg-gradient-to-br from-[#ff6b35] to-[#ff8c42]',
  INTERNSHIP: 'bg-gradient-to-br from-[#10b981] to-[#059669]',
}

function getAwardBadgeClass(award: string): string {
  switch (award) {
    case '一等奖':
    case 'first':
      return 'bg-[linear-gradient(135deg,#ffd700_0%,#ffa500_100%)] text-black'
    case '二等奖':
    case 'second':
      return 'bg-[linear-gradient(135deg,#c0c0c0_0%,#a0a0a0_100%)] text-black'
    case '三等奖':
    case 'third':
    case '铜牌':
      return 'bg-[linear-gradient(135deg,#cd7f32_0%,#b87333_100%)] text-white'
    default:
      return ''
  }
}

function getInternshipBadgeClass(status: InternshipStatus): string {
  return status === 'ACTIVE'
    ? 'bg-[rgba(102,119,255,0.15)] text-[#6677ff]'
    : 'bg-[rgba(140,140,141,0.2)] text-[rgba(140,140,141,1)]'
}

function getInternshipStatusText(status: InternshipStatus): string {
  return status === 'ACTIVE' ? '在职' : '已离职'
}

export default function ExperienceCard({ experience, actions }: ExperienceCardProps) {
  const isProject = experience.type === 'PROJECT'
  const isCompetition = experience.type === 'COMPETITION'
  const isInternship = experience.type === 'INTERNSHIP'

  const displayName = isInternship ? experience.company || experience.name : experience.name
  const displayRole = isInternship ? experience.position : experience.role
  const displayDate = experience.startDate
    ? `${experience.startDate} - ${experience.endDate || '至今'}`
    : experience.date || ''

  const getIcon = () => {
    switch (experience.type) {
      case 'PROJECT':
        return <FolderOutlined />
      case 'COMPETITION':
        return <TrophyOutlined />
      case 'INTERNSHIP':
        return <SolutionOutlined />
    }
  }

  return (
    <div className="bg-white/[0.02] border border-white/[0.05] rounded-xl p-5 transition-all duration-300 hover:bg-white/[0.04] hover:border-[#6677ff]/20 hover:-translate-y-0.5">
      <div className="flex items-start gap-3 mb-3 max-md:flex-wrap">
        <div
          className={`w-11 h-11 rounded-[10px] flex items-center justify-center shrink-0 [&_svg]:w-[22px] [&_svg]:h-[22px] [&_svg]:text-white ${ICON_CLASS_MAP[experience.type] || ''}`}
        >
          {getIcon()}
        </div>
        <div className="flex-1 min-w-0">
          <div className="text-base font-semibold text-white mb-1">{displayName}</div>
          {displayRole && (
            <div className="text-[13px] text-[#6677ff]">
              {displayRole}
              {isCompetition && experience.award && ` · ${experience.award}`}
            </div>
          )}
        </div>
        {isCompetition && experience.award && (
          <div
            className={`px-3.5 py-1.5 rounded-[20px] text-xs font-semibold whitespace-nowrap shrink-0 ${getAwardBadgeClass(experience.award)}`}
          >
            {experience.award}
          </div>
        )}
        {isInternship && experience.status && (
          <div
            className={`px-3.5 py-1.5 rounded-[20px] text-xs font-semibold whitespace-nowrap shrink-0 max-[640px]:ml-auto ${getInternshipBadgeClass(experience.status)}`}
          >
            {getInternshipStatusText(experience.status)}
          </div>
        )}
        <div className="text-[13px] text-[#8c8c8d] whitespace-nowrap max-md:w-full max-md:mt-1">
          {displayDate}
        </div>
      </div>

      {isCompetition && (experience.date || experience.teamSize) && (
        <div className="flex gap-4 mb-3 text-[13px] text-[#8c8c8d]">
          {experience.date && (
            <div className="flex items-center gap-1.5">
              <FileTextOutlined className="text-sm" />
              <span>{experience.date}</span>
            </div>
          )}
          {experience.teamSize && (
            <div className="flex items-center gap-1.5">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                className="w-[14px] h-[14px]"
              >
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
              <span>{experience.teamSize}人团队</span>
            </div>
          )}
        </div>
      )}

      {experience.description && (
        <p className="text-sm leading-[1.6] text-white/70 mb-3">{experience.description}</p>
      )}

      {isProject && experience.techStack && experience.techStack.length > 0 && (
        <div className="flex flex-wrap gap-2 mb-4">
          {experience.techStack.map((tech, index) => (
            <span
              key={index}
              className="px-2.5 py-1 rounded-md text-xs bg-[#6677ff]/10 text-[#6677ff] border border-[#6677ff]/20"
            >
              {tech}
            </span>
          ))}
        </div>
      )}

      <div className="flex items-center justify-between pt-4 border-t border-white/[0.05] max-md:flex-col max-md:gap-4 max-md:items-start">
        <div className="flex gap-4">
          {isProject && experience.demoUrl && (
            <a
              href={experience.demoUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-1.5 text-[13px] text-[#6677ff] no-underline transition-all duration-300 hover:text-[#8895ff] [&_svg]:w-4 [&_svg]:h-4"
            >
              <LinkOutlined />
              项目演示
            </a>
          )}
          {isCompetition && experience.certificateUrl && (
            <a
              href={experience.certificateUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center gap-1.5 text-[13px] text-[#6677ff] no-underline transition-all duration-300 hover:text-[#8895ff] [&_svg]:w-4 [&_svg]:h-4"
            >
              <FileTextOutlined />
              获奖证书
            </a>
          )}
        </div>
        {actions}
      </div>
    </div>
  )
}
