import { useState, useEffect, useCallback } from 'react'
import type { UserInfo, TabCounts, UserExperience } from '@/apis/schema/type'
import type { AssessmentTimeDTO } from '@/apis/schema/assessment.dto'
import { userService } from '@/apis/services/user.service'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'

/**
 * Profile 页面数据获取 Hook
 * 集中管理用户资料、考核、经历等数据的获取和状态
 */

export interface ProfileData {
  profile: UserInfo | null
  tabCounts: TabCounts
  assessments: AssessmentTimeDTO[]
  experiences: UserExperience[]
  loading: boolean
  refresh: () => Promise<void>
}

export function useProfileData(): ProfileData {
  const [profile, setProfile] = useState<UserInfo | null>(null)
  const [tabCounts, setTabCounts] = useState<TabCounts>({
    projects: 0,
    achievements: 0,
    internships: 0,
  })
  const [assessments, setAssessments] = useState<AssessmentTimeDTO[]>([])
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
        setAssessments(sortedAssessmentTimes)
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
