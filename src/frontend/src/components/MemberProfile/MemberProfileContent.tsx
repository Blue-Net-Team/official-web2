'use client'

import React, { useState, useEffect, useCallback } from 'react'
import { MemberDetailDTO, TabCounts, UserExperience } from '@/apis/schema/type'
import { ProfilePanel } from './ProfilePanel'
import { ExperiencePanel } from './ExperiencePanel'
import { MemberService } from '@/apis/services/member.service'
import styles from './MemberProfile.module.css'
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
        <div className={styles.loadingContainer}>
          <Spin size="large" />
        </div>
      )
    }

    return <ExperiencePanel type={type} title={title} experiences={experiences || []} />
  }

  return (
    <div className={styles.contentArea}>
      <nav className={styles.sectionTabs}>
        {TAB_CONFIG.map((tab) => {
          const Icon = tab.icon
          const count = getCount(tab.key)
          return (
            <button
              key={tab.key}
              className={`${styles.tabBtn} ${activeTab === tab.key ? styles.tabBtnActive : ''}`}
              onClick={() => onTabChange(tab.key)}
            >
              <Icon />
              <span className={styles.tabLabel}>{tab.label}</span>
              {count !== undefined && count > 0 && <span className={styles.tabCount}>{count}</span>}
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
