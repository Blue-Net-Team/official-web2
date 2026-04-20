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

/** 编程语言值，和后端 ProgrammingLanguage 的 JsonValue 保持一致 */
export type ProgrammingLanguage = 'python' | 'c' | 'cpp' | 'java' | 'javascript'

/** 判题任务状态 */
export type JudgeJobStatus =
  | 'PENDING'
  | 'RUNNING'
  | 'RETRYING'
  | 'SUCCEEDED'
  | 'FAILED_REVIEW_REQUIRED'

/** 判题用例状态 */
export type JudgeCaseStatus = 'AC' | 'WA' | 'TLE' | 'RE' | 'CE' | 'MLE'

/** 算法运行用例类型 */
export type AlgorithmTestcaseType = 'DEFAULT_RUN' | 'CUSTOM_RUN' | 'FORMAL'

/** 客观题结果码 */
export type ObjectiveResultCode = 'AC' | 'WA' | 'TLE' | 'RE' | 'CE' | 'MLE'

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

/** 算法题题面样例 */
export interface AlgorithmExample {
  /** 输入 */
  input: string
  /** 期望输出 */
  expectedOutput: string
  /** 样例说明 */
  explanation?: string
}

/** 算法题测试用例 */
export interface AlgorithmTestCase {
  /** 输入 */
  input: string
  /** 期望输出 */
  expectedOutput: string
  /** 是否隐藏 */
  hidden?: boolean
  /** 权重 */
  weight?: number
}

/** 算法题内容 */
export interface AlgorithmContent extends BaseQuestionContent {
  type: 'algorithm'
  /** 输入说明 */
  inputDescription?: string
  /** 输出说明 */
  outputDescription?: string
  /** 数据范围 */
  constraints?: string
  /** 题面样例 */
  examples?: AlgorithmExample[]
  /** 默认运行测试用例 */
  runTestCases?: AlgorithmTestCase[]
  /** 正式判题测试用例（仅管理端返回） */
  testCases?: AlgorithmTestCase[]
  /** 各语言初始代码模板，key 同时决定允许提交语言 */
  starterCode?: Record<string, string>
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
  /** 算法题编程语言 */
  language: ProgrammingLanguage | null
  /** 提交时间 */
  submitTime: string | null
  /** 自动或人工评判结果 */
  judgement: AssessmentJudgementDTO | null
}

/** 创建答案请求 - 对应后端 CreateAnswerRequestDTO */
export interface CreateAnswerRequestDTO {
  /** 题目ID */
  questionId: number
  /** 上传的文件ID */
  fileId?: number | null
  /** 答案内容 */
  content?: string | null
  /** 算法题编程语言 */
  language?: ProgrammingLanguage | null
}

/** 评判结果 - 对应后端 AssessmentJudgementDTO */
export interface AssessmentJudgementDTO {
  /** 评判ID */
  id: number
  /** 答案ID */
  answerId: number
  /** 题目ID */
  questionId: number
  /** 考核时间ID */
  assessmentTimeId: number
  /** 用户ID */
  userId: number
  /** 得分 */
  score: number
  /** 满分 */
  maxScore: number
  /** 结果码 */
  resultCode: ObjectiveResultCode | null
  /** 评判状态 */
  status: string
  /** 评判来源 */
  source: string
  /** 评论 */
  comment: string | null
  /** 评判时间 */
  judgedAt: string | null
}

/** 题目提交历史评判记录 */
export interface AssessmentQuestionSubmissionHistoryDTO {
  /** 本次历史评判 */
  judgement: AssessmentJudgementDTO | null
  /** 是否为当前展示用的最佳/最新记录 */
  selectedBest: boolean
}

/** 算法运行请求 */
export interface AlgorithmRunRequestDTO {
  /** 题目ID */
  questionId: number
  /** 编程语言 */
  language: ProgrammingLanguage
  /** 源代码 */
  sourceCode: string
  /** 运行类型 */
  testcaseType?: AlgorithmTestcaseType
  /** 自定义输入 */
  customInput?: string | null
}

/** 算法提交响应 */
export interface AlgorithmSubmitResponseDTO {
  /** 答案ID */
  answerId: number | null
  /** 判题任务ID */
  judgeJobId: number
  /** 判题用例类型 */
  testcaseType: AlgorithmTestcaseType
}

/** 算法单个用例结果 */
export interface JudgeCaseResultDTO {
  /** 用例序号 */
  caseNo: number
  /** 用例类型 */
  testcaseType: AlgorithmTestcaseType
  /** 结果状态 */
  status: JudgeCaseStatus
  /** 输入 */
  input: string | null
  /** 期望输出 */
  expectedOutput: string | null
  /** 实际输出 */
  actualOutput: string | null
  /** 标准输出 */
  stdout: string | null
  /** 标准错误 */
  stderr: string | null
  /** 耗时毫秒 */
  timeUsedMs: number | null
  /** 内存KB */
  memoryUsedKb: number | null
  /** 结果说明 */
  message: string | null
}

/** 算法判题轮询响应 */
export interface JudgeJobPollingResponseDTO {
  /** 判题任务ID */
  judgeJobId: number
  /** 判题用例类型 */
  testcaseType: AlgorithmTestcaseType
  /** 任务状态 */
  status: JudgeJobStatus
  /** 状态说明 */
  statusMessage: string | null
  /** 用例结果 */
  caseResults: JudgeCaseResultDTO[]
  /** 正式提交评判结果 */
  judgement: AssessmentJudgementDTO | null
}

/** 文件上传题人工评分请求 */
export interface ManualReviewRequestDTO {
  /** 答案ID */
  answerId: number
  /** 得分 */
  score: number
  /** 评语 */
  comment?: string | null
}

/** 考核最终通过决策请求 */
export interface AssessmentDecisionRequestDTO {
  /** 考生用户ID */
  userId: number
  /** 考核时间ID */
  assessmentTimeId: number
  /** 是否通过 */
  passed: boolean
  /** 决策备注 */
  decisionComment?: string | null
}

/** 考核最终通过决策结果 */
export interface AssessmentDecisionDTO extends AssessmentDecisionRequestDTO {
  /** 决策ID */
  id: number
  /** 决策人ID */
  decidedBy: number
  /** 决策时间 */
  decidedAt: string | null
}

/** 题目评分汇总 */
export interface AssessmentQuestionScoreboardDTO {
  /** 题目ID */
  questionId: number
  /** 考核时间ID */
  assessmentTimeId: number
  /** 题号 */
  questionNo: number
  /** 题型 */
  questionType: QuestionType
  /** 题目标题 */
  title: string
  /** 满分 */
  maxScore: number
  /** 提交数 */
  submittedCount: number
  /** 已评分数 */
  judgedCount: number
  /** 待评分数 */
  pendingCount: number
  /** 平均分 */
  averageScore: number
}

/** 题目提交评分行 */
export interface AssessmentQuestionSubmissionDTO {
  /** 答案ID */
  answerId: number
  /** 题目ID */
  questionId: number
  /** 考核时间ID */
  assessmentTimeId: number
  /** 题号 */
  questionNo: number
  /** 题目标题 */
  questionTitle: string
  /** 题型 */
  questionType: QuestionType
  /** 题目满分 */
  maxScore: number
  /** 考生用户ID */
  candidateUserId: number
  /** 考生学号 */
  studentId: string
  /** 考生姓名 */
  username: string
  /** 考生昵称 */
  nickname: string | null
  /** 上传文件ID */
  fileId: number | null
  /** 答案内容 */
  content: string | null
  /** 算法题提交语言 */
  language: ProgrammingLanguage | null
  /** 提交时间 */
  submitTime: string | null
  /** 最新评判 */
  latestJudgement: AssessmentJudgementDTO | null
  /** 历史评判记录，算法题用于展开查看所有提交评判 */
  histories: AssessmentQuestionSubmissionHistoryDTO[]
}

/** 考生单题评分状态 */
export interface AssessmentCandidateQuestionScoreDTO {
  /** 题目ID */
  questionId: number
  /** 题号 */
  questionNo: number
  /** 题目标题 */
  questionTitle: string
  /** 题型 */
  questionType: QuestionType
  /** 题目满分 */
  maxScore: number
  /** 答案ID */
  answerId: number | null
  /** 是否已提交 */
  submitted: boolean
  /** 提交时间 */
  submitTime: string | null
  /** 最新得分 */
  score: number | null
  /** 是否已评分 */
  judged: boolean
  /** 最新评判 */
  latestJudgement: AssessmentJudgementDTO | null
}

/** 考生评分汇总 */
export interface AssessmentCandidateScoreboardDTO {
  /** 考生用户ID */
  candidateUserId: number
  /** 考生学号 */
  studentId: string
  /** 考生姓名 */
  username: string
  /** 考生昵称 */
  nickname: string | null
  /** 总得分 */
  totalScore: number
  /** 总满分 */
  maxScore: number
  /** 已评分题数 */
  judgedQuestionCount: number
  /** 待评分题数 */
  pendingJudgementCount: number
  /** 各题评分状态 */
  questionScores: AssessmentCandidateQuestionScoreDTO[]
}

/** 录用决策统计 */
export interface AssessmentDecisionStatisticsDTO {
  /** 候选人数 */
  candidates: number
  /** 待决策人数 */
  pending: number
  /** 通过人数 */
  passed: number
  /** 淘汰人数 */
  eliminated: number
}

/** 录用决策候选人 */
export interface AssessmentDecisionCandidateDTO extends AssessmentCandidateScoreboardDTO {
  /** 决策ID */
  decisionId: number | null
  /** 是否通过，null表示待决策 */
  passed: boolean | null
  /** 决策备注 */
  decisionComment: string | null
  /** 决策人ID */
  decidedBy: number | null
  /** 决策时间 */
  decidedAt: string | null
}

/** 录用决策工作台 */
export interface AssessmentDecisionWorkspaceDTO {
  /** 统计数据 */
  statistics: AssessmentDecisionStatisticsDTO
  /** 候选人列表 */
  candidates: AssessmentDecisionCandidateDTO[]
}

/** 题目统计结果 */
export interface QuestionStatisticsDTO {
  /** 题目ID */
  questionId: number
  /** 题型 */
  questionType: QuestionType
  /** 提交人数 */
  submittedCount: number
  /** 通过人数 */
  acceptedCount: number
  /** 通过率 */
  passRate: number
  /** 结果码分布 */
  resultDistribution: Partial<Record<ObjectiveResultCode, number>>
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
