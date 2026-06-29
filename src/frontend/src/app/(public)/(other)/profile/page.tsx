'use client'

import { useState } from 'react'
import type { TabName } from '@/types/profile'
import type { TabCounts, UserInfo } from '@/apis/schema/type'
import type { ExperienceType } from '@/apis/schema/enumerate'
import { useProfileData, useExperienceActions } from '@/hooks'
import {
  UserOutlined,
  FileTextOutlined,
  FolderOutlined,
  TrophyOutlined,
  SolutionOutlined,
} from '@ant-design/icons'
import {
  ProfileSidebar,
  ProfileTabs,
  ProfileInfo,
  AssessmentList,
  ExperienceSection,
} from '@/components/Profile'
import { API_BASE_URL } from '@/apis/config'
import { Spin } from 'antd'
import DarkVeil from '@/components/Reactbits/DarkVeil'

const DEFAULT_TAB: TabName = 'profile'

const PROFILE_TABS = [
  { key: 'profile' as TabName, label: '个人信息', icon: <UserOutlined /> },
  { key: 'assessment' as TabName, label: '我的考核', icon: <FileTextOutlined /> },
  {
    key: 'projects' as TabName,
    label: '项目经历',
    icon: <FolderOutlined />,
    showCount: true,
    countKey: 'projects' as keyof TabCounts,
  },
  {
    key: 'competitions' as TabName,
    label: '竞赛经历',
    icon: <TrophyOutlined />,
    showCount: true,
    countKey: 'competitions' as keyof TabCounts,
  },
  {
    key: 'internships' as TabName,
    label: '实习经历',
    icon: <SolutionOutlined />,
    showCount: true,
    countKey: 'internships' as keyof TabCounts,
  },
]

export default function ProfilePage() {
  const [currentTab, setCurrentTab] = useState<TabName>(DEFAULT_TAB)
  const { profile, tabCounts, assessments, experiences, loading, refresh } = useProfileData()
  const { addExperience, updateExperience, deleteExperience } = useExperienceActions(refresh)

  const handleTabChange = (tab: string) => {
    setCurrentTab(tab as TabName)
  }

  const getExperiencesByType = (type: string) => {
    return experiences.filter((e) => e.type === type)
  }

  if (loading) {
    return (
      <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
        <div className="flex justify-center items-center min-h-[400px] w-full">
          <Spin size="large" />
        </div>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
        <div className="flex justify-center items-center min-h-[400px] w-full">
          <p>加载失败，请刷新页面重试</p>
        </div>
      </div>
    )
  }

  const sidebarProfile = {
    username: profile.username,
    nickname: profile.nickname,
    college: profile.college,
    major: profile.major,
    grade: profile.grade,
    bio: profile.bio || '',
    avatarFileId: profile.avatarFileId,
    wechatQrcode:
      profile.qrcodeFileId != null ? `${API_BASE_URL}/file/download/${profile.qrcodeFileId}` : null,
    roleName: profile.roleName,
    direction: profile.direction,
  }

  return (
    <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      <div className="fixed inset-0 z-0">
        <DarkVeil hueShift={-130} speed={0.6} offsetY={0.2} />
      </div>

      <main className="flex max-w-[1400px] mx-auto pt-[104px] px-16 pb-10 gap-8 relative z-1 flex-row max-lg:flex-col md:px-6 max-sm:pt-20 max-sm:px-4 max-sm:pb-6">
        <ProfileSidebar profile={sidebarProfile} onAvatarUpdate={refresh} />

        <div className="flex-1 min-w-0">
          <ProfileTabs
            activeTab={currentTab}
            tabs={PROFILE_TABS}
            tabCounts={tabCounts}
            onTabChange={handleTabChange}
          />

          {currentTab === 'profile' && <ProfileInfo profile={profile} onUpdate={refresh} />}

          {currentTab === 'assessment' && <AssessmentList assessments={assessments} />}

          {currentTab === 'projects' && (
            <ExperienceSection
              type="PROJECT"
              title="项目经历"
              data={getExperiencesByType('PROJECT')}
              onAdd={(data) => addExperience('PROJECT', data)}
              onUpdate={updateExperience}
              onDelete={deleteExperience}
            />
          )}

          {currentTab === 'competitions' && (
            <ExperienceSection
              type="COMPETITION"
              title="竞赛经历"
              data={getExperiencesByType('COMPETITION')}
              onAdd={(data) => addExperience('COMPETITION', data)}
              onUpdate={updateExperience}
              onDelete={deleteExperience}
            />
          )}

          {currentTab === 'internships' && (
            <ExperienceSection
              type="INTERNSHIP"
              title="实习经历"
              data={getExperiencesByType('INTERNSHIP')}
              onAdd={(data) => addExperience('INTERNSHIP', data)}
              onUpdate={updateExperience}
              onDelete={deleteExperience}
            />
          )}
        </div>
      </main>
    </div>
  )
}
