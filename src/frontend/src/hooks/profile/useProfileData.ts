import { useState, useEffect, useCallback } from 'react'
import type { TabName, Assessment } from '@/types/profile'
import type { UserInfo, TabCounts, UserExperience } from '@/apis/schema/type'
import type { AssessmentTimeDTO } from '@/apis/schema/assessment.dto'
import { DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { userService } from '@/apis/services/user.service'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'
import { assessmentSessionService } from '@/apis/services/assessment-session.service'
import dayjs from 'dayjs'
import duration from 'dayjs/plugin/duration'

dayjs.extend(duration)

/** 将 AssessmentTimeDTO 转换为前端 Assessment 类型 */
function convertToAssessment(dto: AssessmentTimeDTO): Assessment {
  const directionLabel = DIRECTION_LABELS[dto.direction] || dto.direction
  const title = `${directionLabel}方向${dto.grade}级第${dto.epoch}轮考核`
  const round = `第${dto.epoch}轮`

  return {
    id: dto.id.toString(),
    title,
    round,
    status: 'NOT_STARTED',
    startDate: dto.startTime,
    endDate: dto.endTime,
    totalQuestions: dto.totalQuestions ?? 0,
    completedQuestions: dto.completedQuestions ?? 0,
  }
}

/** 计算考核状态 */
function calculateAssessmentStatus(
  startTime: string,
  endTime: string,
  deadline?: string
): 'NOT_STARTED' | 'IN_PROGRESS' | 'ENDED' {
  const now = dayjs()
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  const deadlineDayjs = deadline ? dayjs(deadline) : null
  const actualEnd = deadlineDayjs && deadlineDayjs.isBefore(end) ? deadlineDayjs : end

  if (now.isBefore(start)) return 'NOT_STARTED'
  if (now.isAfter(actualEnd)) return 'ENDED'
  return 'IN_PROGRESS'
}

/** 格式化剩余时间 */
function formatTimeRemaining(
  startTime: string,
  endTime: string,
  deadline?: string
): { remainingTime?: string; daysUntilStart?: number } {
  const now = dayjs()
  const start = dayjs(startTime)
  const end = dayjs(endTime)
  const deadlineDayjs = deadline ? dayjs(deadline) : null
  const actualEnd = deadlineDayjs && deadlineDayjs.isBefore(end) ? deadlineDayjs : end

  if (now.isBefore(start)) {
    const diffDays = start.diff(now, 'day', true)
    return { daysUntilStart: Math.ceil(diffDays) }
  }

  if (now.isBefore(actualEnd)) {
    const diff = actualEnd.diff(now)
    const dur = dayjs.duration(diff)
    const days = dur.asDays()

    if (days >= 1) {
      const fullDays = Math.floor(days)
      const hours = Math.floor((days - fullDays) * 24)
      return { remainingTime: `${fullDays}天${hours}小时` }
    }

    const hours = dur.hours()
    const minutes = dur.minutes()
    if (hours > 0) {
      return { remainingTime: `${hours}小时${minutes}分钟` }
    }
    return { remainingTime: `${minutes}分钟` }
  }

  return {}
}

export interface ProfileData {
  profile: UserInfo | null
  tabCounts: TabCounts
  assessments: Assessment[]
  experiences: UserExperience[]
  loading: boolean
  refresh: () => Promise<void>
}

/**
 * Profile 页面数据获取 Hook
 * 集中管理用户资料、考核、经历等数据的获取和状态
 */
export function useProfileData(): ProfileData {
  const [profile, setProfile] = useState<UserInfo | null>(null)
  const [tabCounts, setTabCounts] = useState<TabCounts>({
    projects: 0,
    competitions: 0,
    internships: 0,
  })
  const [assessments, setAssessments] = useState<Assessment[]>([])
  const [experiences, setExperiences] = useState<UserExperience[]>([])
  const [loading, setLoading] = useState(true)

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
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

      const expRes = await userService.getExperiences()
      if (expRes.code === 200 && expRes.data) {
        setExperiences(expRes.data)
      }

      const assessmentTimeRes = await assessmentTimeService.getAssessmentTimes(0, 20)
      if (assessmentTimeRes.code === 200 && assessmentTimeRes.data) {
        const assessmentTimes = assessmentTimeRes.data.content || []
        const sortedAssessmentTimes = [...assessmentTimes].sort((a, b) => a.epoch - b.epoch)

        const assessmentsWithSessions = await Promise.all(
          sortedAssessmentTimes.map(async (assessmentTime) => {
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

            const assessment = convertToAssessment(assessmentTime)
            assessment.status = calculateAssessmentStatus(
              assessmentTime.startTime,
              assessmentTime.endTime,
              deadline
            )

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

  return { profile, tabCounts, assessments, experiences, loading, refresh: loadData }
}
