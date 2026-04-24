import type { UploadProps } from 'antd'
import type {
  AssessmentAnswerDTO,
  AssessmentQuestionDTO,
  AssessmentTimeDTO,
  AssessmentStatus,
  ProgrammingLanguage,
  AlgorithmTestcaseType,
  JudgeJobPollingResponseDTO,
  JudgeCaseResultDTO,
  QuestionStatisticsDTO,
} from '@/apis/schema/assessment.dto'

export type UploadPhase =
  | 'idle'
  | 'uploaded'
  | 'answered'
  | 'resubmitting'
  | 'resubmit_uploaded'
  | 'expired'

export interface UploadedFileInfo {
  id: number
  name: string
  size?: number
}

/* ---------- FileUploadArea ---------- */
export interface FileUploadAreaProps {
  uploadPhase: UploadPhase
  uploadedFile: UploadedFileInfo | null
  uploadProgress: number
  isExpired: boolean
  answer: AssessmentAnswerDTO | null
  dropHintText: string
  draggerProps: UploadProps
  onResubmit: () => void
  onRemoveFile: () => void
  onSetUploadedFile: (file: UploadedFileInfo | null) => void
}

/* ---------- ChoiceQuestion ---------- */
export interface ChoiceQuestionProps {
  question: AssessmentQuestionDTO
  isAnswered: boolean
  isResubmitting: boolean
  isExpired: boolean
  selectedOption: string | null
  selectedOptions: string[]
  onSelectOption: (option: string) => void
  onToggleOption: (option: string) => void
  onResubmit: () => void
}

/* ---------- AlgorithmQuestion ---------- */
export interface AlgorithmQuestionProps {
  question: AssessmentQuestionDTO
  answer: AssessmentAnswerDTO | null
  isExpired: boolean
  algorithmLanguage: ProgrammingLanguage | null
  algorithmCode: string
  algorithmRunMode: Exclude<AlgorithmTestcaseType, 'FORMAL'>
  customInput: string
  algorithmLanguageOptions: { value: ProgrammingLanguage; label: string }[]
  onLanguageChange: (value: ProgrammingLanguage, starterCode?: string) => void
  onCodeChange: (code: string) => void
  onRunModeChange: (mode: Exclude<AlgorithmTestcaseType, 'FORMAL'>) => void
  onCustomInputChange: (input: string) => void
}

/* ---------- JudgeResultPanel ---------- */
export interface JudgeResultPanelProps {
  judgeResult: JudgeJobPollingResponseDTO | null
  visibleCaseResults: JudgeCaseResultDTO[]
}

/* ---------- QuestionSidebar ---------- */
export interface QuestionSidebarProps {
  timeInfo: AssessmentTimeDTO | null
  question: AssessmentQuestionDTO
  questionsList: AssessmentQuestionDTO[]
  currentIndex: number
  questionStatistics: QuestionStatisticsDTO | null
  passRateText: string | null
  answer: AssessmentAnswerDTO | null
  isAnswered: boolean
  isResubmitting: boolean
  isExpired: boolean
  isFileUpload: boolean
  isChoiceQuestion: boolean
  isSingleChoice: boolean
  isAlgorithm: boolean
  uploadedFile: UploadedFileInfo | null
  selectedOption: string | null
  selectedOptions: string[]
  algorithmLanguage: ProgrammingLanguage | null
  algorithmCode: string
  pollingJobId: number | null
  pollingFormalJob: boolean
  submitting: boolean
  hasPrev: boolean
  hasNext: boolean
  onPrev: () => void
  onNext: () => void
  onSubmit: () => void
  onResubmitConfirm: () => void
  onCancelResubmit: () => void
  onAlgorithmRun: () => void
  onAlgorithmSubmit: () => void
  onRemoveFile: () => void
}

/* ---------- CountdownSection ---------- */
export interface CountdownSectionProps {
  isTimed: boolean
  deadline: string | null
  sessionStartTime?: string
  onTimeUp: () => void
  timeInfo: AssessmentTimeDTO | null
  statusInfo: { text: string; status: AssessmentStatus } | null
}
