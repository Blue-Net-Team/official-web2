'use client'

import { useState, useEffect, useCallback } from 'react'
import type { TabName, Assessment } from '@/types/profile'
import type { UserInfo, TabCounts, UserExperience } from '@/apis/schema/type'
import type { ExperienceType } from '@/apis/schema/enumerate'
import type { AssessmentTimeDTO } from '@/apis/schema/assessment.dto'
import { DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { userService } from '@/apis/services/user.service'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'
import { assessmentSessionService } from '@/apis/services/assessment-session.service'
import {
  ProfileSidebar,
  ProfileTabs,
  ProfileInfo,
  AssessmentList,
  ExperienceSection,
} from '@/components/Profile'
import { Spin } from 'antd'
import styles from './styles.module.css'
import dayjs from 'dayjs'
import duration from 'dayjs/plugin/duration'

dayjs.extend(duration)

const DEFAULT_TAB: TabName = 'profile'

/**
 * 将 AssessmentTimeDTO 转换为前端 Assessment 类型
 */
function convertToAssessment(dto: AssessmentTimeDTO): Assessment {
  const directionLabel = DIRECTION_LABELS[dto.direction] || dto.direction
  const title = `${directionLabel}方向${dto.grade}级第${dto.epoch}轮考核`
  const round = `第${dto.epoch}轮`

  return {
    id: dto.id.toString(),
    title,
    round,
    status: 'NOT_STARTED', // 后续会更新
    startDate: dto.startTime,
    endDate: dto.endTime,
    totalQuestions: dto.totalQuestions ?? 0,
    completedQuestions: dto.completedQuestions ?? 0,
  }
}

/**
 * 计算考核状态
 * @param startTime 开始时间 (ISO 字符串)
 * @param endTime 结束时间 (ISO 字符串)
 * @param deadline 限时考核截止时间 (ISO 字符串，可选)
 * @returns 考核状态
 */
function calculateAssessmentStatus(
  startTime: string,
  endTime: string,
  deadline?: string
): 'NOT_STARTED' | 'IN_PROGRESS' | 'ENDED' {
  const now = dayjs()
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  const deadlineDayjs = deadline ? dayjs(deadline) : null

  // 限时考核使用 deadline 作为实际结束时间
  const actualEnd = deadlineDayjs && deadlineDayjs.isBefore(end) ? deadlineDayjs : end

  if (now.isBefore(start)) {
    return 'NOT_STARTED'
  } else if (now.isAfter(actualEnd)) {
    return 'ENDED'
  } else {
    return 'IN_PROGRESS'
  }
}

/**
 * 格式化剩余时间
 * @param startTime 开始时间
 * @param endTime 结束时间
 * @param deadline 限时考核截止时间
 * @returns 格式化的剩余时间字符串（如 "2 天 3 小时"）或距离开始天数
 */
function formatTimeRemaining(
  startTime: string,
  endTime: string,
  deadline?: string
): { remainingTime?: string; daysUntilStart?: number } {
  const now = dayjs()
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  const deadlineDayjs = deadline ? dayjs(deadline) : null

  // 限时考核使用 deadline 作为实际结束时间
  const actualEnd = deadlineDayjs && deadlineDayjs.isBefore(end) ? deadlineDayjs : end

  if (now.isBefore(start)) {
    // 还未开始，计算距离开始的天数
    const diffDays = start.diff(now, 'day', true)
    return { daysUntilStart: Math.ceil(diffDays) }
  } else if (now.isBefore(actualEnd)) {
    // 进行中，计算剩余时间
    const diff = actualEnd.diff(now)
    const duration = dayjs.duration(diff)
    const days = duration.asDays()

    if (days >= 1) {
      const fullDays = Math.floor(days)
      const hours = Math.floor((days - fullDays) * 24)
      return { remainingTime: `${fullDays}天${hours}小时` }
    } else {
      const hours = duration.hours()
      const minutes = duration.minutes()
      if (hours > 0) {
        return { remainingTime: `${hours}小时${minutes}分钟` }
      } else {
        return { remainingTime: `${minutes}分钟` }
      }
    }
  }

  return {}
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
  const [experiences, setExperiences] = useState<UserExperience[]>([])
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

      // 加载考核数据 - 使用真实 API
      const assessmentTimeRes = await assessmentTimeService.getAssessmentTimes(0, 20)
      if (assessmentTimeRes.code === 200 && assessmentTimeRes.data) {
        const assessmentTimes = assessmentTimeRes.data.content || []

        // 按轮次（epoch）从低到高排序
        const sortedAssessmentTimes = [...assessmentTimes].sort((a, b) => a.epoch - b.epoch)

        // 并行获取限时考核的会话信息
        const assessmentsWithSessions = await Promise.all(
          sortedAssessmentTimes.map(async (assessmentTime) => {
            // 限时考核需要获取会话信息（deadline）
            let deadline: string | undefined
            if (assessmentTime.timeLimit) {
              try {
                const sessionRes = await assessmentSessionService.getSession(assessmentTime.id)
                if (sessionRes.code === 200 && sessionRes.data) {
                  deadline = sessionRes.data.deadline
                }
              } catch (sessionError) {
                console.warn(
                  `Failed to get session for assessment ${assessmentTime.id}:`,
                  sessionError
                )
              }
            }

            // 转换为前端 Assessment 类型
            const assessment = convertToAssessment(assessmentTime)

            // 计算考核状态
            assessment.status = calculateAssessmentStatus(
              assessmentTime.startTime,
              assessmentTime.endTime,
              deadline
            )

            // 计算剩余时间或距离开始天数
            const timeInfo = formatTimeRemaining(
              assessmentTime.startTime,
              assessmentTime.endTime,
              deadline
            )
            if (timeInfo.remainingTime) {
              assessment.remainingTime = timeInfo.remainingTime
            }
            if (timeInfo.daysUntilStart !== undefined) {
              assessment.daysUntilStart = timeInfo.daysUntilStart
            }

            return assessment
          })
        )

        setAssessments(assessmentsWithSessions)
      }
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
  const handleAddExperience = async (type: string, data: Omit<UserExperience, 'id'>) => {
    const res = await userService.createExperience({ ...data, type: type as ExperienceType })
    if (res.code === 200) {
      await loadData()
    }
  }

  // 处理经历更新
  const handleUpdateExperience = async (id: string, data: Partial<UserExperience>) => {
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
        <ProfileSidebar profile={profile} onAvatarUpdate={loadData} />

        <div className="flex-1 min-w-0">
          <ProfileTabs
            activeTab={currentTab}
            tabCounts={tabCounts}
            roleName={profile.roleName}
            onTabChange={handleTabChange}
          />

          {currentTab === 'profile' && <ProfileInfo profile={profile} onUpdate={loadData} />}

          {currentTab === 'assessment' && <AssessmentList assessments={assessments} />}

          {currentTab === 'projects' && (
            <ExperienceSection
              type="PROJECT"
              title="项目经历"
              data={getExperiencesByType('PROJECT')}
              onAdd={(data) => handleAddExperience('PROJECT', data)}
              onUpdate={handleUpdateExperience}
              onDelete={handleDeleteExperience}
            />
          )}

          {currentTab === 'competitions' && (
            <ExperienceSection
              type="COMPETITION"
              title="竞赛经历"
              data={getExperiencesByType('COMPETITION')}
              onAdd={(data) => handleAddExperience('COMPETITION', data)}
              onUpdate={handleUpdateExperience}
              onDelete={handleDeleteExperience}
            />
          )}

          {currentTab === 'internships' && (
            <ExperienceSection
              type="INTERNSHIP"
              title="实习经历"
              data={getExperiencesByType('INTERNSHIP')}
              onAdd={(data) => handleAddExperience('INTERNSHIP', data)}
              onUpdate={handleUpdateExperience}
              onDelete={handleDeleteExperience}
            />
          )}
        </div>
      </main>
    </div>
  )
}
