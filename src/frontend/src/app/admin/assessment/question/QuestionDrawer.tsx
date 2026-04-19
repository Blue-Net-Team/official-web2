'use client'

import { useEffect, useMemo } from 'react'
import {
  App,
  Button,
  Checkbox,
  Drawer,
  Form,
  Input,
  InputNumber,
  Radio,
  Select,
  Space,
  Upload,
} from 'antd'
import { MinusCircleOutlined, PlusOutlined, PaperClipOutlined } from '@ant-design/icons'
import type {
  AssessmentQuestionDTO,
  CreateQuestionRequestDTO,
  QuestionType,
  QuestionContent,
  UpdateQuestionRequestDTO,
} from '@/apis/schema/assessment.dto'
import { adminAssessmentQuestionService } from '@/apis/services/admin-assessment-question.service'
import { fileService } from '@/apis/services/file.service'

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
  timeLimit: number | null
  memoryLimit: number | null
  testCases: { input: string; expectedOutput: string }[]
}

/** QuestionType enum → 后端 Jackson @JsonSubTypes 小写 name */
const CONTENT_TYPE_MAP: Record<QuestionType, string> = {
  FILE_UPLOAD: 'file_upload',
  SINGLE_CHOICE: 'single_choice',
  MULTIPLE_CHOICE: 'multiple_choice',
  ALGORITHM: 'algorithm',
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
        testCases: values.testCases || [],
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
        testCases: (c.testCases as { input: string; expectedOutput: string }[]) || [],
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
        timeLimit: null,
        memoryLimit: null,
        testCases: [],
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

  const renderContentFields = () => {
    const type = questionTypeValue
    if (!type) return null

    switch (type) {
      case 'FILE_UPLOAD':
        return (
          <Form.Item name="fileContent" label="题干">
            <Input.TextArea rows={4} placeholder="输入题目描述/要求" disabled={isViewMode} />
          </Form.Item>
        )

      case 'SINGLE_CHOICE':
        return (
          <>
            <Form.Item name="choiceContent" label="题干">
              <Input.TextArea rows={3} placeholder="输入题目描述" disabled={isViewMode} />
            </Form.Item>
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
            <Form.Item name="choiceContent" label="题干">
              <Input.TextArea rows={3} placeholder="输入题目描述" disabled={isViewMode} />
            </Form.Item>
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
            <Form.Item name="algorithmContent" label="题干">
              <Input.TextArea rows={4} placeholder="输入题目描述" disabled={isViewMode} />
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
            <Form.List name="testCases">
              {(fields, { add, remove }) => (
                <>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-white/50 text-sm">测试用例</span>
                    {!isViewMode && (
                      <Button
                        type="dashed"
                        size="small"
                        onClick={() => add({ input: '', expectedOutput: '' })}
                        icon={<PlusOutlined />}
                      >
                        添加测试用例
                      </Button>
                    )}
                  </div>
                  {fields.map((field) => (
                    <div key={field.key} className="mb-3">
                      <div className="flex gap-2">
                        <Form.Item
                          name={[field.name, 'input']}
                          label="输入"
                          rules={[{ required: true, message: '请输入' }]}
                          className="flex-1 mb-0"
                        >
                          <Input.TextArea rows={2} placeholder="输入" disabled={isViewMode} />
                        </Form.Item>
                        <Form.Item
                          name={[field.name, 'expectedOutput']}
                          label="期望输出"
                          rules={[{ required: true, message: '请输入' }]}
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
      width={isViewMode ? 500 : 560}
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
          testCases: [],
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
