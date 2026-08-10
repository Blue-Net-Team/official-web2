'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Alert,
  App,
  Button,
  Card,
  Collapse,
  Descriptions,
  Drawer,
  Empty,
  Form,
  Grid,
  Input,
  InputNumber,
  Select,
  Spin,
  Statistic,
  Steps,
  Table,
  Tabs,
  Tag,
  Timeline,
} from 'antd'
import type { TableColumnsType } from 'antd'
import { DownloadOutlined, TeamOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import type {
  AssessmentCandidateQuestionScoreDTO,
  AssessmentCandidateScoreboardDTO,
  AssessmentDecisionCandidateDTO,
  AssessmentDecisionWorkspaceDTO,
  AssessmentQuestionScoreboardDTO,
  AssessmentQuestionSubmissionDTO,
  AssessmentTimeDTO,
  CommentDTO,
  QuestionType,
} from '@/apis/schema/assessment.dto'
import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { adminAssessmentJudgementService } from '@/apis/services/admin-assessment-judgement.service'
import { adminAssessmentTimeService } from '@/apis/services/admin-assessment-time.service'
import { adminCommentService } from '@/apis/services/admin-comment.service'
import { fileService } from '@/apis/services/file.service'
import { useAuth } from '@/hooks'
import { getRoleLevel } from '@/utils/RoleUtils'

type DirectionOrGlobal = Direction | 'GLOBAL'
import {
  QUESTION_TYPE_LABELS,
  QUESTION_TYPE_COLORS,
  formatScore,
  formatTime,
  getResultColor,
  getDecisionTag,
  getReferralTag,
} from '../shared'

const sanitizeFilenameSegment = (value: string) => value.replace(/[\\/:*?"<>|]/g, '_').trim()

/**
 * 拼接管理端下载考生作品时使用的文件名（不含扩展名）。
 * 任一关键字段缺失时返回 undefined，让 fileService 回落到响应头里的原始文件名。
 */
const buildWorkFilename = (params: {
  direction?: Direction | null
  epoch?: number | null
  grade?: number | null
  username?: string
  questionNo?: number | null
}): string | undefined => {
  const { direction, epoch, grade, username, questionNo } = params
  if (!direction || !epoch || !grade || !username || !questionNo) return undefined
  const directionLabel = DIRECTION_LABELS[direction] ?? direction
  return sanitizeFilenameSegment(
    `${directionLabel}-第${epoch}轮-${grade}级-${username}-第${questionNo}题`
  )
}

export default function AssessmentJudgementManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = Grid.useBreakpoint()
  const [form] = Form.useForm<{ score: number }>()

  const { userInfo } = useAuth()
  const isSuperAdmin = getRoleLevel(userInfo?.roleName || '') >= 3
  const isDecisionMaker = getRoleLevel(userInfo?.roleName || '') >= 2
  const userDirection = userInfo?.direction

  const [activeTab, setActiveTab] = useState('score')
  const [scoreView, setScoreView] = useState('questions')
  const [direction, setDirection] = useState<DirectionOrGlobal | undefined>(
    isSuperAdmin ? undefined : (userDirection ?? undefined)
  )
  const [assessmentTimeId, setAssessmentTimeId] = useState<number | undefined>()
  const [assessmentTimes, setAssessmentTimes] = useState<AssessmentTimeDTO[]>([])

  const [questionType, setQuestionType] = useState<QuestionType | undefined>()
  const [scoreKeyword, setScoreKeyword] = useState('')
  const [submissionStatus, setSubmissionStatus] = useState<'JUDGED' | 'PENDING' | undefined>()
  const [questions, setQuestions] = useState<AssessmentQuestionScoreboardDTO[]>([])
  const [selectedQuestionId, setSelectedQuestionId] = useState<number | undefined>()
  const [submissions, setSubmissions] = useState<AssessmentQuestionSubmissionDTO[]>([])
  const [selectedSubmissionIds, setSelectedSubmissionIds] = useState<number[]>([])
  const [candidateScores, setCandidateScores] = useState<AssessmentCandidateScoreboardDTO[]>([])

  type SubmissionTeamHeader = {
    type: 'team'
    key: string
    teamId: number
    teamName: string
    leaderName: string
    memberCount: number
    members: AssessmentQuestionSubmissionDTO[]
  }
  type SubmissionIndependentRow = {
    type: 'independent'
    key: string
  } & AssessmentQuestionSubmissionDTO
  type SubmissionRow = SubmissionTeamHeader | SubmissionIndependentRow

  /** 题目视图：将提交列表按队伍分组，用于 expandable 渲染。 */
  const groupedSubmissions = useMemo(() => {
    const result: SubmissionRow[] = []
    const teams = new Map<number, AssessmentQuestionSubmissionDTO[]>()
    const independents: AssessmentQuestionSubmissionDTO[] = []

    for (const s of submissions) {
      if (s.teamId) {
        if (!teams.has(s.teamId)) teams.set(s.teamId, [])
        teams.get(s.teamId)!.push(s)
      } else {
        independents.push(s)
      }
    }

    for (const [teamId, members] of teams) {
      const leader = members.find((m) => m.isLeader)
      result.push({
        type: 'team',
        key: `sub-team-${teamId}`,
        teamId,
        teamName: members[0].teamName!,
        leaderName: leader?.username ?? '',
        memberCount: members.length,
        members,
      })
    }

    for (const s of independents) {
      result.push({ type: 'independent', key: `sub-ind-${s.answerId}`, ...s })
    }

    return result
  }, [submissions])

  const [decisionKeyword, setDecisionKeyword] = useState('')
  const [decisionStatus, setDecisionStatus] = useState<
    'PENDING' | 'PASSED' | 'ELIMINATED' | undefined
  >()
  const [decisionWorkspace, setDecisionWorkspace] = useState<AssessmentDecisionWorkspaceDTO | null>(
    null
  )
  const [selectedDecisionCandidate, setSelectedDecisionCandidate] =
    useState<AssessmentDecisionCandidateDTO | null>(null)
  const [decisionComment, setDecisionComment] = useState('')

  const [loadingTimes, setLoadingTimes] = useState(false)
  const [loadingQuestions, setLoadingQuestions] = useState(false)
  const [loadingSubmissions, setLoadingSubmissions] = useState(false)
  const [loadingCandidates, setLoadingCandidates] = useState(false)
  const [loadingDecisions, setLoadingDecisions] = useState(false)
  const [reviewing, setReviewing] = useState<AssessmentQuestionSubmissionDTO | null>(null)
  const [viewingCandidateScore, setViewingCandidateScore] =
    useState<AssessmentCandidateScoreboardDTO | null>(null)
  const [candidateQuestionDetails, setCandidateQuestionDetails] = useState<
    Record<number, AssessmentQuestionSubmissionDTO | null>
  >({})
  const [loadingCandidateQuestionIds, setLoadingCandidateQuestionIds] = useState<number[]>([])
  const [savingDecisionUserId, setSavingDecisionUserId] = useState<number | null>(null)

  // 评论相关状态
  const [comments, setComments] = useState<CommentDTO[]>([])
  const [loadingComments, setLoadingComments] = useState(false)
  const [commentForm] = Form.useForm<{ content: string; score?: number }>()
  const [editCommentForm] = Form.useForm<{ content: string; score?: number }>()
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null)
  const [savingComment, setSavingComment] = useState(false)
  const [savingFinalize, setSavingFinalize] = useState(false)

  const directionOptions = useMemo(() => {
    const entries = Object.entries(DIRECTION_LABELS) as [Direction, string][]
    if (!isSuperAdmin && userDirection) {
      return entries.filter(([value]) => value === userDirection)
    }
    if (isSuperAdmin) {
      return [...entries, ['GLOBAL' as DirectionOrGlobal, '全局']]
    }
    return entries
  }, [isSuperAdmin, userDirection])

  const timeOptions = useMemo(
    () =>
      assessmentTimes.map((item) => ({
        value: item.id,
        label: `${item.direction ? DIRECTION_LABELS[item.direction] : '全局'} · 第 ${item.epoch} 轮 · ${item.grade}级`,
      })),
    [assessmentTimes]
  )

  /** 当前选中的考核时间，用于拼接下载文件名等业务上下文。 */
  const currentAssessmentTime = useMemo(
    () => assessmentTimes.find((item) => item.id === assessmentTimeId) ?? null,
    [assessmentTimes, assessmentTimeId]
  )

  const selectedQuestion = useMemo(
    () => questions.find((item) => item.questionId === selectedQuestionId) ?? null,
    [questions, selectedQuestionId]
  )

  /** 加载指定方向下可评判的考核时间。 */
  const fetchAssessmentTimes = useCallback(
    async (nextDirection: DirectionOrGlobal) => {
      setLoadingTimes(true)
      try {
        const response = await adminAssessmentTimeService.getList(0, 100)
        if (nextDirection === 'GLOBAL') {
          setAssessmentTimes(response.data?.content.filter((item) => item.direction === null) ?? [])
        } else {
          setAssessmentTimes(
            response.data?.content.filter((item) => item.direction === nextDirection) ?? []
          )
        }
      } catch {
        messageApi.error('加载考核时间失败')
      } finally {
        setLoadingTimes(false)
      }
    },
    [messageApi]
  )

  /** 加载题目维度的评分汇总。 */
  const fetchQuestionScoreboard = useCallback(async () => {
    if (!assessmentTimeId) {
      setQuestions([])
      setSelectedQuestionId(undefined)
      return
    }
    setLoadingQuestions(true)
    try {
      const response = await adminAssessmentJudgementService.getQuestionScoreboard({
        assessmentTimeId,
        questionType,
        keyword: scoreKeyword || undefined,
      })
      const list = response.data ?? []
      setQuestions(list)
      setSelectedQuestionId((current) => current ?? list[0]?.questionId)
    } catch {
      messageApi.error('加载题目评分汇总失败')
    } finally {
      setLoadingQuestions(false)
    }
  }, [assessmentTimeId, questionType, scoreKeyword, messageApi])

  /** 加载当前选中题目的考生提交列表。 */
  const fetchQuestionSubmissions = useCallback(async () => {
    if (!selectedQuestionId) {
      setSubmissions([])
      return
    }
    setLoadingSubmissions(true)
    try {
      const response = await adminAssessmentJudgementService.getQuestionSubmissions(
        selectedQuestionId,
        {
          keyword: scoreKeyword || undefined,
          status: submissionStatus,
        }
      )
      setSubmissions(response.data ?? [])
    } catch {
      messageApi.error('加载题目提交失败')
    } finally {
      setLoadingSubmissions(false)
    }
  }, [selectedQuestionId, scoreKeyword, submissionStatus, messageApi])

  /** 加载人员视图的考生评分矩阵。 */
  const fetchCandidateScoreboard = useCallback(async () => {
    if (!assessmentTimeId) {
      setCandidateScores([])
      return
    }
    setLoadingCandidates(true)
    try {
      const response = await adminAssessmentJudgementService.getCandidateScoreboard({
        assessmentTimeId,
        keyword: scoreKeyword || undefined,
      })
      setCandidateScores(response.data ?? [])
    } catch {
      messageApi.error('加载人员评分视图失败')
    } finally {
      setLoadingCandidates(false)
    }
  }, [assessmentTimeId, scoreKeyword, messageApi])

  /** 加载录用决策工作台数据。 */
  const fetchDecisionWorkspace = useCallback(async () => {
    if (!assessmentTimeId) {
      setDecisionWorkspace(null)
      setSelectedDecisionCandidate(null)
      return
    }
    setLoadingDecisions(true)
    try {
      const response = await adminAssessmentJudgementService.getDecisionWorkspace({
        assessmentTimeId,
        keyword: decisionKeyword || undefined,
        decisionStatus,
      })
      const workspace = response.data
      setDecisionWorkspace(workspace)
      setSelectedDecisionCandidate((current) => {
        if (!workspace?.candidates.length) return null
        return (
          workspace.candidates.find((item) => item.candidateUserId === current?.candidateUserId) ??
          workspace.candidates[0]
        )
      })
    } catch {
      messageApi.error('加载录用决策数据失败')
    } finally {
      setLoadingDecisions(false)
    }
  }, [assessmentTimeId, decisionKeyword, decisionStatus, messageApi])

  useEffect(() => {
    if (direction === undefined) {
      setAssessmentTimes([])
      setAssessmentTimeId(undefined)
      return
    }
    fetchAssessmentTimes(direction)
    setAssessmentTimeId(undefined)
  }, [direction, fetchAssessmentTimes])

  useEffect(() => {
    setSelectedSubmissionIds([])
  }, [selectedQuestionId])

  /** 全局考核（支持组队）下人员视图意义不大，自动切换到题目视图。 */
  useEffect(() => {
    if (currentAssessmentTime?.direction === null && scoreView === 'candidates') {
      setScoreView('questions')
    }
  }, [currentAssessmentTime, scoreView])

  useEffect(() => {
    if (activeTab === 'score') {
      fetchQuestionScoreboard()
    }
  }, [activeTab, fetchQuestionScoreboard])

  useEffect(() => {
    if (activeTab === 'score' && scoreView === 'questions') {
      fetchQuestionSubmissions()
    }
  }, [activeTab, scoreView, fetchQuestionSubmissions])

  useEffect(() => {
    setCandidateQuestionDetails({})
    setLoadingCandidateQuestionIds([])
  }, [viewingCandidateScore?.candidateUserId])

  useEffect(() => {
    if (activeTab === 'score' && scoreView === 'candidates') {
      fetchCandidateScoreboard()
    }
  }, [activeTab, scoreView, fetchCandidateScoreboard])

  useEffect(() => {
    if (activeTab === 'decision') {
      fetchDecisionWorkspace()
    }
  }, [activeTab, fetchDecisionWorkspace])

  useEffect(() => {
    if (!reviewing) return
    form.setFieldsValue({
      score: Number(reviewing.latestJudgement?.score ?? 0),
    })
    fetchComments(reviewing.answerId)
  }, [reviewing, form])

  /** 加载评论列表 */
  const fetchComments = async (answerId: number) => {
    setLoadingComments(true)
    try {
      const response = await adminCommentService.listComments(answerId)
      setComments(response.data ?? [])
    } catch {
      messageApi.error('加载评论失败')
    } finally {
      setLoadingComments(false)
    }
  }

  /** 提交评论 */
  const handleSubmitComment = async () => {
    if (!reviewing) return
    const values = await commentForm.validateFields()
    setSavingComment(true)
    try {
      await adminCommentService.addComment({
        answerId: reviewing.answerId,
        content: values.content || undefined,
        score: values.score ?? undefined,
      })
      messageApi.success('评论已提交')
      commentForm.resetFields()
      await fetchComments(reviewing.answerId)
    } catch {
      messageApi.error('提交评论失败')
    } finally {
      setSavingComment(false)
    }
  }

  /** 更新评论 */
  const handleUpdateComment = async (commentId: number) => {
    const values = await editCommentForm.validateFields()
    setSavingComment(true)
    try {
      await adminCommentService.updateComment(commentId, {
        answerId: reviewing!.answerId,
        content: values.content || undefined,
        score: values.score ?? undefined,
      })
      messageApi.success('评论已更新')
      setEditingCommentId(null)
      await fetchComments(reviewing!.answerId)
    } catch {
      messageApi.error('更新评论失败')
    } finally {
      setSavingComment(false)
    }
  }

  /** 删除评论 */
  const handleDeleteComment = async (commentId: number) => {
    try {
      await adminCommentService.deleteComment(commentId)
      messageApi.success('评论已删除')
      if (reviewing) {
        await fetchComments(reviewing.answerId)
      }
    } catch {
      messageApi.error('删除评论失败')
    }
  }

  /** 确认最终评分 */
  const handleFinalizeScore = async () => {
    if (!reviewing) return
    const values = await form.validateFields()
    setSavingFinalize(true)
    try {
      await adminAssessmentJudgementService.finalizeScore({
        answerId: reviewing.answerId,
        score: values.score,
      })
      messageApi.success('最终评分已确认')
      setReviewing(null)
      await refreshAfterScore()
    } catch {
      messageApi.error('确认最终评分失败')
    } finally {
      setSavingFinalize(false)
    }
  }

  useEffect(() => {
    setDecisionComment(selectedDecisionCandidate?.decisionComment ?? '')
  }, [selectedDecisionCandidate])

  /** 人工评分保存后刷新所有受分数影响的数据。 */
  const refreshAfterScore = async () => {
    await Promise.all([
      fetchQuestionScoreboard(),
      fetchQuestionSubmissions(),
      fetchCandidateScoreboard(),
    ])
    if (activeTab === 'decision') {
      await fetchDecisionWorkspace()
    }
  }

  /** 保存候选人的通过或淘汰决策。 */
  const handleDecision = async (candidate: AssessmentDecisionCandidateDTO, passed: boolean) => {
    if (!assessmentTimeId) return
    setSavingDecisionUserId(candidate.candidateUserId)
    try {
      await adminAssessmentJudgementService.decide({
        userId: candidate.candidateUserId,
        assessmentTimeId,
        passed,
        decisionComment: decisionComment || undefined,
      })
      messageApi.success(passed ? '已标记通过' : '已标记淘汰')
      await fetchDecisionWorkspace()
    } catch {
      messageApi.error('保存决策失败')
    } finally {
      setSavingDecisionUserId(null)
    }
  }

  /** 向已决策考生发送结果通知邮件。 */
  const handlePublish = async () => {
    if (!assessmentTimeId) return
    try {
      const res = await adminAssessmentJudgementService.publishDecisions(assessmentTimeId)
      messageApi.success(`已发送 ${res.data} 封通知邮件`)
    } catch {
      messageApi.error('发布失败，请稍后重试')
    }
  }

  /** 批量下载选中考生答案为 ZIP。 */
  const handleBatchDownload = async () => {
    if (!selectedQuestion || !currentAssessmentTime || selectedSubmissionIds.length === 0) return

    const selectedSubmissions = submissions.filter((item) =>
      selectedSubmissionIds.includes(item.answerId)
    )
    if (selectedSubmissions.length === 0) return

    const nameCount = new Map<string, number>()
    const entries = selectedSubmissions.map((sub) => {
      const baseName = buildWorkFilename({
        direction: currentAssessmentTime.direction,
        epoch: currentAssessmentTime.epoch,
        grade: currentAssessmentTime.grade,
        username: sub.username,
        questionNo: sub.questionNo,
      })
      let filename = baseName ?? sub.username
      const count = nameCount.get(filename) ?? 0
      nameCount.set(filename, count + 1)
      if (count > 0) {
        filename = `${filename}_${count}`
      }
      return { fileId: sub.fileId!, filename }
    })

    const directionLabel = currentAssessmentTime.direction
      ? DIRECTION_LABELS[currentAssessmentTime.direction]
      : '全局'
    const zipName = sanitizeFilenameSegment(
      `${directionLabel}-第${currentAssessmentTime.epoch}轮-${currentAssessmentTime.grade}级-第${selectedQuestion.questionNo}题-考生答案.zip`
    )

    try {
      await fileService.downloadBatch(entries, zipName)
      setSelectedSubmissionIds([])
    } catch {
      messageApi.error('批量下载失败')
    }
  }

  /** 题目视图左侧题目汇总表列定义。 */
  const questionColumns: TableColumnsType<AssessmentQuestionScoreboardDTO> = [
    {
      title: '题目',
      dataIndex: 'title',
      render: (title: string, record) => (
        <div>
          <div className="text-white/85">
            #{record.questionNo} {title}
          </div>
          <Tag color={QUESTION_TYPE_COLORS[record.questionType]} bordered={false}>
            {QUESTION_TYPE_LABELS[record.questionType]}
          </Tag>
        </div>
      ),
    },
    { title: '满分', dataIndex: 'maxScore', width: 70, render: formatScore },
    { title: '提交', dataIndex: 'submittedCount', width: 70 },
    { title: '待评', dataIndex: 'pendingCount', width: 70 },
    { title: '均分', dataIndex: 'averageScore', width: 70, render: formatScore },
  ]

  /** 题目视图内嵌组员提交列表列定义（不含队伍头行逻辑）。 */
  const submissionMemberColumns: TableColumnsType<AssessmentQuestionSubmissionDTO> = [
    {
      title: '考生',
      render: (_, record) => (
        <div>
          <div className="flex items-center gap-1.5">
            <span className="text-white/85">{record.username}</span>
            {getReferralTag(record)}
          </div>
          <div className="text-xs text-white/35">{record.studentId}</div>
        </div>
      ),
    },
    {
      title: '评判时间',
      width: 150,
      render: (_, record) => formatTime(record.latestJudgement?.judgedAt),
    },
    {
      title: '得分',
      width: 90,
      render: (_, record) =>
        record.latestJudgement ? (
          <span>
            {formatScore(record.latestJudgement.score)} / {formatScore(record.maxScore)}
          </span>
        ) : (
          <Tag>待评分</Tag>
        ),
    },
    {
      title: '结果',
      width: 80,
      render: (_, record) => {
        const resultCode = record.latestJudgement?.resultCode
        if (!resultCode) return <span className="text-white/35">-</span>
        return <Tag color={getResultColor(resultCode)}>{resultCode}</Tag>
      },
    },
    {
      title: '状态',
      width: 90,
      render: (_, record) =>
        record.latestJudgement ? <Tag color="green">已评分</Tag> : <Tag color="orange">待评分</Tag>,
    },
  ]

  /** 题目视图右侧提交列表列定义。 */
  const submissionColumns: TableColumnsType<SubmissionRow> = [
    {
      title: '考生',
      render: (_, record) => {
        if (record.type === 'team') {
          return (
            <div className="flex items-center gap-2 font-medium">
              <TeamOutlined />
              <span>{record.teamName}</span>
              <Tag>{record.members!.length}人</Tag>
              <Tag color="blue">
                队长: {(record.members ?? []).find((m) => m.isLeader)?.username ?? ''}
              </Tag>
            </div>
          )
        }
        return (
          <div>
            <div className="flex items-center gap-1.5">
              <span className="text-white/85">{record.username}</span>
              {record.teamId && (
                <Tag color={record.isLeader ? 'blue' : 'default'}>
                  {record.isLeader ? '👑' : ''}
                  {record.teamName}
                </Tag>
              )}
              {getReferralTag(record)}
            </div>
            <div className="text-xs text-white/35">{record.studentId}</div>
          </div>
        )
      },
    },
    {
      title: '评判时间',
      width: 150,
      render: (_, record) =>
        record.type === 'team' ? null : formatTime(record.latestJudgement?.judgedAt),
    },
    {
      title: '得分',
      width: 90,
      render: (_, record) => {
        if (record.type === 'team') return null
        return record.latestJudgement ? (
          <span>
            {formatScore(record.latestJudgement.score)} / {formatScore(record.maxScore)}
          </span>
        ) : (
          <Tag>待评分</Tag>
        )
      },
    },
    {
      title: '结果',
      width: 80,
      render: (_, record) => {
        if (record.type === 'team') return null
        const resultCode = record.latestJudgement?.resultCode
        if (!resultCode) return <span className="text-white/35">-</span>
        return <Tag color={getResultColor(resultCode)}>{resultCode}</Tag>
      },
    },
    {
      title: '状态',
      width: 90,
      render: (_, record) => {
        if (record.type === 'team') return null
        return record.latestJudgement ? (
          <Tag color="green">已评分</Tag>
        ) : (
          <Tag color="orange">待评分</Tag>
        )
      },
    },
    {
      title: '操作',
      width: 120,
      render: (_, record) => {
        if (record.type === 'team') {
          // 同一队伍作品相同，下载按钮上移到头行
          const fileId =
            record.members.find((m) => m.isLeader)?.fileId ??
            record.members.find((m) => m.fileId)?.fileId
          return fileId ? (
            <Button
              size="small"
              icon={<DownloadOutlined />}
              onClick={(e) => {
                e.stopPropagation()
                fileService.downloadFile(fileId)
              }}
            >
              下载作品
            </Button>
          ) : null
        }
        return record.fileId ? (
          <Button
            size="small"
            icon={<DownloadOutlined />}
            onClick={(e) => {
              e.stopPropagation()
              fileService.downloadFile(record.fileId!)
            }}
          >
            下载
          </Button>
        ) : null
      },
    },
  ]

  /** 渲染单个考生在当前题目下的完整提交评判历史。 */
  const renderSubmissionHistory = (record: AssessmentQuestionSubmissionDTO) => {
    const histories = record.histories ?? []
    const sortedHistories = [...histories].sort((left, right) => {
      const leftTime = left.judgement?.judgedAt ? dayjs(left.judgement.judgedAt).valueOf() : 0
      const rightTime = right.judgement?.judgedAt ? dayjs(right.judgement.judgedAt).valueOf() : 0
      return rightTime - leftTime
    })
    return (
      <div className="px-2 py-2">
        {/* 用 AntD Collapse 和 Timeline 表达历史记录，避免表格套表格造成层级过重。 */}
        <Collapse
          size="small"
          defaultActiveKey={['history']}
          items={[
            {
              key: 'history',
              label: (
                <div className="flex flex-wrap items-center gap-2">
                  <span className="text-white/85">提交评判历史</span>
                  <Tag color="blue" bordered={false}>
                    {sortedHistories.length} 条记录
                  </Tag>
                </div>
              ),
              children: (
                <div className="max-h-[360px] overflow-y-auto overscroll-contain pr-2">
                  <Timeline
                    className="mt-3"
                    items={sortedHistories.map((history, index) => {
                      const judgement = history.judgement
                      const resultCode = judgement?.resultCode ?? '暂无结果'
                      const resultColor = getResultColor(judgement?.resultCode)
                      return {
                        color: resultColor,
                        children: (
                          <div className="rounded-xl border border-white/10 bg-black/20 px-3 py-2">
                            <div className="flex flex-wrap items-center justify-between gap-2">
                              <div className="flex flex-wrap items-center gap-2">
                                <Tag color={resultColor} bordered={false}>
                                  {resultCode}
                                </Tag>
                                {history.selectedBest && <Tag color="green">当前展示</Tag>}
                                <span className="text-white/80">
                                  {formatScore(judgement?.score)} /{' '}
                                  {formatScore(judgement?.maxScore)}
                                </span>
                              </div>
                              <span className="text-xs text-white/40">
                                {formatTime(judgement?.judgedAt)}
                              </span>
                            </div>
                            <div className="mt-1 text-xs text-white/40">
                              第 {index + 1} 条 · {judgement?.source ?? '-'}
                            </div>
                          </div>
                        ),
                      }
                    })}
                  />
                </div>
              ),
            },
          ]}
        />
      </div>
    )
  }

  /** 按需加载人员视图抽屉中某道题的提交详情。 */
  const fetchCandidateQuestionDetail = useCallback(
    async (question: AssessmentCandidateQuestionScoreDTO) => {
      if (!viewingCandidateScore || candidateQuestionDetails[question.questionId] !== undefined) {
        return
      }
      setLoadingCandidateQuestionIds((current) => [...current, question.questionId])
      try {
        const response = await adminAssessmentJudgementService.getQuestionSubmissions(
          question.questionId
        )
        const detail =
          response.data?.find(
            (item) => item.candidateUserId === viewingCandidateScore.candidateUserId
          ) ?? null
        setCandidateQuestionDetails((current) => ({
          ...current,
          [question.questionId]: detail,
        }))
      } catch {
        messageApi.error('加载题目提交记录失败')
      } finally {
        setLoadingCandidateQuestionIds((current) =>
          current.filter((id) => id !== question.questionId)
        )
      }
    },
    [candidateQuestionDetails, messageApi, viewingCandidateScore]
  )

  /** 渲染人员视图抽屉内的单题提交详情。 */
  const renderCandidateQuestionDetail = (question: AssessmentCandidateQuestionScoreDTO) => {
    const detail = candidateQuestionDetails[question.questionId]
    const loading = loadingCandidateQuestionIds.includes(question.questionId)
    if (loading) {
      return (
        <div className="py-6">
          <Spin />
        </div>
      )
    }
    if (!question.submitted) {
      return <Empty description="该题未提交" />
    }
    if (!detail) {
      return <Empty description="暂无提交记录" />
    }
    return (
      <div className="flex flex-col gap-3 px-2 py-2">
        <Descriptions column={screens.md ? 2 : 1} size="small">
          <Descriptions.Item label="答案提交时间">
            {formatTime(detail.submitTime)}
          </Descriptions.Item>
          <Descriptions.Item label="展示评判时间">
            {formatTime(detail.latestJudgement?.judgedAt)}
          </Descriptions.Item>
          <Descriptions.Item label="得分">
            {detail.latestJudgement
              ? `${formatScore(detail.latestJudgement.score)} / ${formatScore(detail.maxScore)}`
              : '待评分'}
          </Descriptions.Item>
          <Descriptions.Item label="结果">
            {detail.latestJudgement?.resultCode ? (
              <Tag color={getResultColor(detail.latestJudgement.resultCode)}>
                {detail.latestJudgement.resultCode}
              </Tag>
            ) : (
              '-'
            )}
          </Descriptions.Item>
        </Descriptions>
        {detail.fileId && (
          <Button
            size="small"
            icon={<DownloadOutlined />}
            onClick={() => fileService.downloadFile(detail.fileId!)}
          >
            下载答案文件
          </Button>
        )}
        {detail.content && (
          <Card size="small" title="答案内容">
            <pre className="m-0 max-h-40 overflow-auto whitespace-pre-wrap text-white/70">
              {detail.content}
            </pre>
          </Card>
        )}
        {(detail.histories?.length ?? 0) > 0 && renderSubmissionHistory(detail)}
      </div>
    )
  }

  /** 人员视图考生评分矩阵列定义。 */
  const candidateColumns: TableColumnsType<AssessmentCandidateScoreboardDTO> = [
    {
      title: '考生',
      render: (_, record) => (
        <div>
          <div className="flex items-center gap-1.5">
            <span className="text-white/85">{record.username}</span>
            {getReferralTag(record)}
          </div>
          <div className="text-xs text-white/35">{record.studentId}</div>
        </div>
      ),
    },
    {
      title: '总分',
      width: 120,
      render: (_, record) => `${formatScore(record.totalScore)} / ${formatScore(record.maxScore)}`,
    },
    { title: '已评分', dataIndex: 'judgedQuestionCount', width: 90 },
    { title: '待评分', dataIndex: 'pendingJudgementCount', width: 90 },
    {
      title: '题目表现',
      render: (_, record) => (
        <div className="flex flex-wrap gap-1.5">
          {record.questionScores.map((item) => (
            <Tag
              key={item.questionId}
              color={item.judged ? 'green' : item.submitted ? 'orange' : 'default'}
            >
              #{item.questionNo}{' '}
              {item.judged ? formatScore(item.score) : item.submitted ? '待评' : '未交'}
            </Tag>
          ))}
        </div>
      ),
    },
  ]

  /** 录用决策候选人表列定义。 */
  const decisionColumns: TableColumnsType<AssessmentDecisionCandidateDTO> = [
    {
      title: '候选人',
      render: (_, record) => (
        <div>
          <div className="flex items-center gap-1.5">
            <span className="text-white/85">{record.username}</span>
            {getReferralTag(record)}
          </div>
          <div className="text-xs text-white/35">{record.studentId}</div>
        </div>
      ),
    },
    {
      title: '总分',
      width: 120,
      render: (_, record) => `${formatScore(record.totalScore)} / ${formatScore(record.maxScore)}`,
    },
    { title: '待评分', dataIndex: 'pendingJudgementCount', width: 90 },
    { title: '状态', width: 90, render: (_, record) => getDecisionTag(record) },
  ]

  /** 渲染考核方向和考核时间的公共筛选器。 */
  const renderFilters = (
    <div className="flex flex-wrap items-center gap-3">
      <Select<DirectionOrGlobal>
        placeholder="考核方向"
        className="w-[160px]"
        value={direction}
        onChange={setDirection}
        options={directionOptions.map(([value, label]) => ({ value, label }))}
      />
      <Select
        placeholder="考核时间"
        loading={loadingTimes}
        disabled={!direction}
        className="w-[240px]"
        value={assessmentTimeId}
        onChange={setAssessmentTimeId}
        options={timeOptions}
      />
    </div>
  )

  /** 在未选完整考核范围时提示用户先选择方向和时间。 */
  const renderScopeGuide = () => {
    if (direction && assessmentTimeId) {
      return null
    }
    const currentStep = direction ? 1 : 0
    return (
      <Card className="border-orange-500/30 bg-orange-500/[0.03]">
        <div className="flex flex-col gap-4">
          <Alert
            type="info"
            showIcon
            message="选择考核范围后查看评分数据"
            description="请先选择考核方向，再选择该方向下的考核时间。系统会加载该轮考核题目、提交记录和评分结果。"
          />
          <Steps
            size="small"
            current={currentStep}
            items={[
              {
                title: '选择考核方向',
                status: direction ? 'finish' : 'process',
              },
              {
                title: '选择考核时间',
                status: assessmentTimeId ? 'finish' : direction ? 'process' : 'wait',
              },
            ]}
          />
        </div>
      </Card>
    )
  }

  /** 渲染题目视图，左侧题目汇总，右侧提交列表。 */
  const renderQuestionView = (
    <div className="grid grid-cols-1 xl:grid-cols-[420px_minmax(0,1fr)] gap-4">
      <Card styles={{ body: { padding: 0 } }}>
        <Spin spinning={loadingQuestions}>
          <Table
            rowKey="questionId"
            size="small"
            pagination={false}
            columns={questionColumns}
            dataSource={questions}
            onRow={(record) => ({
              onClick: () => setSelectedQuestionId(record.questionId),
              className: `cursor-pointer ${record.questionId === selectedQuestionId ? 'bg-white/[0.04]' : ''}`,
            })}
            locale={{ emptyText: assessmentTimeId ? '暂无题目' : '完成上方考核范围选择后加载题目' }}
          />
        </Spin>
      </Card>
      <Card
        title={
          selectedQuestion
            ? `#${selectedQuestion.questionNo} ${selectedQuestion.title}`
            : '题目提交'
        }
        extra={
          selectedQuestion?.questionType === 'FILE_UPLOAD' && selectedSubmissionIds.length > 0 ? (
            <Button size="small" icon={<DownloadOutlined />} onClick={handleBatchDownload}>
              批量下载 ({selectedSubmissionIds.length})
            </Button>
          ) : null
        }
        styles={{ body: { padding: 0 } }}
      >
        <Spin spinning={loadingSubmissions}>
          <div className="overscroll-contain">
            <Table
              rowKey="key"
              size="small"
              pagination={false}
              columns={submissionColumns}
              dataSource={groupedSubmissions}
              rowSelection={
                selectedQuestion?.questionType === 'FILE_UPLOAD'
                  ? {
                      type: 'checkbox',
                      selectedRowKeys: selectedSubmissionIds,
                      onChange: (keys) => setSelectedSubmissionIds(keys as number[]),
                      getCheckboxProps: (record) => ({
                        disabled: record.type === 'team' || !record.fileId,
                      }),
                    }
                  : undefined
              }
              scroll={{ y: 'calc(100vh - 360px)' }}
              onRow={(record) => ({
                onClick: () => {
                  if (record.type === 'team') return
                  setReviewing(record as AssessmentQuestionSubmissionDTO)
                },
                className: record.type === 'team' ? '' : 'cursor-pointer',
              })}
              expandable={{
                expandedRowRender: (record) => {
                  if (record.type !== 'team') return null
                  return (
                    <Table
                      size="small"
                      showHeader={false}
                      pagination={false}
                      columns={submissionMemberColumns}
                      dataSource={record.members ?? []}
                      rowKey="answerId"
                      onRow={(m) => ({
                        onClick: () => setReviewing(m),
                        className: 'cursor-pointer',
                      })}
                    />
                  )
                },
                rowExpandable: (record) => record.type === 'team',
                defaultExpandAllRows: true,
              }}
              locale={{ emptyText: selectedQuestionId ? '暂无提交' : '请选择左侧题目查看提交' }}
            />
          </div>
        </Spin>
      </Card>
    </div>
  )

  /** 渲染人员视图的考生评分矩阵。 */
  const renderCandidateView = (
    <Card styles={{ body: { padding: 0 } }}>
      <Spin spinning={loadingCandidates}>
        <Table
          rowKey="candidateUserId"
          size="small"
          pagination={{ pageSize: 10 }}
          columns={candidateColumns}
          dataSource={candidateScores}
          onRow={(record) => ({
            onClick: () => setViewingCandidateScore(record),
            className: 'cursor-pointer',
          })}
          locale={{
            emptyText: assessmentTimeId ? '暂无考生评分数据' : '完成上方考核范围选择后加载人员评分',
          }}
        />
      </Spin>
    </Card>
  )

  /** 渲染题目评分页面主体。 */
  const renderScoreTab = (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-3">
          {renderFilters}
          <Select
            allowClear
            placeholder="题型"
            className="w-[140px]"
            value={questionType}
            onChange={setQuestionType}
            options={Object.entries(QUESTION_TYPE_LABELS).map(([value, label]) => ({
              value,
              label,
            }))}
          />
          <Select
            allowClear
            placeholder="评分状态"
            className="w-[140px]"
            value={submissionStatus}
            onChange={setSubmissionStatus}
            options={[
              { value: 'PENDING', label: '待评分' },
              { value: 'JUDGED', label: '已评分' },
            ]}
          />
        </div>
        <Input.Search
          allowClear
          placeholder="搜索题目 / 考生"
          style={{ width: screens.md ? 260 : '100%' }}
          onSearch={setScoreKeyword}
        />
      </div>
      {renderScopeGuide()}
      <Tabs
        activeKey={scoreView}
        onChange={setScoreView}
        items={
          currentAssessmentTime?.direction === null
            ? [{ key: 'questions', label: '题目视图', children: renderQuestionView }]
            : [
                { key: 'questions', label: '题目视图', children: renderQuestionView },
                { key: 'candidates', label: '人员视图', children: renderCandidateView },
              ]
        }
      />
    </div>
  )

  /** 渲染录用决策主体，保留给旧入口复用。 */
  const renderDecisionTab = (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-3">
          {renderFilters}
          <Select
            allowClear
            placeholder="决策状态"
            className="w-[150px]"
            value={decisionStatus}
            onChange={setDecisionStatus}
            options={[
              { value: 'PENDING', label: '待决策' },
              { value: 'PASSED', label: '通过' },
              { value: 'ELIMINATED', label: '淘汰' },
            ]}
          />
        </div>
        <div className="flex flex-wrap gap-3">
          <Input.Search
            allowClear
            placeholder="搜索考生 / 学号"
            style={{ width: screens.md ? 240 : '100%' }}
            onSearch={setDecisionKeyword}
          />
          <Button
            type="primary"
            onClick={handlePublish}
            disabled={!assessmentTimeId || !isDecisionMaker}
          >
            发布本轮结果
          </Button>
        </div>
      </div>
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
        <Card>
          <Statistic title="候选人" value={decisionWorkspace?.statistics.candidates ?? 0} />
        </Card>
        <Card>
          <Statistic title="待决策" value={decisionWorkspace?.statistics.pending ?? 0} />
        </Card>
        <Card>
          <Statistic title="通过" value={decisionWorkspace?.statistics.passed ?? 0} />
        </Card>
        <Card>
          <Statistic title="淘汰" value={decisionWorkspace?.statistics.eliminated ?? 0} />
        </Card>
      </div>
      <div className="grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_360px] gap-4">
        <Card styles={{ body: { padding: 0 } }}>
          <Spin spinning={loadingDecisions}>
            <Table
              rowKey="candidateUserId"
              size="small"
              pagination={{ pageSize: 10 }}
              columns={decisionColumns}
              dataSource={decisionWorkspace?.candidates ?? []}
              onRow={(record) => ({
                onClick: () => setSelectedDecisionCandidate(record),
                className: 'cursor-pointer',
              })}
              locale={{ emptyText: assessmentTimeId ? '暂无候选人' : '请先选择考核时间' }}
            />
          </Spin>
        </Card>
        <Card title="候选人决策">
          {selectedDecisionCandidate ? (
            <div className="flex flex-col gap-4">
              <Descriptions column={1} size="small">
                <Descriptions.Item label="姓名">
                  {selectedDecisionCandidate.username}
                </Descriptions.Item>
                <Descriptions.Item label="学号">
                  {selectedDecisionCandidate.studentId}
                </Descriptions.Item>
                <Descriptions.Item label="总分">
                  {formatScore(selectedDecisionCandidate.totalScore)} /{' '}
                  {formatScore(selectedDecisionCandidate.maxScore)}
                </Descriptions.Item>
                <Descriptions.Item label="状态">
                  {getDecisionTag(selectedDecisionCandidate)}
                </Descriptions.Item>
              </Descriptions>
              <div className="flex flex-wrap gap-1.5">
                {selectedDecisionCandidate.questionScores.map((item) => (
                  <Tag
                    key={item.questionId}
                    color={item.judged ? 'green' : item.submitted ? 'orange' : 'default'}
                  >
                    #{item.questionNo}{' '}
                    {item.judged ? formatScore(item.score) : item.submitted ? '待评' : '未交'}
                  </Tag>
                ))}
              </div>
              <Input.TextArea
                rows={3}
                maxLength={200}
                showCount
                placeholder="决策备注（可选）"
                value={decisionComment}
                onChange={(event) => setDecisionComment(event.target.value)}
              />
              <div className="grid grid-cols-2 gap-2">
                <Button
                  type="primary"
                  loading={savingDecisionUserId === selectedDecisionCandidate.candidateUserId}
                  onClick={() => handleDecision(selectedDecisionCandidate, true)}
                  disabled={!isDecisionMaker}
                >
                  通过
                </Button>
                <Button
                  danger
                  loading={savingDecisionUserId === selectedDecisionCandidate.candidateUserId}
                  onClick={() => handleDecision(selectedDecisionCandidate, false)}
                  disabled={!isDecisionMaker}
                >
                  淘汰
                </Button>
              </div>
              <div className="text-xs text-white/35">
                点击通过或淘汰后会立即保存该考生决策；发布本轮结果将向已决策考生发送邮件通知。
              </div>
            </div>
          ) : (
            <Empty description="请选择候选人" />
          )}
        </Card>
      </div>
    </div>
  )

  /** 人员评分明细抽屉中的题目列定义。 */
  const candidateQuestionColumns: TableColumnsType<AssessmentCandidateQuestionScoreDTO> = [
    {
      title: '题目',
      render: (_, record) => (
        <div>
          <div className="text-white/85">
            #{record.questionNo} {record.questionTitle}
          </div>
          <Tag color={QUESTION_TYPE_COLORS[record.questionType]} bordered={false}>
            {QUESTION_TYPE_LABELS[record.questionType]}
          </Tag>
        </div>
      ),
    },
    {
      title: '得分',
      width: 110,
      render: (_, record) =>
        record.judged
          ? `${formatScore(record.score)} / ${formatScore(record.maxScore)}`
          : record.submitted
            ? '待评分'
            : '未提交',
    },
    {
      title: '结果',
      width: 80,
      render: (_, record) => {
        const resultCode = record.latestJudgement?.resultCode
        if (!resultCode) return <span className="text-white/35">-</span>
        return <Tag color={getResultColor(resultCode)}>{resultCode}</Tag>
      },
    },
    {
      title: '答案提交时间',
      width: 150,
      render: (_, record) => formatTime(record.submitTime),
    },
  ]

  const isFileUploadReview = reviewing?.questionType === 'FILE_UPLOAD'

  return (
    <div className="min-h-full bg-black text-white">
      {renderScoreTab}

      <Drawer
        title={reviewing ? `${reviewing.username}的作品评分` : '作品评分'}
        open={!!reviewing}
        onClose={() => setReviewing(null)}
        width={screens.md ? 520 : '100%'}
        extra={
          reviewing?.fileId ? (
            <Button
              icon={<DownloadOutlined />}
              onClick={() => fileService.downloadFile(reviewing.fileId!)}
            >
              下载作品
            </Button>
          ) : null
        }
      >
        {reviewing && (
          <div className="flex flex-col gap-4">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="考生">
                {reviewing.username}（{reviewing.studentId}）
              </Descriptions.Item>
              <Descriptions.Item label="题目">
                #{reviewing.questionNo} {reviewing.questionTitle}
              </Descriptions.Item>
              <Descriptions.Item label="题型">
                <Tag color={QUESTION_TYPE_COLORS[reviewing.questionType]} bordered={false}>
                  {QUESTION_TYPE_LABELS[reviewing.questionType]}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="答案提交时间">
                {formatTime(reviewing.submitTime)}
              </Descriptions.Item>
              <Descriptions.Item label="展示评判时间">
                {formatTime(reviewing.latestJudgement?.judgedAt)}
              </Descriptions.Item>
              <Descriptions.Item label="当前得分">
                {reviewing.latestJudgement
                  ? `${formatScore(reviewing.latestJudgement.score)} / ${formatScore(reviewing.maxScore)}`
                  : '待评分'}
              </Descriptions.Item>
            </Descriptions>

            {reviewing.content && (
              <Card size="small" title="答案内容">
                <pre className="m-0 max-h-48 overflow-auto whitespace-pre-wrap text-white/70">
                  {reviewing.content}
                </pre>
              </Card>
            )}

            {/* 评论列表 */}
            {isFileUploadReview && (
              <Card size="small" title="团队评论">
                <Spin spinning={loadingComments}>
                  {comments.length === 0 ? (
                    <Empty description="暂无评论" />
                  ) : (
                    <div className="flex flex-col gap-3">
                      {comments.map((comment) => (
                        <div
                          key={comment.id}
                          className="rounded-lg border border-white/10 bg-black/20 px-3 py-2"
                        >
                          {editingCommentId === comment.id ? (
                            <Form form={editCommentForm} layout="vertical">
                              <Form.Item name="content" initialValue={comment.content ?? ''}>
                                <Input.TextArea rows={2} maxLength={500} showCount />
                              </Form.Item>
                              <Form.Item name="score" initialValue={comment.score ?? undefined}>
                                <InputNumber
                                  min={0}
                                  max={Number(reviewing.maxScore)}
                                  precision={1}
                                  placeholder="参考评分"
                                  className="w-full"
                                />
                              </Form.Item>
                              <div className="flex gap-2">
                                <Button
                                  size="small"
                                  loading={savingComment}
                                  onClick={() => handleUpdateComment(comment.id)}
                                >
                                  保存
                                </Button>
                                <Button size="small" onClick={() => setEditingCommentId(null)}>
                                  取消
                                </Button>
                              </div>
                            </Form>
                          ) : (
                            <>
                              <div className="flex items-center justify-between">
                                <span className="text-sm text-white/70">
                                  {comment.username ?? `用户 ${comment.userId}`}
                                </span>
                                <span className="text-xs text-white/40">
                                  {formatTime(comment.commentTime)}
                                </span>
                              </div>
                              <div className="mt-1 text-white/85">
                                {comment.content || (
                                  <span className="text-white/35">无评论内容</span>
                                )}
                              </div>
                              {comment.score != null && (
                                <div className="mt-1 text-sm text-white/60">
                                  参考评分：{formatScore(comment.score)}
                                </div>
                              )}
                              {comment.userId === userInfo?.id && (
                                <div className="mt-2 flex gap-2">
                                  <Button
                                    size="small"
                                    onClick={() => {
                                      setEditingCommentId(comment.id)
                                      editCommentForm.setFieldsValue({
                                        content: comment.content ?? '',
                                        score: comment.score ?? undefined,
                                      })
                                    }}
                                  >
                                    编辑
                                  </Button>
                                  <Button
                                    size="small"
                                    danger
                                    onClick={() => handleDeleteComment(comment.id)}
                                  >
                                    删除
                                  </Button>
                                </div>
                              )}
                            </>
                          )}
                        </div>
                      ))}
                    </div>
                  )}
                </Spin>
              </Card>
            )}

            {/* 评论表单（未评论时显示） */}
            {isFileUploadReview && !comments.some((c) => c.userId === userInfo?.id) && (
              <Card size="small" title="发表评论">
                <Form form={commentForm} layout="vertical">
                  <Form.Item name="content" label="评论内容">
                    <Input.TextArea rows={3} maxLength={500} showCount placeholder="输入评论内容" />
                  </Form.Item>
                  <Form.Item name="score" label="参考评分">
                    <InputNumber
                      min={0}
                      max={Number(reviewing.maxScore)}
                      precision={1}
                      className="w-full"
                      placeholder="可选"
                    />
                  </Form.Item>
                  <Button
                    type="default"
                    block
                    loading={savingComment}
                    onClick={handleSubmitComment}
                  >
                    提交评论
                  </Button>
                </Form>
              </Card>
            )}

            {isFileUploadReview ? (
              <Form form={form} layout="vertical">
                {/* 文件上传题是唯一允许人工改分的题型，客观题保持后端自动评判只读。 */}
                <Form.Item
                  name="score"
                  label={`评分（满分 ${formatScore(reviewing.maxScore)}）`}
                  rules={[{ required: true, message: '请输入评分' }]}
                >
                  <InputNumber
                    min={0}
                    max={Number(reviewing.maxScore)}
                    precision={1}
                    className="w-full"
                  />
                </Form.Item>
                <div className="flex flex-col gap-2">
                  {isDecisionMaker && (
                    <Button
                      type="primary"
                      danger
                      block
                      loading={savingFinalize}
                      onClick={handleFinalizeScore}
                    >
                      确认最终评分
                    </Button>
                  )}
                </div>
              </Form>
            ) : (
              <Card size="small">
                <div className="flex flex-wrap items-center gap-2 text-white/65">
                  <span>客观题由系统自动评判，不能人工修改分数。</span>
                  <span>结果：</span>
                  {reviewing.latestJudgement?.resultCode ? (
                    <Tag color={getResultColor(reviewing.latestJudgement.resultCode)}>
                      {reviewing.latestJudgement.resultCode}
                    </Tag>
                  ) : (
                    <span>暂无评判结果</span>
                  )}
                </div>
              </Card>
            )}
          </div>
        )}
      </Drawer>

      <Drawer
        title={viewingCandidateScore ? `${viewingCandidateScore.username}的评分明细` : '评分明细'}
        open={!!viewingCandidateScore}
        onClose={() => setViewingCandidateScore(null)}
        width={screens.md ? 640 : '100%'}
      >
        {viewingCandidateScore && (
          <div className="flex flex-col gap-4">
            <Descriptions column={1} size="small">
              <Descriptions.Item label="考生">
                {viewingCandidateScore.username}（{viewingCandidateScore.studentId}）
              </Descriptions.Item>
              <Descriptions.Item label="总分">
                {formatScore(viewingCandidateScore.totalScore)} /{' '}
                {formatScore(viewingCandidateScore.maxScore)}
              </Descriptions.Item>
              <Descriptions.Item label="待评分题目">
                {viewingCandidateScore.pendingJudgementCount}
              </Descriptions.Item>
            </Descriptions>
            <Table
              rowKey="questionId"
              size="small"
              pagination={false}
              columns={candidateQuestionColumns}
              dataSource={viewingCandidateScore.questionScores}
              expandable={{
                expandedRowRender: renderCandidateQuestionDetail,
                rowExpandable: (record) => record.submitted,
                onExpand: (expanded, record) => {
                  if (expanded) {
                    fetchCandidateQuestionDetail(record)
                  }
                },
              }}
            />
          </div>
        )}
      </Drawer>
    </div>
  )
}
