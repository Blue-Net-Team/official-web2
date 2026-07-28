'use client'

import React from 'react'
import { UserExperience } from '@/apis/schema/type'
import type { InternshipStatus } from '@/apis/schema/enumerate'
import { FolderOutlined, SolutionOutlined, LinkOutlined } from '@ant-design/icons'

interface ExperienceCardProps {
  experience: UserExperience
  actions?: React.ReactNode
}

const ICON_CLASS_MAP: Record<string, string> = {
  PROJECT: 'bg-gradient-to-br from-[#6677ff] to-[#2f27b0]',
  INTERNSHIP: 'bg-gradient-to-br from-[#10b981] to-[#059669]',
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
  const isInternship = experience.type === 'INTERNSHIP'

  const displayName = isInternship ? experience.company || experience.name : experience.name
  const displayRole = isInternship ? experience.position : experience.role
  const displayDate = experience.startDate
    ? `${experience.startDate} - ${experience.endDate || '至今'}`
    : ''

  const getIcon = () => {
    switch (experience.type) {
      case 'PROJECT':
        return <FolderOutlined />
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
          {displayRole && <div className="text-[13px] text-[#6677ff]">{displayRole}</div>}
        </div>
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
        </div>
        {actions}
      </div>
    </div>
  )
}
