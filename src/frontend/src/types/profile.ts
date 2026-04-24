/**
 * 个人主页前端独有类型
 *
 * 后端 DTO 类型请使用 @/apis/schema/ 目录
 */

import type { AssessmentStatus } from '@/apis/schema/assessment.dto'

export type { AssessmentStatus }

export interface UserStats {
  assessmentCount: number
  completedCount: number
  averageScore: number
}

export interface UserProfileWithStats {
  stats: UserStats
  tabCounts: import('@/apis/schema/type').TabCounts
}

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
