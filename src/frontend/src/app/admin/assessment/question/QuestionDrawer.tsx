'use client'

import { useEffect, useMemo, useState } from 'react'
import {
  Alert,
  App,
  Button,
  Checkbox,
  Drawer,
  Form,
  Grid,
  Input,
  InputNumber,
  Radio,
  Select,
  Space,
  Tabs,
  Tag,
  Upload,
} from 'antd'
import {
  MinusCircleOutlined,
  PaperClipOutlined,
  PlusOutlined,
  ReloadOutlined,
} from '@ant-design/icons'
import type {
  AlgorithmTestCase,
  AssessmentQuestionDTO,
  CreateQuestionRequestDTO,
  JudgeProblemConfigDTO,
  ProgrammingLanguage,
  QuestionContent,
  QuestionType,
  UpdateQuestionRequestDTO,
  UpsertJudgeProblemConfigRequestDTO,
} from '@/apis/schema/assessment.dto'
import { adminAssessmentQuestionService } from '@/apis/services/admin-assessment-question.service'
import { adminJudgeProblemConfigService } from '@/apis/services/admin-judge-problem-config.service'
import { usePresignedUpload } from '@/hooks/usePresignedUpload'
import { MarkdownRenderer, QuestionStemMarkdownEditor } from '@/components/Assessment'
import CodeEditor from '@/components/CodeEditor'
import type { CodeEditorProps } from '@/components/CodeEditor'
import { QUESTION_TYPE_LABELS } from '@/app/admin/assessment/judge/shared'

export type DrawerMode = 'view' | 'edit' | 'create'
export { QUESTION_TYPE_LABELS }

interface FormListCodeEditorProps extends Omit<CodeEditorProps, 'language'> {
  languageFieldPath: (string | number)[]
}

function FormListCodeEditor({ languageFieldPath, ...rest }: FormListCodeEditorProps) {
  const language = Form.useWatch(languageFieldPath)
  return <CodeEditor {...rest} language={language} />
}

interface QuestionDrawerProps {
  open: boolean
  question: AssessmentQuestionDTO | null
  assessmentTimeId: number | null
  mode: DrawerMode
  onClose: () => void
  onSuccess: () => void
  onDelete: (item: AssessmentQuestionDTO) => void
  onEdit: () => void
}

interface JudgeStandardSolutionForm {
  language: ProgrammingLanguage
  source: string
  primarySolution?: boolean
}

interface JudgeTestcaseConfigForm {
  caseNo: number
  category: string
  generatorArgs: string
  weight: number
  hidden?: boolean
  sample?: boolean
  description?: string
}

interface FormValues {
  questionNo: number
  questionType: QuestionType
  title: string
  score: number
  attachmentId: number | null
  // File upload content
  fileContent: string
  // Single/Multiple choice content
  choiceContent: string
  options: string[]
  correctAnswer: number
  correctAnswers: number[]
  // Algorithm statement content
  algorithmContent: string
  inputDescription: string
  outputDescription: string
  constraints: string
  examples: { input: string; expectedOutput: string; explanation?: string }[]
  runTestCases: AlgorithmTestCase[]
  starterCodeTemplates: { language: ProgrammingLanguage; code: string }[]
  // Algorithm judge configuration
  generatorLanguage: ProgrammingLanguage
  generatorSource: string
  primaryStandardLanguage: ProgrammingLanguage
  benchmarkRepeatTimes: number
  marginMultiplier: number
  minExtraMs: number
  roundToMs: number
  standardSolutions: JudgeStandardSolutionForm[]
  testcases: JudgeTestcaseConfigForm[]
  confirmLanguage: ProgrammingLanguage
  confirmTimeLimitMs: number | null
  confirmMemoryLimitKb: number | null
  confirmOutputLimitKb: number | null
}

/** QuestionType enum → 后端 Jackson @JsonSubTypes 小写 name */
const CONTENT_TYPE_MAP: Record<QuestionType, string> = {
  FILE_UPLOAD: 'file_upload',
  SINGLE_CHOICE: 'single_choice',
  MULTIPLE_CHOICE: 'multiple_choice',
  ALGORITHM: 'algorithm',
}

const QUESTION_TYPE_OPTIONS: { value: QuestionType; label: string }[] = [
  { value: 'FILE_UPLOAD', label: '文件上传' },
  { value: 'SINGLE_CHOICE', label: '单选题' },
  { value: 'MULTIPLE_CHOICE', label: '多选题' },
  { value: 'ALGORITHM', label: '算法题' },
]

const PROGRAMMING_LANGUAGE_OPTIONS: { value: ProgrammingLanguage; label: string }[] = [
  { value: 'python', label: 'Python' },
  { value: 'c', label: 'C' },
  { value: 'cpp', label: 'C++' },
  { value: 'java', label: 'Java' },
  { value: 'javascript', label: 'JavaScript' },
]

const TESTCASE_CATEGORY_OPTIONS = [
  { value: 'SAMPLE', label: '样例' },
  { value: 'NORMAL', label: '常规' },
  { value: 'EDGE', label: '边界' },
  { value: 'EMPTY', label: '空数据' },
  { value: 'MINIMUM', label: '最小规模' },
  { value: 'MAXIMUM', label: '最大规模' },
  { value: 'LARGE', label: '长数据' },
  { value: 'RANDOM', label: '随机' },
  { value: 'WORST_CASE', label: '最坏情况' },
  { value: 'SPECIAL', label: '特殊构造' },
  { value: 'REGRESSION', label: '回归' },
]

const JUDGE_STATUS_LABELS: Record<string, { text: string; color: string }> = {
  DRAFT: { text: '草稿', color: 'default' },
  GENERATING: { text: '生成中', color: 'processing' },
  GENERATED: { text: '已生成', color: 'blue' },
  BENCHMARKING: { text: '测速中', color: 'processing' },
  READY: { text: '可判题', color: 'success' },
  FAILED: { text: '失败', color: 'error' },
}

const DEFAULT_FORM_VALUES: FormValues = {
  questionNo: 0,
  questionType: 'FILE_UPLOAD',
  title: '',
  score: 0,
  attachmentId: null,
  fileContent: '',
  choiceContent: '',
  options: [''],
  correctAnswer: -1,
  correctAnswers: [],
  algorithmContent: '',
  inputDescription: '',
  outputDescription: '',
  constraints: '',
  examples: [],
  runTestCases: [],
  starterCodeTemplates: [],
  generatorLanguage: 'python',
  generatorSource: '',
  primaryStandardLanguage: 'python',
  benchmarkRepeatTimes: 5,
  marginMultiplier: 1.2,
  minExtraMs: 50,
  roundToMs: 50,
  standardSolutions: [],
  testcases: [],
  confirmLanguage: 'python',
  confirmTimeLimitMs: null,
  confirmMemoryLimitKb: null,
  confirmOutputLimitKb: 1024,
}

const DEFAULT_GENERATOR_ARGS = '{\n  "n": 10\n}'

/** 表单用数组编辑语言模板，提交给后端时需要合并为 starterCode map。 */
function buildStarterCodeMap(templates: FormValues['starterCodeTemplates']) {
  return (templates || []).reduce<Record<string, string>>((acc, item) => {
    const language = item.language?.trim()
    if (language && item.code?.trim()) {
      acc[language] = item.code
    }
    return acc
  }, {})
}

/** 后端 starterCode map 转成 Form.List 可编辑的语言模板数组。 */
function parseStarterCodeTemplates(starterCode: unknown): FormValues['starterCodeTemplates'] {
  if (!starterCode || typeof starterCode !== 'object') return []

  return Object.entries(starterCode as Record<string, string>).map(([language, code]) => ({
    language: language as ProgrammingLanguage,
    code,
  }))
}

/** correctAnswer 在表单中存的是选项索引(number)，提交时转为选项文本。 */
function buildContentFromForm(values: FormValues): QuestionContent | null {
  const type = CONTENT_TYPE_MAP[values.questionType]
  switch (values.questionType) {
    case 'FILE_UPLOAD':
      return { type, content: values.fileContent || '' } as QuestionContent
    case 'SINGLE_CHOICE':
      return {
        type,
        content: values.choiceContent || '',
        options: values.options || [],
        correctAnswer: values.options?.[values.correctAnswer] ?? '',
      } as QuestionContent
    case 'MULTIPLE_CHOICE':
      return {
        type,
        content: values.choiceContent || '',
        options: values.options || [],
        correctAnswers: (values.correctAnswers ?? [])
          .map((i) => values.options?.[i])
          .filter(Boolean) as string[],
      } as QuestionContent
    case 'ALGORITHM':
      return {
        type,
        content: values.algorithmContent || '',
        inputDescription: values.inputDescription || '',
        outputDescription: values.outputDescription || '',
        constraints: values.constraints || '',
        examples: values.examples || [],
        runTestCases: values.runTestCases || [],
        starterCode: buildStarterCodeMap(values.starterCodeTemplates),
      } as QuestionContent
    default:
      return null
  }
}

/** 后端 correctAnswer 是选项文本，转为表单用的索引。 */
function parseContentToForm(content: unknown, questionType: QuestionType): Partial<FormValues> {
  if (!content || typeof content !== 'object') return {}

  const c = content as Record<string, unknown>
  switch (questionType) {
    case 'FILE_UPLOAD':
      return { fileContent: (c.content as string) || '' }
    case 'SINGLE_CHOICE': {
      const options = (c.options as string[]) || ['']
      const correctText = c.correctAnswer as string
      const correctIndex = correctText ? options.indexOf(correctText) : -1
      return {
        choiceContent: (c.content as string) || '',
        options,
        correctAnswer: correctIndex >= 0 ? correctIndex : -1,
      }
    }
    case 'MULTIPLE_CHOICE': {
      const options = (c.options as string[]) || ['']
      const correctTexts = (c.correctAnswers as string[]) || []
      const correctIndices = correctTexts
        .map((text: string) => options.indexOf(text))
        .filter((i: number) => i >= 0)
      return {
        choiceContent: (c.content as string) || '',
        options,
        correctAnswers: correctIndices,
      }
    }
    case 'ALGORITHM':
      return {
        algorithmContent: (c.content as string) || '',
        inputDescription: (c.inputDescription as string) || '',
        outputDescription: (c.outputDescription as string) || '',
        constraints: (c.constraints as string) || '',
        examples: (c.examples as FormValues['examples']) || [],
        runTestCases: (c.runTestCases as AlgorithmTestCase[]) || [],
        starterCodeTemplates: parseStarterCodeTemplates(c.starterCode),
      }
    default:
      return {}
  }
}

/** 将接口返回的 JSON 参数转成可编辑的格式化 JSON 字符串。 */
function stringifyJsonForForm(value: unknown) {
  if (value === null || value === undefined) return '{}'
  try {
    return JSON.stringify(value, null, 2)
  } catch {
    return '{}'
  }
}

/** 判题配置元数据回填；源码随 DTO 返回。 */
function parseJudgeConfigToForm(config: JudgeProblemConfigDTO): Partial<FormValues> {
  return {
    generatorLanguage: config.generatorLanguage,
    generatorSource: config.generatorSource || '',
    primaryStandardLanguage: config.primaryStandardLanguage,
    benchmarkRepeatTimes: config.benchmarkRepeatTimes,
    marginMultiplier: config.marginMultiplier,
    minExtraMs: config.minExtraMs,
    roundToMs: config.roundToMs,
    standardSolutions: config.standardSolutions.map((solution) => ({
      language: solution.language,
      source: solution.source || '',
      primarySolution: solution.primarySolution,
    })),
    testcases: config.testcases.map((testcase) => ({
      caseNo: testcase.caseNo,
      category: testcase.category,
      generatorArgs: stringifyJsonForForm(testcase.generatorArgs),
      weight: testcase.weight,
      hidden: testcase.hidden,
      sample: testcase.sample,
      description: testcase.description || '',
    })),
  }
}

/** 判断本次保存是否包含可提交的新判题源码配置。 */
function shouldUpsertJudgeConfig(values: FormValues) {
  return Boolean(
    values.generatorSource?.trim() ||
    (values.standardSolutions || []).some((solution) => solution.source?.trim())
  )
}

/** 构建判题配置请求，并校验 generatorArgs 是合法 JSON。 */
function buildJudgeConfigRequest(values: FormValues): UpsertJudgeProblemConfigRequestDTO {
  const testcases = (values.testcases || []).map((testcase, index) => {
    let generatorArgs: unknown = {}
    try {
      generatorArgs = testcase.generatorArgs?.trim() ? JSON.parse(testcase.generatorArgs) : {}
    } catch {
      throw new Error(`第 ${index + 1} 个测试用例的 generator 参数不是合法 JSON`)
    }

    return {
      caseNo: testcase.caseNo,
      category: testcase.category,
      generatorArgs,
      weight: testcase.weight,
      hidden: testcase.hidden ?? true,
      sample: testcase.sample ?? false,
      description: testcase.description || null,
    }
  })

  return {
    generatorLanguage: values.generatorLanguage,
    generatorSource: values.generatorSource,
    primaryStandardLanguage: values.primaryStandardLanguage,
    benchmarkRepeatTimes: values.benchmarkRepeatTimes,
    marginMultiplier: values.marginMultiplier,
    minExtraMs: values.minExtraMs,
    roundToMs: values.roundToMs,
    standardSolutions: (values.standardSolutions || []).map((solution) => ({
      language: solution.language,
      source: solution.source,
      primarySolution: solution.language === values.primaryStandardLanguage,
    })),
    testcases,
  }
}

/** 状态标签渲染。 */
function renderJudgeStatus(status?: string | null) {
  if (!status) return <Tag>未配置</Tag>
  const option = JUDGE_STATUS_LABELS[status] || { text: status, color: 'default' }
  return <Tag color={option.color}>{option.text}</Tag>
}

export default function QuestionDrawer({
  open,
  question,
  assessmentTimeId,
  mode,
  onClose,
  onSuccess,
  onDelete,
  onEdit,
}: QuestionDrawerProps) {
  const { message: messageApi } = App.useApp()
  const [form] = Form.useForm<FormValues>()
  const screens = Grid.useBreakpoint()
  const [judgeConfig, setJudgeConfig] = useState<JudgeProblemConfigDTO | null>(null)
  const [judgeConfigLoading, setJudgeConfigLoading] = useState(false)

  const isViewMode = mode === 'view'
  const isCreateMode = mode === 'create'
  const questionTypeValue = Form.useWatch('questionType', form)
  const optionsValue = Form.useWatch('options', form)
  const standardSolutionsValue = Form.useWatch('standardSolutions', form)
  const generatorLanguage = Form.useWatch('generatorLanguage', form)

  // Populate form on open.
  useEffect(() => {
    if (!open) return
    setJudgeConfig(null)

    if (isCreateMode) {
      form.setFieldsValue(DEFAULT_FORM_VALUES)
    } else if (question) {
      const contentFields = parseContentToForm(question.content, question.questionType)
      form.setFieldsValue({
        ...DEFAULT_FORM_VALUES,
        questionNo: question.questionNo,
        questionType: question.questionType,
        title: question.title,
        score: question.score,
        attachmentId: question.attachmentId,
        ...contentFields,
      })
    }
  }, [open, question, form, isCreateMode])

  // Load algorithm judge config after the base question has been loaded.
  useEffect(() => {
    if (!open || !question || question.questionType !== 'ALGORITHM') return

    let cancelled = false
    setJudgeConfigLoading(true)
    adminJudgeProblemConfigService
      .get(question.id)
      .then((res) => {
        if (cancelled) return
        if (res.code >= 400 || !res.data) {
          setJudgeConfig(null)
          return
        }
        setJudgeConfig(res.data)
        form.setFieldsValue(parseJudgeConfigToForm(res.data))
      })
      .catch(() => {
        if (!cancelled) setJudgeConfig(null)
      })
      .finally(() => {
        if (!cancelled) setJudgeConfigLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [open, question, form])

  // Reset content fields when type changes.
  useEffect(() => {
    if (!open || isViewMode) return
    const currentType = form.getFieldValue('questionType')
    if (currentType && questionTypeValue && currentType !== questionTypeValue) {
      setJudgeConfig(null)
      form.setFieldsValue({
        fileContent: '',
        choiceContent: '',
        options: [''],
        correctAnswer: -1,
        correctAnswers: [],
        algorithmContent: '',
        inputDescription: '',
        outputDescription: '',
        constraints: '',
        examples: [],
        runTestCases: [],
        starterCodeTemplates: [],
        generatorLanguage: 'python',
        generatorSource: '',
        primaryStandardLanguage: 'python',
        benchmarkRepeatTimes: 5,
        marginMultiplier: 1.2,
        minExtraMs: 50,
        roundToMs: 50,
        standardSolutions: [],
        testcases: [],
      })
    }
  }, [questionTypeValue, form, open, isViewMode])

  const refreshJudgeConfig = async (questionId: number) => {
    setJudgeConfigLoading(true)
    try {
      const res = await adminJudgeProblemConfigService.get(questionId)
      if (res.code >= 400 || !res.data) {
        setJudgeConfig(null)
        return
      }
      setJudgeConfig(res.data)
      form.setFieldsValue(parseJudgeConfigToForm(res.data))
    } finally {
      setJudgeConfigLoading(false)
    }
  }

  const saveQuestion = async (values: FormValues) => {
    if (!assessmentTimeId) return null
    const content = buildContentFromForm(values)

    if (isCreateMode) {
      const payload: CreateQuestionRequestDTO = {
        assessmentTimeId,
        questionNo: values.questionNo,
        questionType: values.questionType,
        title: values.title,
        score: values.score,
        content,
        attachmentId: values.attachmentId || null,
      }
      const res = await adminAssessmentQuestionService.create(payload)
      return res.data || null
    }

    if (!question) return null
    const payload: UpdateQuestionRequestDTO = {
      questionNo: values.questionNo,
      questionType: values.questionType,
      title: values.title,
      score: values.score,
      content,
      attachmentId: values.attachmentId || null,
    }
    const res = await adminAssessmentQuestionService.update(question.id, payload)
    return res.data || question
  }

  const saveJudgeConfigIfNeeded = async (
    savedQuestion: AssessmentQuestionDTO,
    values: FormValues,
    requireConfig: boolean
  ) => {
    if (values.questionType !== 'ALGORITHM') return

    if (!shouldUpsertJudgeConfig(values)) {
      if (requireConfig && !judgeConfig) {
        throw new Error('请先填写 generator 源码和标准解源码，再生成测试数据')
      }
      return
    }

    const payload = buildJudgeConfigRequest(values)
    const res = await adminJudgeProblemConfigService.upsert(savedQuestion.id, payload)
    if (res.data) {
      setJudgeConfig(res.data)
      form.setFieldsValue(parseJudgeConfigToForm(res.data))
    }
  }

  const handleSubmit = async (generateAfterSave = false) => {
    try {
      const values = await form.validateFields()
      const savedQuestion = await saveQuestion(values)
      if (!savedQuestion) return

      await saveJudgeConfigIfNeeded(savedQuestion, values, generateAfterSave)
      if (generateAfterSave && values.questionType === 'ALGORITHM') {
        await adminJudgeProblemConfigService.requestGeneration(savedQuestion.id)
        await refreshJudgeConfig(savedQuestion.id)
        messageApi.success('已保存并提交测试数据生成任务')
      } else {
        messageApi.success(isCreateMode ? '创建成功' : '更新成功')
      }
      onSuccess()
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'errorFields' in err) return
      const msg =
        err instanceof Error
          ? err.message
          : (err as { response?: { data?: { msg?: string } } })?.response?.data?.msg
      messageApi.error(msg || '操作失败')
    }
  }

  const handleConfirmLanguageLimit = async () => {
    if (!question) return
    try {
      const values = await form.validateFields([
        'confirmLanguage',
        'confirmTimeLimitMs',
        'confirmMemoryLimitKb',
        'confirmOutputLimitKb',
      ])
      if (
        values.confirmTimeLimitMs == null ||
        values.confirmMemoryLimitKb == null ||
        values.confirmOutputLimitKb == null
      ) {
        throw new Error('请填写完整的资源限制')
      }
      await adminJudgeProblemConfigService.confirmLanguageLimit(
        question.id,
        values.confirmLanguage,
        {
          timeLimitMs: values.confirmTimeLimitMs,
          memoryLimitKb: values.confirmMemoryLimitKb,
          outputLimitKb: values.confirmOutputLimitKb,
        }
      )
      messageApi.success('资源限制已确认')
      await refreshJudgeConfig(question.id)
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'errorFields' in err) return
      const msg = (err as { response?: { data?: { msg?: string } } })?.response?.data?.msg
      messageApi.error(msg || '确认失败')
    }
  }

  const { upload } = usePresignedUpload()

  const handleUpload = async (file: File) => {
    try {
      const fileId = await upload(file, 'ASSESSMENT_ATTACHMENT')
      if (fileId != null) {
        form.setFieldsValue({ attachmentId: fileId })
        messageApi.success('附件上传成功')
      }
    } catch {
      messageApi.error('附件上传失败')
    }
    return false
  }

  const drawerTitle = useMemo(() => {
    if (isCreateMode) return '新增考题'
    if (mode === 'edit') return '编辑考题'
    return '考题详情'
  }, [mode, isCreateMode])

  const drawerWidth = !screens.md ? '100vw' : isViewMode ? 500 : 'min(1120px, calc(100vw - 48px))'

  const renderStemField = (
    name: 'fileContent' | 'choiceContent' | 'algorithmContent',
    rows: number,
    placeholder: string
  ) =>
    isViewMode ? (
      <Form.Item label="题干" shouldUpdate>
        {({ getFieldValue }) => <MarkdownRenderer content={getFieldValue(name) as string} />}
      </Form.Item>
    ) : (
      <Form.Item name={name} label="题干">
        <QuestionStemMarkdownEditor rows={rows} placeholder={placeholder} />
      </Form.Item>
    )

  const renderAlgorithmTestCases = (
    name: 'examples' | 'runTestCases',
    label: string,
    buttonText: string,
    includeExplanation = false
  ) => (
    <Form.List name={name}>
      {(fields, { add, remove }) => (
        <>
          <div className="flex items-center justify-between mb-2">
            <span className="text-white/50 text-sm">{label}</span>
            {!isViewMode && (
              <Button
                type="dashed"
                size="small"
                onClick={() => add({ input: '', expectedOutput: '' })}
                icon={<PlusOutlined />}
              >
                {buttonText}
              </Button>
            )}
          </div>
          {fields.map((field) => (
            <div key={field.key} className="mb-3">
              <div className="flex gap-2">
                <Form.Item
                  name={[field.name, 'input']}
                  label="输入"
                  rules={[{ required: true, message: '请输入输入' }]}
                  className="flex-1 mb-0"
                >
                  <Input.TextArea rows={2} placeholder="输入" disabled={isViewMode} />
                </Form.Item>
                <Form.Item
                  name={[field.name, 'expectedOutput']}
                  label="期望输出"
                  rules={[{ required: true, message: '请输入期望输出' }]}
                  className="flex-1 mb-0"
                >
                  <Input.TextArea rows={2} placeholder="期望输出" disabled={isViewMode} />
                </Form.Item>
                {!isViewMode && (
                  <Button
                    type="text"
                    danger
                    icon={<MinusCircleOutlined />}
                    onClick={() => remove(field.name)}
                    className="mt-7"
                  />
                )}
              </div>
              {includeExplanation && (
                <Form.Item name={[field.name, 'explanation']} label="样例说明" className="mt-2">
                  <Input.TextArea rows={2} placeholder="可选，支持普通文本" disabled={isViewMode} />
                </Form.Item>
              )}
            </div>
          ))}
        </>
      )}
    </Form.List>
  )

  const renderAlgorithmStatementFields = () => (
    <>
      {renderStemField('algorithmContent', 10, '输入题目描述，支持 Markdown')}
      <Form.Item name="inputDescription" label="输入说明">
        <Input.TextArea rows={3} placeholder="描述标准输入格式" disabled={isViewMode} />
      </Form.Item>
      <Form.Item name="outputDescription" label="输出说明">
        <Input.TextArea rows={3} placeholder="描述标准输出格式" disabled={isViewMode} />
      </Form.Item>
      <Form.Item name="constraints" label="数据范围">
        <Input.TextArea rows={3} placeholder="描述数据范围和约束" disabled={isViewMode} />
      </Form.Item>
      {renderAlgorithmTestCases('examples', '题面样例', '添加样例', true)}
      {renderAlgorithmTestCases('runTestCases', '默认运行用例', '添加默认运行用例')}
      <Form.List name="starterCodeTemplates">
        {(fields, { add, remove }) => (
          <>
            <div className="flex items-center justify-between mb-2">
              <span className="text-white/50 text-sm">语言模板</span>
              {!isViewMode && (
                <Button
                  type="dashed"
                  size="small"
                  onClick={() => add({ language: 'python', code: '' })}
                  icon={<PlusOutlined />}
                >
                  添加语言模板
                </Button>
              )}
            </div>
            {fields.map((field) => (
              <div key={field.key} className="mb-3">
                <div className="flex gap-2 items-start">
                  <Form.Item
                    name={[field.name, 'language']}
                    label="语言"
                    rules={[{ required: true, message: '请选择语言' }]}
                    className="w-44 mb-0"
                  >
                    <Select options={PROGRAMMING_LANGUAGE_OPTIONS} disabled={isViewMode} />
                  </Form.Item>
                  <Form.Item
                    name={[field.name, 'code']}
                    label="模板代码"
                    rules={[{ required: true, message: '请输入模板代码' }]}
                    className="flex-1 mb-0"
                  >
                    <FormListCodeEditor
                      languageFieldPath={['starterCodeTemplates', field.name, 'language']}
                      readOnly={isViewMode}
                      height={260}
                      placeholder="输入该语言的初始代码"
                    />
                  </Form.Item>
                  {!isViewMode && (
                    <Button
                      type="text"
                      danger
                      icon={<MinusCircleOutlined />}
                      onClick={() => remove(field.name)}
                      className="mt-7"
                    />
                  )}
                </div>
              </div>
            ))}
          </>
        )}
      </Form.List>
    </>
  )

  const renderJudgeConfigFields = () => (
    <>
      <div className="mb-4 flex items-center gap-2">
        <span className="text-white/50 text-sm">配置状态</span>
        {renderJudgeStatus(judgeConfig?.status)}
        {question && (
          <Button
            size="small"
            icon={<ReloadOutlined />}
            loading={judgeConfigLoading}
            onClick={() => refreshJudgeConfig(question.id)}
          >
            刷新
          </Button>
        )}
      </div>
      {judgeConfig && !isViewMode && (
        <Alert
          className="mb-4"
          type="info"
          showIcon
          message="已有判题配置已回显源码；直接修改 generator 或标准解后保存即可替换。"
        />
      )}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Form.Item
          name="generatorLanguage"
          label="Generator 语言"
          rules={[{ required: true, message: '请选择 generator 语言' }]}
        >
          <Select options={PROGRAMMING_LANGUAGE_OPTIONS} disabled={isViewMode} />
        </Form.Item>
        <Form.Item
          name="primaryStandardLanguage"
          label="主标准解语言"
          rules={[{ required: true, message: '请选择主标准解语言' }]}
        >
          <Select options={PROGRAMMING_LANGUAGE_OPTIONS} disabled={isViewMode} />
        </Form.Item>
      </div>
      <Form.Item name="generatorSource" label="Generator 源码">
        <CodeEditor
          language={generatorLanguage}
          readOnly={isViewMode}
          height={300}
          placeholder="输入用于生成测试数据的 generator 源码"
        />
      </Form.Item>
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Form.Item
          name="benchmarkRepeatTimes"
          label="测速次数"
          rules={[{ required: true, message: '请输入测速次数' }]}
        >
          <InputNumber min={1} className="w-full" disabled={isViewMode} />
        </Form.Item>
        <Form.Item
          name="marginMultiplier"
          label="限时倍率"
          rules={[{ required: true, message: '请输入限时倍率' }]}
        >
          <InputNumber min={1} step={0.1} className="w-full" disabled={isViewMode} />
        </Form.Item>
        <Form.Item
          name="minExtraMs"
          label="最小额外毫秒"
          rules={[{ required: true, message: '请输入最小额外毫秒' }]}
        >
          <InputNumber min={0} className="w-full" disabled={isViewMode} />
        </Form.Item>
        <Form.Item
          name="roundToMs"
          label="取整粒度(ms)"
          rules={[{ required: true, message: '请输入取整粒度' }]}
        >
          <InputNumber min={1} className="w-full" disabled={isViewMode} />
        </Form.Item>
      </div>
      <Form.List name="standardSolutions">
        {(fields, { add, remove }) => (
          <>
            <div className="flex items-center justify-between mb-2">
              <span className="text-white/50 text-sm">标准解源码</span>
              {!isViewMode && (
                <Button
                  type="dashed"
                  size="small"
                  onClick={() => add({ language: 'python', source: '', primarySolution: false })}
                  icon={<PlusOutlined />}
                >
                  添加标准解
                </Button>
              )}
            </div>
            {fields.map((field) => (
              <div key={field.key} className="mb-3">
                <div className="flex gap-2 items-start">
                  <Form.Item
                    name={[field.name, 'language']}
                    label="语言"
                    rules={[{ required: true, message: '请选择语言' }]}
                    className="w-44 mb-0"
                  >
                    <Select options={PROGRAMMING_LANGUAGE_OPTIONS} disabled={isViewMode} />
                  </Form.Item>
                  <Form.Item
                    name={[field.name, 'source']}
                    label="标准解源码"
                    className="flex-1 mb-0"
                  >
                    <FormListCodeEditor
                      languageFieldPath={['standardSolutions', field.name, 'language']}
                      readOnly={isViewMode}
                      height={260}
                      placeholder="输入该语言标准解源码"
                    />
                  </Form.Item>
                  {!isViewMode && (
                    <Button
                      type="text"
                      danger
                      icon={<MinusCircleOutlined />}
                      onClick={() => remove(field.name)}
                      className="mt-7"
                    />
                  )}
                </div>
              </div>
            ))}
          </>
        )}
      </Form.List>
      <Form.List name="testcases">
        {(fields, { add, remove }) => (
          <>
            <div className="flex items-center justify-between mb-2">
              <span className="text-white/50 text-sm">测试用例生成配置</span>
              {!isViewMode && (
                <Button
                  type="dashed"
                  size="small"
                  onClick={() =>
                    add({
                      caseNo: fields.length + 1,
                      category: 'NORMAL',
                      generatorArgs: DEFAULT_GENERATOR_ARGS,
                      weight: 1,
                      hidden: true,
                      sample: false,
                      description: '',
                    })
                  }
                  icon={<PlusOutlined />}
                >
                  添加用例配置
                </Button>
              )}
            </div>
            {fields.map((field) => (
              <div key={field.key} className="mb-4 border border-white/10 rounded-md p-3">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
                  <Form.Item
                    name={[field.name, 'caseNo']}
                    label="序号"
                    rules={[{ required: true, message: '请输入序号' }]}
                    className="mb-0"
                  >
                    <InputNumber min={1} className="w-full" disabled={isViewMode} />
                  </Form.Item>
                  <Form.Item
                    name={[field.name, 'category']}
                    label="分类"
                    rules={[{ required: true, message: '请选择分类' }]}
                    className="mb-0"
                  >
                    <Select options={TESTCASE_CATEGORY_OPTIONS} disabled={isViewMode} />
                  </Form.Item>
                  <Form.Item
                    name={[field.name, 'weight']}
                    label="权重"
                    rules={[{ required: true, message: '请输入权重' }]}
                    className="mb-0"
                  >
                    <InputNumber min={0} step={0.5} className="w-full" disabled={isViewMode} />
                  </Form.Item>
                  <div className="flex items-end gap-3">
                    <Form.Item
                      name={[field.name, 'hidden']}
                      valuePropName="checked"
                      className="mb-0"
                    >
                      <Checkbox disabled={isViewMode}>隐藏</Checkbox>
                    </Form.Item>
                    <Form.Item
                      name={[field.name, 'sample']}
                      valuePropName="checked"
                      className="mb-0"
                    >
                      <Checkbox disabled={isViewMode}>样例</Checkbox>
                    </Form.Item>
                    {!isViewMode && (
                      <Button
                        type="text"
                        danger
                        icon={<MinusCircleOutlined />}
                        onClick={() => remove(field.name)}
                      />
                    )}
                  </div>
                </div>
                <Form.Item
                  name={[field.name, 'generatorArgs']}
                  label="Generator 参数 JSON"
                  className="mt-3"
                  rules={[{ required: true, message: '请输入 generator 参数 JSON' }]}
                >
                  <Input.TextArea
                    rows={5}
                    placeholder={DEFAULT_GENERATOR_ARGS}
                    disabled={isViewMode}
                  />
                </Form.Item>
                <Form.Item name={[field.name, 'description']} label="说明" className="mb-0">
                  <Input placeholder="说明该测试用例覆盖的场景" disabled={isViewMode} />
                </Form.Item>
              </div>
            ))}
          </>
        )}
      </Form.List>
    </>
  )

  const renderLanguageLimitFields = () => (
    <>
      <div className="mb-4 flex items-center gap-2">
        <span className="text-white/50 text-sm">配置状态</span>
        {renderJudgeStatus(judgeConfig?.status)}
      </div>
      {!judgeConfig ? (
        <Alert type="warning" showIcon message="当前算法题尚未保存判题配置。" />
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-white/50 border-b border-white/10">
                  <th className="text-left py-2 pr-3">语言</th>
                  <th className="text-left py-2 pr-3">主标准解</th>
                  <th className="text-left py-2 pr-3">测速状态</th>
                  <th className="text-left py-2 pr-3">p95(ms)</th>
                  <th className="text-left py-2 pr-3">max(ms)</th>
                  <th className="text-left py-2 pr-3">内存(KB)</th>
                  <th className="text-left py-2 pr-3">建议限时(ms)</th>
                </tr>
              </thead>
              <tbody>
                {judgeConfig.standardSolutions.map((solution) => (
                  <tr key={solution.language} className="border-b border-white/5">
                    <td className="py-2 pr-3">{solution.language}</td>
                    <td className="py-2 pr-3">{solution.primarySolution ? '是' : '否'}</td>
                    <td className="py-2 pr-3">{solution.benchmarkStatus || '-'}</td>
                    <td className="py-2 pr-3">{solution.p95TimeMs ?? '-'}</td>
                    <td className="py-2 pr-3">{solution.maxTimeMs ?? '-'}</td>
                    <td className="py-2 pr-3">{solution.peakMemoryKb ?? '-'}</td>
                    <td className="py-2 pr-3">{solution.suggestedTimeLimitMs ?? '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {!isViewMode && (
            <div className="mt-5">
              <div className="text-white/50 text-sm mb-2">确认正式资源限制</div>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <Form.Item
                  name="confirmLanguage"
                  label="语言"
                  rules={[{ required: true, message: '请选择语言' }]}
                >
                  <Select
                    options={(standardSolutionsValue || []).map((solution) => ({
                      value: solution.language,
                      label: solution.language,
                    }))}
                  />
                </Form.Item>
                <Form.Item
                  name="confirmTimeLimitMs"
                  label="时间(ms)"
                  rules={[{ required: true, message: '请输入时间限制' }]}
                >
                  <InputNumber min={1} className="w-full" />
                </Form.Item>
                <Form.Item
                  name="confirmMemoryLimitKb"
                  label="内存(KB)"
                  rules={[{ required: true, message: '请输入内存限制' }]}
                >
                  <InputNumber min={1} className="w-full" />
                </Form.Item>
                <Form.Item
                  name="confirmOutputLimitKb"
                  label="输出(KB)"
                  rules={[{ required: true, message: '请输入输出限制' }]}
                >
                  <InputNumber min={1} className="w-full" />
                </Form.Item>
              </div>
              <Button type="primary" onClick={handleConfirmLanguageLimit}>
                确认限制
              </Button>
            </div>
          )}
        </>
      )}
    </>
  )

  const renderAlgorithmTabs = () => (
    <Tabs
      items={[
        { key: 'statement', label: '题面信息', children: renderAlgorithmStatementFields() },
        { key: 'judge-config', label: '判题配置', children: renderJudgeConfigFields() },
        { key: 'language-limits', label: '资源限制', children: renderLanguageLimitFields() },
      ]}
    />
  )

  const renderContentFields = () => {
    const type = questionTypeValue
    if (!type) return null

    switch (type) {
      case 'FILE_UPLOAD':
        return renderStemField('fileContent', 10, '输入题目描述/要求，支持 Markdown')

      case 'SINGLE_CHOICE':
        return (
          <>
            {renderStemField('choiceContent', 8, '输入题目描述，支持 Markdown')}
            <Form.List name="options">
              {(fields, { add, remove }) => (
                <>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-white/50 text-sm">选项列表</span>
                    {!isViewMode && (
                      <Button
                        type="dashed"
                        size="small"
                        onClick={() => add('')}
                        icon={<PlusOutlined />}
                      >
                        添加选项
                      </Button>
                    )}
                  </div>
                  {fields.map((field, index) => (
                    <Space key={field.key} align="baseline" className="mb-2">
                      <span className="text-white/40 shrink-0">
                        {String.fromCharCode(65 + index)}
                      </span>
                      <Form.Item
                        {...field}
                        rules={[{ required: true, message: '请输入选项内容' }]}
                        className="flex-1 mb-0"
                      >
                        <Input
                          placeholder={`选项 ${String.fromCharCode(65 + index)}`}
                          disabled={isViewMode}
                        />
                      </Form.Item>
                      {!isViewMode && fields.length > 1 && (
                        <Button
                          type="text"
                          danger
                          icon={<MinusCircleOutlined />}
                          onClick={() => remove(field.name)}
                        />
                      )}
                    </Space>
                  ))}
                </>
              )}
            </Form.List>
            <Form.Item name="correctAnswer" label="正确答案">
              <Radio.Group disabled={isViewMode}>
                {(optionsValue || []).map(
                  (opt, index) =>
                    opt && (
                      <Radio key={index} value={index}>
                        {String.fromCharCode(65 + index)}. {opt}
                      </Radio>
                    )
                )}
              </Radio.Group>
            </Form.Item>
          </>
        )

      case 'MULTIPLE_CHOICE':
        return (
          <>
            {renderStemField('choiceContent', 8, '输入题目描述，支持 Markdown')}
            <Form.List name="options">
              {(fields, { add, remove }) => (
                <>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-white/50 text-sm">选项列表</span>
                    {!isViewMode && (
                      <Button
                        type="dashed"
                        size="small"
                        onClick={() => add('')}
                        icon={<PlusOutlined />}
                      >
                        添加选项
                      </Button>
                    )}
                  </div>
                  {fields.map((field, index) => (
                    <Space key={field.key} align="baseline" className="mb-2">
                      <span className="text-white/40 shrink-0">
                        {String.fromCharCode(65 + index)}
                      </span>
                      <Form.Item
                        {...field}
                        rules={[{ required: true, message: '请输入选项内容' }]}
                        className="flex-1 mb-0"
                      >
                        <Input
                          placeholder={`选项 ${String.fromCharCode(65 + index)}`}
                          disabled={isViewMode}
                        />
                      </Form.Item>
                      {!isViewMode && fields.length > 1 && (
                        <Button
                          type="text"
                          danger
                          icon={<MinusCircleOutlined />}
                          onClick={() => remove(field.name)}
                        />
                      )}
                    </Space>
                  ))}
                </>
              )}
            </Form.List>
            <Form.Item name="correctAnswers" label="正确答案">
              <Checkbox.Group disabled={isViewMode}>
                {(optionsValue || []).map(
                  (opt, index) =>
                    opt && (
                      <Checkbox key={index} value={index}>
                        {String.fromCharCode(65 + index)}. {opt}
                      </Checkbox>
                    )
                )}
              </Checkbox.Group>
            </Form.Item>
          </>
        )

      case 'ALGORITHM':
        return renderAlgorithmTabs()

      default:
        return null
    }
  }

  const isAlgorithm = questionTypeValue === 'ALGORITHM'

  return (
    <Drawer
      title={drawerTitle}
      open={open}
      onClose={onClose}
      width={drawerWidth}
      footer={
        isViewMode ? (
          <div className="flex justify-end gap-2">
            {question && (
              <Button danger onClick={() => onDelete(question)}>
                删除
              </Button>
            )}
            <Button type="primary" onClick={onEdit}>
              编辑
            </Button>
          </div>
        ) : (
          <div className="flex justify-end gap-2">
            <Button onClick={onClose}>取消</Button>
            {isAlgorithm && (
              <Button onClick={() => handleSubmit(true)}>
                {isCreateMode ? '创建并生成测试数据' : '保存并生成测试数据'}
              </Button>
            )}
            <Button type="primary" onClick={() => handleSubmit(false)}>
              {isCreateMode ? '创建' : '保存'}
            </Button>
          </div>
        )
      }
    >
      <Form
        form={form}
        layout="vertical"
        disabled={isViewMode}
        initialValues={{
          questionType: 'FILE_UPLOAD',
          options: [''],
          examples: [],
          runTestCases: [],
          starterCodeTemplates: [],
          correctAnswer: -1,
          correctAnswers: [],
          generatorLanguage: 'python',
          generatorSource: '',
          primaryStandardLanguage: 'python',
          benchmarkRepeatTimes: 5,
          marginMultiplier: 1.2,
          minExtraMs: 50,
          roundToMs: 50,
          standardSolutions: [],
          testcases: [],
          confirmLanguage: 'python',
          confirmTimeLimitMs: null,
          confirmMemoryLimitKb: null,
          confirmOutputLimitKb: 1024,
        }}
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Form.Item
            name="questionNo"
            label="题号"
            rules={[{ required: true, message: '请输入题号' }]}
          >
            <InputNumber min={1} placeholder="题号" className="w-full" />
          </Form.Item>

          <Form.Item
            name="questionType"
            label="题型"
            rules={[{ required: true, message: '请选择题型' }]}
          >
            <Select options={QUESTION_TYPE_OPTIONS} placeholder="选择题型" />
          </Form.Item>
        </div>

        <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}>
          <Input placeholder="题目标题" />
        </Form.Item>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <Form.Item name="score" label="分值" rules={[{ required: true, message: '请输入分值' }]}>
            <InputNumber min={0} precision={1} placeholder="分值" className="w-full" />
          </Form.Item>

          <Form.Item name="attachmentId" label="附件">
            <Upload
              beforeUpload={handleUpload}
              showUploadList={false}
              accept=".pdf,.doc,.docx,.zip,.rar,.png,.jpg,.jpeg"
            >
              <Button icon={<PaperClipOutlined />} disabled={isViewMode}>
                {form.getFieldValue('attachmentId') ? '更换附件' : '上传附件'}
              </Button>
            </Upload>
            {form.getFieldValue('attachmentId') && (
              <span className="ml-2 text-white/50 text-sm">
                已上传 (ID: {form.getFieldValue('attachmentId')})
              </span>
            )}
          </Form.Item>
        </div>

        {renderContentFields()}
      </Form>
    </Drawer>
  )
}
