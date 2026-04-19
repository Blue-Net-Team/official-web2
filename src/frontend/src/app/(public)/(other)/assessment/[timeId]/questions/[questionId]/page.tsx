'use client'

import { useState, useEffect, useCallback, useRef } from 'react'
import { useParams, useRouter } from 'next/navigation'
import {
  ArrowLeftOutlined,
  LeftOutlined,
  RightOutlined,
  FileOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  PaperClipOutlined,
  InboxOutlined,
  CalendarOutlined,
  WarningOutlined,
  UploadOutlined,
  FileTextOutlined,
  SendOutlined,
  DownOutlined,
  ExperimentOutlined,
  RedoOutlined,
  DownloadOutlined,
} from '@ant-design/icons'
import { Button, Tag, message, Spin, Upload, type UploadProps } from 'antd'
import { assessmentQuestionService } from '@/apis/services/assessment-question.service'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'
import { assessmentAnswerService } from '@/apis/services/assessment-answer.service'
import { assessmentSessionService } from '@/apis/services/assessment-session.service'
import { fileService } from '@/apis/services/file.service'
import authStore from '@/stores/authStore'
import type {
  AssessmentQuestionDTO,
  AssessmentAnswerDTO,
  FileUploadContent,
  AssessmentTimeDTO,
  AssessmentSessionDTO,
  AssessmentStatus,
} from '@/apis/schema/assessment.dto'
import { DIRECTION_LABELS as DirectionLabels } from '@/apis/schema/enumerate'
import { QuestionTypeLabels } from '@/types/assessment'
import CountdownTimer from './CountdownTimer'
import styles from './styles.module.css'

function getStatusInfo(
  startTime: string,
  endTime: string
): { text: string; status: AssessmentStatus } {
  const now = Date.now()
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  if (now < start) return { text: '未开始', status: 'NOT_STARTED' }
  if (now > end) return { text: '已结束', status: 'ENDED' }
  return { text: '进行中', status: 'IN_PROGRESS' }
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

type UploadPhase =
  | 'idle'
  | 'uploaded'
  | 'answered'
  | 'resubmitting'
  | 'resubmit_uploaded'
  | 'expired'

function getUploadPhase(
  isExpired: boolean,
  isAnswered: boolean,
  isResubmitting: boolean,
  hasUploadedFile: boolean
): UploadPhase {
  if (isExpired) return 'expired'
  if (isAnswered && !isResubmitting) return 'answered'
  if (isAnswered && isResubmitting && !hasUploadedFile) return 'resubmitting'
  if (isAnswered && isResubmitting && hasUploadedFile) return 'resubmit_uploaded'
  if (hasUploadedFile) return 'uploaded'
  return 'idle'
}

export default function QuestionDetailPage() {
  const router = useRouter()
  const params = useParams()
  const timeId = Number(params.timeId)
  const questionId = Number(params.questionId)

  const [question, setQuestion] = useState<AssessmentQuestionDTO | null>(null)
  const [answer, setAnswer] = useState<AssessmentAnswerDTO | null>(null)
  const [timeInfo, setTimeInfo] = useState<AssessmentTimeDTO | null>(null)
  const [session, setSession] = useState<AssessmentSessionDTO | null>(null)
  const [questionsList, setQuestionsList] = useState<AssessmentQuestionDTO[]>([])
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [isResubmitting, setIsResubmitting] = useState(false)
  const [uploadProgress, setUploadProgress] = useState(0)
  const [uploadedFile, setUploadedFile] = useState<{
    id: number
    name: string
    size?: number
  } | null>(null)
  const [isExpired, setIsExpired] = useState(false)
  const autoSubmitRef = useRef(false)
  const { isAuthenticated, checkAuthStatus } = authStore()
  const [messageApi, contextHolder] = message.useMessage()

  // 认证检查
  useEffect(() => {
    const checkAuth = async () => {
      const isAuth = await checkAuthStatus()
      if (!isAuth) {
        router.replace('/login')
      }
    }
    checkAuth()
  }, [checkAuthStatus, router])

  // 加载考核时间信息
  const fetchTimeInfo = useCallback(async () => {
    try {
      const response = await assessmentTimeService.getAssessmentTimes(0, 50)
      if (response.code === 200 && response.data) {
        const found = response.data.content.find((t: AssessmentTimeDTO) => t.id === timeId)
        if (found) {
          setTimeInfo(found)
          return found
        }
      }
    } catch (error) {
      console.error('Failed to fetch assessment time info:', error)
    }
    return null
  }, [timeId])

  // 加载考核会话
  const fetchSession = useCallback(async () => {
    try {
      const response = await assessmentSessionService.getSession(timeId)
      if (response.code === 200 && response.data) {
        setSession(response.data)
        if (new Date(response.data.deadline).getTime() <= Date.now()) {
          setIsExpired(true)
        }
      } else {
        setSession(null)
        setIsExpired(false)
      }
    } catch {
      // 没有会话（非限时考核或未开始），忽略
    }
  }, [timeId])

  // 加载题目详情
  const fetchQuestion = useCallback(async () => {
    try {
      const response = await assessmentQuestionService.getQuestionDetail(questionId)
      if (response.code === 200 && response.data) {
        setQuestion(response.data)
      }
    } catch (error) {
      console.error('Failed to fetch question detail:', error)
    }
  }, [questionId])

  // 加载题目列表
  const fetchQuestionsList = useCallback(async () => {
    try {
      const response = await assessmentQuestionService.getQuestions(timeId, 0, 100)
      if (response.code === 200 && response.data) {
        setQuestionsList(response.data.questions.content)
      }
    } catch (error) {
      console.error('Failed to fetch questions list:', error)
    }
  }, [timeId])

  // 加载已有答案
  const fetchAnswer = useCallback(async () => {
    try {
      const response = await assessmentAnswerService.getMyAnswer(questionId)
      if (response.code === 200 && response.data) {
        setAnswer(response.data)
      }
    } catch {
      // 没有答案，忽略
    }
  }, [questionId])

  useEffect(() => {
    if (isAuthenticated) {
      Promise.all([
        fetchQuestion(),
        fetchAnswer(),
        fetchQuestionsList(),
        fetchTimeInfo().then((found) => {
          if (found?.timeLimit) return fetchSession()
          setSession(null)
          setIsExpired(false)
          return undefined
        }),
      ]).finally(() => setLoading(false))
    }
  }, [isAuthenticated, fetchQuestion, fetchAnswer, fetchQuestionsList, fetchTimeInfo, fetchSession])

  // 提交答案
  const handleSubmit = async () => {
    if (!uploadedFile) {
      messageApi.warning('请先上传文件')
      return
    }
    setSubmitting(true)
    try {
      const response = await assessmentAnswerService.createAnswer({
        questionId,
        fileId: uploadedFile.id,
      })
      if (response.code === 200 && response.data) {
        setAnswer(response.data)
        setIsResubmitting(false)
        setUploadedFile(null)
        messageApi.success('提交成功')
      } else {
        messageApi.error(response.msg || '提交失败')
      }
    } catch (error) {
      messageApi.error('提交失败，请重试')
      console.error('Submit error:', error)
    } finally {
      setSubmitting(false)
    }
  }

  const handleResubmit = async () => {
    if (!uploadedFile) {
      messageApi.warning('请先上传新文件')
      return
    }
    setSubmitting(true)
    try {
      const response = await assessmentAnswerService.updateAnswer({
        questionId,
        fileId: uploadedFile.id,
      })
      if (response.code === 200 && response.data) {
        setAnswer(response.data)
        setIsResubmitting(false)
        setUploadedFile(null)
        messageApi.success('重新提交成功')
      } else {
        messageApi.error(response.msg || '重新提交失败')
      }
    } catch (error) {
      messageApi.error('重新提交失败，请重试')
      console.error('Resubmit error:', error)
    } finally {
      setSubmitting(false)
    }
  }

  // 超时自动提交
  const handleTimeUp = useCallback(() => {
    setIsExpired(true)
    if (answer) return
    if (!uploadedFile) return
    if (autoSubmitRef.current) return
    autoSubmitRef.current = true

    setSubmitting(true)
    assessmentAnswerService
      .createAnswer({ questionId, fileId: uploadedFile.id })
      .then((response) => {
        if (response.code === 200 && response.data) {
          setAnswer(response.data)
          messageApi.warning('考核时间已到，已自动提交')
        } else {
          messageApi.error('自动提交失败')
        }
      })
      .catch((error) => {
        messageApi.error('自动提交失败')
        console.error('Auto-submit error:', error)
      })
      .finally(() => {
        setSubmitting(false)
      })
  }, [answer, uploadedFile, questionId, messageApi])

  // 删除已上传文件
  const handleRemoveFile = () => {
    if (isExpired) return
    setUploadedFile(null)
  }

  // 导航
  const currentIndex = questionsList.findIndex((q) => q.id === questionId)
  const hasPrev = currentIndex > 0
  const hasNext = currentIndex < questionsList.length - 1 && currentIndex >= 0

  const handlePrev = () => {
    if (hasPrev)
      router.push(`/assessment/${timeId}/questions/${questionsList[currentIndex - 1].id}`)
  }
  const handleNext = () => {
    if (hasNext)
      router.push(`/assessment/${timeId}/questions/${questionsList[currentIndex + 1].id}`)
  }

  if (!isAuthenticated || loading) {
    return (
      <div className="min-h-screen bg-[#0a0a0a] relative flex flex-col">
        <div className={`${styles.bg} top-0 left-0 w-full h-full z-0 pointer-events-none fixed`} />
        <div className="flex flex-col justify-center items-center min-h-[300px] relative z-10">
          <Spin size="large" />
        </div>
      </div>
    )
  }

  if (!question) {
    return (
      <div className="min-h-screen bg-[#0a0a0a] relative flex flex-col">
        <div className={`${styles.bg} top-0 left-0 w-full h-full z-0 pointer-events-none fixed`} />
        <div className="flex flex-col justify-center items-center min-h-[300px] relative z-10">
          <p className="text-white/50 mb-4">题目不存在或无权查看</p>
          <Button onClick={() => router.push(`/assessment/${timeId}/questions`)}>
            返回题目列表
          </Button>
        </div>
      </div>
    )
  }

  const fileContent = question.content as FileUploadContent | null
  const isFileUpload = question.questionType === 'FILE_UPLOAD'
  const isAnswered = !!answer
  const isTimed = Boolean(timeInfo?.timeLimit && session?.deadline)
  const deadline = session?.deadline ?? null
  const statusInfo = timeInfo ? getStatusInfo(timeInfo.startTime, timeInfo.endTime) : null

  const allowedExtsText = fileContent?.allowedExtensions
    ? `支持 ${fileContent.allowedExtensions.join(', ')} 格式`
    : '支持所有文件格式'
  const maxSizeText = fileContent?.maxFileSize
    ? `最大 ${formatFileSize(fileContent.maxFileSize)}`
    : ''
  const uploadHintText = [allowedExtsText, maxSizeText].filter(Boolean).join('，')

  const uploadPhase = getUploadPhase(isExpired, isAnswered, isResubmitting, !!uploadedFile)

  const draggerProps: UploadProps = {
    name: 'file',
    multiple: false,
    showUploadList: false,
    accept: fileContent?.allowedExtensions?.map((ext) => `.${ext}`).join(',') || undefined,
    customRequest: async ({ file, onSuccess, onError }) => {
      try {
        setUploadProgress(0)
        const response = await fileService.upload(file as File, 'WORK', (progress) => {
          setUploadProgress(progress)
        })
        if (response.code === 200 && response.data) {
          setUploadedFile({
            id: response.data.id,
            name: (file as File).name,
            size: (file as File).size,
          })
          messageApi.success('文件上传成功')
          onSuccess?.(response.data)
        } else {
          messageApi.error(response.msg || '上传失败')
          onError?.(new Error(response.msg || '上传失败'))
        }
      } catch (error) {
        messageApi.error('上传失败，请重试')
        onError?.(error as Error)
      } finally {
        setUploadProgress(0)
      }
    },
    beforeUpload: (file) => {
      if (fileContent?.maxFileSize && file.size > fileContent.maxFileSize) {
        messageApi.error(`文件大小不能超过 ${formatFileSize(fileContent.maxFileSize)}`)
        return Upload.LIST_IGNORE
      }
      return true
    },
  }

  const renderUploadedFileRow = (meta: string, onRemove: () => void) => (
    <div className="flex items-center justify-between p-3.5 rounded-lg bg-white/[0.08]">
      <div className="flex items-center gap-3">
        <div className="w-9 h-9 rounded-md bg-[#6677ff]/[0.1] flex items-center justify-center flex-shrink-0">
          <FileOutlined className="text-base text-[#6677ff]" />
        </div>
        <div className="flex flex-col gap-0.5">
          <span className="text-[13px] font-medium text-white">{uploadedFile!.name}</span>
          <span className="text-[11px] text-white/30">
            {uploadedFile!.size ? formatFileSize(uploadedFile!.size) : ''} · {meta}
          </span>
        </div>
      </div>
      <button
        title="删除文件"
        className="w-7 h-7 rounded-md bg-[#f5222d]/[0.24] border-none flex items-center justify-center cursor-pointer transition-colors duration-200 hover:bg-[#f5222d]/[0.48] flex-shrink-0"
        onClick={onRemove}
      >
        <DeleteOutlined className="text-sm text-[#f5222d]!" />
      </button>
    </div>
  )

  const renderProgressBar = () =>
    uploadProgress > 0 && uploadProgress < 100 ? (
      <div className="mt-4">
        <div className="h-1 rounded-[2px] bg-white/[0.04] overflow-hidden">
          <div
            className="h-full rounded-[2px] transition-[width] duration-300 ease-out"
            style={{
              width: `${uploadProgress}%`,
              background: 'linear-gradient(90deg, #6677ff, #8594ff)',
            }}
          />
        </div>
      </div>
    ) : null

  const dropHintText = [
    fileContent?.allowedExtensions ? `${fileContent.allowedExtensions.join(', ')}` : '所有文件格式',
    fileContent?.maxFileSize ? `最大 ${formatFileSize(fileContent.maxFileSize)}` : '',
  ]
    .filter(Boolean)
    .join(' · ')

  const UploadArea = () => {
    switch (uploadPhase) {
      case 'answered':
        return (
          <div className="flex items-center gap-4 p-5 rounded-[10px] bg-[#07c160]/[0.06] border border-[#07c160]/[0.12]">
            <CheckCircleOutlined className="text-[32px] text-[#07c160]" />
            <div className="flex-1">
              <p className="text-base font-semibold text-[#07c160] mb-1">已提交答案</p>
              <p className="text-[13px] text-white/45 m-0">
                提交时间：
                {answer?.submitTime ? new Date(answer.submitTime).toLocaleString('zh-CN') : '-'}
              </p>
            </div>
            {!isExpired && (
              <button
                className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-[#fa8c16]/[0.1] border border-[#fa8c16]/[0.19] text-[#fa8c16] text-xs font-medium cursor-pointer transition-all duration-200 flex-shrink-0 hover:bg-[#fa8c16]/[0.19]"
                onClick={() => {
                  setIsResubmitting(true)
                  setUploadedFile(null)
                }}
              >
                <RedoOutlined className="text-sm" />
                重新提交
              </button>
            )}
          </div>
        )
      case 'resubmit_uploaded':
        return (
          <>
            {renderUploadedFileRow('已上传（新文件）', () => setUploadedFile(null))}
            {renderProgressBar()}
          </>
        )
      case 'resubmitting':
        return (
          <>
            <Upload.Dragger {...draggerProps}>
              <p className="text-[36px] text-white/30 m-0">
                <InboxOutlined />
              </p>
              <p className="mt-3 text-sm text-white/65">上传新文件替换已提交的答案</p>
              <p className="mt-2 text-xs text-white/30">{dropHintText}</p>
            </Upload.Dragger>
            {renderProgressBar()}
          </>
        )
      case 'expired':
        return (
          <div className="flex items-center gap-4 p-5 rounded-[10px] bg-[#ff4d4f]/[0.06] border border-[#ff4d4f]/[0.12]">
            <WarningOutlined className="text-[32px] text-[#ff4d4f]" />
            <div>
              <p className="text-base font-semibold text-[#ff4d4f] mb-1">考核已结束</p>
              <p className="text-[13px] text-white/45 m-0">
                {uploadedFile ? '考核时间已到，答案已自动提交' : '考核已结束，未提交答案'}
              </p>
            </div>
          </div>
        )
      case 'uploaded':
        return (
          <>
            {renderUploadedFileRow('已上传', handleRemoveFile)}
            {renderProgressBar()}
          </>
        )
      case 'idle':
      default:
        return (
          <>
            <Upload.Dragger {...draggerProps}>
              <p className="text-[36px] text-white/30 m-0">
                <InboxOutlined />
              </p>
              <p className="mt-3 text-sm text-white/65">拖拽文件到此处，或点击选择文件</p>
              <p className="mt-2 text-xs text-white/30">{dropHintText}</p>
            </Upload.Dragger>
            {renderProgressBar()}
          </>
        )
    }
  }

  const descriptionLines = fileContent?.content?.split('\n').filter(Boolean) || []
  const mainDesc = descriptionLines[0] || '暂无题目描述'
  const requirements = descriptionLines.slice(1)

  return (
    <div className="min-h-screen bg-[#0a0a0a] relative flex flex-col">
      <div className={`${styles.bg} top-0 left-0 w-full h-full z-0 pointer-events-none fixed`} />
      {contextHolder}

      {isExpired && (
        <div className="relative z-[2] flex items-center justify-center px-6 py-3 bg-[#ff4d4f]/[0.12] border-b border-[#ff4d4f]/[0.25] text-[#ff4d4f] text-sm font-semibold backdrop-blur-[8px]">
          <WarningOutlined className="mr-2" />
          {isAnswered
            ? '考核已结束'
            : uploadedFile
              ? '考核时间已到，已自动提交'
              : '考核已结束，未提交答案'}
        </div>
      )}

      <div className="relative z-10 max-w-[1440px] w-full mx-auto px-5 md:px-10 lg:px-20 py-[60px] flex flex-col gap-8">
        <header className="flex items-center gap-3 flex-wrap">
          <button
            className="inline-flex items-center gap-2 px-3 py-2 rounded-lg border-none bg-white/[0.08] text-white/65 text-[13px] cursor-pointer transition-all duration-200 flex-shrink-0 hover:bg-white/[0.08] hover:text-white"
            onClick={() => router.push(`/assessment/${timeId}/questions`)}
          >
            <ArrowLeftOutlined />
            <span>返回目录</span>
          </button>

          <div className="flex flex-col gap-1">
            <div className="flex items-center gap-3 flex-wrap">
              <h1 className="text-[22px] font-semibold text-white m-0">
                题目 {question.questionNo} · {QuestionTypeLabels[question.questionType]}
              </h1>
              <span className="inline-flex items-center px-3 py-1 rounded-md bg-[#fa8c16]/[0.1] text-xs font-semibold text-[#fa8c16] border-none">
                {question.score} 分
              </span>
              {isAnswered && (
                <Tag color="success" style={{ margin: 0 }}>
                  <CheckCircleOutlined /> 已答
                </Tag>
              )}
            </div>
            <div className="flex items-center gap-4 flex-wrap">
              {timeInfo && (
                <Tag color="blue" style={{ margin: 0 }}>
                  {DirectionLabels[timeInfo.direction]}
                </Tag>
              )}
              {timeInfo && (
                <Tag
                  style={{
                    margin: 0,
                    background: '#ffffff08',
                    border: '1px solid #ffffff0a',
                    color: 'rgba(255,255,255,0.45)',
                  }}
                >
                  {timeInfo.grade ? `${timeInfo.grade}级` : ''}
                </Tag>
              )}
            </div>
          </div>
        </header>

        <div className="flex flex-col lg:flex-row gap-8">
          <main className="flex-1 min-w-0 flex flex-col gap-6">
            <section className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-7 h-fit">
              <div className="flex items-center gap-2.5">
                <FileTextOutlined className="text-xl text-[#fa8c16]" />
                <h2 className="text-base font-semibold text-white m-0">{question.title}</h2>
              </div>
              <hr className="w-full h-px bg-white/[0.04] border-none m-0 my-4" />
              <div className="flex flex-col gap-4">
                <p className="text-sm leading-relaxed text-white/65 whitespace-pre-wrap m-0">
                  {mainDesc}
                </p>
                {requirements.length > 0 && (
                  <div className="flex flex-col gap-2.5">
                    {requirements.map((req, i) => (
                      <div key={i} className="flex gap-2 text-[13px] text-white/45">
                        <span className="flex-shrink-0 text-white/45">{i + 1}.</span>
                        <span className="text-white/65 leading-relaxed">{req}</span>
                      </div>
                    ))}
                  </div>
                )}
                {fileContent?.allowedExtensions && (
                  <div className="flex gap-2 text-[13px] text-white/45">
                    <span className="flex-shrink-0 text-white/45">允许的文件类型：</span>
                    <span className="text-white/65 leading-relaxed">
                      {fileContent.allowedExtensions.join(', ')}
                    </span>
                  </div>
                )}
                {fileContent?.maxFileSize && (
                  <div className="flex gap-2 text-[13px] text-white/45">
                    <span className="flex-shrink-0 text-white/45">最大文件大小：</span>
                    <span className="text-white/65 leading-relaxed">
                      {formatFileSize(fileContent.maxFileSize)}
                    </span>
                  </div>
                )}
              </div>
            </section>

            {question.attachmentId && (
              <div className="bg-white/[0.06] border border-white/[0.08] rounded-xl px-5 py-4 flex items-center gap-3.5">
                <div className="w-10 h-10 rounded-lg bg-[#6677ff]/[0.1] flex items-center justify-center flex-shrink-0">
                  <PaperClipOutlined className="text-lg text-[#6677ff]" />
                </div>
                <div className="flex-1 flex flex-col gap-0.5 min-w-0">
                  <span className="text-sm font-medium text-white overflow-hidden text-ellipsis whitespace-nowrap">
                    考题附件
                  </span>
                  <span className="text-xs text-white/30">点击右侧下载</span>
                </div>
                <button
                  className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-[#6677ff]/[0.15] border-none text-[#6677ff] text-xs font-medium cursor-pointer transition-all duration-200 flex-shrink-0 hover:bg-[#6677ff]/[0.25]"
                  onClick={() => fileService.downloadFile(question.attachmentId!)}
                >
                  <DownOutlined className="text-sm" />
                  下载附件
                </button>
              </div>
            )}

            {isFileUpload ? (
              <section className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-7 h-fit">
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-2.5">
                    <UploadOutlined className="text-xl text-[#6677ff]" />
                    <h2 className="text-base font-semibold text-white m-0">上传答案</h2>
                  </div>
                  <span className="text-xs text-white/30 flex-shrink-0">{uploadHintText}</span>
                </div>
                <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
                <div className="flex-1 pt-[18px]">
                  <UploadArea />
                </div>
              </section>
            ) : (
              <div className="flex flex-col items-center justify-center gap-3 min-h-[200px] bg-white/[0.06] border border-white/[0.08] rounded-xl px-5 py-10">
                <ExperimentOutlined className="text-[40px] text-white/15" />
                <p className="text-base font-medium text-white/40 m-0">正在开发</p>
                <p className="text-[13px] text-white/25 m-0">
                  {QuestionTypeLabels[question.questionType]}功能即将上线
                </p>
              </div>
            )}
          </main>

          <aside className="w-full lg:w-80 flex-shrink-0 lg:sticky lg:top-6 lg:self-start flex flex-col gap-6">
            {isTimed && deadline && (
              <div className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-6 flex flex-col items-center gap-5">
                <div className="w-full flex items-center justify-center gap-2">
                  <ClockCircleOutlined className="text-base text-[#fa8c16]" />
                  <span className="text-[13px] font-medium text-white/65">剩余时间</span>
                </div>
                <CountdownTimer
                  deadline={deadline}
                  startedAt={session?.startTime}
                  onTimeUp={handleTimeUp}
                />
              </div>
            )}

            {!isTimed && timeInfo && (
              <div className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-5 flex flex-col gap-4">
                <div className="flex items-center gap-2">
                  <CalendarOutlined className="text-base text-[#6677ff]" />
                  <span className="text-[13px] font-medium text-white/65">考核时间</span>
                </div>
                <div className="flex flex-col items-center gap-2.5">
                  <span className="text-sm text-white/65 tabular-nums">
                    {formatDate(timeInfo.startTime)}
                  </span>
                  <DownOutlined className="text-sm text-white/30" />
                  <span className="text-sm text-white/65 tabular-nums">
                    {formatDate(timeInfo.endTime)}
                  </span>
                  {statusInfo && (
                    <span className="inline-flex items-center gap-1 px-3 py-1 rounded-md bg-[#07c160]/[0.1] text-[11px] text-[#07c160] border-none">
                      {statusInfo.text === '进行中' ? '进行中 · 无限时' : statusInfo.text}
                    </span>
                  )}
                </div>
              </div>
            )}

            <div className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-5 flex flex-col gap-4">
              <h3 className="text-sm font-semibold text-white m-0">答题信息</h3>
              <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
              <div className="flex justify-between items-center">
                <span className="text-[13px] text-white/45">考核轮次</span>
                <span className="text-[13px] text-white/65">
                  {timeInfo ? `第${timeInfo.epoch}轮考核` : '-'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[13px] text-white/45">题目序号</span>
                <span className="text-[13px] text-white/65">
                  {currentIndex >= 0 ? `${currentIndex + 1} / ${questionsList.length}` : '-'}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-[13px] text-white/45">分值</span>
                <span className="text-[13px] font-semibold text-[#fa8c16]">
                  {question.score} 分
                </span>
              </div>
            </div>

            {isFileUpload && uploadedFile && !isAnswered && !isExpired && (
              <div className="bg-white/[0.06] border border-[#07c160]/[0.1] rounded-xl p-5 flex flex-col gap-4">
                <div className="flex items-center gap-2">
                  <CheckCircleOutlined className="text-base text-[#07c160]" />
                  <span className="text-sm font-semibold text-white">已上传文件</span>
                </div>
                <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
                <div className="flex items-center gap-3 p-3.5 rounded-lg bg-white/[0.08]">
                  <div className="w-9 h-9 rounded-md bg-[#6677ff]/[0.1] flex items-center justify-center flex-shrink-0">
                    <FileOutlined className="text-base text-[#6677ff]" />
                  </div>
                  <div className="flex flex-col gap-0.5 flex-1 min-w-0">
                    <span className="text-[13px] font-medium text-white overflow-hidden text-ellipsis whitespace-nowrap">
                      {uploadedFile.name}
                    </span>
                    <span className="text-[11px] text-white/30">
                      {uploadedFile.size ? `${formatFileSize(uploadedFile.size)} · ` : ''}刚刚上传
                    </span>
                  </div>
                  <button
                    title="删除文件"
                    className="w-7 h-7 rounded-md bg-[#f5222d]/[0.24] border-none flex items-center justify-center cursor-pointer transition-colors duration-200 hover:bg-[#f5222d]/[0.48] flex-shrink-0"
                    onClick={handleRemoveFile}
                  >
                    <DeleteOutlined className="text-sm text-[#f5222d]!" />
                  </button>
                </div>
              </div>
            )}

            {isAnswered && !isResubmitting && (
              <div className="bg-white/[0.06] border border-[#07c160]/[0.1] rounded-xl p-5 flex flex-col gap-4">
                <div className="flex items-center gap-2">
                  <CheckCircleOutlined className="text-base text-[#07c160]" />
                  <span className="text-sm font-semibold text-white">已提交</span>
                </div>
                <hr className="w-full h-px bg-white/[0.04] border-none m-0" />
                <div className="text-[13px] text-white/45 mb-2">
                  提交时间：
                  {answer?.submitTime ? new Date(answer.submitTime).toLocaleString('zh-CN') : '-'}
                </div>
                {answer?.fileId && (
                  <button
                    className="inline-flex items-center gap-1 px-4 py-2 rounded-lg bg-[#6677ff]/[0.15] border-none text-[#6677ff] text-[13px] font-medium cursor-pointer transition-all duration-200 w-fit hover:bg-[#6677ff]/[0.25]"
                    onClick={() => fileService.downloadFile(answer.fileId!)}
                  >
                    <DownloadOutlined className="text-sm" />
                    下载已提交的答案
                  </button>
                )}
              </div>
            )}

            <div className="flex flex-col gap-3">
              {isFileUpload && !isAnswered && !isExpired && (
                <button
                  className="w-full h-11 rounded-lg border-none text-white text-[15px] font-semibold cursor-pointer flex items-center justify-center gap-2 transition-opacity duration-200 hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed bg-gradient-to-b from-[#6677ff] to-[#4455dd]"
                  disabled={!uploadedFile || submitting}
                  onClick={handleSubmit}
                >
                  <SendOutlined className="text-base" />
                  {submitting ? '提交中...' : '提交答案'}
                </button>
              )}
              {isFileUpload && isAnswered && isResubmitting && !isExpired && (
                <>
                  <button
                    className="w-full h-11 rounded-lg border-none text-white text-[15px] font-semibold cursor-pointer flex items-center justify-center gap-2 transition-opacity duration-200 hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed bg-gradient-to-b from-[#6677ff] to-[#4455dd]"
                    disabled={!uploadedFile || submitting}
                    onClick={handleResubmit}
                  >
                    <SendOutlined className="text-base" />
                    {submitting ? '提交中...' : '确认重新提交'}
                  </button>
                  <button
                    className="w-full h-11 rounded-lg bg-white/[0.08] border-none text-white/45 text-[15px] font-semibold cursor-pointer flex items-center justify-center gap-2 transition-opacity duration-200 hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed"
                    onClick={() => {
                      setIsResubmitting(false)
                      setUploadedFile(null)
                    }}
                  >
                    取消
                  </button>
                </>
              )}
              <div className="flex gap-3 flex-col sm:flex-row">
                <button
                  className="flex-1 h-10 rounded-lg bg-white/[0.08] border-none text-white/45 text-[13px] cursor-pointer flex items-center justify-center gap-1.5 transition-all duration-200 hover:bg-white/[0.08] hover:text-white/65 disabled:opacity-40 disabled:cursor-not-allowed"
                  onClick={handlePrev}
                  disabled={!hasPrev}
                >
                  <LeftOutlined className="text-sm" />
                  上一题
                </button>
                <button
                  className="flex-1 h-10 rounded-lg bg-white/[0.08] border-none text-white/45 text-[13px] cursor-pointer flex items-center justify-center gap-1.5 transition-all duration-200 hover:bg-white/[0.08] hover:text-white/65 disabled:opacity-40 disabled:cursor-not-allowed"
                  onClick={handleNext}
                  disabled={!hasNext}
                >
                  下一题
                  <RightOutlined className="text-sm" />
                </button>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  )
}
