/**
 * Bug 报告状态枚举
 * 对应后端 BugReportStatus.java
 */
export type BugReportStatus = 'PENDING' | 'IN_PROGRESS' | 'RESOLVED'

/**
 * Bug 报告状态标签
 */
export const BUG_REPORT_STATUS_LABELS: Record<BugReportStatus, string> = {
  PENDING: '待处理',
  IN_PROGRESS: '处理中',
  RESOLVED: '已解决',
}

/**
 * Bug 报告状态颜色
 */
export const BUG_REPORT_STATUS_COLORS: Record<BugReportStatus, string> = {
  PENDING: 'orange',
  IN_PROGRESS: 'blue',
  RESOLVED: 'green',
}

/**
 * 创建 Bug 报告响应
 * 对应后端 BugReportCreatedDTO
 */
export interface BugReportCreatedDTO {
  id: number
  status: BugReportStatus
  githubIssueUrl: string | null
}

/**
 * 创建 Bug 报告请求
 * 对应后端 CreateBugReportCommand / CreateBugReportRequestDTO
 */
export interface CreateBugReportRequestDTO {
  /** Bug 标题 */
  title: string
  /** 问题描述 */
  description: string
  /** 截图文件 ID 列表 */
  fileIds: number[]
  /** 联系邮箱（选填） */
  reporterEmail?: string
  /** 页面 URL */
  pageUrl: string
  /** 环境信息 JSON */
  environmentJson: string
}

/**
 * Bug 报告列表项
 * 对应后端 BugReportListItemDTO
 */
export interface BugReportListItemDTO {
  id: number
  title: string
  description: string
  status: BugReportStatus
  pageUrl: string
  reporterEmail: string | null
  githubIssueUrl: string | null
  githubIssueNumber: number | null
}

/**
 * Bug 报告详情
 * 对应后端 BugReportDetailDTO
 */
export interface BugReportDetailDTO {
  id: number
  title: string
  description: string
  status: BugReportStatus
  pageUrl: string
  reporterEmail: string | null
  environmentJson: string
  githubIssueUrl: string | null
  githubIssueNumber: number | null
  fileIds: number[]
}

/**
 * Bug 报告列表查询参数
 */
export interface BugReportListQueryDTO {
  page?: number
  size?: number
  status?: BugReportStatus
}
