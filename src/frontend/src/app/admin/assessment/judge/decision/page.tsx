'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  Alert,
  App,
  Button,
  Card,
  Descriptions,
  Empty,
  Grid,
  Input,
  Modal,
  Select,
  Spin,
  Statistic,
  Steps,
  Table,
  Tag,
} from 'antd'
import type { TableColumnsType } from 'antd'
import type {
  AssessmentDecisionCandidateDTO,
  AssessmentDecisionWorkspaceDTO,
  AssessmentTimeDTO,
} from '@/apis/schema/assessment.dto'
import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { adminAssessmentJudgementService } from '@/apis/services/admin-assessment-judgement.service'
import { adminAssessmentTimeService } from '@/apis/services/admin-assessment-time.service'
import authStore from '@/stores/authStore'
import { getRoleLevel } from '@/utils/RoleUtils'
import { formatScore, getDecisionTag } from '../shared'

export default function AssessmentJudgementManagementPage() {
  const { message: messageApi } = App.useApp()
  const screens = Grid.useBreakpoint()

  const userInfo = authStore((state) => state.userInfo)
  const isSuperAdmin = getRoleLevel(userInfo?.roleName || '') >= 3
  const isDecisionMaker = getRoleLevel(userInfo?.roleName || '') >= 2
  const userDirection = userInfo?.direction

  const [direction, setDirection] = useState<Direction | undefined>(
    isSuperAdmin ? undefined : (userDirection ?? undefined)
  )
  const [assessmentTimeId, setAssessmentTimeId] = useState<number | undefined>()
  const [assessmentTimes, setAssessmentTimes] = useState<AssessmentTimeDTO[]>([])

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
  const [loadingDecisions, setLoadingDecisions] = useState(false)
  const [savingDecisionUserId, setSavingDecisionUserId] = useState<number | null>(null)

  const directionOptions = useMemo(() => {
    const entries = Object.entries(DIRECTION_LABELS) as [Direction, string][]
    if (!isSuperAdmin && userDirection) {
      return entries.filter(([value]) => value === userDirection)
    }
    return entries
  }, [isSuperAdmin, userDirection])

  const timeOptions = useMemo(
    () =>
      assessmentTimes.map((item) => ({
        value: item.id,
        label: `${DIRECTION_LABELS[item.direction]} · 第 ${item.epoch} 轮 · ${item.grade}级`,
      })),
    [assessmentTimes]
  )

  /** 加载指定方向下可决策的考核时间。 */
  const fetchAssessmentTimes = useCallback(
    async (nextDirection: Direction) => {
      setLoadingTimes(true)
      try {
        const response = await adminAssessmentTimeService.getList(0, 100)
        setAssessmentTimes(
          response.data?.content.filter((item) => item.direction === nextDirection) ?? []
        )
      } catch {
        messageApi.error('加载考核时间失败')
      } finally {
        setLoadingTimes(false)
      }
    },
    [messageApi]
  )

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
    if (!direction) {
      setAssessmentTimes([])
      setAssessmentTimeId(undefined)
      return
    }
    fetchAssessmentTimes(direction)
    setAssessmentTimeId(undefined)
  }, [direction, fetchAssessmentTimes])

  useEffect(() => {
    fetchDecisionWorkspace()
  }, [fetchDecisionWorkspace])

  useEffect(() => {
    setDecisionComment(selectedDecisionCandidate?.decisionComment ?? '')
  }, [selectedDecisionCandidate])

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

  /** 提示发布结果邮件能力尚未接入。 */
  const showPublishNotice = () => {
    Modal.info({
      title: '发布本轮结果',
      content: '邮件通知接口尚未接入。本次操作不会发送邮件，也不会修改任何考生决策。',
      okText: '知道了',
    })
  }

  /** 录用决策候选人表列定义。 */
  const decisionColumns: TableColumnsType<AssessmentDecisionCandidateDTO> = [
    {
      title: '候选人',
      render: (_, record) => (
        <div>
          <div className="text-white/85">{record.username}</div>
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
      <Select
        placeholder="考核方向"
        style={{ width: 160 }}
        value={direction}
        onChange={setDirection}
        options={directionOptions.map(([value, label]) => ({ value, label }))}
      />
      <Select
        placeholder="考核时间"
        loading={loadingTimes}
        disabled={!direction}
        style={{ width: 240 }}
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
            message="选择考核范围后查看录用决策"
            description="请先选择考核方向和考核时间，再确认候选人的通过/淘汰结果。"
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

  return (
    <div className="min-h-full bg-black text-white">
      <div className="flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-3">
            {renderFilters}
            <Select
              allowClear
              placeholder="决策状态"
              style={{ width: 150 }}
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
              onClick={showPublishNotice}
              disabled={!assessmentTimeId || !isDecisionMaker}
            >
              发布本轮结果
            </Button>
          </div>
        </div>
        {renderScopeGuide()}
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
                locale={{
                  emptyText: assessmentTimeId ? '暂无候选人' : '完成上方考核范围选择后加载候选人',
                }}
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
                <div className="mb-2">
                  <Input.TextArea
                    rows={3}
                    maxLength={200}
                    showCount
                    placeholder="决策备注（可选）"
                    value={decisionComment}
                    onChange={(event) => setDecisionComment(event.target.value)}
                  />
                </div>
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
                  点击通过或淘汰后会立即保存该考生决策；发布本轮结果入口当前不会发送邮件。
                </div>
              </div>
            ) : (
              <Empty description="请选择候选人" />
            )}
          </Card>
        </div>
      </div>
    </div>
  )
}
