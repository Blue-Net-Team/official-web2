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
} from '@ant-design/icons'
import { Spin, Table, Tag } from 'antd'
import type { TableColumnsType } from 'antd'
import { assessmentQuestionService } from '@/apis/services/assessment-question.service'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'
import authStore from '@/stores/authStore'
import type {
  AssessmentQuestionDTO,
  AssessmentTimeDTO,
  QuestionType,
} from '@/apis/schema/assessment.dto'
import { QuestionTypeLabels } from '@/types/assessment'
import { DIRECTION_LABELS as DirectionLabels } from '@/apis/schema/enumerate'
import type { Direction } from '@/apis/schema/enumerate'
import styles from './styles.module.css'

/** 获取状态显示文本 */
function getStatusText(
  startTime: string,
  endTime: string
): { text: string; status: 'not-started' | 'in-progress' | 'ended' } {
  const now = new Date().getTime()
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

const QuestionTypePresetColor: Record<QuestionType, string> = {
  single_choice: 'geekblue',
  multiple_choice: 'purple',
  file_upload: 'orange',
  algorithm: 'cyan',
}

function getQuestionTypeColor(type: QuestionType): string {
  return QuestionTypePresetColor[type] || 'default'
}

/** 获取轮次名称 */
function getEpochLabel(epoch: number): string {
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
  const [loading, setLoading] = useState(true)
  const { isAuthenticated, checkAuthStatus } = authStore()

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

  useEffect(() => {
    if (isAuthenticated) {
      fetchTimeInfo()
      fetchQuestions()
    }
  }, [isAuthenticated, fetchTimeInfo, fetchQuestions])

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
            style={{
              padding: '2px 10px',
              borderRadius: '4px',
              fontSize: '12px',
              fontWeight: 500,
            }}
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
            <span className={styles.answeredBadge}>
              <CheckCircleOutlined /> 已答
            </span>
          ) : (
            <span className={styles.unansweredBadge}>未答</span>
          ),
      },
    ],
    []
  )

  if (!isAuthenticated) {
    return (
      <div className={styles.container}>
        <div className={styles.pageBg} />
        <div className={styles.loading}>
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
    <div className={styles.container}>
      <div className={styles.pageBg} />
      <div className={styles.content}>
        <div className={styles.header}>
          <button className={styles.backButton} onClick={() => router.push('/assessment')}>
            <ArrowLeftOutlined />
            <span>返回考核列表</span>
          </button>
          <div className={styles.titleSection}>
            <div className={styles.titleRow}>
              <h1 className={styles.title}>
                {timeInfo ? getEpochLabel(timeInfo.epoch) : '考核'} · 考题目录
              </h1>
              {statusInfo && (
                <Tag
                  variant="outlined"
                  color={
                    statusInfo.status === 'not-started'
                      ? 'default'
                      : statusInfo.status === 'in-progress'
                        ? 'processing'
                        : 'success'
                  }
                >
                  {statusInfo.text}
                </Tag>
              )}
            </div>
            <div className={styles.metaRow}>
              {timeInfo && (
                <>
                  <Tag variant="outlined">
                    {DirectionLabels[timeInfo.direction as Direction] || timeInfo.direction}
                  </Tag>
                  <Tag variant="outlined">
                    {timeInfo.grade === 1 ? '大一' : timeInfo.grade === 2 ? '大二' : '大三'}
                  </Tag>
                  <span className={styles.metaItem}>
                    <CalendarOutlined />
                    <span>
                      {formatDate(timeInfo.startTime)} — {formatDate(timeInfo.endTime)}
                    </span>
                  </span>
                </>
              )}
            </div>
          </div>
        </div>

        {loading ? (
          <div className={styles.loading}>
            <Spin size="large" />
          </div>
        ) : (
          <>
            <div className={styles.statsRow}>
              <div className={styles.statCard}>
                <FileTextOutlined className={styles.statIcon} />
                <div className={styles.statInfo}>
                  <span className={styles.statValue}>{totalElements}</span>
                  <span className={styles.statLabel}>题目总数</span>
                </div>
              </div>
              <div className={styles.statCard}>
                <CheckCircleOutlined className={`${styles.statIcon} ${styles.statIconGreen}`} />
                <div className={styles.statInfo}>
                  <span className={styles.statValue}>{answeredCount}</span>
                  <span className={styles.statLabel}>已作答</span>
                </div>
              </div>
              <div className={styles.statCard}>
                <MinusCircleOutlined className={`${styles.statIcon} ${styles.statIconDim}`} />
                <div className={styles.statInfo}>
                  <span className={styles.statValue}>{unansweredCount}</span>
                  <span className={styles.statLabel}>未作答</span>
                </div>
              </div>
              <div className={styles.statCard}>
                <TrophyOutlined className={`${styles.statIcon} ${styles.statIconGold}`} />
                <div className={styles.statInfo}>
                  <span className={styles.statValue}>{totalScore}</span>
                  <span className={styles.statLabel}>总分</span>
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
                style: { cursor: 'pointer' },
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
              className={styles.questionTable}
            />

            {questions.length > 0 && (
              <div className={styles.footer}>
                <span className={styles.footerSummary}>
                  共 {totalElements} 道题目 · 已作答 {answeredCount} · 未答 {unansweredCount} · 总分{' '}
                  {totalScore}
                </span>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
