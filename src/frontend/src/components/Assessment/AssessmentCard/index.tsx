'use client'

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

interface AssessmentCardProps {
  assessment: AssessmentTimeDTO
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
  const isInProgress = actualStatus === 'IN_PROGRESS' && !eliminated
  const isEnded = actualStatus === 'ENDED' && !eliminated
  const isNotStarted = actualStatus === 'NOT_STARTED'

  const epoch = assessment.epoch
  const direction = assessment.direction
  const timeLimit = assessment.timeLimit
  const timeLimitMinutes = assessment.timeLimitMinutes
  const allowTeam = assessment.allowTeam

  return (
    <div
      className={`relative bg-white/[0.04] border rounded-2xl p-6 max-sm:p-[18px] backdrop-blur-[24px] transition-all overflow-hidden ${
        eliminated
          ? 'border-white/[0.04] opacity-70'
          : isInProgress
            ? 'border-[rgba(102,119,255,0.2)] shadow-[0_0_20px_rgba(102,119,255,0.06),inset_0_1px_0_rgba(102,119,255,0.1)] hover:-translate-y-0.5 hover:bg-white/[0.06] hover:shadow-[0_8px_32px_rgba(102,119,255,0.12),inset_0_1px_0_rgba(102,119,255,0.15)] hover:border-[rgba(102,119,255,0.3)]'
            : isEnded
              ? 'border-[rgba(7,193,96,0.2)] shadow-[0_0_20px_rgba(7,193,96,0.06),inset_0_1px_0_rgba(7,193,96,0.1)] hover:-translate-y-0.5 hover:bg-white/[0.06] hover:shadow-[0_8px_32px_rgba(7,193,96,0.12),inset_0_1px_0_rgba(7,193,96,0.15)] hover:border-[rgba(7,193,96,0.3)]'
              : 'border-white/[0.06] hover:-translate-y-0.5 hover:bg-white/[0.06] hover:shadow-[0_4px_16px_rgba(255,255,255,0.04)]'
      }`}
    >
      {isInProgress && (
        <div className="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-[rgba(102,119,255,0.3)] to-transparent pointer-events-none" />
      )}
      {isEnded && (
        <div className="absolute top-0 left-0 right-0 h-px bg-gradient-to-r from-transparent via-[rgba(7,193,96,0.3)] to-transparent pointer-events-none" />
      )}

      <div className="flex justify-between items-start mb-5">
        <div className="flex items-center gap-[14px]">
          <div
            className={`w-11 h-11 rounded-xl flex items-center justify-center text-xl shrink-0 backdrop-blur-[8px] ${
              eliminated
                ? 'bg-[rgba(140,140,141,0.08)] text-[#8c8c8d]/50'
                : isInProgress
                  ? 'bg-[rgba(102,119,255,0.15)] text-[#6677ff] shadow-[0_0_16px_rgba(102,119,255,0.15)]'
                  : isEnded
                    ? 'bg-[rgba(7,193,96,0.15)] text-[#07c160] shadow-[0_0_16px_rgba(7,193,96,0.15)]'
                    : 'bg-[rgba(140,140,141,0.1)] text-[#8c8c8d]'
            }`}
          >
            {isEnded ? (
              <DesktopOutlined />
            ) : isInProgress ? (
              <FieldTimeOutlined />
            ) : (
              <InboxOutlined />
            )}
          </div>
          <div className="flex flex-col gap-1">
            <span className="text-base font-semibold text-white">{getEpochLabel(epoch)}</span>
            <span className="text-[13px] text-white/45">
              {direction ? DIRECTION_LABELS[direction] : '全局'}
            </span>
          </div>
        </div>
        <span
          className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium whitespace-nowrap shrink-0 backdrop-blur-[8px] ${
            eliminated
              ? 'bg-[rgba(255,77,79,0.1)] text-[#ff4d4f] border border-[rgba(255,77,79,0.15)]'
              : isInProgress
                ? 'bg-[rgba(102,119,255,0.15)] text-[#6677ff] border border-[rgba(102,119,255,0.2)]'
                : isEnded
                  ? 'bg-[rgba(7,193,96,0.15)] text-[#07c160] border border-[rgba(7,193,96,0.2)]'
                  : 'bg-[rgba(140,140,141,0.1)] text-[#8c8c8d] border border-[rgba(140,140,141,0.15)]'
          }`}
        >
          {eliminated
            ? '已被淘汰'
            : actualStatus === 'ENDED'
              ? '已结束'
              : actualStatus === 'IN_PROGRESS'
                ? '进行中'
                : '未开始'}
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
              className={`h-full rounded-[3px] transition-[width] duration-300 ${
                isInProgress
                  ? 'bg-gradient-to-r from-[#6677ff] to-[#2f27b0] shadow-[0_0_8px_rgba(102,119,255,0.3)]'
                  : isEnded
                    ? 'bg-gradient-to-r from-[#07c160] to-[#05a34e] shadow-[0_0_8px_rgba(7,193,96,0.3)]'
                    : 'bg-[rgba(140,140,141,0.3)]'
              }`}
              style={{ width: `${progressPercent}%` }}
            />
          </div>
        </div>
      )}

      <div className="flex justify-end">
        <button
          className={`inline-flex items-center gap-[6px] px-5 py-2 rounded-lg text-[13px] font-medium border-none cursor-pointer transition-all backdrop-blur-[8px] ${
            eliminated
              ? 'bg-[rgba(140,140,141,0.1)] text-[#8c8c8d]/70 cursor-not-allowed'
              : isInProgress
                ? 'bg-gradient-to-br from-[#6677ff] to-[#2f27b0] text-white shadow-[0_4px_16px_rgba(102,119,255,0.3)] hover:shadow-[0_6px_24px_rgba(102,119,255,0.4)]'
                : isEnded
                  ? 'bg-gradient-to-br from-[#07c160] to-[#05a34e] text-white shadow-[0_4px_16px_rgba(7,193,96,0.3)] hover:shadow-[0_6px_24px_rgba(7,193,96,0.4)]'
                  : 'bg-[rgba(140,140,141,0.15)] text-[#8c8c8d] cursor-not-allowed border border-[rgba(140,140,141,0.15)]'
          }`}
          onClick={() => {
            if (isNotStarted || eliminated) return
            router.push(`/assessment/${assessment.id.toString()}/questions`)
          }}
        >
          {eliminated
            ? '已被淘汰'
            : isInProgress
              ? '继续答题'
              : isEnded
                ? '查看详情'
                : '暂不可进入'}
          {!isNotStarted && !eliminated && <RightOutlined className="text-xs" />}
        </button>
      </div>
    </div>
  )
}

export type { AssessmentCardProps }
