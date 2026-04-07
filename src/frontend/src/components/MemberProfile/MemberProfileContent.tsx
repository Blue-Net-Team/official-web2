'use client'

import React, { useState, useEffect, useCallback } from 'react'
import { MemberDetailDTO, TabCounts, UserExperience } from '@/apis/schema/type'
import { ProfilePanel } from './ProfilePanel'
import { ExperiencePanel } from './ExperiencePanel'
import { MemberService } from '@/apis/services/member.service'
import { UserOutlined, FolderOutlined, TrophyOutlined, SolutionOutlined } from '@ant-design/icons'
import { Spin } from 'antd'

interface MemberProfileContentProps {
  member: MemberDetailDTO
  activeTab: string
  onTabChange: (tab: string) => void
  tabCounts: TabCounts
}

const TAB_CONFIG = [
  { key: 'profile', label: '个人信息', icon: UserOutlined },
  { key: 'projects', label: '项目经历', icon: FolderOutlined },
  { key: 'competitions', label: '竞赛经历', icon: TrophyOutlined },
  { key: 'internships', label: '实习经历', icon: SolutionOutlined },
]

type ExperienceCache = {
  projects: UserExperience[] | null
  competitions: UserExperience[] | null
  internships: UserExperience[] | null
}

type LoadingState = {
  projects: boolean
  competitions: boolean
  internships: boolean
}

export const MemberProfileContent: React.FC<MemberProfileContentProps> = ({
  member,
  activeTab,
  onTabChange,
  tabCounts,
}) => {
  const [experienceCache, setExperienceCache] = useState<ExperienceCache>({
    projects: null,
    competitions: null,
    internships: null,
  })

  const [loading, setLoading] = useState<LoadingState>({
    projects: false,
    competitions: false,
    internships: false,
  })

  const fetchExperience = useCallback(
    async (type: 'projects' | 'competitions' | 'internships') => {
      if (experienceCache[type] !== null || loading[type]) {
        return
      }

      setLoading((prev) => ({ ...prev, [type]: true }))

      try {
        let data: UserExperience[]
        switch (type) {
          case 'projects':
            data = await MemberService.getMemberProjects(member.id)
            break
          case 'competitions':
            data = await MemberService.getMemberCompetitions(member.id)
            break
          case 'internships':
            data = await MemberService.getMemberInternships(member.id)
            break
        }
        setExperienceCache((prev) => ({ ...prev, [type]: data }))
      } catch (error) {
        console.error(`Failed to fetch ${type}:`, error)
        setExperienceCache((prev) => ({ ...prev, [type]: [] }))
      } finally {
        setLoading((prev) => ({ ...prev, [type]: false }))
      }
    },
    [member.id, experienceCache, loading]
  )

  useEffect(() => {
    if (activeTab === 'projects' || activeTab === 'competitions' || activeTab === 'internships') {
      fetchExperience(activeTab)
    }
  }, [activeTab, fetchExperience])

  const getCount = (key: string): number | undefined => {
    if (key === 'profile') return undefined
    return tabCounts[key as keyof typeof tabCounts]
  }

  const renderExperiencePanel = (type: 'project' | 'competition' | 'internship', title: string) => {
    const cacheKey = `${type}s` as keyof ExperienceCache
    const isLoading = loading[cacheKey]
    const experiences = experienceCache[cacheKey]

    if (isLoading) {
      return (
        <div className="flex items-center justify-center min-h-[400px]">
          <Spin size="large" />
        </div>
      )
    }

    return <ExperiencePanel type={type} title={title} experiences={experiences || []} />
  }

  return (
    <div className="flex-1 min-w-0">
      <nav className="flex gap-2 mb-6 p-1 bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-xl max-[1024px]:flex-wrap">
        {TAB_CONFIG.map((tab) => {
          const Icon = tab.icon
          const count = getCount(tab.key)
          return (
            <button
              key={tab.key}
              className={`flex-1 py-3 px-4 border-none bg-transparent text-sm font-medium cursor-pointer rounded-lg transition-all duration-300 flex items-center justify-center gap-1.5 no-underline max-[1024px]:flex-[1_1_calc(33.333%-6px)] max-md:flex-[1_1_calc(50%-4px)] max-md:py-2.5 max-md:px-2 max-md:text-xs max-[480px]:flex-[1_1_100%] [&_svg]:w-[18px] [&_svg]:h-[18px] [&_svg]:shrink-0 ${
                activeTab === tab.key
                  ? 'bg-gradient-to-br from-[#6677ff] to-[#2f27b0] text-white shadow-[0_4px_16px_rgba(102,119,255,0.3)]'
                  : 'text-[#8c8c8d] hover:text-white hover:bg-white/[0.05]'
              }`}
              onClick={() => onTabChange(tab.key)}
            >
              <Icon />
              <span className="inline">{tab.label}</span>
              {count !== undefined && count > 0 && (
                <span className="text-xs opacity-80 bg-white/20 px-2 py-0.5 rounded-[10px]">
                  {count}
                </span>
              )}
            </button>
          )
        })}
      </nav>

      {activeTab === 'profile' && <ProfilePanel member={member} />}
      {activeTab === 'projects' && renderExperiencePanel('project', '项目经历')}
      {activeTab === 'competitions' && renderExperiencePanel('competition', '竞赛经历')}
      {activeTab === 'internships' && renderExperiencePanel('internship', '实习经历')}
    </div>
  )
}
