/**
 * 考核模块 DTO 类型定义
 *
 * 与后端 DTO 保持一致
 */
import type { Direction } from './enumerate'

/** 考核状态 */
export type AssessmentStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'ENDED'

/** 题型枚举 */
export type QuestionType = 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'FILE_UPLOAD' | 'ALGORITHM'

/** 创建考核时间请求 - 对应后端 CreateAssessmentTimeRequestDTO */
export interface CreateAssessmentTimeRequestDTO {
  /** 方向 */
  direction: Direction
  /** 届次（第几轮） */
  epoch: number
  /** 入学年份 */
  grade: number
  /** 开始时间 */
  startTime: string
  /** 结束时间 */
  endTime: string
  /** 是否限时 */
  timeLimit: boolean
  /** 限时分钟数（timeLimit为true时必填） */
  timeLimitMinutes?: number | null
}

/** 更新考核时间请求 - 对应后端 UpdateAssessmentTimeRequestDTO */
export interface UpdateAssessmentTimeRequestDTO {
  /** 方向 */
  direction?: Direction
  /** 届次 */
  epoch?: number
  /** 入学年份 */
  grade?: number
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
  /** 是否限时 */
  timeLimit?: boolean
  /** 限时分钟数 */
  timeLimitMinutes?: number | null
}

/** 考核时间信息 - 对应后端 AssessmentTimeDTO */
export interface AssessmentTimeDTO {
  /** 考核时间ID */
  id: number
  /** 方向 */
  direction: Direction
  /** 届次（第几轮） */
  epoch: number
  /** 入学年份（如 2024、2025） */
  grade: number
  /** 开始时间 */
  startTime: string
  /** 结束时间 */
  endTime: string
  /** 是否限制时间 */
  timeLimit: boolean
  /** 限时分钟数 */
  timeLimitMinutes: number | null
  /** 题目总数 */
  totalQuestions: number | null
  /** 已完成题目数 */
  completedQuestions: number | null
}

/** 考核进度信息 - 对应后端 AssessmentProgressDTO */
export interface AssessmentProgressDTO {
  /** 考核时间ID */
  assessmentTimeId: number
  /** 题目总数 */
  totalQuestions: number
  /** 已完成题目数 */
  completedQuestions: number
}

/** 考题信息 - 对应后端 AssessmentQuestionDTO */
export interface AssessmentQuestionDTO {
  /** 考题ID */
  id: number
  /** 考核时间ID */
  assessmentTimeId: number
  /** 题号 */
  questionNo: number
  /** 题型 */
  questionType: QuestionType
  /** 题目标题 */
  title: string
  /** 题目内容（仅管理端返回） */
  content: unknown | null
  /** 附件ID */
  attachmentId: number | null
  /** 分值 */
  score: number
  /** 当前用户是否已作答（仅用户端返回） */
  answered: boolean | null
}

/** 题目内容基类（对应后端 QuestionContent 多态结构，type 使用后端 @JsonSubTypes 的小写 name） */
export interface BaseQuestionContent {
  /** 题型标识（后端 Jackson @JsonSubTypes name，小写） */
  type: 'file_upload' | 'single_choice' | 'multiple_choice' | 'algorithm'
  /** 题干 */
  content: string
}

/** 文件上传题内容 */
export interface FileUploadContent extends BaseQuestionContent {
  type: 'file_upload'
  /** 允许的文件类型（扩展名列表，前端预留字段，后端未返回） */
  allowedExtensions?: string[]
  /** 最大文件大小（字节，前端预留字段，后端未返回） */
  maxFileSize?: number
}

/** 单选题内容 */
export interface SingleChoiceContent extends BaseQuestionContent {
  type: 'single_choice'
  /** 选项列表 */
  options: string[]
  /** 正确答案（选项文本，仅管理端返回） */
  correctAnswer?: string
}

/** 多选题内容 */
export interface MultipleChoiceContent extends BaseQuestionContent {
  type: 'multiple_choice'
  /** 选项列表 */
  options: string[]
  /** 正确答案列表（选项文本数组，仅管理端返回） */
  correctAnswers?: string[]
}

/** 算法题测试用例 */
export interface AlgorithmTestCase {
  /** 输入 */
  input: string
  /** 期望输出 */
  expectedOutput: string
}

/** 算法题内容 */
export interface AlgorithmContent extends BaseQuestionContent {
  type: 'algorithm'
  /** 测试用例（仅管理端返回） */
  testCases?: AlgorithmTestCase[]
  /** 时间限制（毫秒） */
  timeLimit?: number
  /** 内存限制（KB） */
  memoryLimit?: number
}

/** 考题内容联合类型 */
export type QuestionContent =
  | FileUploadContent
  | SingleChoiceContent
  | MultipleChoiceContent
  | AlgorithmContent

/** 创建考题请求 - 对应后端 CreateQuestionRequestDTO */
export interface CreateQuestionRequestDTO {
  /** 考核时间ID */
  assessmentTimeId: number
  /** 题号 */
  questionNo: number
  /** 题型 */
  questionType: QuestionType
  /** 题目标题 */
  title: string
  /** 题目内容（多态 JSON） */
  content?: QuestionContent | null
  /** 附件ID */
  attachmentId?: number | null
  /** 分值 */
  score: number
}

/** 更新考题请求 - 对应后端 UpdateQuestionRequestDTO */
export interface UpdateQuestionRequestDTO {
  /** 题号 */
  questionNo?: number
  /** 题型 */
  questionType?: QuestionType
  /** 题目标题 */
  title?: string
  /** 题目内容（多态 JSON） */
  content?: QuestionContent | null
  /** 附件ID */
  attachmentId?: number | null
  /** 分值 */
  score?: number
}

/** 答案信息 - 对应后端 AssessmentAnswerDTO */
export interface AssessmentAnswerDTO {
  /** 答案ID */
  id: number
  /** 题目ID */
  questionId: number
  /** 上传的文件ID */
  fileId: number | null
  /** 答案内容 */
  content: string | null
  /** 提交时间 */
  submitTime: string | null
}

/** 创建答案请求 - 对应后端 CreateAnswerRequestDTO */
export interface CreateAnswerRequestDTO {
  /** 题目ID */
  questionId: number
  /** 上传的文件ID */
  fileId?: number | null
  /** 答案内容 */
  content?: string | null
}

/** 考核会话 - 对应后端 AssessmentSessionVO */
export interface AssessmentSessionDTO {
  /** 会话ID */
  id: number
  /** 用户ID */
  userId: number
  /** 考核时间ID */
  assessmentTimeId: number
  /** 会话开始时间（ISO格式） */
  startTime: string
  /** 限时考核截止时间（ISO格式） */
  deadline: string
}

/** 用户端考题列表响应 - 包含限时截止时间 */
export interface UserQuestionListResponseDTO {
  /** 考题分页数据 */
  questions: import('./type').PageDTO<AssessmentQuestionDTO>
  /** 限时考核截止时间（ISO格式），非限时考核为null */
  deadline: string | null
}
