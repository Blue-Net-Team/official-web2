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
