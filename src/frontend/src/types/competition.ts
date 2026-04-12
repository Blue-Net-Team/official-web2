/**
 * 竞赛前端显示常量
 *
 * 后端 DTO 类型请使用 @/apis/schema/type 中的 CompetitionBriefDTO
 */
import type { CompetitionLevel } from '@/apis/schema/type'

export type { CompetitionLevel }

export const COMPETITION_LEVEL_LABELS: Record<CompetitionLevel, string> = {
  national: '国家级',
  provincial: '省级',
  school: '校级',
  国家级: '国家级',
  省级: '省级',
  校级: '校级',
}

export const COMPETITION_LEVEL_COLORS: Record<CompetitionLevel, string> = {
  national: '#E86835',
  provincial: '#4A90E2',
  school: '#52C41A',
  国家级: '#E86835',
  省级: '#4A90E2',
  校级: '#52C41A',
}
