/**
 * 个人主页前端独有类型
 *
 * 后端 DTO 类型已迁移至 @/apis/schema/ 目录
 */
import type { UserInfo, TabCounts } from '@/apis/schema/type'

export type { UserInfo, TabCounts }

export interface UserStats {
  assessmentCount: number
  completedCount: number
  averageScore: number
}

export interface UserProfileWithStats extends UserInfo {
  stats: UserStats
  tabCounts: TabCounts
}

export type AssessmentStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'ENDED'

export interface Assessment {
  id: string
  title: string
  round: string
  status: AssessmentStatus
  startDate: string
  endDate: string
  totalQuestions: number
  completedQuestions: number
  score?: number
  remainingTime?: string
  daysUntilStart?: number
}

export type TabName = 'profile' | 'assessment' | 'projects' | 'competitions' | 'internships'
