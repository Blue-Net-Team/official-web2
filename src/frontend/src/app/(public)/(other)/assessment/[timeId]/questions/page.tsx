'use client'

import { useState, useEffect, useCallback, useMemo } from 'react'
import { useParams, useRouter, useSearchParams } from 'next/navigation'
import {
  ArrowLeftOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  MinusCircleOutlined,
  FileTextOutlined,
  TrophyOutlined,
  TeamOutlined,
  PlusOutlined,
  LoginOutlined,
} from '@ant-design/icons'
import { Spin, Table, Tag, message, Button, Modal, Input, Form } from 'antd'
import type { TableColumnsType } from 'antd'
import { assessmentQuestionService } from '@/apis/services/assessment-question.service'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'
import { assessmentTeamService } from '@/apis/services/assessment-team.service'
import { useAuth } from '@/hooks'
import type {
  AssessmentQuestionDTO,
  AssessmentTimeDTO,
  QuestionType,
  AssessmentTeamDTO,
} from '@/apis/schema/assessment.dto'
import { QuestionTypeLabels } from '@/types/assessment'
import { DIRECTION_LABELS as DirectionLabels } from '@/apis/schema/enumerate'
import type { Direction } from '@/apis/schema/enumerate'
import styles from './styles.module.css'

/** 获取状态显示文本 */
function getStatusText(
  startTime: string,
  endTime: string
): { text: string; status: 'NOT_STARTED' | 'IN_PROGRESS' | 'ENDED' } {
  const now = new Date().getTime()
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  if (now < start) return { text: '未开始', status: 'NOT_STARTED' }
  if (now > end) return { text: '已结束', status: 'ENDED' }
  return { text: '进行中', status: 'IN_PROGRESS' }
}

/** 格式化日期 */
function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const QuestionTypePresetColor: Record<QuestionType, string> = {
  SINGLE_CHOICE: 'geekblue',
  MULTIPLE_CHOICE: 'purple',
  FILE_UPLOAD: 'orange',
  ALGORITHM: 'cyan',
}

function getQuestionTypeColor(type: QuestionType): string {
  return QuestionTypePresetColor[type] || 'default'
}

/** 获取轮次名称 */
function getEpochLabel(epoch: number): string {
  if (epoch === 0) return '最终考核'
  const chineseNumbers = ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
  return `第${chineseNumbers[epoch - 1] || epoch}轮考核`
}

export default function QuestionsPage() {
  const router = useRouter()
  const params = useParams()
  const searchParams = useSearchParams()
  const timeId = Number(params.timeId)
  const currentPage = Number(searchParams.get('page') || '0')

  const [timeInfo, setTimeInfo] = useState<AssessmentTimeDTO | null>(null)
  const [questions, setQuestions] = useState<AssessmentQuestionDTO[]>([])
  const [totalElements, setTotalElements] = useState(0)
  const [ended, setEnded] = useState(false)
  const [loading, setLoading] = useState(true)
  const [teamInfo, setTeamInfo] = useState<AssessmentTeamDTO | null>(null)
  const [teamLoading, setTeamLoading] = useState(false)
  const [createTeamModalOpen, setCreateTeamModalOpen] = useState(false)
  const [joinTeamModalOpen, setJoinTeamModalOpen] = useState(false)
  const [createTeamForm] = Form.useForm()
  const [joinTeamForm] = Form.useForm()
  const [teamActionLoading, setTeamActionLoading] = useState(false)
  const { isAuthenticated, checkAuthStatus, userInfo } = useAuth()

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

  // 加载考题列表
  const fetchQuestions = useCallback(async () => {
    try {
      const response = await assessmentQuestionService.getQuestions(timeId, currentPage, 10)
      if (response.code === 200 && response.data) {
        setQuestions(response.data.questions.content ?? [])
        setTotalElements(response.data.questions.totalElements ?? 0)
        setEnded(response.data.ended ?? false)
      } else {
        setQuestions([])
      }
    } catch (error) {
      console.error('Failed to fetch questions:', error)
      setQuestions([])
    } finally {
      setLoading(false)
    }
  }, [timeId, currentPage])

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

  useEffect(() => {
    if (isAuthenticated) {
      fetchTimeInfo()
      fetchQuestions()
    }
  }, [isAuthenticated, fetchTimeInfo, fetchQuestions])

  // 考核时间信息加载完成后，加载队伍信息
  useEffect(() => {
    if (isAuthenticated && timeInfo?.allowTeam) {
      fetchTeamInfo()
    }
  }, [isAuthenticated, timeInfo?.allowTeam, fetchTeamInfo])

  // 创建队伍
  const handleCreateTeam = async () => {
    try {
      const values = await createTeamForm.validateFields()
      setTeamActionLoading(true)
      const response = await assessmentTeamService.createTeam({
        assessmentTimeId: timeId,
        name: values.name,
      })
      if (response.code === 200 && response.data) {
        setTeamInfo(response.data)
        setCreateTeamModalOpen(false)
        createTeamForm.resetFields()
        message.success('队伍创建成功')
      } else {
        message.error(response.msg || '创建失败')
      }
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'errorFields' in err) return
      const msg = (err as { response?: { data?: { msg?: string } } })?.response?.data?.msg
      message.error(msg || '创建失败')
    } finally {
      setTeamActionLoading(false)
    }
  }

  // 加入队伍
  const handleJoinTeam = async () => {
    try {
      const values = await joinTeamForm.validateFields()
      setTeamActionLoading(true)
      const response = await assessmentTeamService.joinTeam({
        inviteCode: values.inviteCode,
      })
      if (response.code === 200 && response.data) {
        setTeamInfo(response.data)
        setJoinTeamModalOpen(false)
        joinTeamForm.resetFields()
        message.success('加入队伍成功')
      } else {
        message.error(response.msg || '加入失败')
      }
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'errorFields' in err) return
      const msg = (err as { response?: { data?: { msg?: string } } })?.response?.data?.msg
      message.error(msg || '加入失败')
    } finally {
      setTeamActionLoading(false)
    }
  }

  const columns: TableColumnsType<AssessmentQuestionDTO> = useMemo(
    () => [
      {
        title: '序号',
        dataIndex: 'questionNo',
        key: 'questionNo',
        width: 80,
        align: 'center',
      },
      {
        title: '题目',
        dataIndex: 'title',
        key: 'title',
        ellipsis: true,
      },
      {
        title: '题型',
        dataIndex: 'questionType',
        key: 'questionType',
        width: 140,
        align: 'center',
        render: (type: QuestionType) => (
          <Tag
            color={getQuestionTypeColor(type)}
            className="px-2.5 py-0.5 rounded text-xs font-medium"
          >
            {QuestionTypeLabels[type] || type}
          </Tag>
        ),
      },
      {
        title: '分值',
        dataIndex: 'score',
        key: 'score',
        width: 80,
        align: 'center',
      },
      {
        title: '状态',
        dataIndex: 'answered',
        key: 'answered',
        width: 80,
        align: 'center',
        render: (answered: boolean) =>
          answered ? (
            <span className="inline-flex items-center gap-1 text-xs text-[#07c160]">
              <CheckCircleOutlined /> 已答
            </span>
          ) : (
            <span className="text-xs text-[#8c8c8d]/60">未答</span>
          ),
      },
    ],
    []
  )

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-[#0a0a0a] px-6 py-10 pb-20 relative overflow-x-hidden">
        <div className={`${styles.bg} top-0 left-0 w-full h-full z-0 pointer-events-none fixed`} />
        <div className="flex justify-center items-center min-h-[300px]">
          <Spin size="large" />
        </div>
      </div>
    )
  }

  const statusInfo = timeInfo ? getStatusText(timeInfo.startTime, timeInfo.endTime) : null

  const answeredCount = questions.filter((q) => q.answered === true).length
  const unansweredCount = questions.filter((q) => q.answered === false).length
  const totalScore = questions.reduce((sum, q) => sum + (q.score || 0), 0)

  const handlePageChange = (page: number) => {
    router.push(`/assessment/${timeId}/questions?page=${page}`)
  }

  const handleRowClick = (record: AssessmentQuestionDTO) => {
    router.push(`/assessment/${timeId}/questions/${record.id}`)
  }

  return (
    <div className="min-h-screen bg-[#0a0a0a] px-6 py-10 pb-20 relative overflow-x-hidden">
      <div className={`${styles.bg} top-0 left-0 w-full h-full z-0 pointer-events-none fixed`} />
      <div className="max-w-[960px] mx-auto relative z-10">
        <div className="mb-8">
          <button
            className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-lg border border-white/10 bg-white/[0.04] text-white/65 text-[13px] cursor-pointer transition-all duration-200 mb-5 hover:bg-white/[0.08] hover:text-white"
            onClick={() => router.push('/assessment')}
          >
            <ArrowLeftOutlined />
            <span>返回考核列表</span>
          </button>
          <div className="mt-2">
            <div className="flex items-center gap-3 mb-2.5">
              <h1 className="text-2xl md:text-[28px] font-bold text-white bg-gradient-to-r from-white to-white/70 bg-clip-text text-transparent">
                {timeInfo ? getEpochLabel(timeInfo.epoch) : '考核'} · 考题目录
              </h1>
              {statusInfo && (
                <Tag
                  variant="outlined"
                  color={
                    statusInfo.status === 'NOT_STARTED'
                      ? 'default'
                      : statusInfo.status === 'IN_PROGRESS'
                        ? 'processing'
                        : 'success'
                  }
                >
                  {statusInfo.text}
                </Tag>
              )}
            </div>
            <div className="flex flex-wrap items-center gap-2.5 mb-3.5">
              {timeInfo && (
                <>
                  <Tag variant="outlined">
                    {DirectionLabels[timeInfo.direction as Direction] || timeInfo.direction}
                  </Tag>
                  <Tag variant="outlined">{timeInfo.grade ? `${timeInfo.grade}级` : ''}</Tag>
                  <span className="flex items-center gap-1.5 text-[13px] text-white/45">
                    <CalendarOutlined />
                    <span>
                      {formatDate(timeInfo.startTime)} — {formatDate(timeInfo.endTime)}
                    </span>
                  </span>
                  {timeInfo.allowTeam && (
                    <Tag color="blue" icon={<TeamOutlined />}>
                      允许组队
                    </Tag>
                  )}
                </>
              )}
            </div>

            {/* 队伍信息区域 */}
            {timeInfo?.allowTeam && !ended && (
              <div className="mb-6 p-4 rounded-xl bg-white/[0.04] border border-white/[0.08] backdrop-blur-xl">
                {teamLoading ? (
                  <div className="flex items-center gap-2 text-white/45">
                    <Spin size="small" />
                    <span className="text-[13px]">加载队伍信息...</span>
                  </div>
                ) : teamInfo ? (
                  <div className="flex items-center justify-between flex-wrap gap-3">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-lg bg-[#6677ff]/[0.15] flex items-center justify-center">
                        <TeamOutlined className="text-lg text-[#6677ff]" />
                      </div>
                      <div className="flex flex-col">
                        <span className="text-sm font-medium text-white">{teamInfo.name}</span>
                        <span className="text-[12px] text-white/45">
                          队长：{teamInfo.leaderName} · 成员 {teamInfo.members.length} 人
                        </span>
                      </div>
                    </div>
                    <Button
                      type="primary"
                      size="small"
                      onClick={() =>
                        router.push(`/assessment/${timeId}/questions/${questions[0]?.id || ''}`)
                      }
                    >
                      进入答题
                    </Button>
                  </div>
                ) : (
                  <div className="flex items-center justify-between flex-wrap gap-3">
                    <div className="flex items-center gap-2 text-white/45">
                      <TeamOutlined className="text-sm" />
                      <span className="text-[13px]">本考核允许组队，您当前未加入队伍</span>
                    </div>
                    <div className="flex gap-2">
                      <Button
                        type="primary"
                        size="small"
                        icon={<PlusOutlined />}
                        onClick={() => setCreateTeamModalOpen(true)}
                      >
                        创建队伍
                      </Button>
                      <Button
                        size="small"
                        icon={<LoginOutlined />}
                        onClick={() => setJoinTeamModalOpen(true)}
                      >
                        加入队伍
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        {loading ? (
          <div className="flex justify-center items-center min-h-[300px]">
            <Spin size="large" />
          </div>
        ) : (
          <>
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-7">
              <div className="flex items-center gap-3 p-4 rounded-xl bg-white/[0.04] border border-white/[0.08] backdrop-blur-xl">
                <FileTextOutlined className="text-[22px] text-white/40" />
                <div className="flex flex-col">
                  <span className="text-xl font-semibold text-white">{totalElements}</span>
                  <span className="text-xs text-white/45">题目总数</span>
                </div>
              </div>
              <div className="flex items-center gap-3 p-4 rounded-xl bg-white/[0.04] border border-white/[0.08] backdrop-blur-xl">
                <CheckCircleOutlined className="text-[22px] text-[#07c160]" />
                <div className="flex flex-col">
                  <span className="text-xl font-semibold text-white">{answeredCount}</span>
                  <span className="text-xs text-white/45">已作答</span>
                </div>
              </div>
              <div className="flex items-center gap-3 p-4 rounded-xl bg-white/[0.04] border border-white/[0.08] backdrop-blur-xl">
                <MinusCircleOutlined className="text-[22px] text-[#8c8c8d]/60" />
                <div className="flex flex-col">
                  <span className="text-xl font-semibold text-white">{unansweredCount}</span>
                  <span className="text-xs text-white/45">未作答</span>
                </div>
              </div>
              <div className="flex items-center gap-3 p-4 rounded-xl bg-white/[0.04] border border-white/[0.08] backdrop-blur-xl">
                <TrophyOutlined className="text-[22px] text-[#fa8c16]" />
                <div className="flex flex-col">
                  <span className="text-xl font-semibold text-white">{totalScore}</span>
                  <span className="text-xs text-white/45">总分</span>
                </div>
              </div>
            </div>

            <Table<AssessmentQuestionDTO>
              columns={columns}
              dataSource={questions}
              rowKey="id"
              scroll={{ x: 500 }}
              onRow={(record) => ({
                onClick: () => handleRowClick(record),
                className: 'cursor-pointer',
              })}
              pagination={{
                current: currentPage + 1,
                total: totalElements,
                pageSize: 10,
                onChange: (page: number) => handlePageChange(page - 1),
                showSizeChanger: false,
                showTotal: (total: number) => `共 ${total} 道题目`,
              }}
              locale={{ emptyText: '该考核暂未发布考题' }}
              className="rounded-xl overflow-hidden border border-white/[0.08] bg-white/[0.02]"
            />

            {questions.length > 0 && (
              <div className="mt-6 text-center">
                <span className="text-[13px] text-white/35">
                  共 {totalElements} 道题目 · 已作答 {answeredCount} · 未答 {unansweredCount} · 总分{' '}
                  {totalScore}
                </span>
              </div>
            )}
          </>
        )}

        {/* 创建队伍弹窗 */}
        <Modal
          title="创建队伍"
          open={createTeamModalOpen}
          onOk={handleCreateTeam}
          onCancel={() => {
            setCreateTeamModalOpen(false)
            createTeamForm.resetFields()
          }}
          confirmLoading={teamActionLoading}
          okText="创建"
          cancelText="取消"
        >
          <Form form={createTeamForm} layout="vertical">
            <Form.Item
              name="name"
              label="队伍名称"
              rules={[{ required: true, message: '请输入队伍名称' }]}
            >
              <Input placeholder="请输入队伍名称" maxLength={30} showCount />
            </Form.Item>
          </Form>
        </Modal>

        {/* 加入队伍弹窗 */}
        <Modal
          title="加入队伍"
          open={joinTeamModalOpen}
          onOk={handleJoinTeam}
          onCancel={() => {
            setJoinTeamModalOpen(false)
            joinTeamForm.resetFields()
          }}
          confirmLoading={teamActionLoading}
          okText="加入"
          cancelText="取消"
        >
          <Form form={joinTeamForm} layout="vertical">
            <Form.Item
              name="inviteCode"
              label="邀请码"
              rules={[{ required: true, message: '请输入邀请码' }]}
            >
              <Input placeholder="请输入队伍邀请码" />
            </Form.Item>
          </Form>
        </Modal>
      </div>
    </div>
  )
}
