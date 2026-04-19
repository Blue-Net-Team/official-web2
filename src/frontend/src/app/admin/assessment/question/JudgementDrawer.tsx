'use client'

import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  App,
  Button,
  Drawer,
  Form,
  Input,
  InputNumber,
  Select,
  Spin,
  Statistic,
  Table,
  Tag,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type {
  AssessmentJudgementDTO,
  AssessmentQuestionDTO,
  QuestionStatisticsDTO,
} from '@/apis/schema/assessment.dto'
import { adminAssessmentJudgementService } from '@/apis/services/admin-assessment-judgement.service'
import { adminAssessmentStatisticsService } from '@/apis/services/admin-assessment-statistics.service'
import { QUESTION_TYPE_LABELS } from './QuestionDrawer'

interface JudgementDrawerProps {
  open: boolean
  question: AssessmentQuestionDTO | null
  canManualReview: boolean
  canDecide: boolean
  onClose: () => void
}

const RESULT_COLORS: Record<string, string> = {
  AC: 'green',
  WA: 'red',
  TLE: 'orange',
  RE: 'red',
  CE: 'orange',
  MLE: 'orange',
}

export default function JudgementDrawer({
  open,
  question,
  canManualReview,
  canDecide,
  onClose,
}: JudgementDrawerProps) {
  const { message: messageApi } = App.useApp()
  const [manualForm] = Form.useForm()
  const [decisionForm] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [judgements, setJudgements] = useState<AssessmentJudgementDTO[]>([])
  const [statistics, setStatistics] = useState<QuestionStatisticsDTO | null>(null)

  const canReviewCurrentQuestion = canManualReview && question?.questionType === 'FILE_UPLOAD'

  const fetchData = useCallback(async () => {
    if (!open || !question) return
    setLoading(true)
    try {
      const [judgementRes, statisticsRes] = await Promise.all([
        adminAssessmentJudgementService.getByQuestion(question.id),
        question.questionType === 'FILE_UPLOAD'
          ? Promise.resolve(null)
          : adminAssessmentStatisticsService.getQuestionStatistics(question.id),
      ])
      setJudgements(judgementRes.data ?? [])
      setStatistics(statisticsRes?.data ?? null)
    } finally {
      setLoading(false)
    }
  }, [open, question])

  useEffect(() => {
    fetchData()
  }, [fetchData])

  const columns: ColumnsType<AssessmentJudgementDTO> = useMemo(
    () => [
      { title: '答案ID', dataIndex: 'answerId', width: 80 },
      { title: '用户ID', dataIndex: 'userId', width: 80 },
      {
        title: '结果',
        dataIndex: 'resultCode',
        width: 90,
        render: (resultCode: string | null) =>
          resultCode ? <Tag color={RESULT_COLORS[resultCode]}>{resultCode}</Tag> : '-',
      },
      { title: '得分', dataIndex: 'score', width: 80 },
      { title: '满分', dataIndex: 'maxScore', width: 80 },
      { title: '来源', dataIndex: 'source', width: 90 },
      { title: '评论', dataIndex: 'comment', ellipsis: true },
    ],
    []
  )

  const handleManualReview = async () => {
    const values = await manualForm.validateFields()
    await adminAssessmentJudgementService.manualReview({
      answerId: values.answerId,
      score: values.score,
      comment: values.comment,
    })
    messageApi.success('人工评分已保存')
    manualForm.resetFields()
    fetchData()
  }

  const handleDecision = async () => {
    const values = await decisionForm.validateFields()
    await adminAssessmentJudgementService.decide({
      userId: values.userId,
      assessmentTimeId: values.assessmentTimeId,
      passed: values.passed,
      decisionComment: values.decisionComment,
    })
    messageApi.success('通过决策已保存')
    decisionForm.resetFields()
  }

  return (
    <Drawer
      title={question ? `评判与统计 · ${question.title}` : '评判与统计'}
      open={open}
      onClose={onClose}
      width="min(960px, calc(100vw - 48px))"
    >
      <Spin spinning={loading}>
        {question && (
          <div className="flex flex-col gap-6">
            <div className="flex flex-wrap gap-3 items-center">
              <Tag bordered={false}>{QUESTION_TYPE_LABELS[question.questionType]}</Tag>
              <span className="text-white/45 text-sm">题号 {question.questionNo}</span>
              <span className="text-white/45 text-sm">{question.score} 分</span>
            </div>

            {statistics && (
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <Statistic title="提交人数" value={statistics.submittedCount} />
                <Statistic title="通过人数" value={statistics.acceptedCount} />
                <Statistic title="通过率" value={`${(statistics.passRate * 100).toFixed(2)}%`} />
                <div className="sm:col-span-3 flex flex-wrap gap-2">
                  {Object.entries(statistics.resultDistribution).map(([code, count]) => (
                    <Tag key={code} color={RESULT_COLORS[code] ?? 'default'}>
                      {code}: {count}
                    </Tag>
                  ))}
                </div>
              </div>
            )}

            <Table
              rowKey={(record) => record.id}
              size="small"
              columns={columns}
              dataSource={judgements}
              pagination={false}
            />

            {canReviewCurrentQuestion && (
              <Form form={manualForm} layout="vertical">
                <h3 className="text-sm font-semibold text-white/80">文件题人工评分</h3>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  <Form.Item name="answerId" label="答案ID" rules={[{ required: true }]}>
                    <InputNumber min={1} style={{ width: '100%' }} />
                  </Form.Item>
                  <Form.Item name="score" label="得分" rules={[{ required: true }]}>
                    <InputNumber min={0} max={question.score} style={{ width: '100%' }} />
                  </Form.Item>
                  <Form.Item name="comment" label="评论">
                    <Input />
                  </Form.Item>
                </div>
                <Button type="primary" onClick={handleManualReview}>
                  保存评分
                </Button>
              </Form>
            )}

            {canDecide && (
              <Form
                form={decisionForm}
                layout="vertical"
                initialValues={{ assessmentTimeId: question.assessmentTimeId }}
              >
                <h3 className="text-sm font-semibold text-white/80">考核通过决策</h3>
                <div className="grid grid-cols-1 sm:grid-cols-4 gap-3">
                  <Form.Item name="userId" label="用户ID" rules={[{ required: true }]}>
                    <InputNumber min={1} style={{ width: '100%' }} />
                  </Form.Item>
                  <Form.Item
                    name="assessmentTimeId"
                    label="考核时间ID"
                    rules={[{ required: true }]}
                  >
                    <InputNumber min={1} style={{ width: '100%' }} />
                  </Form.Item>
                  <Form.Item name="passed" label="是否通过" rules={[{ required: true }]}>
                    <Select
                      options={[
                        { value: true, label: '通过' },
                        { value: false, label: '不通过' },
                      ]}
                    />
                  </Form.Item>
                  <Form.Item name="decisionComment" label="备注">
                    <Input />
                  </Form.Item>
                </div>
                <Button type="primary" onClick={handleDecision}>
                  保存决策
                </Button>
              </Form>
            )}
          </div>
        )}
      </Spin>
    </Drawer>
  )
}
