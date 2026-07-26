import { Tag } from 'antd'
import dayjs from 'dayjs'
import type { AssessmentDecisionCandidateDTO, QuestionType } from '@/apis/schema/assessment.dto'

export const QUESTION_TYPE_LABELS: Record<QuestionType, string> = {
  FILE_UPLOAD: '文件上传',
  SINGLE_CHOICE: '单选题',
  MULTIPLE_CHOICE: '多选题',
  ALGORITHM: '算法题',
}

export const QUESTION_TYPE_COLORS: Record<QuestionType, string> = {
  FILE_UPLOAD: 'blue',
  SINGLE_CHOICE: 'green',
  MULTIPLE_CHOICE: 'purple',
  ALGORITHM: 'orange',
}

/** 格式化分数，隐藏无意义的小数零。 */
export function formatScore(value: number | string | null | undefined) {
  if (value === null || value === undefined) return '-'
  const numeric = Number(value)
  return Number.isNaN(numeric) ? '-' : numeric.toFixed(1).replace(/\.0$/, '')
}

/** 格式化后端返回的时间字符串。 */
export function formatTime(value: string | null | undefined) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm') : '-'
}

/** 根据客观题结果码返回符合语义的 AntD 标签颜色。 */
export function getResultColor(resultCode: string | null | undefined) {
  switch (resultCode) {
    case 'AC':
      return 'green'
    case 'WA':
      return 'red'
    case 'TLE':
      return 'gold'
    case 'RE':
      return 'volcano'
    case 'CE':
      return 'purple'
    case 'MLE':
      return 'orange'
    default:
      return 'default'
  }
}

/** 渲染候选人的录用决策状态标签。 */
export function getDecisionTag(candidate: AssessmentDecisionCandidateDTO) {
  if (candidate.passed === true) return <Tag color="green">通过</Tag>
  if (candidate.passed === false) return <Tag color="red">淘汰</Tag>
  return <Tag color="default">待决策</Tag>
}

interface ReferralInfo {
  internalReferralCode: string | null
  referralUserName: string | null
}

/** 渲染候选人的内推标签。仅内推码有效（匹配到推荐人）时展示，无效码不视为内推。 */
export function getReferralTag(candidate: ReferralInfo) {
  if (!candidate.referralUserName) return null
  return <Tag color="gold">{candidate.referralUserName} 内推</Tag>
}
