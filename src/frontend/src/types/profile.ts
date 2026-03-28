/**
 * 个人主页相关类型定义
 *
 * 与后端DTO保持一致
 *
 * @author BlueNet Team
 */

// ==================== 枚举类型 ====================

/** 方向/类型枚举 */
export type Direction = 'computer_vision' | 'structural_design' | 'embedded'

/** 方向枚举描述映射 */
export const DirectionLabels: Record<Direction, string> = {
  computer_vision: '计算机视觉',
  structural_design: '结构设计',
  embedded: '嵌入式开发',
}

/** 性别枚举 */
export type Gender = 'male' | 'female' | 'unknown'

/** 性别枚举描述映射 */
export const GenderLabels: Record<Gender, string> = {
  male: '男',
  female: '女',
  unknown: '未知',
}

// ==================== 用户基础类型 ====================

/** 用户基本信息 - 对应后端 UserInfo */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  college: string
  major: string
  grade: string
  enrollmentYear: number
  email: string
  avatarFileId: number | null
  roleName: string
  direction: Direction
  gender: Gender
  bio: string
}

/** 更新用户信息请求 - 对应后端 UpdateProfileRequestDTO */
export interface UpdateProfileRequest {
  username?: string
  nickname?: string
  college?: string
  major?: string
  direction?: Direction
  gender?: Gender
  bio?: string
}

/** Tab计数 - 对应后端 TabCountsDTO */
export interface TabCounts {
  projects: number
  competitions: number
  internships: number
}

/** 用户统计数据（前端扩展，用于考核统计） */
export interface UserStats {
  assessmentCount: number
  completedCount: number
  averageScore: number
}

/** 完整的用户画像数据（包含统计） */
export interface UserProfileWithStats extends UserInfo {
  stats: UserStats
  tabCounts: TabCounts
}

// ==================== 经历类型（统一格式） ====================

/** 经历类型 */
export type ExperienceType = 'project' | 'competition' | 'internship'

/** 实习状态 */
export type InternshipStatus = 'active' | 'ended'

/** 经历信息 - 对应后端 ExperienceDTO */
export interface Experience {
  id: string
  type: ExperienceType
  name: string
  startDate: string
  endDate: string
  role?: string
  description?: string
  techStack?: string[]
  demoUrl?: string
  date?: string
  level?: string
  award?: string
  teamSize?: number
  certificateUrl?: string
  company?: string
  position?: string
  status?: InternshipStatus
  achievements?: string[]
}

/** 创建经历请求 - 对应后端 CreateExperienceRequestDTO */
export interface CreateExperienceRequest {
  type: ExperienceType
  name?: string
  role?: string
  startDate?: string
  endDate?: string
  description?: string
  techStack?: string[]
  demoUrl?: string
  date?: string
  level?: string
  award?: string
  teamSize?: number
  certificateUrl?: string
  company?: string
  position?: string
  status?: InternshipStatus
  achievements?: string[]
}

/** 更新经历请求 - 对应后端 UpdateExperienceRequestDTO */
export interface UpdateExperienceRequest {
  name?: string
  startDate?: string
  endDate?: string
  description?: string
  role?: string
  techStack?: string[]
  demoUrl?: string
  date?: string
  level?: string
  award?: string
  teamSize?: number
  certificateUrl?: string
  position?: string
  status?: InternshipStatus
  achievements?: string[]
}

// ==================== 考核相关类型 ====================

/** 考核状态 */
export type AssessmentStatus = 'not-started' | 'in-progress' | 'ended'

/** 考核信息 */
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

// ==================== Tab类型 ====================

/** Tab名称类型 */
export type TabName = 'profile' | 'assessment' | 'projects' | 'competitions' | 'internships'

// ==================== 兼容旧类型（过渡期使用） ====================

/**
 * @deprecated 使用 UserInfo 代替
 */
export type UserProfile = UserInfo

/**
 * @deprecated 使用 Experience 代替
 */
export type Project = Experience

/**
 * @deprecated 使用 Experience 代替
 */
export type Competition = Experience

/**
 * @deprecated 使用 Experience 代替
 */
export type Internship = Experience

/**
 * @deprecated 使用 InternshipStatus 代替
 */
export type { InternshipStatus as AwardLevel }
