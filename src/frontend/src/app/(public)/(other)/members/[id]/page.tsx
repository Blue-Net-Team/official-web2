'use client'

import { useState, useEffect, useCallback } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { memberService } from '@/apis/services/member.service'
import { MemberDetailDTO, TabCounts, UserExperience } from '@/apis/schema/type'
import type { ExperienceType } from '@/apis/schema/enumerate'
import {
  ProfileSidebar,
  ProfileInfoDisplay,
  ProfileTabs,
  ExperienceSection,
} from '@/components/Profile'
import type { SidebarProfile } from '@/components/Profile/ProfileSidebar'
import type { ProfileDisplayData } from '@/components/Profile/ProfileInfoDisplay'
import { UserOutlined, FolderOutlined, TrophyOutlined, SolutionOutlined } from '@ant-design/icons'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { Spin } from 'antd'

/** 将 MemberDetailDTO 适配为 SidebarProfile */
function adaptToSidebarProfile(member: MemberDetailDTO): SidebarProfile {
  return {
    username: member.username,
    nickname: member.nickname,
    college: member.college,
    major: member.major,
    grade: member.grade,
    bio: member.bio || '',
    avatarFileId: member.avatarFileId,
    roleName: member.role,
    direction: member.direction,
  }
}

/** 将 MemberDetailDTO 适配为 ProfileDisplayData */
function adaptToDisplayData(member: MemberDetailDTO): ProfileDisplayData {
  return {
    username: member.username,
    nickname: member.nickname,
    grade: member.grade,
    college: member.college,
    major: member.major,
    direction: member.direction,
    gender: member.gender,
    roleName: member.role,
    bio: member.bio,
  }
}

const TAB_CONFIG = [
  { key: 'profile', label: '个人信息', icon: <UserOutlined /> },
  {
    key: 'projects',
    label: '项目经历',
    icon: <FolderOutlined />,
    showCount: true,
    countKey: 'projects' as const,
  },
  {
    key: 'competitions',
    label: '竞赛经历',
    icon: <TrophyOutlined />,
    showCount: true,
    countKey: 'competitions' as const,
  },
  {
    key: 'internships',
    label: '实习经历',
    icon: <SolutionOutlined />,
    showCount: true,
    countKey: 'internships' as const,
  },
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

export default function MemberProfilePage() {
  const params = useParams()
  const router = useRouter()
  const memberId = Number(params.id)

  const [member, setMember] = useState<MemberDetailDTO | null>(null)
  const [tabCounts, setTabCounts] = useState<TabCounts>({
    projects: 0,
    competitions: 0,
    internships: 0,
  })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState('profile')

  const [experienceCache, setExperienceCache] = useState<ExperienceCache>({
    projects: null,
    competitions: null,
    internships: null,
  })
  const [expLoading, setExpLoading] = useState<LoadingState>({
    projects: false,
    competitions: false,
    internships: false,
  })

  useEffect(() => {
    const fetchMember = async () => {
      if (!memberId || isNaN(memberId)) {
        setError('无效的成员ID')
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        const [memberData, countsData] = await Promise.all([
          memberService.getMemberById(memberId),
          memberService.getMemberTabCounts(memberId),
        ])
        if (memberData.code === 200 && memberData.data) {
          setMember(memberData.data)
        } else {
          throw new Error(memberData.msg || '获取成员信息失败')
        }
        setTabCounts(countsData)
        setError(null)
      } catch (err) {
        console.error('Failed to fetch member:', err)
        setError('成员不存在或加载失败')
      } finally {
        setLoading(false)
      }
    }

    fetchMember()
  }, [memberId])

  const fetchExperience = useCallback(
    async (type: 'projects' | 'competitions' | 'internships') => {
      if (experienceCache[type] !== null || expLoading[type] || !member) {
        return
      }

      setExpLoading((prev) => ({ ...prev, [type]: true }))

      try {
        let data: UserExperience[]
        switch (type) {
          case 'projects': {
            const res = await memberService.getMemberProjects(member.id)
            data = res.data || []
            break
          }
          case 'competitions': {
            const res = await memberService.getMemberCompetitions(member.id)
            data = res.data || []
            break
          }
          case 'internships': {
            const res = await memberService.getMemberInternships(member.id)
            data = res.data || []
            break
          }
        }
        setExperienceCache((prev) => ({ ...prev, [type]: data }))
      } catch (error) {
        console.error(`Failed to fetch ${type}:`, error)
        setExperienceCache((prev) => ({ ...prev, [type]: [] }))
      } finally {
        setExpLoading((prev) => ({ ...prev, [type]: false }))
      }
    },
    [member, experienceCache, expLoading]
  )

  useEffect(() => {
    if (activeTab === 'projects' || activeTab === 'competitions' || activeTab === 'internships') {
      fetchExperience(activeTab)
    }
  }, [activeTab, fetchExperience])

  const getExperiencesByType = (type: ExperienceType): UserExperience[] => {
    const keyMap: Record<ExperienceType, keyof ExperienceCache> = {
      PROJECT: 'projects',
      COMPETITION: 'competitions',
      INTERNSHIP: 'internships',
    }
    return experienceCache[keyMap[type]] || []
  }

  const isExpLoading = (type: ExperienceType): boolean => {
    const keyMap: Record<ExperienceType, keyof LoadingState> = {
      PROJECT: 'projects',
      COMPETITION: 'competitions',
      INTERNSHIP: 'internships',
    }
    return expLoading[keyMap[type]]
  }

  if (loading) {
    return (
      <div className="w-full min-h-screen relative overflow-x-hidden">
        <div
          className="fixed top-0 left-0 w-full h-full z-0 pointer-events-none"
          style={{
            background:
              'radial-gradient(ellipse 80% 50% at 20% 40%, rgba(102, 119, 255, 0.15) 0%, transparent 50%), radial-gradient(ellipse 60% 40% at 80% 60%, rgba(255, 107, 53, 0.1) 0%, transparent 50%)',
          }}
        />
        <div className="flex items-center justify-center min-h-[400px]">
          <Spin size="large" />
        </div>
      </div>
    )
  }

  if (error || !member) {
    return (
      <div className="w-full min-h-screen relative overflow-x-hidden">
        <div
          className="fixed top-0 left-0 w-full h-full z-0 pointer-events-none"
          style={{
            background:
              'radial-gradient(ellipse 80% 50% at 20% 40%, rgba(102, 119, 255, 0.15) 0%, transparent 50%), radial-gradient(ellipse 60% 40% at 80% 60%, rgba(255, 107, 53, 0.1) 0%, transparent 50%)',
          }}
        />
        <div className="flex flex-col items-center justify-center min-h-[400px] text-center">
          <div className="w-16 h-16 rounded-2xl bg-red-500/10 flex items-center justify-center mb-4 text-[#ef4444] [&_svg]:w-8 [&_svg]:h-8">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
          </div>
          <h2 className="text-lg font-semibold text-white mb-2">{error || '成员不存在'}</h2>
          <p className="text-sm text-[#8c8c8d] mb-6">该成员可能已离开团队或ID不正确</p>
          <button
            className="py-3 px-6 rounded-[10px] text-sm font-medium cursor-pointer transition-all duration-300 border-none bg-gradient-to-br from-[#6677ff] to-[#2f27b0] text-white no-underline inline-flex items-center gap-2 hover:-translate-y-0.5 hover:shadow-[0_8px_24px_rgba(102,119,255,0.4)]"
            onClick={() => router.push('/members')}
          >
            <ArrowLeftOutlined />
            返回成员列表
          </button>
        </div>
      </div>
    )
  }

  const sidebarProfile = adaptToSidebarProfile(member)
  const displayData = adaptToDisplayData(member)

  const renderExperience = (type: ExperienceType, title: string) => {
    if (isExpLoading(type)) {
      return (
        <div className="flex items-center justify-center min-h-[400px]">
          <Spin size="large" />
        </div>
      )
    }
    return (
      <ExperienceSection type={type} title={title} data={getExperiencesByType(type)} readOnly />
    )
  }

  return (
    <div className="w-full min-h-screen relative overflow-x-hidden">
      <div
        className="fixed top-0 left-0 w-full h-full z-0 pointer-events-none"
        style={{
          background:
            'radial-gradient(ellipse 80% 50% at 20% 40%, rgba(102, 119, 255, 0.15) 0%, transparent 50%), radial-gradient(ellipse 60% 40% at 80% 60%, rgba(255, 107, 53, 0.1) 0%, transparent 50%)',
        }}
      />
      <main className="flex max-w-[1400px] mx-auto px-16 py-10 gap-8 relative z-1 max-[1024px]:flex-col max-[1024px]:p-6 max-md:p-4">
        <ProfileSidebar
          profile={sidebarProfile}
          allowAvatarUpload={false}
          tabCounts={tabCounts}
          activeTab={activeTab}
          onTabChange={setActiveTab}
        />
        <div className="flex-1 min-w-0">
          <ProfileTabs
            activeTab={activeTab}
            tabs={TAB_CONFIG}
            tabCounts={tabCounts}
            onTabChange={setActiveTab}
          />
          {activeTab === 'profile' && <ProfileInfoDisplay profile={displayData} />}
          {activeTab === 'projects' && renderExperience('PROJECT', '项目经历')}
          {activeTab === 'competitions' && renderExperience('COMPETITION', '竞赛经历')}
          {activeTab === 'internships' && renderExperience('INTERNSHIP', '实习经历')}
        </div>
      </main>
    </div>
  )
}
