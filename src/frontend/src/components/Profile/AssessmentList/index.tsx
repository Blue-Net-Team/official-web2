/**
 * 考核列表组件 - 服务端组件
 *
 * 功能：
 * - 展示考核列表（进行中/已结束/未开始三种状态）
 * - 每个考核卡片包含：考核名称、轮次、时间、题目数量、进度
 * - 支持点击进入考核详情（进行中/已结束）
 * - 空状态展示
 *
 * @author BlueNet Team
 */
import type { Assessment } from '@/types/profile'
import styles from './styles.module.css'
import {
  ClockCircleOutlined,
  FileTextOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  RightOutlined,
  TrophyOutlined,
} from '@ant-design/icons'

interface AssessmentListProps {
  assessments: Assessment[]
}

function StatusIcon({ status }: { status: Assessment['status'] }) {
  if (status === 'ended') {
    return <CheckCircleOutlined />
  }
  if (status === 'in-progress') {
    return <ClockCircleOutlined />
  }
  return <CalendarOutlined />
}

function getProgressFillClass(status: Assessment['status']): string {
  if (status === 'ended') return styles.progressFillEnded
  if (status === 'in-progress') return styles.progressFillInProgress
  return styles.progressFillNotStarted
}

function getStatusClass(status: Assessment['status']): string {
  if (status === 'ended') return styles.statusEnded
  if (status === 'in-progress') return styles.statusInProgress
  return styles.statusNotStarted
}

function getStatusText(status: Assessment['status']): string {
  if (status === 'ended') return '已结束'
  if (status === 'in-progress') return '进行中'
  return '未开始'
}

function getProgressText(assessment: Assessment): string {
  if (assessment.status === 'ended') return '已完成'
  if (assessment.status === 'in-progress') {
    return `${assessment.completedQuestions}/${assessment.totalQuestions} 已完成`
  }
  return '未开始'
}

function getProgressPercent(assessment: Assessment): number {
  if (assessment.status === 'ended') return 100
  if (assessment.status === 'in-progress' && assessment.totalQuestions > 0) {
    return (assessment.completedQuestions / assessment.totalQuestions) * 100
  }
  return 0
}

export default function AssessmentList({ assessments }: AssessmentListProps) {
  if (assessments.length === 0) {
    return (
      <div className={styles.emptyState}>
        <div className={styles.emptyIcon}>
          <FileTextOutlined />
        </div>
        <h3 className={styles.emptyTitle}>暂无考核记录</h3>
        <p className={styles.emptyDesc}>您还没有参与任何考核</p>
      </div>
    )
  }

  return (
    <div className={styles.assessmentList}>
      {assessments.map((assessment) => (
        <div key={assessment.id} className={styles.assessmentCard}>
          <div className={styles.assessmentHeader}>
            <div className={styles.assessmentTitleSection}>
              <div className={styles.assessmentIcon}>
                <StatusIcon status={assessment.status} />
              </div>
              <div className={styles.assessmentTitleInfo}>
                <div className={styles.assessmentTitle}>{assessment.title}</div>
                <div className={styles.assessmentRound}>{assessment.round}</div>
              </div>
            </div>
            <span className={`${styles.assessmentStatus} ${getStatusClass(assessment.status)}`}>
              {getStatusText(assessment.status)}
            </span>
          </div>

          <div className={styles.assessmentMeta}>
            <div className={styles.assessmentMetaItem}>
              <CalendarOutlined />
              <span>
                {assessment.startDate} 至 {assessment.endDate}
              </span>
            </div>
            {assessment.status === 'in-progress' && assessment.remainingTime && (
              <div className={styles.assessmentMetaItem}>
                <ClockCircleOutlined />
                <span>剩余 {assessment.remainingTime}</span>
              </div>
            )}
            {assessment.status === 'not-started' && assessment.daysUntilStart && (
              <div className={styles.assessmentMetaItem}>
                <ClockCircleOutlined />
                <span>距开始还有 {assessment.daysUntilStart} 天</span>
              </div>
            )}
            {assessment.totalQuestions > 0 && (
              <div className={styles.assessmentMetaItem}>
                <FileTextOutlined />
                <span>{assessment.totalQuestions} 道题目</span>
              </div>
            )}
            {assessment.status === 'not-started' && assessment.totalQuestions === 0 && (
              <div className={styles.assessmentMetaItem}>
                <FileTextOutlined />
                <span>题目数量待定</span>
              </div>
            )}
          </div>

          <div className={styles.assessmentProgress}>
            <div className={styles.progressBar}>
              <div
                className={`${styles.progressFill} ${getProgressFillClass(assessment.status)}`}
                style={{ width: `${getProgressPercent(assessment)}%` }}
              />
            </div>
            <span className={styles.progressText}>{getProgressText(assessment)}</span>
          </div>

          <div className={styles.assessmentFooter}>
            <div className={styles.assessmentScore}>
              {assessment.status === 'ended' && assessment.score !== undefined ? (
                <>
                  最终得分：
                  <TrophyOutlined style={{ color: '#ff6b35', marginRight: 4 }} />
                  <span className={styles.scoreValue}>{assessment.score}</span>
                </>
              ) : assessment.status === 'in-progress' ? (
                <>
                  当前得分：<span className={styles.scoreValue}>--</span>
                </>
              ) : (
                <>
                  状态：<span style={{ color: 'rgba(140, 140, 141, 1)' }}>等待开始</span>
                </>
              )}
            </div>
            <div
              className={`${styles.assessmentAction} ${
                assessment.status === 'not-started' ? styles.assessmentActionDisabled : ''
              }`}
            >
              {assessment.status === 'ended'
                ? '查看详情'
                : assessment.status === 'in-progress'
                  ? '继续答题'
                  : '暂不可进入'}
              {assessment.status !== 'not-started' && <RightOutlined />}
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}
