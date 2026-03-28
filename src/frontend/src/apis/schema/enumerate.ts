/**
 * 用户角色枚举
 * 对应后端 RoleType.java
 */
export type Role = 'SUPER_ADMIN' | 'DIRECTION_ADMIN' | 'MEMBER' | 'CANDIDATE'

/**
 * 方向枚举
 * 对应后端 Direction.java
 */
export type Direction = 'COMPUTER_VISION' | 'STRUCTURAL_DESIGN' | 'EMBEDDED'

/**
 * 方向枚举标签
 */
export const DIRECTION_LABELS: Record<Direction, string> = {
  COMPUTER_VISION: '计算机视觉',
  STRUCTURAL_DESIGN: '结构设计',
  EMBEDDED: '嵌入式开发',
}

/**
 * 性别枚举
 * 对应后端 Gender.java
 */
export type Gender = 'MALE' | 'FEMALE' | 'UNKNOWN'

/**
 * 性别枚举标签
 */
export const GENDER_LABELS: Record<Gender, string> = {
  MALE: '男',
  FEMALE: '女',
  UNKNOWN: '未知',
}

/**
 * 报名状态枚举
 * 对应后端 EnrollStatus.java
 */
export type EnrollStatus = 'pending' | 'approved' | 'rejected'

/**
 * 文件类型枚举
 * 对应后端 FileType.java
 */
export type FileType = 'avatar' | 'normal-img' | 'assessment-attachment' | 'work' | 'qrcode'

/**
 * 成就类型枚举
 * 对应后端 AchievementType.java
 */
export type AchievementType = 'paper' | 'patent' | 'competition'

/**
 * 成就类型标签
 */
export const ACHIEVEMENT_TYPE_LABELS: Record<AchievementType, string> = {
  paper: '论文',
  patent: '专利',
  competition: '竞赛',
}

/**
 * 奖项级别枚举
 * 对应后端 AwardLevel.java
 */
export type AwardLevel = 'national' | 'provincial' | 'school'

/**
 * 奖项级别标签
 */
export const AWARD_LEVEL_LABELS: Record<AwardLevel, string> = {
  national: '国家级',
  provincial: '省级',
  school: '校级',
}
