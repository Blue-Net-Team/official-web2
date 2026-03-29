import { Direction, Gender, EnrollStatus, FileType, AchievementType, AwardLevel } from './enumerate'

export type { Direction, Gender, EnrollStatus, FileType, AchievementType, AwardLevel }

/**
 * 统一 API 响应包装
 * 对应后端 ResponseMessage.java
 */
export interface ResponseMessage<T> {
  /** 业务/HTTP 状态码，成功时为 200 */
  code: number
  /** 提示信息 */
  msg: string
  /** 业务数据，成功时存在，错误时通常为 null */
  data: T | null
}

/**
 * 用户基本信息
 * 对应后端 UserInfo.java
 */
export interface UserInfo {
  /** 用户 ID */
  id: number
  /** 用户名 */
  username: string
  /** 学院 */
  college: string
  /** 专业 */
  major: string
  /** 年级 */
  grade: string
  /** 邮箱 */
  email: string
  /** 头像 URL */
  avatarUrl: string | null
  /** 角色名称 */
  roleName: string
  /** 方向/类型 */
  direction: Direction | null
  /** 性别 */
  gender: Gender | null
}

/**
 * 学号登录请求
 * 对应后端 StudentIdLoginRequestDTO.java
 */
export interface StudentIdLoginRequestDTO {
  /** 学号 */
  studentId: string
  /** 密码 */
  password: string
}

/**
 * 登录成功响应
 * 对应后端 UserAuthResponseDTO.java
 * JWT 通过 HttpOnly Cookie 自动设置，响应体仅返回 CSRF Token
 */
export interface UserAuthResponseDTO {
  /** CSRF Token，状态修改请求需在 X-CSRF-Token Header 中携带 */
  csrfToken: string
  /** 当前用户信息 */
  userInfo: UserInfo
}

/**
 * 获取当前登录状态响应
 * 对应后端 AuthMeResponseDTO.java
 * 用于页面刷新后恢复登录状态
 */
export interface AuthMeResponseDTO {
  /** 是否已登录 */
  authenticated: boolean
  /** 当前用户信息（未登录时为 null） */
  userInfo: UserInfo | null
  /** CSRF Token（未登录时为 null） */
  csrfToken: string | null
}

/**
 * 竞赛级别
 * 对应后端存储的中文值
 */
export type CompetitionLevel = 'national' | 'provincial' | 'school' | '国家级' | '省级' | '校级'

/**
 * 竞赛简介
 * 对应后端 CompetitionBriefDTO.java
 */
export interface CompetitionBriefDTO {
  id: number
  name: string
  shortName: string
  /** @deprecated 请使用 logoFileId */
  logoUrl: string | null
  /** Logo文件ID，用于调用下载接口 */
  logoFileId: number | null
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

/**
 * 成员简要信息
 * 对应后端 MemberBriefDTO.java
 */
export interface MemberBriefDTO {
  /** 成员ID */
  id: number
  /** 真实姓名 */
  username: string
  /** 昵称 */
  nickname: string
  /** 方向 */
  direction: Direction
  /** 职责 */
  job: string
  /** 头像文件ID */
  avatarFileId: number | null
  /** 学院 */
  college: string
  /** 专业 */
  major: string
  /** 入学年份 */
  enrollmentYear: number
  /** 性别 */
  gender: Gender
  /** 角色名称 */
  roleName: string
}

/**
 * 发起报名请求
 * 对应后端 CreateEnrollmentRequestDTO.java
 */
export interface CreateEnrollmentRequestDTO {
  /** 真实姓名 */
  username: string
  /** 学号，12-13位数字 */
  studentId: string
  /** 邮箱，用于接收通知 */
  email: string
  /** 学院ID */
  collegeId: number
  /** 专业 */
  major: string
  /** 年级，1-6 */
  grade: number
  /** 报名方向 */
  direction: Direction
  /** 头像文件ID（需先调用文件上传接口获取） */
  avatarId?: number
  /** 自我介绍，100-500字 */
  introduction: string
  /** 内推码，8位大写字母+数字 */
  internalReferralCode?: string
  /** 是否强制更新已有报名，默认false */
  forceUpdate?: boolean
}

/**
 * 报名简要信息
 * 对应后端 EnrollmentBriefDTO.java
 */
export interface EnrollmentBriefDTO {
  /** 报名ID */
  id: number
  /** 真实姓名 */
  username: string
  /** 学号 */
  studentId: string
  /** 邮箱 */
  email: string
  /** 学院名称 */
  collegeName: string
  /** 专业 */
  major: string
  /** 年级 */
  grade: number
  /** 报名方向 */
  direction: Direction
  /** 报名状态 */
  status: EnrollStatus
  /** 头像文件ID */
  avatarFileId: number | null
}

/**
 * 学院信息
 * 对应后端 CollegeDTO.java
 */
export interface CollegeDTO {
  /** 学院ID */
  id: number
  /** 学院名称 */
  name: string
}

/**
 * 文件信息
 * 对应后端 FileInfo.java
 */
export interface FileInfo {
  /** 文件ID */
  id: number
  /** 文件名称 */
  name: string
  /** 文件类型 */
  type: FileType
  /** @deprecated 已废弃，文件下载请使用 /api/v1/file/download/{id} 接口 */
  url: string
}

/**
 * 成员经历类型
 * 对应后端 ExperienceDTO.java
 */
export interface UserExperience {
  /** 经历ID */
  id: string
  /** 经历类型: project/competition/internship */
  type: 'project' | 'competition' | 'internship'
  /** 名称（项目名/竞赛名/公司名） */
  name: string
  /** 开始时间 */
  startDate: string
  /** 结束时间 */
  endDate: string | null
  /** 角色/职位 */
  role?: string
  /** 描述 */
  description?: string
  /** 技术栈 */
  techStack?: string[]
  /** 演示链接 */
  demoUrl?: string
  /** 竞赛时间 */
  date?: string
  /** 竞赛级别 */
  level?: string
  /** 获奖等级 */
  award?: string
  /** 团队人数 */
  teamSize?: number
  /** 证书链接 */
  certificateUrl?: string
  /** 公司名称（实习） */
  company?: string
  /** 职位（实习） */
  position?: string
  /** 实习状态 */
  status?: 'active' | 'ended'
  /** 主要成就 */
  achievements?: string[]
}

/**
 * 经历统计
 * 对应后端 TabCountsDTO.java
 */
export interface TabCounts {
  /** 项目经历数 */
  projects: number
  /** 竞赛经历数 */
  competitions: number
  /** 实习经历数 */
  internships: number
}

/**
 * 成员详情信息
 * 对应后端 MemberDetailDTO.java
 * 注意：部分字段为 Mock 数据，后端尚未实现
 */
export interface MemberDetailDTO {
  /** 成员 ID */
  id: number
  /** 真实姓名 */
  username: string
  /** 昵称 */
  nickname: string
  /** 方向 */
  direction: Direction
  /** 职责 */
  job: string
  /** 头像文件 ID */
  avatarFileId: number | null
  /** 学院 */
  college: string
  /** 专业 */
  major: string
  /** 入学年份 */
  enrollmentYear: number
  /** 性别 */
  gender: Gender
  /** 角色名称 */
  role: string
  /** 个人简介 */
  bio: string | null
}

/**
 * 分页响应DTO
 * 对应后端 PageDTO.java
 */
export interface PageDTO<T> {
  /** 内容列表 */
  content: T[]
  /** 总元素数 */
  totalElements: number
  /** 总页数 */
  totalPages: number
  /** 当前页码(从0开始) */
  number: number
  /** 每页大小 */
  size: number
  /** 当前页元素数 */
  numberOfElements: number
  /** 是否第一页 */
  first: boolean
  /** 是否最后一页 */
  last: boolean
  /** 是否为空 */
  empty: boolean
}

/**
 * 成就信息
 * 对应后端 AchievementDTO.java
 */
export interface AchievementDTO {
  /** 成就ID */
  id: number
  /** 成就标题（竞赛名/论文标题/专利标题） */
  title: string
  /** 成就类型：paper/patent/competition */
  type: AchievementType
  /** 关联信息（竞赛赛项名/论文学期名/专利可为null） */
  relateTo: string | null
  /** 获奖日期 */
  achieveAt: string
  /** 奖项级别：national/provincial/school，仅竞赛类型有效 */
  awardLevel: AwardLevel | null
  /** 奖项级别名称 */
  awardLevelName: string | null
  /** 奖项名称，仅竞赛类型有效 */
  awardName: string | null
  /** 竞赛名称 */
  competitionName: string | null
  /** 竞赛简称 */
  competitionShortName: string | null
  /** 竞赛Logo文件ID */
  competitionLogoFileId: number | null
  /** 成就图片文件ID */
  fileId: number | null
  /** 成就图片URL */
  fileUrl: string | null
}

/**
 * 成就统计信息
 * 对应后端 AchievementStatsDTO.java
 */
export interface AchievementStatsDTO {
  /** 总成就数 */
  totalAchievements: number
  /** 国家级奖项数 */
  nationalCount: number
  /** 省级奖项数 */
  provincialCount: number
  /** 校级奖项数 */
  schoolCount: number
}
