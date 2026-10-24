import type {
  AlgorithmContent,
  BaseQuestionContent,
  QuestionType,
} from '@/apis/schema/assessment.dto'
import type { PreviewStatement } from '@/components/Assessment/QuestionStatement/QuestionStatementPreview'

/**
 * 管理端表单值中与题面相关的字段。
 *
 * 刻意不引用 QuestionDrawer 内部的 FormValues 类型：预览层只认这一小组字段，
 * 表单新增判题配置类字段时不会被自动带进预览。
 */
export interface PreviewSourceValues {
  questionType: QuestionType
  title?: string
  fileContent?: string
  choiceContent?: string
  options?: string[]
  algorithmContent?: string
  inputDescription?: string
  outputDescription?: string
  constraints?: string
  examples?: { input: string; expectedOutput: string; explanation?: string }[]
}

/**
 * 把管理端扁平的表单值转成预览所需的题面数据。
 *
 * **白名单式构造**：逐个挑出预览要用的字段，而不是从表单值里删黑名单。
 * 这样 `correctAnswer` / `correctAnswers` / `runTestCases` / `starterCode` /
 * `testCases` / `timeLimit` / `memoryLimit` 在结构上就没有进入预览的路径，
 * 将来给表单加字段时也不会意外泄漏。
 */
export function buildPreviewStatement(values: PreviewSourceValues): PreviewStatement {
  const title = (values.title || '').trim()

  switch (values.questionType) {
    case 'FILE_UPLOAD':
      return {
        questionType: values.questionType,
        title,
        content: { type: 'file_upload', content: values.fileContent || '' },
      }

    case 'SINGLE_CHOICE':
      return {
        questionType: values.questionType,
        title,
        content: {
          type: 'single_choice',
          content: values.choiceContent || '',
          options: values.options || [],
        } as BaseQuestionContent,
      }

    case 'MULTIPLE_CHOICE':
      return {
        questionType: values.questionType,
        title,
        content: {
          type: 'multiple_choice',
          content: values.choiceContent || '',
          options: values.options || [],
        } as BaseQuestionContent,
      }

    case 'ALGORITHM':
      return {
        questionType: values.questionType,
        title,
        content: {
          type: 'algorithm',
          content: values.algorithmContent || '',
          inputDescription: values.inputDescription || '',
          outputDescription: values.outputDescription || '',
          constraints: values.constraints || '',
          examples: values.examples || [],
        } as AlgorithmContent,
      }

    default:
      return { questionType: values.questionType, title, content: null }
  }
}
