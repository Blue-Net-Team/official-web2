import {
  Direction,
  Gender,
  EnrollStatus,
  FileType,
  AchievementType,
  AwardLevel,
  ExperienceType,
  InternshipStatus,
} from './enumerate'

export type {
  Direction,
  Gender,
  EnrollStatus,
  FileType,
  AchievementType,
  AwardLevel,
  ExperienceType,
  InternshipStatus,
}

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
  /** 昵称 */
  nickname: string
  /** 学院 */
  college: string
  /** 专业 */
  major: string
  /** 年级 */
  grade: string
  /** 邮箱 */
  email: string
  /** 头像文件ID */
  avatarFileId: number | null
  /** 角色名称 */
  roleName: string
  /** 方向/类型 */
  direction: Direction | null
  /** 性别 */
  gender: Gender | null
  /** 个人简介 */
  bio: string
  /** GitHub 用户名 */
  githubUsername: string | null
  /** 微信二维码文件ID */
  qrcodeFileId: number | null
  /** 内推码 */
  internalReferralCode: string | null
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

/** 邮箱登录请求 */
export interface EmailLoginRequestDTO {
  /** 邮箱 */
  email: string
  /** 验证码 */
  verifyCode: string
}

/** 发送验证码请求 */
export interface SendVerificationCodeRequestDTO {
  /** 邮箱 */
  email: string
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
 * 对应后端 AwardLevel 枚举值
 */
export type CompetitionLevel = 'national' | 'provincial' | 'school'

/**
 * 竞赛信息
 * 对应后端 CompetitionResponseDTO.java
 */
export interface CompetitionResponseDTO {
  id: number
  name: string
  shortName: string
  /** Logo文件ID，用于调用下载接口 */
  logoFileId: number | null
  summary: string
  /** 竞赛级别 */
  level: CompetitionLevel
  /** 举办月份（可选） */
  month?: string
  /** 主办单位（可选） */
  organizer?: string
  /** 封面图片文件ID */
  coverFileId: number | null
  /** 排序号，数值越小越靠前 */
  sortOrder?: number
}

/**
 * 竞赛创建/更新请求
 * 对应后端 CompetitionRequestDTO.java
 */
export interface CompetitionRequestDTO {
  name?: string
  shortName?: string
  logoFileId?: number | null
  coverFileId?: number | null
  summary?: string
  level?: CompetitionLevel
  month?: string
  organizer?: string
}

/**
 * 批量排序请求
 * 对应后端 BatchSortRequestDTO.java
 */
export interface BatchSortRequestDTO {
  items: BatchSortItemDTO[]
}

export interface BatchSortItemDTO {
  id: number
  sortOrder: number
}

/**
 * 移动竞赛排序请求
 * 对应后端 MoveCompetitionRequestDTO.java
 */
export interface MoveCompetitionRequestDTO {
  direction: 'UP' | 'DOWN'
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
  /** 性别 */
  gender: Gender
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
  /** 性别 */
  gender: Gender
  /** 报名方向 */
  direction: Direction
  /** 报名状态 */
  status: EnrollStatus
  /** 头像文件ID */
  avatarFileId: number | null
  /** 内推码（报名时填写） */
  internalReferralCode: string | null
  /** 推荐人姓名（内推码有效时返回） */
  referralUserName: string | null
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
 * 创建学院请求
 * 对应后端 CreateCollegeRequestDTO.java
 */
export interface CreateCollegeRequestDTO {
  name: string
}

/**
 * 更新学院请求
 * 对应后端 UpdateCollegeRequestDTO.java
 */
export interface UpdateCollegeRequestDTO {
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
 * 预签名上传准备响应
 * 对应后端 PrepareUploadResponse.java
 */
export interface PrepareUploadResponse {
  /** 文件ID */
  fileId: number
  /** 预签名上传 URL */
  uploadUrl: string
  /** 回调令牌 */
  callbackToken: string
  /** 生成的文件名 */
  filename: string
  /** 文件类型 */
  type: FileType
}

/**
 * 预签名上传确认响应
 * 对应后端 ConfirmUploadResponse.java
 */
export interface ConfirmUploadResponse {
  /** 文件ID */
  fileId: number
  /** 文件名 */
  filename: string
  /** 文件类型 */
  type: FileType
  /** 文件状态 */
  status: 'PENDING' | 'ACTIVE' | 'REJECTED'
}

/**
 * 成员经历类型
 * 对应后端 ExperienceDTO.java
 */
export interface UserExperience {
  /** 经历ID */
  id: string
  /** 经历类型 */
  type: ExperienceType
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
  /** 公司名称（实习） */
  company?: string
  /** 职位（实习） */
  position?: string
  /** 实习状态 */
  status?: InternshipStatus
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
  /** 个人成就数 */
  achievements: number
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
  /** 年级 */
  grade: string
  /** 性别 */
  gender: Gender
  /** 角色名称 */
  role: string
  /** 个人简介 */
  bio: string | null
  /** 微信二维码文件ID */
  qrcodeFileId: number | null
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
 * 成就关联的系统内成员
 * 对应后端 AchievementMemberDTO.java
 */
export interface AchievementMemberDTO {
  /** 成员用户ID */
  userId: number
  /** 成员姓名 */
  username: string
  /** 头像文件ID */
  avatarFileId: number | null
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
  /** 关联的系统内成员 */
  members: AchievementMemberDTO[]
  /** 外部协作者姓名列表 */
  externalMembers: string[]
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

/**
 * 创建成就请求
 * 对应后端 CreateAchievementRequestDTO.java
 */
export interface CreateAchievementRequestDTO {
  /** 成就标题 */
  title: string
  /** 成就类型：PAPER/PATENT/COMPETITION */
  type: AchievementType
  /** 关联信息（竞赛赛项名/期刊名） */
  relateTo?: string | null
  /** 获奖日期 */
  achieveAt: string
  /** 奖项级别：NATIONAL/PROVINCIAL/SCHOOL */
  awardLevel?: AwardLevel | null
  /** 奖项名称 */
  awardName?: string | null
  /** 成就图片文件ID */
  fileId: number
  /** 关联的系统内成员用户ID列表 */
  userIds?: number[]
  /** 外部协作者姓名列表 */
  externalMembers?: string[]
}

/**
 * 更新成就请求
 * 对应后端 UpdateAchievementRequestDTO.java
 */
export interface UpdateAchievementRequestDTO {
  /** 成就标题 */
  title?: string
  /** 成就类型：PAPER/PATENT/COMPETITION */
  type?: AchievementType
  /** 关联信息（竞赛赛项名/期刊名） */
  relateTo?: string | null
  /** 获奖日期 */
  achieveAt?: string
  /** 奖项级别：NATIONAL/PROVINCIAL/SCHOOL */
  awardLevel?: AwardLevel | null
  /** 奖项名称 */
  awardName?: string | null
  /** 成就图片文件ID */
  fileId?: number
  /** 关联的系统内成员用户ID列表 */
  userIds?: number[]
  /** 外部协作者姓名列表 */
  externalMembers?: string[]
}

/**
 * 场地信息
 * 对应后端 VenueDTO.java
 */
export interface VenueDTO {
  /** 场地ID */
  id: number
  /** 场地名称 */
  name: string
  /** 副标题 */
  subtitle: string | null
  /** 描述 */
  description: string | null
  /** 图片URL */
  imageUrl: string | null
  /** 图片文件ID */
  imageFileId: number | null
}

/**
 * 设备信息
 * 对应后端 EquipmentDTO.java
 */
export interface EquipmentDTO {
  /** 设备ID */
  id: number
  /** 设备名称 */
  name: string
  /** 品牌 */
  brand: string | null
  /** 描述 */
  description: string | null
  /** 图片URL */
  imageUrl: string | null
  /** 图片文件ID */
  imageFileId: number | null
}

/**
 * 创建场地请求
 * 对应后端 CreateVenueRequestDTO.java
 */
export interface CreateVenueRequestDTO {
  name: string
  subtitle?: string
  description?: string
  imageFileId?: number | null
  sortOrder?: number
}

/**
 * 更新场地请求
 * 对应后端 UpdateVenueRequestDTO.java
 */
export interface UpdateVenueRequestDTO {
  name?: string
  subtitle?: string
  description?: string
  imageFileId?: number | null
  sortOrder?: number
}

/**
 * 创建设备请求
 * 对应后端 CreateEquipmentRequestDTO.java
 */
export interface CreateEquipmentRequestDTO {
  name: string
  brand?: string
  description?: string
  imageFileId?: number | null
  sortOrder?: number
}

/**
 * 更新设备请求
 * 对应后端 UpdateEquipmentRequestDTO.java
 */
export interface UpdateEquipmentRequestDTO {
  name?: string
  brand?: string
  description?: string
  imageFileId?: number | null
  sortOrder?: number
}

/**
 * 审计统计 - 请求量趋势数据点
 * 对应后端 TrendPointDTO.java
 */
export interface TrendPointDTO {
  /** 时间桶起始时间 */
  time: string
  /** 该时间段内的请求数量 */
  count: number
}

/**
 * 审计统计 - 接口访问排名条目
 * 对应后端 EndpointRankingDTO.java
 */
export interface EndpointRankingDTO {
  /** URI 路径模板 */
  pattern: string
  /** 总请求数 */
  count: number
  /** 平均响应时间（毫秒） */
  avgDurationMs: number
  /** 失败请求数 */
  errorCount: number
}

/**
 * 审计统计 - 接口响应时间排名条目
 * 对应后端 EndpointLatencyDTO.java
 */
export interface EndpointLatencyDTO {
  /** URI 路径模板 */
  pattern: string
  /** 平均响应时间（毫秒） */
  avgDurationMs: number
  /** 最大响应时间（毫秒） */
  maxDurationMs: number
  /** 总请求数 */
  count: number
}

/**
 * 管理端报名详情
 * 对应后端 EnrollmentDetailDTO.java
 */
export interface EnrollmentDetailDTO {
  id: number
  username: string
  studentId: string
  email: string
  collegeId: number
  collegeName: string
  major: string
  gender: Gender
  direction: Direction
  status: EnrollStatus
  avatarFileId: number | null
  introduction: string
  internalReferralCode: string | null
  referralUserName: string | null
}

/**
 * 报名统计
 * 对应后端 EnrollmentStatisticsDTO.java
 */
export interface EnrollmentStatisticsDTO {
  total: number
  byStatus: Record<string, number>
  byDirection: Record<string, number>
}

/**
 * 报名审批结果
 * 对应后端 EnrollmentApprovalResultDTO.java
 */
export interface EnrollmentApprovalResultDTO {
  id: number
  status: EnrollStatus
  createdUserId: number | null
}

export interface ApproveEnrollmentRequestDTO {
  assessmentGradeYear?: number
}

/**
 * 拒绝报名请求
 * 对应后端 RejectEnrollmentRequestDTO.java
 */
export interface RejectEnrollmentRequestDTO {
  reason?: string
}

/**
 * 管理端报名列表查询参数
 */
export interface EnrollmentListQueryDTO {
  page?: number
  size?: number
  keyword?: string
  status?: EnrollStatus
  direction?: Direction
}

export interface PermissionDTO {
  id: number
  value: string
  name: string
  url: string | null
  method: string | null
  accessLevel: string
  assignedRoles: string[]
}

export interface PermissionQueryDTO {
  page?: number
  size?: number
  keyword?: string
  format?: string
}

export interface PermissionTreeDTO {
  key: string
  title: string
  value: string | null
  permissionId: number | null
  leaf: boolean
  accessLevel: string | null
  children: PermissionTreeDTO[]
  permissionCount: number
}

export interface RolePermissionBatchRequestDTO {
  permissionIds: number[]
}

export interface PermissionRoleBatchRequestDTO {
  roleNames: string[]
}

export interface RolePermissionResponseDTO {
  successCount: number
  currentPermissions: string[]
}

export interface PermissionRoleResponseDTO {
  successCount: number
  currentRoles: string[]
}

/**
 * 消息模板信息
 * 对应后端 MessageTemplateInfo.java
 */
export interface MessageTemplateInfoDTO {
  /** 模板唯一编码 */
  code: string
  /** 模板名称 */
  name: string
  /** 邮件主题 */
  subject: string
  /** 模板描述 */
  description: string
  /** 可用变量列表 */
  variables: string[]
  /** 当前模板内容（可能被覆盖） */
  content: string
  /** 默认模板内容 */
  defaultContent: string
  /** 是否启用 */
  enabled: boolean
}

/**
 * 更新消息模板请求
 * 对应后端 UpdateTemplateRequest.java
 */
export interface UpdateMessageTemplateRequestDTO {
  subject: string
  content: string
}

// ========== Admin User Management ==========

export interface AdminUserListQueryDTO {
  page?: number
  size?: number
  roleId?: number
  direction?: string
  collegeId?: number
  keyword?: string
}

export interface AdminUserListItemDTO {
  id: number
  studentId: string
  username: string
  nickname: string | null
  email: string | null
  roleId: number | null
  roleName: string | null
  direction: string | null
  collegeId: number | null
  college: string | null
  major: string | null
  gender: string | null
  job: string | null
  disable: boolean
  avatarFileId: number | null
  assessmentGradeYear: number | null
}

export interface AdminUserDetailDTO {
  id: number
  studentId: string
  username: string
  nickname: string | null
  email: string | null
  roleId: number | null
  roleName: string | null
  direction: string | null
  collegeId: number | null
  college: string | null
  major: string | null
  gender: string | null
  job: string | null
  disable: boolean
  avatarFileId: number | null
  githubUsername: string | null
  bio: string | null
  assessmentGradeYear: number | null
  experienceCount: number
  achievementCount: number
  answerCount: number
  commentCount: number
}

export interface AdminUserUpdateRequestDTO {
  roleId?: number
  direction?: string
  disable?: boolean
  job?: string
  studentId?: string
  email?: string
  username?: string
  nickname?: string
  collegeId?: number
  major?: string
  gender?: string
  assessmentGradeYear?: number
}

export interface AdminUserResetPasswordRequestDTO {
  newPassword: string
  confirmPassword: string
}

export interface AdminUserBatchOperateRequestDTO {
  userIds: number[]
}

export interface AdminUserBatchUpdateRoleRequestDTO {
  userIds: number[]
  roleId: number
}

export interface AdminUserCreateRequestDTO {
  studentId: string
  email: string
  username: string
  password: string
  nickname?: string
  roleId: number
  collegeId?: number
  major?: string
  direction?: string
  gender?: string
  job?: string
  assessmentGradeYear?: number
}

export interface AdminUserCreateResponseDTO {
  id: number
  studentId: string
  username: string
  roleId: number
}

/**
 * 软件资源信息
 * 对应后端 SoftwareResourceDTO.java
 */
export interface SoftwareResourceDTO {
  id: number
  name: string
  direction: import('./enumerate').SoftwareResourceDirection
  category: string | null
  description: string | null
  externalUrl: string
  sortOrder: number
  status: import('./enumerate').SoftwareResourceStatus
}

/**
 * 软件资源列表查询参数
 * 对应后端 SoftwareResourceListRequestDTO.java
 */
export interface SoftwareResourceListRequestDTO {
  direction?: string
  page?: number
  size?: number
}

/**
 * 创建软件资源请求
 * 对应后端 CreateSoftwareResourceRequestDTO.java
 */
export interface CreateSoftwareResourceRequestDTO {
  name: string
  direction: import('./enumerate').SoftwareResourceDirection
  category?: string
  description?: string
  externalUrl: string
  sortOrder?: number
}

/**
 * 更新软件资源请求
 * 对应后端 UpdateSoftwareResourceRequestDTO.java
 */
export interface UpdateSoftwareResourceRequestDTO {
  name: string
  direction: import('./enumerate').SoftwareResourceDirection
  category?: string
  description?: string
  externalUrl: string
  sortOrder?: number
  status: import('./enumerate').SoftwareResourceStatus
}
