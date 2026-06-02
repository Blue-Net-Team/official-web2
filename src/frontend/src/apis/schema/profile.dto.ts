import type { Direction, Gender, ExperienceType, InternshipStatus } from './enumerate'

/**
 * 更新用户信息请求
 * 对应后端 UpdateProfileRequestDTO.java
 */
export interface UpdateProfileRequestDTO {
  username?: string
  nickname?: string
  college?: string
  major?: string
  direction?: Direction
  gender?: Gender
  bio?: string
  /** 微信二维码文件ID */
  qrcodeFileId?: number | null
}

/**
 * 创建经历请求
 * 对应后端 CreateExperienceRequestDTO.java
 */
export interface CreateExperienceRequestDTO {
  type: ExperienceType
  name?: string
  role?: string
  startDate?: string
  endDate?: string | null
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

/**
 * 更新经历请求
 * 对应后端 UpdateExperienceRequestDTO.java
 */
export interface UpdateExperienceRequestDTO {
  name?: string
  startDate?: string
  endDate?: string | null
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
