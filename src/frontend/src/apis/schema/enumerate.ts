/**
 * 用户角色枚举
 * 对应后端 RoleType.java
 */
export type Role = 'SUPER_ADMIN' | 'DIRECTION_ADMIN' | 'MEMBER' | 'CANDIDATE'

/**
 * 角色标签
 */
export const ROLE_LABELS: Record<string, string> = {
  CANDIDATE: '考生',
  MEMBER: '成员',
  DIRECTION_ADMIN: '方向管理员',
  SUPER_ADMIN: '超级管理员',
}

/**
 * 获取角色对应的 antd Tag 颜色
 */
export const getRoleTagColor = (roleName: string): string => {
  switch (roleName) {
    case 'CANDIDATE':
      return 'orange'
    case 'MEMBER':
      return 'blue'
    case 'DIRECTION_ADMIN':
      return 'purple'
    case 'SUPER_ADMIN':
      return 'red'
    default:
      return 'default'
  }
}

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
 * 软件资源方向（包含通用）
 * 对应后端 SoftwareResourceDirection.java
 */
export type SoftwareResourceDirection =
  | 'COMPUTER_VISION'
  | 'STRUCTURAL_DESIGN'
  | 'EMBEDDED'
  | 'GENERAL'

/**
 * 软件资源方向标签
 */
export const SOFTWARE_RESOURCE_DIRECTION_LABELS: Record<SoftwareResourceDirection, string> = {
  ...DIRECTION_LABELS,
  GENERAL: '通用',
}

/**
 * 软件资源状态枚举
 * 对应后端 SoftwareResourceStatus.java
 */
export type SoftwareResourceStatus = 'ACTIVE' | 'DISABLED'

/**
 * 软件资源状态标签
 */
export const SOFTWARE_RESOURCE_STATUS_LABELS: Record<SoftwareResourceStatus, string> = {
  ACTIVE: '已启用',
  DISABLED: '已禁用',
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
export type EnrollStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/**
 * 文件类型枚举
 * 对应后端 FileType.java
 */
export type FileType = 'AVATAR' | 'NORMAL_IMG' | 'ASSESSMENT_ATTACHMENT' | 'WORK' | 'QRCODE'

/**
 * 成就类型枚举
 * 对应后端 AchievementType.java
 */
export type AchievementType = 'PAPER' | 'PATENT' | 'COMPETITION'

/**
 * 成就类型标签
 */
export const ACHIEVEMENT_TYPE_LABELS: Record<AchievementType, string> = {
  PAPER: '论文',
  PATENT: '专利',
  COMPETITION: '竞赛',
}

/**
 * 奖项级别枚举
 * 对应后端 AwardLevel.java
 */
export type AwardLevel = 'NATIONAL' | 'PROVINCIAL' | 'SCHOOL'

/**
 * 奖项级别标签
 */
export const AWARD_LEVEL_LABELS: Record<AwardLevel, string> = {
  NATIONAL: '国家级',
  PROVINCIAL: '省级',
  SCHOOL: '校级',
}

/**
 * 经历类型枚举
 * 对应后端 ExperienceType.java
 */
export type ExperienceType = 'COMPETITION' | 'PROJECT' | 'INTERNSHIP'

/**
 * 实习状态枚举
 * 对应后端 ExperienceDTO.status 字段值
 */
export type InternshipStatus = 'ACTIVE' | 'ENDED'
