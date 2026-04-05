/**
 * 考核时间相关类型定义
 *
 * 与后端DTO保持一致
 */

/** 方向/类型枚举 - 复用全局枚举定义 */
import type { Direction } from '@/apis/schema/enumerate'
export type { Direction }
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

/** 题型枚举 */
export type QuestionType = 'single_choice' | 'multiple_choice' | 'file_upload' | 'algorithm'

/** 题型标签映射 */
export const QuestionTypeLabels: Record<QuestionType, string> = {
  single_choice: '单选题',
  multiple_choice: '多选题',
  file_upload: '文件上传',
  algorithm: '算法题',
}

/** 考题信息 - 对应后端 AssessmentQuestionDTO */
export interface AssessmentQuestionDTO {
  /** 考题ID */
  id: number
  /** 考核时间ID */
  assessmentTimeId: number
  /** 题号 */
  questionNo: number
  /** 题型 */
  questionType: QuestionType
  /** 题目标题 */
  title: string
  /** 题目内容（仅管理端返回） */
  content: unknown | null
  /** 附件ID */
  attachmentId: number | null
  /** 分值 */
  score: number
  /** 当前用户是否已作答（仅用户端返回） */
  answered: boolean | null
}
