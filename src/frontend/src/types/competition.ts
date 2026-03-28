/**
 * 竞赛级别（支持英文枚举和中文值）
 */
export type CompetitionLevel = 'national' | 'provincial' | 'school' | '国家级' | '省级' | '校级'

/**
 * 竞赛级别标签
 */
export const COMPETITION_LEVEL_LABELS: Record<CompetitionLevel, string> = {
  national: '国家级',
  provincial: '省级',
  school: '校级',
  国家级: '国家级',
  省级: '省级',
  校级: '校级',
}

/**
 * 竞赛级别颜色
 */
export const COMPETITION_LEVEL_COLORS: Record<CompetitionLevel, string> = {
  national: '#E86835',
  provincial: '#4A90E2',
  school: '#52C41A',
  国家级: '#E86835',
  省级: '#4A90E2',
  校级: '#52C41A',
}

/**
 * 竞赛详细信息
 * 对应后端 CompetitionBriefDTO 扩展字段
 */
export interface Competition {
  /** 竞赛ID */
  id: number
  /** 竞赛名称 */
  name: string
  /** 竞赛简称 */
  shortName: string
  /** Logo文件ID */
  logoFileId: number | null
  /** 竞赛简介 */
  summary: string
  /** 竞赛级别 */
  level: CompetitionLevel
  /** 举办月份（可选） */
  month?: string
  /** 主办单位（可选） */
  organizer?: string
  /** 介绍图片文件ID（可选） */
  introduceImageFileId?: number | null
}
