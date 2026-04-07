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
  if (status === 'ended') return 'bg-[#07c160]'
  if (status === 'in-progress') return 'bg-[linear-gradient(90deg,#6677ff_0%,#2f27b0_100%)]'
  return 'bg-[rgba(140,140,141,0.5)]'
}

function getStatusClass(status: Assessment['status']): string {
  if (status === 'ended') return 'bg-[rgba(7,193,96,0.15)] text-[#07c160]'
  if (status === 'in-progress')
    return 'bg-[linear-gradient(135deg,#6677ff_0%,#2f27b0_100%)] text-white'
  return 'bg-[rgba(140,140,141,0.15)] text-[rgba(140,140,141,1)]'
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
      <div className="text-center py-[60px] px-5 bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl">
        <div className="w-20 h-20 mx-auto mb-5 rounded-full bg-[rgba(102,119,255,0.1)] flex items-center justify-center [&>svg]:w-10 [&>svg]:h-10 [&>svg]:text-[#6677ff]">
          <FileTextOutlined />
        </div>
        <h3 className="text-lg font-semibold text-white mb-2">暂无考核记录</h3>
        <p className="text-sm text-[rgba(140,140,141,1)]">您还没有参与任何考核</p>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4">
      {assessments.map((assessment) => (
        <div
          key={assessment.id}
          className="bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-2xl p-6 transition-all duration-300 cursor-pointer hover:-translate-y-1 hover:border-[rgba(102,119,255,0.2)] hover:shadow-[0_8px_32px_rgba(102,119,255,0.1)] max-[640px]:p-4"
        >
          <div className="flex items-start justify-between mb-4 max-[640px]:flex-col max-[640px]:gap-3">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-xl bg-[linear-gradient(135deg,#6677ff_0%,#2f27b0_100%)] flex items-center justify-center [&>svg]:w-6 [&>svg]:h-6 [&>svg]:text-white">
                <StatusIcon status={assessment.status} />
              </div>
              <div className="flex flex-col gap-1">
                <div className="text-lg font-semibold text-white">{assessment.title}</div>
                <div className="text-sm text-[#6677ff]">{assessment.round}</div>
              </div>
            </div>
            <span
              className={`py-1.5 px-[14px] rounded-[20px] text-xs font-semibold ${getStatusClass(assessment.status)}`}
            >
              {getStatusText(assessment.status)}
            </span>
          </div>

          <div className="flex flex-wrap gap-5 mb-4 pb-4 border-b border-white/[0.05] max-[640px]:flex-col max-[640px]:gap-2">
            <div className="flex items-center gap-2 text-[13px] text-[rgba(140,140,141,1)] [&>svg]:w-4 [&>svg]:h-4">
              <CalendarOutlined />
              <span>
                {assessment.startDate} 至 {assessment.endDate}
              </span>
            </div>
            {assessment.status === 'in-progress' && assessment.remainingTime && (
              <div className="flex items-center gap-2 text-[13px] text-[rgba(140,140,141,1)] [&>svg]:w-4 [&>svg]:h-4">
                <ClockCircleOutlined />
                <span>剩余 {assessment.remainingTime}</span>
              </div>
            )}
            {assessment.status === 'not-started' && assessment.daysUntilStart && (
              <div className="flex items-center gap-2 text-[13px] text-[rgba(140,140,141,1)] [&>svg]:w-4 [&>svg]:h-4">
                <ClockCircleOutlined />
                <span>距开始还有 {assessment.daysUntilStart} 天</span>
              </div>
            )}
            {assessment.totalQuestions > 0 && (
              <div className="flex items-center gap-2 text-[13px] text-[rgba(140,140,141,1)] [&>svg]:w-4 [&>svg]:h-4">
                <FileTextOutlined />
                <span>{assessment.totalQuestions} 道题目</span>
              </div>
            )}
            {assessment.status === 'not-started' && assessment.totalQuestions === 0 && (
              <div className="flex items-center gap-2 text-[13px] text-[rgba(140,140,141,1)] [&>svg]:w-4 [&>svg]:h-4">
                <FileTextOutlined />
                <span>题目数量待定</span>
              </div>
            )}
          </div>

          <div className="flex items-center gap-3">
            <div className="flex-1 h-1.5 bg-white/[0.05] rounded-[3px] overflow-hidden">
              <div
                className={`h-full rounded-[3px] transition-[width] duration-300 ${getProgressFillClass(assessment.status)}`}
                style={{ width: `${getProgressPercent(assessment)}%` }}
              />
            </div>
            <span className="text-[13px] text-[rgba(140,140,141,1)] min-w-[60px] text-right">
              {getProgressText(assessment)}
            </span>
          </div>

          <div className="flex items-center justify-between mt-4 pt-4 border-t border-white/[0.05]">
            <div className="flex items-center gap-2 text-sm text-white/80">
              {assessment.status === 'ended' && assessment.score !== undefined ? (
                <>
                  最终得分：
                  <TrophyOutlined style={{ color: '#ff6b35', marginRight: 4 }} />
                  <span className="text-xl font-bold text-[#ff6b35]">{assessment.score}</span>
                </>
              ) : assessment.status === 'in-progress' ? (
                <>
                  当前得分：<span className="text-xl font-bold text-[#ff6b35]">--</span>
                </>
              ) : (
                <>
                  状态：<span style={{ color: 'rgba(140, 140, 141, 1)' }}>等待开始</span>
                </>
              )}
            </div>
            <div
              className={`flex items-center gap-1.5 text-sm text-[#6677ff] transition-all duration-300 ${
                assessment.status === 'not-started' ? 'text-[rgba(140,140,141,0.6)]' : ''
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
