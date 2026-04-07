'use client'

import { useState, useEffect, useCallback } from 'react'
import type {
  TabName,
  UserInfo,
  Experience,
  TabCounts,
  UserStats,
  Assessment,
  ExperienceType,
} from '@/types/profile'
import { userService } from '@/apis/services/user.service'
import MockProfileService from '@/mocks/services/profile.service'
import {
  ProfileSidebar,
  ProfileTabs,
  ProfileInfo,
  AssessmentList,
  ExperienceSection,
} from '@/components/Profile'
import { Spin } from 'antd'
import styles from './styles.module.css'

const DEFAULT_TAB: TabName = 'profile'

/** Mock 用户统计数据（后端暂无对应 API） */
const mockUserStats: UserStats = {
  assessmentCount: 3,
  completedCount: 1,
  averageScore: 88,
}

export default function ProfilePage() {
  const [currentTab, setCurrentTab] = useState<TabName>(DEFAULT_TAB)
  const [profile, setProfile] = useState<UserInfo | null>(null)
  const [tabCounts, setTabCounts] = useState<TabCounts>({
    projects: 0,
    competitions: 0,
    internships: 0,
  })
  const [assessments, setAssessments] = useState<Assessment[]>([])
  const [experiences, setExperiences] = useState<Experience[]>([])
  const [loading, setLoading] = useState(true)

  // 加载数据
  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      // 并行请求用户信息和 Tab 计数
      const [userInfoRes, tabCountsRes] = await Promise.all([
        userService.getUserInfo(),
        userService.getTabCounts(),
      ])

      if (userInfoRes.code === 200 && userInfoRes.data) {
        setProfile(userInfoRes.data)
      }

      if (tabCountsRes.code === 200 && tabCountsRes.data) {
        setTabCounts(tabCountsRes.data)
      }

      // 加载经历数据
      const expRes = await userService.getExperiences()
      if (expRes.code === 200 && expRes.data) {
        setExperiences(expRes.data)
      }

      // 考核数据继续使用 Mock
      const mockAssessments = await MockProfileService.getAssessments()
      setAssessments(mockAssessments)
    } catch (error) {
      console.error('Failed to load profile data:', error)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  // 处理 Tab 切换
  const handleTabChange = (tab: TabName) => {
    setCurrentTab(tab)
  }

  // 处理经历添加
  const handleAddExperience = async (type: string, data: Omit<Experience, 'id'>) => {
    const res = await userService.createExperience({ ...data, type: type as ExperienceType })
    if (res.code === 200) {
      await loadData()
    }
  }

  // 处理经历更新
  const handleUpdateExperience = async (id: string, data: Partial<Experience>) => {
    const res = await userService.updateExperience(id, data)
    if (res.code === 200) {
      await loadData() // 刷新数据
    }
  }

  // 处理经历删除
  const handleDeleteExperience = async (id: string) => {
    const res = await userService.deleteExperience(id)
    if (res.code === 200) {
      await loadData() // 刷新数据
    }
  }

  // 根据类型过滤经历
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

  return (
    <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      <div className={`fixed w-full h-full pointer-events-none z-0 ${styles.pageBg}`} />

      <main className="flex max-w-[1400px] mx-auto pt-[104px] px-16 pb-10 gap-8 relative z-[1] flex-row max-lg:flex-col md:px-6 max-sm:pt-20 max-sm:px-4 max-sm:pb-6">
        <ProfileSidebar profile={profile} stats={mockUserStats} onAvatarUpdate={loadData} />

        <div className="flex-1 min-w-0">
          <ProfileTabs activeTab={currentTab} tabCounts={tabCounts} onTabChange={handleTabChange} />

          {currentTab === 'profile' && <ProfileInfo profile={profile} onUpdate={loadData} />}

          {currentTab === 'assessment' && <AssessmentList assessments={assessments} />}

          {currentTab === 'projects' && (
            <ExperienceSection
              type="project"
              title="项目经历"
              data={getExperiencesByType('project')}
              onAdd={(data) => handleAddExperience('project', data)}
              onUpdate={handleUpdateExperience}
              onDelete={handleDeleteExperience}
            />
          )}

          {currentTab === 'competitions' && (
            <ExperienceSection
              type="competition"
              title="竞赛经历"
              data={getExperiencesByType('competition')}
              onAdd={(data) => handleAddExperience('competition', data)}
              onUpdate={handleUpdateExperience}
              onDelete={handleDeleteExperience}
            />
          )}

          {currentTab === 'internships' && (
            <ExperienceSection
              type="internship"
              title="实习经历"
              data={getExperiencesByType('internship')}
              onAdd={(data) => handleAddExperience('internship', data)}
              onUpdate={handleUpdateExperience}
              onDelete={handleDeleteExperience}
            />
          )}
        </div>
      </main>
    </div>
  )
}
