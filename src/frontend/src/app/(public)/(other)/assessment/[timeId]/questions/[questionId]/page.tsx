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

/** 获取考核状态 */
function getStatusInfo(
  startTime: string,
  endTime: string
): { text: string; status: AssessmentStatus } {
  const now = Date.now()
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  if (now < start) return { text: '未开始', status: 'not-started' }
  if (now > end) return { text: '已结束', status: 'ended' }
  return { text: '进行中', status: 'in-progress' }
}

/** 格式化日期 */
function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 格式化文件大小 */
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
        if (found) setTimeInfo(found)
      }
    } catch (error) {
      console.error('Failed to fetch assessment time info:', error)
    }
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
        fetchTimeInfo(),
        fetchSession(),
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

  // ====== Loading / Not Found ======
  if (!isAuthenticated || loading) {
    return (
      <div className={styles.container}>
        <div className={styles.pageBg} />
        <div className={styles.loading}>
          <Spin size="large" />
        </div>
      </div>
    )
  }

  if (!question) {
    return (
      <div className={styles.container}>
        <div className={styles.pageBg} />
        <div className={styles.notFound}>
          <p>题目不存在或无权查看</p>
          <Button onClick={() => router.push(`/assessment/${timeId}/questions`)}>
            返回题目列表
          </Button>
        </div>
      </div>
    )
  }

  // ====== 数据准备 ======
  const fileContent = question.content as FileUploadContent | null
  const isFileUpload = question.questionType === 'file_upload'
  const isAnswered = !!answer
  const isTimed = session !== null
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
        const response = await fileService.uploadWork(file as File, questionId, (progress) => {
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
    <div className={styles.uploadedFileRow}>
      <div className={styles.uploadedFileInfo}>
        <div className={styles.uploadedFileIconWrap}>
          <FileOutlined className={styles.uploadedFileIcon} />
        </div>
        <div className={styles.uploadedFileDetail}>
          <span className={styles.uploadedFileName}>{uploadedFile!.name}</span>
          <span className={styles.uploadedFileMeta}>
            {uploadedFile!.size ? formatFileSize(uploadedFile!.size) : ''} · {meta}
          </span>
        </div>
      </div>
      <button className={styles.uploadedFileRemove} onClick={onRemove}>
        <DeleteOutlined className={styles.uploadedFileRemoveIcon} />
      </button>
    </div>
  )

  const renderProgressBar = () =>
    uploadProgress > 0 && uploadProgress < 100 ? (
      <div style={{ marginTop: 16 }}>
        <div
          style={{
            height: 4,
            borderRadius: 2,
            background: '#ffffff0a',
            overflow: 'hidden',
          }}
        >
          <div
            style={{
              height: '100%',
              width: `${uploadProgress}%`,
              background: 'linear-gradient(90deg, #6677ff, #8594ff)',
              borderRadius: 2,
              transition: 'width 0.3s ease',
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
          <div className={styles.answeredInfo}>
            <CheckCircleOutlined className={styles.answeredIcon} />
            <div style={{ flex: 1 }}>
              <p className={styles.answeredText}>已提交答案</p>
              <p className={styles.answeredTime}>
                提交时间：
                {answer?.submitTime ? new Date(answer.submitTime).toLocaleString('zh-CN') : '-'}
              </p>
            </div>
            {!isExpired && (
              <button
                className={styles.resubmitButton}
                onClick={() => {
                  setIsResubmitting(true)
                  setUploadedFile(null)
                }}
              >
                <RedoOutlined style={{ fontSize: 14 }} />
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
            <Upload.Dragger {...draggerProps} rootClassName={styles.dropZone}>
              <p className={styles.dropIcon}>
                <InboxOutlined />
              </p>
              <p className={styles.dropText}>上传新文件替换已提交的答案</p>
              <p className={styles.dropSubtext}>{dropHintText}</p>
            </Upload.Dragger>
            {renderProgressBar()}
          </>
        )
      case 'expired':
        return (
          <div className={styles.expiredUploadInfo}>
            <WarningOutlined className={styles.expiredIcon} />
            <div>
              <p className={styles.expiredText}>考核已结束</p>
              <p className={styles.expiredDesc}>
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
            <Upload.Dragger {...draggerProps} rootClassName={styles.dropZone}>
              <p className={styles.dropIcon}>
                <InboxOutlined />
              </p>
              <p className={styles.dropText}>拖拽文件到此处，或点击选择文件</p>
              <p className={styles.dropSubtext}>{dropHintText}</p>
            </Upload.Dragger>
            {renderProgressBar()}
          </>
        )
    }
  }

  // 解析题目描述中的要求列表
  const descriptionLines = fileContent?.content?.split('\n').filter(Boolean) || []
  const mainDesc = descriptionLines[0] || '暂无题目描述'
  const requirements = descriptionLines.slice(1)

  return (
    <div className={styles.container}>
      <div className={styles.pageBg} />
      {contextHolder}

      {/* 超时锁定横幅 */}
      {isExpired && (
        <div className={styles.expiredBanner}>
          <WarningOutlined style={{ marginRight: 8 }} />
          {isAnswered
            ? '考核已结束'
            : uploadedFile
              ? '考核时间已到，已自动提交'
              : '考核已结束，未提交答案'}
        </div>
      )}

      <div className={styles.pageContent}>
        {/* ====== Header ====== */}
        <header className={styles.header}>
          <button
            className={styles.backButton}
            onClick={() => router.push(`/assessment/${timeId}/questions`)}
          >
            <ArrowLeftOutlined />
            <span>返回目录</span>
          </button>

          <div className={styles.titleSection}>
            <div className={styles.titleRow}>
              <h1 className={styles.questionTitle}>
                题目 {question.questionNo} · {QuestionTypeLabels[question.questionType]}
              </h1>
              <span className={styles.scoreTag}>{question.score} 分</span>
              {isAnswered && (
                <Tag color="success" style={{ margin: 0 }}>
                  <CheckCircleOutlined /> 已答
                </Tag>
              )}
            </div>
            <div className={styles.metaRow}>
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
                  {timeInfo.grade ? ['大一', '大二', '大三'][timeInfo.grade - 1] : ''}
                </Tag>
              )}
            </div>
          </div>
        </header>

        {/* ====== 双栏布局 ====== */}
        <div className={styles.bodyLayout}>
          {/* ====== 左栏：主内容 ====== */}
          <main className={styles.mainContent}>
            {/* 题目要求卡片 */}
            <section className={styles.card}>
              <div className={styles.cardHeader}>
                <FileTextOutlined
                  className={`${styles.cardHeaderIcon} ${styles.cardHeaderIconOrange}`}
                />
                <h2 className={styles.cardHeaderTitle}>{question.title}</h2>
              </div>
              <hr className={styles.divider} />
              <div className={styles.descriptionBody}>
                <p className={styles.descriptionText}>{mainDesc}</p>
                {requirements.length > 0 && (
                  <div className={styles.requirementsList}>
                    {requirements.map((req, i) => (
                      <div key={i} className={styles.requirementItem}>
                        <span className={styles.requirementNum}>{i + 1}.</span>
                        <span className={styles.requirementText}>{req}</span>
                      </div>
                    ))}
                  </div>
                )}
                {fileContent?.allowedExtensions && (
                  <div className={styles.requirementItem}>
                    <span className={styles.requirementNum}>允许的文件类型：</span>
                    <span className={styles.requirementText}>
                      {fileContent.allowedExtensions.join(', ')}
                    </span>
                  </div>
                )}
                {fileContent?.maxFileSize && (
                  <div className={styles.requirementItem}>
                    <span className={styles.requirementNum}>最大文件大小：</span>
                    <span className={styles.requirementText}>
                      {formatFileSize(fileContent.maxFileSize)}
                    </span>
                  </div>
                )}
              </div>
            </section>

            {/* 附件下载卡片 */}
            {question.attachmentId && (
              <div className={styles.attachmentCard}>
                <div className={styles.attachmentIconWrap}>
                  <PaperClipOutlined className={styles.attachmentIcon} />
                </div>
                <div className={styles.attachmentInfo}>
                  <span className={styles.attachmentName}>考题附件</span>
                  <span className={styles.attachmentMeta}>点击右侧下载</span>
                </div>
                <button
                  className={styles.downloadButton}
                  onClick={() => fileService.downloadFile(question.attachmentId!)}
                >
                  <DownOutlined style={{ fontSize: 14 }} />
                  下载附件
                </button>
              </div>
            )}

            {/* ====== 题型内容区域 ====== */}
            {isFileUpload ? (
              <section className={styles.card}>
                <div className={styles.uploadHeader}>
                  <div className={styles.uploadTitleRow}>
                    <UploadOutlined
                      className={`${styles.cardHeaderIcon} ${styles.cardHeaderIconBlue}`}
                    />
                    <h2 className={styles.cardHeaderTitle}>上传答案</h2>
                  </div>
                  <span className={styles.uploadHint}>{uploadHintText}</span>
                </div>
                <hr className={styles.divider} />
                <div style={{ flex: 1, paddingTop: 18 }}>
                  <UploadArea />
                </div>
              </section>
            ) : (
              /* 其他题型：正在开发占位符 */
              <div className={styles.developingPlaceholder}>
                <ExperimentOutlined className={styles.developingIcon} />
                <p className={styles.developingText}>正在开发</p>
                <p className={styles.developingSubtext}>
                  {QuestionTypeLabels[question.questionType]}功能即将上线
                </p>
              </div>
            )}
          </main>

          {/* ====== 右栏：侧栏 ====== */}
          <aside className={styles.sidebar}>
            {/* 倒计时卡片（限时） */}
            {isTimed && deadline && (
              <div className={styles.timerCard}>
                <div className={styles.timerHeader}>
                  <ClockCircleOutlined className={styles.timerHeaderIcon} />
                  <span className={styles.timerLabel}>剩余时间</span>
                </div>
                <CountdownTimer
                  deadline={deadline}
                  startedAt={session?.startTime}
                  onTimeUp={handleTimeUp}
                />
              </div>
            )}

            {/* 时间范围卡片（无限时） */}
            {!isTimed && timeInfo && (
              <div className={styles.timeRangeCard}>
                <div className={styles.timeRangeHeader}>
                  <CalendarOutlined className={styles.timeRangeHeaderIcon} />
                  <span className={styles.timeRangeTitle}>考核时间</span>
                </div>
                <div className={styles.timeRangeBody}>
                  <span className={styles.timeRangeValue}>{formatDate(timeInfo.startTime)}</span>
                  <DownOutlined className={styles.timeRangeArrow} />
                  <span className={styles.timeRangeValue}>{formatDate(timeInfo.endTime)}</span>
                  {statusInfo && (
                    <span className={styles.timeRangeStatusBadge}>
                      {statusInfo.text === '进行中' ? '进行中 · 无限时' : statusInfo.text}
                    </span>
                  )}
                </div>
              </div>
            )}

            {/* 答题信息卡片 */}
            <div className={styles.infoCard}>
              <h3 className={styles.infoTitle}>答题信息</h3>
              <hr className={styles.divider} />
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>考核轮次</span>
                <span className={styles.infoValue}>
                  {timeInfo ? `第${timeInfo.epoch}轮考核` : '-'}
                </span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>题目序号</span>
                <span className={styles.infoValue}>
                  {currentIndex >= 0 ? `${currentIndex + 1} / ${questionsList.length}` : '-'}
                </span>
              </div>
              <div className={styles.infoRow}>
                <span className={styles.infoLabel}>分值</span>
                <span className={styles.infoValueHighlight}>{question.score} 分</span>
              </div>
            </div>

            {/* 已上传文件卡片（仅文件上传题 + 已上传文件 + 未提交） */}
            {isFileUpload && uploadedFile && !isAnswered && !isExpired && (
              <div className={styles.uploadedCard}>
                <div className={styles.uploadedCardHeader}>
                  <CheckCircleOutlined className={styles.uploadedCardIcon} />
                  <span className={styles.uploadedCardTitle}>已上传文件</span>
                </div>
                <hr className={styles.divider} />
                <div className={styles.uploadedCardItem}>
                  <div className={styles.uploadedFileIconWrap}>
                    <FileOutlined className={styles.uploadedFileIcon} />
                  </div>
                  <div className={styles.uploadedFileDetail}>
                    <span className={styles.uploadedFileName}>{uploadedFile.name}</span>
                    <span className={styles.uploadedFileMeta}>
                      {uploadedFile.size ? `${formatFileSize(uploadedFile.size)} · ` : ''}刚刚上传
                    </span>
                  </div>
                  <button className={styles.uploadedFileRemove} onClick={handleRemoveFile}>
                    <DeleteOutlined className={styles.uploadedFileRemoveIcon} />
                  </button>
                </div>
              </div>
            )}

            {/* 已提交状态卡片 */}
            {isAnswered && !isResubmitting && (
              <div className={styles.uploadedCard}>
                <div className={styles.uploadedCardHeader}>
                  <CheckCircleOutlined className={styles.uploadedCardIcon} />
                  <span className={styles.uploadedCardTitle}>已提交</span>
                </div>
                <hr className={styles.divider} />
                <div
                  style={{
                    fontSize: 13,
                    color: 'rgba(255, 255, 255, 0.45)',
                    marginBottom: 8,
                  }}
                >
                  提交时间：
                  {answer?.submitTime ? new Date(answer.submitTime).toLocaleString('zh-CN') : '-'}
                </div>
                {answer?.fileId && (
                  <button
                    className={styles.downloadButton}
                    onClick={() => fileService.downloadFile(answer.fileId!)}
                    style={{ display: 'inline-flex', alignItems: 'center', gap: 4, fontSize: 13 }}
                  >
                    <DownloadOutlined style={{ fontSize: 14 }} />
                    下载已提交的答案
                  </button>
                )}
              </div>
            )}

            {/* 提交区域 */}
            <div className={styles.submitSection}>
              {isFileUpload && !isAnswered && !isExpired && (
                <button
                  className={styles.submitButton}
                  disabled={!uploadedFile || submitting}
                  onClick={handleSubmit}
                >
                  <SendOutlined className={styles.submitButtonIcon} />
                  {submitting ? '提交中...' : '提交答案'}
                </button>
              )}
              {isFileUpload && isAnswered && isResubmitting && !isExpired && (
                <>
                  <button
                    className={styles.submitButton}
                    disabled={!uploadedFile || submitting}
                    onClick={handleResubmit}
                  >
                    <SendOutlined className={styles.submitButtonIcon} />
                    {submitting ? '提交中...' : '确认重新提交'}
                  </button>
                  <button
                    className={styles.cancelButton}
                    onClick={() => {
                      setIsResubmitting(false)
                      setUploadedFile(null)
                    }}
                    style={{ width: '100%', marginTop: 8 }}
                  >
                    取消
                  </button>
                </>
              )}
              <div className={styles.navRow}>
                <button className={styles.navButton} onClick={handlePrev} disabled={!hasPrev}>
                  <LeftOutlined style={{ fontSize: 14 }} />
                  上一题
                </button>
                <button className={styles.navButton} onClick={handleNext} disabled={!hasNext}>
                  下一题
                  <RightOutlined style={{ fontSize: 14 }} />
                </button>
              </div>
            </div>
          </aside>
        </div>
      </div>
    </div>
  )
}
