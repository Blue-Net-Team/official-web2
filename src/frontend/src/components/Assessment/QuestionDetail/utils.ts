import type { AssessmentStatus } from '@/apis/schema/assessment.dto'
import type { UploadPhase } from './types'

export function getStatusInfo(
  startTime: string,
  endTime: string
): { text: string; status: AssessmentStatus } {
  const now = Date.now()
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  if (now < start) return { text: '未开始', status: 'NOT_STARTED' }
  if (now > end) return { text: '已结束', status: 'ENDED' }
  return { text: '进行中', status: 'IN_PROGRESS' }
}

export function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

export function getUploadPhase(
  isExpired: boolean,
  isAnswered: boolean,
  isResubmitting: boolean,
  hasUploadedFile: boolean
): UploadPhase {
  if (isAnswered && !isResubmitting) return 'answered'
  if (isExpired) return 'expired'
  if (isAnswered && isResubmitting && !hasUploadedFile) return 'resubmitting'
  if (isAnswered && isResubmitting && hasUploadedFile) return 'resubmit_uploaded'
  if (hasUploadedFile) return 'uploaded'
  return 'idle'
}
