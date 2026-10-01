'use client'

import type { CSSProperties, ReactNode } from 'react'
import { useRouter } from 'next/navigation'
import {
  ClockCircleOutlined,
  CalendarOutlined,
  FieldTimeOutlined,
  InboxOutlined,
  DesktopOutlined,
  RightOutlined,
  TeamOutlined,
} from '@ant-design/icons'
import type { AssessmentTimeDTO, AssessmentStatus } from '@/apis/schema/assessment.dto'
import { DIRECTION_LABELS } from '@/apis/schema/enumerate'
import SmokedGlassCard from '@/components/SmokedGlassCard'

interface AssessmentCardProps {
  assessment: AssessmentTimeDTO
}

/** 卡片视觉状态：按优先级 eliminated > inProgress > ended > notStarted 派生 */
type VisualState = 'eliminated' | 'inProgress' | 'ended' | 'notStarted'

/**
 * 单一视觉状态对应的视觉规格。
 * token 化设计：状态只贡献 accent 颜色变量（外加语义色：被淘汰=红），
 * 卡片边框/光晕/内高光由 SmokedGlassCard 经 --accent 派生，
 * 图标/进度条/按钮在渲染处经 color-mix 派生，不再逐状态手写全部颜色。
 */
interface VisualStateStyle {
  /** 主题色 token（未设置则为中性态） */
  accent?: string
  /** 是否可点击进入答题页 */
  clickable: boolean
  /** 整体置灰（被淘汰） */
  dimmed?: boolean
  /** 左上角图标元素 */
  icon: ReactNode
  /** 状态徽章：语义色类名（被淘汰=红、未开始=灰）；accent 态留空，渲染处派生 */
  badgeClass: string
  /** 状态徽章文案 */
  badgeText: string
  /** 操作按钮配色：accent（渐变发光）或 gray（中性禁用） */
  buttonTone: 'accent' | 'gray'
  /** 操作按钮文案 */
  buttonText: string
  /** 进度条配色：accent 或 gray */
  progressTone: 'accent' | 'gray'
  /** 是否渲染顶部高光线 */
  topLine: boolean
}

/** 四态视觉映射表：新增状态时 Record 会强制补齐全部字段，杜绝遗漏导致的不一致 */
const STYLES: Record<VisualState, VisualStateStyle> = {
  eliminated: {
    clickable: false,
    dimmed: true,
    icon: <InboxOutlined />,
    badgeClass: 'bg-[rgba(255,77,79,0.1)] text-[#ff4d4f] border border-[rgba(255,77,79,0.15)]',
    badgeText: '已被淘汰',
    buttonTone: 'gray',
    buttonText: '已被淘汰',
    progressTone: 'gray',
    topLine: false,
  },
  inProgress: {
    accent: '#07c160',
    clickable: true,
    icon: <FieldTimeOutlined />,
    badgeClass: '',
    badgeText: '进行中',
    buttonTone: 'accent',
    buttonText: '继续答题',
    progressTone: 'accent',
    topLine: true,
  },
  ended: {
    accent: '#6677ff',
    clickable: true,
    icon: <DesktopOutlined />,
    badgeClass: '',
    badgeText: '已结束',
    buttonTone: 'accent',
    buttonText: '查看详情',
    progressTone: 'accent',
    topLine: true,
  },
  notStarted: {
    clickable: false,
    icon: <InboxOutlined />,
    badgeClass: 'bg-[rgba(140,140,141,0.1)] text-[#8c8c8d] border border-[rgba(140,140,141,0.15)]',
    badgeText: '未开始',
    buttonTone: 'gray',
    buttonText: '暂不可进入',
    progressTone: 'gray',
    topLine: false,
  },
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function getEpochLabel(epoch: number): string {
  if (epoch === 0) return '最终考核'
  const chineseNumbers = ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
  return `第${chineseNumbers[epoch - 1] || epoch}轮考核`
}

function getAssessmentStatus(startTime: string, endTime: string): AssessmentStatus {
  const now = new Date().getTime()
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  if (now < start) return 'NOT_STARTED'
  if (now > end) return 'ENDED'
  return 'IN_PROGRESS'
}

export default function AssessmentCard({ assessment }: AssessmentCardProps) {
  const router = useRouter()

  const total = assessment.totalQuestions ?? 0
  const completed = assessment.completedQuestions ?? 0
  const progressPercent = total > 0 ? Math.round((completed / total) * 100) : 0

  const actualStatus = getAssessmentStatus(assessment.startTime, assessment.endTime)
  const eliminated = !!assessment.eliminated
  const visualState: VisualState = eliminated
    ? 'eliminated'
    : actualStatus === 'IN_PROGRESS'
      ? 'inProgress'
      : actualStatus === 'ENDED'
        ? 'ended'
        : 'notStarted'
  const cardStyle = STYLES[visualState]
  const accent = cardStyle.accent

  // token 派生：状态色统一经 --accent（color-mix），中性态回落灰色
  const iconStyle: CSSProperties = accent
    ? {
        background: 'color-mix(in srgb, var(--accent) 15%, transparent)',
        color: 'var(--accent)',
        boxShadow: '0 0 16px color-mix(in srgb, var(--accent) 15%, transparent)',
      }
    : { background: 'rgba(140,140,141,0.1)', color: '#8c8c8d' }

  const badgeStyle: CSSProperties = accent
    ? {
        background: 'color-mix(in srgb, var(--accent) 15%, transparent)',
        color: 'var(--accent)',
        border: '1px solid color-mix(in srgb, var(--accent) 20%, transparent)',
      }
    : {}

  const progressStyle: CSSProperties = accent
    ? {
        background:
          'linear-gradient(to right, var(--accent), color-mix(in srgb, var(--accent) 70%, black))',
        boxShadow: '0 0 8px color-mix(in srgb, var(--accent) 30%, transparent)',
      }
    : { background: 'rgba(140,140,141,0.3)' }

  const buttonStyle: CSSProperties =
    cardStyle.buttonTone === 'accent' && accent
      ? {
          background:
            'linear-gradient(135deg, var(--accent), color-mix(in srgb, var(--accent) 65%, black))',
          color: '#fff',
          boxShadow: '0 4px 16px color-mix(in srgb, var(--accent) 30%, transparent)',
        }
      : {
          background: 'rgba(140,140,141,0.15)',
          color: 'rgba(140,140,141,0.7)',
          border: '1px solid rgba(140,140,141,0.15)',
          cursor: 'not-allowed',
        }

  const epoch = assessment.epoch
  const direction = assessment.direction
  const timeLimit = assessment.timeLimit
  const timeLimitMinutes = assessment.timeLimitMinutes
  const allowTeam = assessment.allowTeam

  return (
    <SmokedGlassCard
      hoverable={cardStyle.clickable}
      accent={accent}
      radius={16}
      className={`overflow-hidden max-sm:[--sgc-padding:18px] ${cardStyle.dimmed ? 'opacity-70' : ''}`}
      style={{ '--sgc-padding': '24px' } as CSSProperties}
    >
      {cardStyle.topLine && accent && (
        <div
          className="absolute top-0 left-0 right-0 h-px pointer-events-none"
          style={{
            background:
              'linear-gradient(to right, transparent, color-mix(in srgb, var(--accent) 30%, transparent), transparent)',
          }}
        />
      )}

      <div className="flex justify-between items-start mb-5">
        <div className="flex items-center gap-[14px]">
          <div
            className="w-11 h-11 rounded-xl flex items-center justify-center text-xl shrink-0 backdrop-blur-[8px]"
            style={iconStyle}
          >
            {cardStyle.icon}
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-base font-semibold text-white">{getEpochLabel(epoch)}</span>
            <span className="text-[13px] text-white/45">
              {direction ? DIRECTION_LABELS[direction] : '全局'}
            </span>
          </div>
        </div>
        <span
          className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium whitespace-nowrap shrink-0 backdrop-blur-[8px] ${cardStyle.badgeClass}`}
          style={badgeStyle}
        >
          {cardStyle.badgeText}
        </span>
      </div>

      <div className="flex flex-wrap gap-4 mb-4">
        <div className="flex items-center gap-[6px] text-[13px] text-white/50">
          <CalendarOutlined className="text-sm" />
          <span>
            {formatDate(assessment.startTime)} — {formatDate(assessment.endTime)}
          </span>
        </div>
        {timeLimit && timeLimitMinutes ? (
          <div className="flex items-center gap-[6px] text-[13px] text-[#fa8c16] font-medium">
            <ClockCircleOutlined className="text-sm" />
            <span>限时 {timeLimitMinutes} 分钟</span>
          </div>
        ) : (
          <div className="flex items-center gap-[6px] text-[13px] text-white/50">
            <ClockCircleOutlined className="text-sm" />
            <span>不限时</span>
          </div>
        )}
        {allowTeam && (
          <div className="flex items-center gap-[6px] text-[13px] text-[#6677ff] font-medium">
            <TeamOutlined className="text-sm" />
            <span>允许组队</span>
          </div>
        )}
      </div>

      {total > 0 && (
        <div className="mb-5">
          <div className="flex justify-between items-center mb-2">
            <span className="text-[13px] text-white/50">答题进度</span>
            <span className="text-[13px] font-medium text-white/65">
              {completed}/{total} 已完成
            </span>
          </div>
          <div className="h-[6px] bg-white/[0.06] rounded-[3px] overflow-hidden">
            <div
              className="h-full rounded-[3px] transition-[width] duration-300"
              style={{ ...progressStyle, width: `${progressPercent}%` }}
            />
          </div>
        </div>
      )}

      <div className="flex justify-end">
        <button
          className="inline-flex items-center gap-[6px] px-5 py-2 rounded-lg text-[13px] font-medium border-none cursor-pointer transition-all backdrop-blur-[8px]"
          style={buttonStyle}
          onClick={() => {
            if (!cardStyle.clickable) return
            router.push(`/assessment/${assessment.id.toString()}/questions`)
          }}
        >
          {cardStyle.buttonText}
          {cardStyle.clickable && <RightOutlined className="text-xs" />}
        </button>
      </div>
    </SmokedGlassCard>
  )
}

export type { AssessmentCardProps }
