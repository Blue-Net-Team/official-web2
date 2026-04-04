/**
 * 考核时间相关类型定义
 *
 * 与后端DTO保持一致
 */

/** 方向/类型枚举 - 复用全局枚举定义 */
export type { Direction } from '@/apis/schema/enumerate'
export { DIRECTION_LABELS as DirectionLabels } from '@/apis/schema/enumerate'

/** 年级枚举描述映射 */
export const GradeLabels: Record<number, string> = {
  1: '大一',
  2: '大二',
  3: '大三',
}

/** 考核状态 */
export type AssessmentStatus = 'not-started' | 'in-progress' | 'ended'

/** 考核时间信息 - 对应后端 AssessmentTimeDTO */
export interface AssessmentTimeDTO {
  /** 考核时间ID */
  id: number
  /** 方向 */
  direction: Direction
  /** 届次（第几轮） */
  epoch: number
  /** 年级（1=大一, 2=大二, 3=大三） */
  grade: number
  /** 开始时间 */
  startTime: string
  /** 结束时间 */
  endTime: string
  /** 是否限制时间 */
  timeLimit: boolean
  /** 限时分钟数 */
  timeLimitMinutes: number | null
  /** 题目总数 */
  totalQuestions: number | null
  /** 已完成题目数 */
  completedQuestions: number | null
}

/** 考核进度信息 - 对应后端 AssessmentProgressDTO */
export interface AssessmentProgressDTO {
  /** 考核时间ID */
  assessmentTimeId: number
  /** 题目总数 */
  totalQuestions: number
  /** 已完成题目数 */
  completedQuestions: number
}
