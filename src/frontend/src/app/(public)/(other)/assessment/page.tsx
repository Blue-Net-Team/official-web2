'use client'

import { useState, useEffect, useCallback } from 'react'
import {
  ClockCircleOutlined,
  CalendarOutlined,
  FieldTimeOutlined,
  InboxOutlined,
  DesktopOutlined,
  RightOutlined,
} from '@ant-design/icons'
import { Spin } from 'antd'
import { useRouter } from 'next/navigation'
import authStore from '@/stores/authStore'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'
import type { AssessmentTimeDTO, AssessmentStatus } from '@/types/assessment'
import { DirectionLabels } from '@/types/assessment'
import styles from './styles.module.css'

/** 计算考核状态 */
function getAssessmentStatus(startTime: string, endTime: string): AssessmentStatus {
  const now = new Date().getTime()
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  if (now < start) return 'not-started'
  if (now > end) return 'ended'
  return 'in-progress'
}

/** 格式化日期 */
function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/** 获取状态显示文本 */
function getStatusText(status: AssessmentStatus): string {
  const map: Record<AssessmentStatus, string> = {
    'not-started': '未开始',
    'in-progress': '进行中',
    ended: '已结束',
  }
  return map[status]
}

/** 获取状态样式类 */
function getStatusClass(status: AssessmentStatus): string {
  const map: Record<AssessmentStatus, string> = {
    'in-progress': styles.statusInProgress,
    'not-started': styles.statusNotStarted,
    ended: styles.statusEnded,
  }
  return map[status]
}

/** 获取卡片样式类 */
function getCardClass(status: AssessmentStatus): string {
  const map: Record<AssessmentStatus, string> = {
    'in-progress': styles.cardInProgress,
    'not-started': styles.cardNotStarted,
    ended: styles.cardEnded,
  }
  return map[status]
}

/** 获取图标样式类 */
function getIconClass(status: AssessmentStatus): string {
  const map: Record<AssessmentStatus, string> = {
    'in-progress': styles.cardIconInProgress,
    'not-started': styles.cardIconNotStarted,
    ended: styles.cardIconEnded,
  }
  return map[status]
}

/** 获取进度条样式类 */
function getProgressClass(status: AssessmentStatus): string {
  const map: Record<AssessmentStatus, string> = {
    'in-progress': styles.progressFillInProgress,
    'not-started': styles.progressFillNotStarted,
    ended: styles.progressFillEnded,
  }
  return map[status]
}

/** 获取按钮样式类 */
function getButtonClass(status: AssessmentStatus): string {
  const map: Record<AssessmentStatus, string> = {
    'in-progress': styles.actionInProgress,
    'not-started': styles.actionNotStarted,
    ended: styles.actionEnded,
  }
  return map[status]
}

/** 获取按钮文本 */
function getButtonText(status: AssessmentStatus): string {
  const map: Record<AssessmentStatus, string> = {
    'in-progress': '继续答题',
    'not-started': '暂不可进入',
    ended: '查看详情',
  }
  return map[status]
}

/** 获取轮次名称 */
function getEpochLabel(epoch: number): string {
  const chineseNumbers = ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
  return `第${chineseNumbers[epoch - 1] || epoch}轮考核`
}

export default function AssessmentPage() {
  const router = useRouter()
  const [assessmentTimes, setAssessmentTimes] = useState<AssessmentTimeDTO[]>([])
  const [loading, setLoading] = useState(true)
  const { userInfo, isAuthenticated, checkAuthStatus } = authStore()

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

  // 加载考核时间列表
  const fetchAssessmentTimes = useCallback(async () => {
    try {
      const response = await assessmentTimeService.getAssessmentTimes(0, 50)
      if (response.code === 200 && response.data) {
        const sorted = [...response.data.content].sort((a, b) => a.epoch - b.epoch)
        setAssessmentTimes(sorted)
      } else {
        setAssessmentTimes([])
      }
    } catch (error) {
      console.error('Failed to fetch assessment times:', error)
      setAssessmentTimes([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (isAuthenticated) {
      fetchAssessmentTimes()
    }
  }, [isAuthenticated, fetchAssessmentTimes])

  // 未认证时显示 loading
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

  return (
    <div className={styles.container}>
      <div className={styles.pageBg} />
      <div className={styles.content}>
        {/* Header */}
        <div className={styles.header}>
          <h1 className={styles.title}>考核时间安排</h1>
          <p className={styles.subtitle}>查看当前可参加的考核时间安排</p>
          {userInfo && (
            <div className={styles.tags}>
              <span className={styles.tag}>
                {DirectionLabels[userInfo.direction as keyof typeof DirectionLabels] ||
                  userInfo.direction}
              </span>
              <span className={styles.tag}>{userInfo.grade}</span>
            </div>
          )}
        </div>

        {/* Loading */}
        {loading ? (
          <div className={styles.loading}>
            <Spin size="large" />
          </div>
        ) : assessmentTimes.length === 0 ? (
          /* Empty State */
          <div className={styles.emptyState}>
            <div className={styles.emptyIcon}>
              <CalendarOutlined />
            </div>
            <h3 className={styles.emptyTitle}>暂无考核安排</h3>
            <p className={styles.emptyDesc}>当前没有可参加的考核时间</p>
          </div>
        ) : (
          /* Card List */
          <div className={styles.cardList}>
            {assessmentTimes.map((item) => {
              const status = getAssessmentStatus(item.startTime, item.endTime)
              const total = item.totalQuestions ?? 0
              const completed = item.completedQuestions ?? 0
              const progressPercent = total > 0 ? Math.round((completed / total) * 100) : 0

              return (
                <div key={item.id} className={`${styles.card} ${getCardClass(status)}`}>
                  {/* Card Header */}
                  <div className={styles.cardHeader}>
                    <div className={styles.cardTitleSection}>
                      <div className={`${styles.cardIcon} ${getIconClass(status)}`}>
                        {status === 'ended' ? (
                          <DesktopOutlined />
                        ) : status === 'in-progress' ? (
                          <FieldTimeOutlined />
                        ) : (
                          <InboxOutlined />
                        )}
                      </div>
                      <div className={styles.cardTitleInfo}>
                        <span className={styles.cardTitle}>{getEpochLabel(item.epoch)}</span>
                        <span className={styles.cardSubtitle}>
                          {DirectionLabels[item.direction] || item.direction}
                        </span>
                      </div>
                    </div>
                    <span className={`${styles.statusBadge} ${getStatusClass(status)}`}>
                      {getStatusText(status)}
                    </span>
                  </div>

                  {/* Card Meta */}
                  <div className={styles.cardMeta}>
                    <div className={styles.metaItem}>
                      <CalendarOutlined />
                      <span>
                        {formatDate(item.startTime)} — {formatDate(item.endTime)}
                      </span>
                    </div>
                    {item.timeLimit && item.timeLimitMinutes ? (
                      <div className={`${styles.metaItem} ${styles.timeLimitHighlight}`}>
                        <ClockCircleOutlined />
                        <span>限时 {item.timeLimitMinutes} 分钟</span>
                      </div>
                    ) : (
                      <div className={styles.metaItem}>
                        <ClockCircleOutlined />
                        <span>不限时</span>
                      </div>
                    )}
                  </div>

                  {/* Progress */}
                  {total > 0 && (
                    <div className={styles.progressSection}>
                      <div className={styles.progressHeader}>
                        <span className={styles.progressLabel}>答题进度</span>
                        <span className={styles.progressValue}>
                          {completed}/{total} 已完成
                        </span>
                      </div>
                      <div className={styles.progressBar}>
                        <div
                          className={`${styles.progressFill} ${getProgressClass(status)}`}
                          style={{ width: `${progressPercent}%` }}
                        />
                      </div>
                    </div>
                  )}

                  {/* Action Button */}
                  <div className={styles.cardFooter}>
                    <button className={`${styles.actionButton} ${getButtonClass(status)}`}>
                      {getButtonText(status)}
                      {status !== 'not-started' && <RightOutlined />}
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
