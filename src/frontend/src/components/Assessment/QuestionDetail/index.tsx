'use client'

import { useState, useEffect, useCallback, useRef } from 'react'
import { useParams, useRouter } from 'next/navigation'
import {
  ArrowLeftOutlined,
  FileTextOutlined,
  PaperClipOutlined,
  DownOutlined,
  WarningOutlined,
  UploadOutlined,
  ExperimentOutlined,
  CheckCircleOutlined,
  TeamOutlined,
  PlusOutlined,
} from '@ant-design/icons'
import { Button, Tag, message, Spin, Upload, type UploadProps } from 'antd'
import { assessmentQuestionService } from '@/apis/services/assessment-question.service'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'
import { assessmentAnswerService } from '@/apis/services/assessment-answer.service'
import { assessmentSessionService } from '@/apis/services/assessment-session.service'
import { assessmentTeamService } from '@/apis/services/assessment-team.service'
import { algorithmJudgeService } from '@/apis/services/algorithm-judge.service'
import { assessmentStatisticsService } from '@/apis/services/assessment-statistics.service'
import { fileService } from '@/apis/services/file.service'
import { useAuth } from '@/hooks'
import { usePresignedUpload } from '@/hooks/usePresignedUpload'
import type {
  AssessmentQuestionDTO,
  AssessmentAnswerDTO,
  FileUploadContent,
  AlgorithmContent,
  AssessmentTimeDTO,
  AssessmentSessionDTO,
  AlgorithmTestcaseType,
  JudgeJobPollingResponseDTO,
  ProgrammingLanguage,
  QuestionStatisticsDTO,
  AssessmentTeamDTO,
} from '@/apis/schema/assessment.dto'
import { DIRECTION_LABELS as DirectionLabels } from '@/apis/schema/enumerate'
import { QuestionTypeLabels } from '@/types/assessment'
import { MarkdownRenderer } from '@/components/Assessment'
import FileUploadArea from './FileUploadArea'
import ChoiceQuestion from './ChoiceQuestion'
import AlgorithmQuestion from './AlgorithmQuestion'
import JudgeResultPanel from './JudgeResultPanel'
import QuestionSidebar from './QuestionSidebar'
import CountdownSection from './CountdownSection'
import TeamPanel from './TeamPanel'
import styles from '@/app/(public)/(other)/assessment/[timeId]/questions/[questionId]/styles.module.css'
import { getStatusInfo, formatFileSize, getUploadPhase } from './utils'
import { LANGUAGE_LABELS } from './constants'
import type { UploadedFileInfo } from './types'

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
  const [uploadedFile, setUploadedFile] = useState<UploadedFileInfo | null>(null)
  const { phase: presignedPhase, progress: uploadProgress, upload } = usePresignedUpload()
  const [isExpired, setIsExpired] = useState(false)
  const [selectedOption, setSelectedOption] = useState<string | null>(null)
  const [selectedOptions, setSelectedOptions] = useState<string[]>([])
  const [algorithmLanguage, setAlgorithmLanguage] = useState<ProgrammingLanguage | null>(null)
  const [algorithmCode, setAlgorithmCode] = useState('')
  const [algorithmRunMode, setAlgorithmRunMode] =
    useState<Exclude<AlgorithmTestcaseType, 'FORMAL'>>('DEFAULT_RUN')
  const [customInput, setCustomInput] = useState('')
  const [judgeResult, setJudgeResult] = useState<JudgeJobPollingResponseDTO | null>(null)
  const [questionStatistics, setQuestionStatistics] = useState<QuestionStatisticsDTO | null>(null)
  const [pollingJobId, setPollingJobId] = useState<number | null>(null)
  const [pollingFormalJob, setPollingFormalJob] = useState(false)
  const [teamInfo, setTeamInfo] = useState<AssessmentTeamDTO | null>(null)
  const [teamLoading, setTeamLoading] = useState(false)
  const autoSubmitRef = useRef(false)
  const { isAuthenticated, checkAuthStatus, userInfo } = useAuth()

  // 认证检查
  useEffect(() => {
    const checkAuth = async () => {
      const isAuth = await checkAuthStatus()
      if (!isAuth) router.replace('/login')
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
          if (found.endTime && new Date(found.endTime).getTime() <= Date.now()) {
            setIsExpired(true)
          }
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
        if (new Date(response.data.deadline).getTime() <= Date.now()) setIsExpired(true)
      } else {
        setSession(null)
        // 不重置 isExpired，由 fetchTimeInfo 中已做的 endTime 判断决定
      }
    } catch {
      // 网络错误等异常情况，不重置 isExpired，保留 fetchTimeInfo 中已计算好的值
      setSession(null)
    }
  }, [timeId])

  // 加载题目详情
  const fetchQuestion = useCallback(async () => {
    try {
      const response = await assessmentQuestionService.getQuestionDetail(questionId)
      if (response.code === 200 && response.data) setQuestion(response.data)
    } catch (error) {
      console.error('Failed to fetch question detail:', error)
    }
  }, [questionId])

  // 加载题目列表
  const fetchQuestionsList = useCallback(async () => {
    try {
      const response = await assessmentQuestionService.getQuestions(timeId, 0, 100)
      if (response.code === 200 && response.data) setQuestionsList(response.data.questions.content)
    } catch (error) {
      console.error('Failed to fetch questions list:', error)
    }
  }, [timeId])

  // 加载已有答案
  const fetchAnswer = useCallback(async () => {
    try {
      const response = await assessmentAnswerService.getMyAnswer(questionId)
      if (response.code === 200 && response.data) setAnswer(response.data)
    } catch {
      // 没有答案，忽略
    }
  }, [questionId])

  // 加载队伍信息
  const fetchTeamInfo = useCallback(async () => {
    if (!timeInfo?.allowTeam) return
    setTeamLoading(true)
    try {
      const response = await assessmentTeamService.getMyTeam(timeId)
      if (response.code === 200) {
        setTeamInfo(response.data)
      }
    } catch (error) {
      console.error('Failed to fetch team info:', error)
    } finally {
      setTeamLoading(false)
    }
  }, [timeId, timeInfo?.allowTeam])

  // 后端未开启候选人通过率展示时会返回错误，这里静默隐藏可选统计卡片
  const fetchQuestionStatistics = useCallback(async () => {
    try {
      const response = await assessmentStatisticsService.getCandidateQuestionStatistics(questionId)
      if (response.code === 200 && response.data) {
        setQuestionStatistics(response.data)
      } else {
        setQuestionStatistics(null)
      }
    } catch {
      setQuestionStatistics(null)
    }
  }, [questionId])

  useEffect(() => {
    if (isAuthenticated) {
      Promise.all([
        fetchQuestion(),
        fetchAnswer(),
        fetchQuestionStatistics(),
        fetchQuestionsList(),
        fetchTimeInfo().then((found) => {
          if (found?.timeLimit) return fetchSession()
          setSession(null)
          // 不重置 isExpired，由 fetchTimeInfo 内部已做的 endTime 判断决定
          return undefined
        }),
      ]).finally(() => setLoading(false))
    }
  }, [
    isAuthenticated,
    fetchQuestion,
    fetchAnswer,
    fetchQuestionStatistics,
    fetchQuestionsList,
    fetchTimeInfo,
    fetchSession,
  ])

  // timeInfo 加载完成后获取队伍信息
  useEffect(() => {
    if (isAuthenticated && timeInfo?.allowTeam && !loading) {
      fetchTeamInfo()
    }
  }, [isAuthenticated, timeInfo?.allowTeam, loading, fetchTeamInfo])

  // 非限时考核：endTime 到达时自动标记过期
  useEffect(() => {
    if (!timeInfo || timeInfo.timeLimit || !timeInfo.endTime) return
    const end = new Date(timeInfo.endTime).getTime()
    const now = Date.now()
    if (end <= now) {
      setIsExpired(true)
      return
    }
    const timer = window.setTimeout(() => setIsExpired(true), end - now)
    return () => window.clearTimeout(timer)
  }, [timeInfo])

  // 同步已有答案到选项状态
  useEffect(() => {
    if (!answer?.content) {
      setSelectedOption(null)
      setSelectedOptions([])
      if (question?.questionType !== 'ALGORITHM') return
    }
    if (question?.questionType === 'SINGLE_CHOICE' && answer?.content) {
      setSelectedOption(answer.content)
      setSelectedOptions([])
    } else if (question?.questionType === 'MULTIPLE_CHOICE' && answer?.content) {
      try {
        const parsed = JSON.parse(answer.content)
        if (Array.isArray(parsed)) setSelectedOptions(parsed)
      } catch {
        setSelectedOptions([])
      }
      setSelectedOption(null)
    } else if (question?.questionType === 'ALGORITHM') {
      const content = question.content as AlgorithmContent | null
      const languages = Object.keys(content?.starterCode ?? {}) as ProgrammingLanguage[]
      const selectedLanguage =
        answer?.language && languages.includes(answer.language) ? answer.language : languages[0]
      setAlgorithmLanguage(selectedLanguage ?? null)
      setAlgorithmCode(
        answer?.content || (selectedLanguage ? content?.starterCode?.[selectedLanguage] : '') || ''
      )
    }
  }, [answer?.content, answer?.language, question?.content, question?.questionType])

  // 判题接口按任务 ID 轮询，完成或需要人工排查时停止
  useEffect(() => {
    if (!pollingJobId) return
    let stopped = false
    const poll = async () => {
      try {
        const response = await algorithmJudgeService.getJob(pollingJobId)
        if (response.code !== 200 || !response.data || stopped) return
        setJudgeResult(response.data)
        if (
          response.data.status === 'SUCCEEDED' ||
          response.data.status === 'FAILED_REVIEW_REQUIRED'
        ) {
          setPollingJobId(null)
          if (pollingFormalJob) fetchAnswer()
        }
      } catch (error) {
        console.error('Judge polling error:', error)
      }
    }
    poll()
    const timer = window.setInterval(poll, 1500)
    return () => {
      stopped = true
      window.clearInterval(timer)
    }
  }, [pollingJobId, pollingFormalJob, fetchAnswer])

  // 提交答案（文件上传）
  const handleSubmit = async () => {
    if (isExpired) {
      message.warning('考核已结束，无法提交')
      return
    }
    if (!uploadedFile) {
      message.warning('请先上传文件')
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
        message.success('提交成功')
      } else {
        message.error(response.msg || '提交失败')
      }
    } catch (error) {
      message.error('提交失败，请重试')
      console.error('Submit error:', error)
    } finally {
      setSubmitting(false)
    }
  }

  const handleResubmit = async () => {
    if (isExpired) {
      message.warning('考核已结束，无法提交')
      return
    }
    if (!uploadedFile) {
      message.warning('请先上传新文件')
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
        message.success('重新提交成功')
      } else {
        message.error(response.msg || '重新提交失败')
      }
    } catch (error) {
      message.error('重新提交失败，请重试')
      console.error('Resubmit error:', error)
    } finally {
      setSubmitting(false)
    }
  }

  // 提交选择题答案
  const handleSubmitChoice = async () => {
    if (isExpired) {
      message.warning('考核已结束，无法提交')
      return
    }
    if (question?.questionType === 'SINGLE_CHOICE' && !selectedOption) {
      message.warning('请选择一个选项')
      return
    }
    if (question?.questionType === 'MULTIPLE_CHOICE' && selectedOptions.length === 0) {
      message.warning('请至少选择一个选项')
      return
    }
    const content =
      question?.questionType === 'SINGLE_CHOICE' ? selectedOption! : JSON.stringify(selectedOptions)
    const hasExistingAnswer = !!answer
    setSubmitting(true)
    try {
      const response = hasExistingAnswer
        ? await assessmentAnswerService.updateAnswer({ questionId, content })
        : await assessmentAnswerService.createAnswer({ questionId, content })
      if (response.code === 200 && response.data) {
        setAnswer(response.data)
        setIsResubmitting(false)
        message.success(hasExistingAnswer ? '重新提交成功' : '提交成功')
      } else {
        message.error(response.msg || '提交失败')
      }
    } catch (error) {
      message.error('提交失败，请重试')
      console.error('Choice submit error:', error)
    } finally {
      setSubmitting(false)
    }
  }

  const handleAlgorithmRun = async () => {
    if (isExpired) {
      message.warning('考核已结束，无法提交')
      return
    }
    if (!algorithmLanguage) {
      message.warning('请选择提交语言')
      return
    }
    if (!algorithmCode.trim()) {
      message.warning('请输入代码')
      return
    }
    setSubmitting(true)
    try {
      const response = await algorithmJudgeService.run({
        questionId,
        language: algorithmLanguage,
        sourceCode: algorithmCode,
        testcaseType: algorithmRunMode,
        customInput: algorithmRunMode === 'CUSTOM_RUN' ? customInput : null,
      })
      if (response.code === 200 && response.data) {
        setJudgeResult({
          judgeJobId: response.data.judgeJobId,
          testcaseType: response.data.testcaseType,
          status: 'PENDING',
          statusMessage: '等待判题',
          caseResults: [],
          judgement: null,
        })
        setPollingFormalJob(false)
        setPollingJobId(response.data.judgeJobId)
        message.success('运行任务已提交')
      } else {
        message.error(response.msg || '运行失败')
      }
    } catch (error) {
      message.error('运行失败，请重试')
      console.error('Algorithm run error:', error)
    } finally {
      setSubmitting(false)
    }
  }

  const handleAlgorithmSubmit = async () => {
    if (isExpired) {
      message.warning('考核已结束，无法提交')
      return
    }
    if (!algorithmLanguage) {
      message.warning('请选择提交语言')
      return
    }
    if (!algorithmCode.trim()) {
      message.warning('请输入代码')
      return
    }
    setSubmitting(true)
    try {
      const response = await algorithmJudgeService.submit({
        questionId,
        language: algorithmLanguage,
        content: algorithmCode,
      })
      if (response.code === 200 && response.data) {
        setJudgeResult({
          judgeJobId: response.data.judgeJobId,
          testcaseType: response.data.testcaseType,
          status: 'PENDING',
          statusMessage: '等待判题',
          caseResults: [],
          judgement: null,
        })
        setPollingFormalJob(true)
        setPollingJobId(response.data.judgeJobId)
        message.success('提交成功，正在判题')
      } else {
        message.error(response.msg || '提交失败')
      }
    } catch (error) {
      message.error('提交失败，请重试')
      console.error('Algorithm submit error:', error)
    } finally {
      setSubmitting(false)
    }
  }

  // 超时自动提交
  const handleTimeUp = useCallback(() => {
    setIsExpired(true)
    if (answer) return
    if (autoSubmitRef.current) return
    autoSubmitRef.current = true

    if (uploadedFile) {
      setSubmitting(true)
      assessmentAnswerService
        .createAnswer({ questionId, fileId: uploadedFile.id })
        .then((response) => {
          if (response.code === 200 && response.data) {
            setAnswer(response.data)
            message.warning('考核时间已到，已自动提交')
          } else {
            message.error('自动提交失败')
          }
        })
        .catch((error) => {
          message.error('自动提交失败')
          console.error('Auto-submit error:', error)
        })
        .finally(() => setSubmitting(false))
      return
    }

    const choiceContent =
      question?.questionType === 'SINGLE_CHOICE'
        ? selectedOption
        : question?.questionType === 'MULTIPLE_CHOICE' && selectedOptions.length > 0
          ? JSON.stringify(selectedOptions)
          : null
    if (choiceContent) {
      setSubmitting(true)
      assessmentAnswerService
        .createAnswer({ questionId, content: choiceContent })
        .then((response) => {
          if (response.code === 200 && response.data) {
            setAnswer(response.data)
            message.warning('考核时间已到，已自动提交')
          } else {
            message.error('自动提交失败')
          }
        })
        .catch((error) => {
          message.error('自动提交失败')
          console.error('Auto-submit error:', error)
        })
        .finally(() => setSubmitting(false))
    }
  }, [answer, uploadedFile, questionId, question?.questionType, selectedOption, selectedOptions])

  const handleRemoveFile = () => {
    if (isExpired) return
    setUploadedFile(null)
  }

  const handleDownloadFile = async (fileId: number) => {
    try {
      await fileService.downloadFile(fileId)
    } catch {
      message.error('文件不存在或无权访问')
    }
  }

  // 队伍管理操作
  const handleLeaveTeam = async () => {
    if (!teamInfo) return
    setTeamLoading(true)
    try {
      const response = await assessmentTeamService.leaveTeam({ teamId: teamInfo.id })
      if (response.code === 200) {
        setTeamInfo(null)
        message.success('已退出队伍')
      } else {
        message.error(response.msg || '退出失败')
      }
    } catch (error) {
      message.error('退出失败')
    } finally {
      setTeamLoading(false)
    }
  }

  const handleTransferLeader = async (newLeaderId: number) => {
    if (!teamInfo) return
    setTeamLoading(true)
    try {
      const response = await assessmentTeamService.transferLeader({
        teamId: teamInfo.id,
        newLeaderId,
      })
      if (response.code === 200 && response.data) {
        setTeamInfo(response.data)
        message.success('队长转让成功')
      } else {
        message.error(response.msg || '转让失败')
      }
    } catch (error) {
      message.error('转让失败')
    } finally {
      setTeamLoading(false)
    }
  }

  const handleDisbandTeam = async () => {
    if (!teamInfo) return
    setTeamLoading(true)
    try {
      const response = await assessmentTeamService.disbandTeam(teamInfo.id)
      if (response.code === 200) {
        setTeamInfo(null)
        message.success('队伍已解散')
      } else {
        message.error(response.msg || '解散失败')
      }
    } catch (error) {
      message.error('解散失败')
    } finally {
      setTeamLoading(false)
    }
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
  const isSingleChoice = question.questionType === 'SINGLE_CHOICE'
  const isMultipleChoice = question.questionType === 'MULTIPLE_CHOICE'
  const isAlgorithm = question.questionType === 'ALGORITHM'
  const isChoiceQuestion = isSingleChoice || isMultipleChoice
  const algorithmContent = isAlgorithm ? (question.content as AlgorithmContent | null) : null
  const algorithmLanguages = Object.keys(
    algorithmContent?.starterCode ?? {}
  ) as ProgrammingLanguage[]
  const algorithmLanguageOptions = algorithmLanguages.map((language) => ({
    value: language,
    label: LANGUAGE_LABELS[language] ?? language,
  }))
  const isAnswered = !!answer
  const isTimed = Boolean(timeInfo?.timeLimit && session?.deadline)
  const deadline = session?.deadline ?? null
  const statusInfo = timeInfo ? getStatusInfo(timeInfo.startTime, timeInfo.endTime) : null
  const passRateText =
    questionStatistics?.passRate !== undefined
      ? `${(Number(questionStatistics.passRate) * 100).toFixed(2)}%`
      : null

  // 队伍相关计算
  const allowTeam = timeInfo?.allowTeam ?? false
  const isInTeam = teamInfo !== null
  const isTeamLeader = isInTeam && teamInfo?.leaderId === userInfo?.id
  const canUploadFile = !allowTeam || isTeamLeader
  const showTeamPanel = allowTeam && isFileUpload
  const showTeamActionInUpload = allowTeam && isFileUpload && !isInTeam && !isExpired

  const allowedExtsText = fileContent?.allowedExtensions
    ? `支持 ${fileContent.allowedExtensions.join(', ')} 格式`
    : '支持所有文件格式'
  const maxSizeText = fileContent?.maxFileSize
    ? `最大 ${formatFileSize(fileContent.maxFileSize)}`
    : ''
  const uploadHintText = [allowedExtsText, maxSizeText].filter(Boolean).join('，')

  const uploadPhase = getUploadPhase(isExpired, isAnswered, isResubmitting, !!uploadedFile)
  const visibleJudgeCaseResults = judgeResult
    ? judgeResult.caseResults.filter(
        (caseResult) => judgeResult.testcaseType !== 'FORMAL' || caseResult.status !== 'AC'
      )
    : []

  const draggerProps: UploadProps = {
    name: 'file',
    multiple: false,
    showUploadList: false,
    accept: fileContent?.allowedExtensions?.map((ext) => `.${ext}`).join(',') || undefined,
    customRequest: async ({ file, onSuccess, onError }) => {
      try {
        const fileId = await upload(file as File, 'WORK')
        if (fileId != null) {
          setUploadedFile({
            id: fileId,
            name: (file as File).name,
            size: (file as File).size,
          })
          message.success('文件上传成功')
          onSuccess?.({ id: fileId })
        } else {
          onError?.(new Error('上传失败'))
        }
      } catch (error) {
        message.error('上传失败，请重试')
        onError?.(error as Error)
      }
    },
    beforeUpload: (file) => {
      if (fileContent?.maxFileSize && file.size > fileContent.maxFileSize) {
        message.error(`文件大小不能超过 ${formatFileSize(fileContent.maxFileSize)}`)
        return Upload.LIST_IGNORE
      }
      return true
    },
  }

  const dropHintText = [
    fileContent?.allowedExtensions ? `${fileContent.allowedExtensions.join(', ')}` : '所有文件格式',
    fileContent?.maxFileSize ? `最大 ${formatFileSize(fileContent.maxFileSize)}` : '',
  ]
    .filter(Boolean)
    .join(' · ')

  return (
    <div className="min-h-screen bg-[#0a0a0a] relative flex flex-col">
      <div className={`${styles.bg} top-0 left-0 w-full h-full z-0 pointer-events-none fixed`} />

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
        {/* Header */}
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
                <Tag color="success" className="!m-0">
                  <CheckCircleOutlined /> 已答
                </Tag>
              )}
            </div>
            <div className="flex items-center gap-4 flex-wrap">
              {timeInfo && (
                <Tag color="blue" className="!m-0">
                  {DirectionLabels[timeInfo.direction]}
                </Tag>
              )}
              {timeInfo && (
                <Tag className="!m-0 bg-[#ffffff08] border border-[#ffffff0a] text-white/45">
                  {timeInfo.grade ? `${timeInfo.grade}级` : ''}
                </Tag>
              )}
            </div>
          </div>
        </header>

        <div className="flex flex-col lg:flex-row gap-8">
          {/* Main Content */}
          <main className="flex-1 min-w-0 flex flex-col gap-6">
            {/* 题目描述 */}
            <section className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-7 h-fit">
              <div className="flex items-center gap-2.5">
                <FileTextOutlined className="text-xl text-[#fa8c16]" />
                <h2 className="text-base font-semibold text-white m-0">{question.title}</h2>
              </div>
              <hr className="w-full h-px bg-white/[0.04] border-none m-0 my-4" />
              <div className="flex flex-col gap-4">
                <MarkdownRenderer content={fileContent?.content} emptyText="暂无题目描述" />
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

            {/* 附件下载 */}
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
                  onClick={() => handleDownloadFile(question.attachmentId!)}
                >
                  <DownOutlined className="text-sm" />
                  下载附件
                </button>
              </div>
            )}

            {/* 答题区域 - 根据题型渲染不同组件 */}
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
                  {/* 允许组队但未在队伍中：显示创建/加入队伍按钮 */}
                  {showTeamActionInUpload ? (
                    <div className="flex flex-col items-center gap-4 py-8 px-4 rounded-[10px] bg-white/[0.03] border border-white/[0.06]">
                      <TeamOutlined className="text-[32px] text-[#6677ff]/40" />
                      <p className="text-[13px] text-white/45 m-0">
                        本考核允许组队，请先创建或加入队伍
                      </p>
                      <div className="flex gap-3">
                        <Button
                          type="primary"
                          icon={<PlusOutlined />}
                          onClick={() => router.push(`/assessment/${timeId}/questions`)}
                        >
                          去创建/加入队伍
                        </Button>
                      </div>
                    </div>
                  ) : /* 在队伍中但不是队长：只读显示 */
                  allowTeam && isInTeam && !isTeamLeader ? (
                    <div className="flex flex-col gap-4">
                      {answer?.fileId ? (
                        <div className="flex items-center gap-4 p-5 rounded-[10px] bg-[#07c160]/[0.06] border border-[#07c160]/[0.12]">
                          <CheckCircleOutlined className="text-[32px] text-[#07c160]" />
                          <div className="flex-1">
                            <p className="text-base font-semibold text-[#07c160] mb-1">
                              队长已提交
                            </p>
                            <p className="text-[13px] text-white/45 m-0">
                              提交时间：
                              {answer?.submitTime
                                ? new Date(answer.submitTime).toLocaleString('zh-CN')
                                : '-'}
                            </p>
                          </div>
                        </div>
                      ) : (
                        <div className="flex items-center gap-4 p-5 rounded-[10px] bg-white/[0.03] border border-white/[0.06]">
                          <TeamOutlined className="text-[32px] text-white/20" />
                          <div>
                            <p className="text-base font-semibold text-white/45 mb-1">
                              等待队长提交
                            </p>
                            <p className="text-[13px] text-white/30 m-0">
                              您无上传权限，请联系队长
                            </p>
                          </div>
                        </div>
                      )}
                    </div>
                  ) : (
                    /* 普通情况或队长：正常上传 */
                    <FileUploadArea
                      uploadPhase={uploadPhase}
                      uploadedFile={uploadedFile}
                      uploadProgress={uploadProgress}
                      presignedPhase={presignedPhase}
                      isExpired={isExpired}
                      answer={answer}
                      dropHintText={dropHintText}
                      draggerProps={draggerProps}
                      onResubmit={() => setIsResubmitting(true)}
                      onRemoveFile={handleRemoveFile}
                      onSetUploadedFile={setUploadedFile}
                    />
                  )}
                </div>
              </section>
            ) : isChoiceQuestion ? (
              <ChoiceQuestion
                question={question}
                isAnswered={isAnswered}
                isResubmitting={isResubmitting}
                isExpired={isExpired}
                selectedOption={selectedOption}
                selectedOptions={selectedOptions}
                onSelectOption={setSelectedOption}
                onToggleOption={(option) =>
                  setSelectedOptions((prev) =>
                    prev.includes(option) ? prev.filter((o) => o !== option) : [...prev, option]
                  )
                }
                onResubmit={() => setIsResubmitting(true)}
              />
            ) : isAlgorithm ? (
              <>
                <AlgorithmQuestion
                  question={question}
                  answer={answer}
                  isExpired={isExpired}
                  algorithmLanguage={algorithmLanguage}
                  algorithmCode={algorithmCode}
                  algorithmRunMode={algorithmRunMode}
                  customInput={customInput}
                  algorithmLanguageOptions={algorithmLanguageOptions}
                  onLanguageChange={(value, starterCode) => {
                    setAlgorithmLanguage(value)
                    if (starterCode !== undefined) setAlgorithmCode(starterCode)
                  }}
                  onCodeChange={setAlgorithmCode}
                  onRunModeChange={setAlgorithmRunMode}
                  onCustomInputChange={setCustomInput}
                />
                <JudgeResultPanel
                  judgeResult={judgeResult}
                  visibleCaseResults={visibleJudgeCaseResults}
                />
              </>
            ) : (
              <div className="flex flex-col items-center justify-center gap-3 min-h-[200px] bg-white/[0.06] border border-white/[0.08] rounded-xl px-5 py-10">
                <ExperimentOutlined className="text-[40px] text-white/15" />
                <p className="text-base font-medium text-white/40 m-0">暂不支持该题型</p>
                <p className="text-[13px] text-white/25 m-0">
                  {QuestionTypeLabels[question.questionType]}
                </p>
              </div>
            )}
          </main>

          {/* Sidebar */}
          <aside className="w-full lg:w-80 flex-shrink-0 lg:sticky lg:top-6 lg:self-start flex flex-col gap-6">
            {showTeamPanel && teamInfo && userInfo && (
              <TeamPanel
                team={teamInfo}
                currentUserId={userInfo.id}
                onLeaveTeam={handleLeaveTeam}
                onTransferLeader={handleTransferLeader}
                onDisbandTeam={handleDisbandTeam}
                loading={teamLoading}
              />
            )}
            <CountdownSection
              isTimed={isTimed}
              deadline={deadline}
              sessionStartTime={session?.startTime}
              onTimeUp={handleTimeUp}
              timeInfo={timeInfo}
              statusInfo={statusInfo}
            />
            <QuestionSidebar
              timeInfo={timeInfo}
              question={question}
              questionsList={questionsList}
              currentIndex={currentIndex}
              questionStatistics={questionStatistics}
              passRateText={passRateText}
              answer={answer}
              isAnswered={isAnswered}
              isResubmitting={isResubmitting}
              isExpired={isExpired}
              isFileUpload={isFileUpload}
              isChoiceQuestion={isChoiceQuestion}
              isSingleChoice={isSingleChoice}
              isAlgorithm={isAlgorithm}
              uploadedFile={uploadedFile}
              selectedOption={selectedOption}
              selectedOptions={selectedOptions}
              algorithmLanguage={algorithmLanguage}
              algorithmCode={algorithmCode}
              pollingJobId={pollingJobId}
              pollingFormalJob={pollingFormalJob}
              submitting={submitting}
              hasPrev={hasPrev}
              hasNext={hasNext}
              onPrev={handlePrev}
              onNext={handleNext}
              onSubmit={isFileUpload ? handleSubmit : handleSubmitChoice}
              onResubmitConfirm={isFileUpload ? handleResubmit : handleSubmitChoice}
              onCancelResubmit={() => {
                setIsResubmitting(false)
                setUploadedFile(null)
                if (answer?.content) {
                  if (question?.questionType === 'SINGLE_CHOICE') {
                    setSelectedOption(answer.content)
                  } else if (question?.questionType === 'MULTIPLE_CHOICE') {
                    try {
                      const parsed = JSON.parse(answer.content)
                      if (Array.isArray(parsed)) setSelectedOptions(parsed)
                    } catch {
                      /* ignore */
                    }
                  }
                }
              }}
              onAlgorithmRun={handleAlgorithmRun}
              onAlgorithmSubmit={handleAlgorithmSubmit}
              onRemoveFile={handleRemoveFile}
              onDownloadFile={handleDownloadFile}
            />
          </aside>
        </div>
      </div>
    </div>
  )
}
