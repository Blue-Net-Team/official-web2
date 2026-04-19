'use client'

import { useEffect, useMemo } from 'react'
import {
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
  Upload,
} from 'antd'
import { MinusCircleOutlined, PlusOutlined, PaperClipOutlined } from '@ant-design/icons'
import type {
  AlgorithmTestCase,
  AssessmentQuestionDTO,
  CreateQuestionRequestDTO,
  QuestionType,
  QuestionContent,
  UpdateQuestionRequestDTO,
} from '@/apis/schema/assessment.dto'
import { adminAssessmentQuestionService } from '@/apis/services/admin-assessment-question.service'
import { fileService } from '@/apis/services/file.service'
import { MarkdownRenderer, QuestionStemMarkdownEditor } from '@/components/Assessment'

export type DrawerMode = 'view' | 'edit' | 'create'

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

const QUESTION_TYPE_OPTIONS: { value: QuestionType; label: string }[] = [
  { value: 'FILE_UPLOAD', label: '文件上传' },
  { value: 'SINGLE_CHOICE', label: '单选题' },
  { value: 'MULTIPLE_CHOICE', label: '多选题' },
  { value: 'ALGORITHM', label: '算法题' },
]

const QUESTION_TYPE_LABELS: Record<QuestionType, string> = {
  FILE_UPLOAD: '文件上传',
  SINGLE_CHOICE: '单选题',
  MULTIPLE_CHOICE: '多选题',
  ALGORITHM: '算法题',
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
  // Algorithm content
  algorithmContent: string
  inputDescription: string
  outputDescription: string
  constraints: string
  examples: { input: string; expectedOutput: string; explanation?: string }[]
  runTestCases: AlgorithmTestCase[]
  timeLimit: number | null
  memoryLimit: number | null
  testCases: AlgorithmTestCase[]
  starterCodeTemplates: { language: string; code: string }[]
}

/** QuestionType enum → 后端 Jackson @JsonSubTypes 小写 name */
const CONTENT_TYPE_MAP: Record<QuestionType, string> = {
  FILE_UPLOAD: 'file_upload',
  SINGLE_CHOICE: 'single_choice',
  MULTIPLE_CHOICE: 'multiple_choice',
  ALGORITHM: 'algorithm',
}

const PROGRAMMING_LANGUAGE_OPTIONS = [
  { value: 'python', label: 'Python' },
  { value: 'c', label: 'C' },
  { value: 'cpp', label: 'C++' },
  { value: 'java', label: 'Java' },
  { value: 'javascript', label: 'JavaScript' },
]

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
function parseStarterCodeTemplates(starterCode: unknown) {
  if (!starterCode || typeof starterCode !== 'object') return []

  return Object.entries(starterCode as Record<string, string>).map(([language, code]) => ({
    language,
    code,
  }))
}

/** correctAnswer 在表单中存的是选项索引(number)，提交时转为选项文本 */
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
        testCases: values.testCases || [],
        starterCode: buildStarterCodeMap(values.starterCodeTemplates),
        timeLimit: values.timeLimit ?? undefined,
        memoryLimit: values.memoryLimit ?? undefined,
      } as QuestionContent
    default:
      return null
  }
}

/** 后端 correctAnswer 是选项文本，转为表单用的索引 */
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
        testCases: (c.testCases as AlgorithmTestCase[]) || [],
        starterCodeTemplates: parseStarterCodeTemplates(c.starterCode),
        timeLimit: (c.timeLimit as number) ?? null,
        memoryLimit: (c.memoryLimit as number) ?? null,
      }
    default:
      return {}
  }
}

export { QUESTION_TYPE_LABELS }

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

  const isViewMode = mode === 'view'
  const isCreateMode = mode === 'create'
  const questionTypeValue = Form.useWatch('questionType', form)
  const optionsValue = Form.useWatch('options', form)

  // Populate form on open
  useEffect(() => {
    if (!open) return

    if (isCreateMode) {
      form.resetFields()
    } else if (question) {
      const contentFields = parseContentToForm(question.content, question.questionType)
      form.setFieldsValue({
        questionNo: question.questionNo,
        questionType: question.questionType,
        title: question.title,
        score: question.score,
        attachmentId: question.attachmentId,
        ...contentFields,
      })
    }
  }, [open, mode, question, form, isCreateMode])

  // Reset content fields when type changes
  useEffect(() => {
    if (!open || isViewMode) return
    const currentType = form.getFieldValue('questionType')
    if (currentType && questionTypeValue && currentType !== questionTypeValue) {
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
        timeLimit: null,
        memoryLimit: null,
        testCases: [],
        starterCodeTemplates: [],
      })
    }
  }, [questionTypeValue, form, open, isViewMode])

  const handleSubmit = async () => {
    if (!assessmentTimeId) return
    try {
      const values = await form.validateFields()
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
        await adminAssessmentQuestionService.create(payload)
        messageApi.success('创建成功')
      } else if (question) {
        const payload: UpdateQuestionRequestDTO = {
          questionNo: values.questionNo,
          questionType: values.questionType,
          title: values.title,
          score: values.score,
          content,
          attachmentId: values.attachmentId || null,
        }
        await adminAssessmentQuestionService.update(question.id, payload)
        messageApi.success('更新成功')
      }
      onSuccess()
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'errorFields' in err) return
      const msg = (err as { response?: { data?: { msg?: string } } })?.response?.data?.msg
      messageApi.error(msg || '操作失败')
    }
  }

  const handleUpload = async (file: File) => {
    try {
      const res = await fileService.upload(file, 'ASSESSMENT_ATTACHMENT')
      if (res.data) {
        form.setFieldsValue({ attachmentId: res.data.id })
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
    name: 'examples' | 'runTestCases' | 'testCases',
    label: string,
    buttonText: string,
    includeExplanation = false,
    includeFormalFields = false
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
                onClick={() =>
                  add(
                    includeFormalFields
                      ? { input: '', expectedOutput: '', hidden: true, weight: 1 }
                      : { input: '', expectedOutput: '' }
                  )
                }
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
              {includeFormalFields && (
                <div className="flex gap-4 mt-2">
                  <Form.Item
                    name={[field.name, 'hidden']}
                    label="隐藏用例"
                    valuePropName="checked"
                    className="mb-0"
                  >
                    <Checkbox disabled={isViewMode}>隐藏</Checkbox>
                  </Form.Item>
                  <Form.Item name={[field.name, 'weight']} label="权重" className="flex-1 mb-0">
                    <InputNumber
                      min={1}
                      placeholder="默认 1"
                      style={{ width: '100%' }}
                      disabled={isViewMode}
                    />
                  </Form.Item>
                </div>
              )}
            </div>
          ))}
        </>
      )}
    </Form.List>
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
                    <Space key={field.key} align="baseline" style={{ marginBottom: 8 }}>
                      <span className="text-white/40 shrink-0">
                        {String.fromCharCode(65 + index)}
                      </span>
                      <Form.Item
                        {...field}
                        rules={[{ required: true, message: '请输入选项内容' }]}
                        style={{ flex: 1, marginBottom: 0 }}
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
                    <Space key={field.key} align="baseline" style={{ marginBottom: 8 }}>
                      <span className="text-white/40 shrink-0">
                        {String.fromCharCode(65 + index)}
                      </span>
                      <Form.Item
                        {...field}
                        rules={[{ required: true, message: '请输入选项内容' }]}
                        style={{ flex: 1, marginBottom: 0 }}
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
        return (
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
            <div className="flex gap-4">
              <Form.Item name="timeLimit" label="时间限制(ms)" className="flex-1">
                <InputNumber
                  min={1}
                  placeholder="如 1000"
                  style={{ width: '100%' }}
                  disabled={isViewMode}
                />
              </Form.Item>
              <Form.Item name="memoryLimit" label="内存限制(KB)" className="flex-1">
                <InputNumber
                  min={1}
                  placeholder="如 256"
                  style={{ width: '100%' }}
                  disabled={isViewMode}
                />
              </Form.Item>
            </div>
            {renderAlgorithmTestCases('examples', '题面样例', '添加样例', true)}
            {renderAlgorithmTestCases('runTestCases', '默认运行用例', '添加默认运行用例')}
            {renderAlgorithmTestCases('testCases', '正式判题用例', '添加正式判题用例', false, true)}
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
                          <Input.TextArea
                            rows={8}
                            placeholder="输入该语言的初始代码"
                            disabled={isViewMode}
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

      default:
        return null
    }
  }

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
            <Button type="primary" onClick={handleSubmit}>
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
          testCases: [],
          starterCodeTemplates: [],
          correctAnswer: -1,
          correctAnswers: [],
        }}
      >
        <Form.Item
          name="questionNo"
          label="题号"
          rules={[{ required: true, message: '请输入题号' }]}
        >
          <InputNumber min={1} placeholder="题号" style={{ width: '100%' }} />
        </Form.Item>

        <Form.Item
          name="questionType"
          label="题型"
          rules={[{ required: true, message: '请选择题型' }]}
        >
          <Select options={QUESTION_TYPE_OPTIONS} placeholder="选择题型" />
        </Form.Item>

        <Form.Item name="title" label="标题" rules={[{ required: true, message: '请输入标题' }]}>
          <Input placeholder="题目标题" />
        </Form.Item>

        <Form.Item name="score" label="分值" rules={[{ required: true, message: '请输入分值' }]}>
          <InputNumber min={0} precision={1} placeholder="分值" style={{ width: '100%' }} />
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

        {renderContentFields()}
      </Form>
    </Drawer>
  )
}
