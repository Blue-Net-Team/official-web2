'use client'

import { ClockCircleOutlined, CalendarOutlined, DownOutlined } from '@ant-design/icons'
import CountdownTimer from '@/app/(public)/(other)/assessment/[timeId]/questions/[questionId]/CountdownTimer'
import { formatDate } from './utils'
import type { CountdownSectionProps } from './types'

export default function CountdownSection({
  isTimed,
  deadline,
  sessionStartTime,
  onTimeUp,
  timeInfo,
  statusInfo,
}: CountdownSectionProps) {
  if (isTimed && deadline) {
    return (
      <div className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-6 flex flex-col items-center gap-5">
        <div className="w-full flex items-center justify-center gap-2">
          <ClockCircleOutlined className="text-base text-[#fa8c16]" />
          <span className="text-[13px] font-medium text-white/65">剩余时间</span>
        </div>
        <CountdownTimer deadline={deadline} startedAt={sessionStartTime} onTimeUp={onTimeUp} />
      </div>
    )
  }

  if (!isTimed && timeInfo) {
    return (
      <div className="bg-white/[0.06] border border-white/[0.08] rounded-xl p-5 flex flex-col gap-4">
        <div className="flex items-center gap-2">
          <CalendarOutlined className="text-base text-[#6677ff]" />
          <span className="text-[13px] font-medium text-white/65">考核时间</span>
        </div>
        <div className="flex flex-col items-center gap-2.5">
          <span className="text-sm text-white/65 tabular-nums">
            {formatDate(timeInfo.startTime)}
          </span>
          <DownOutlined className="text-sm text-white/30" />
          <span className="text-sm text-white/65 tabular-nums">{formatDate(timeInfo.endTime)}</span>
          {statusInfo && (
            <span className="inline-flex items-center gap-1 px-3 py-1 rounded-md bg-[#07c160]/[0.1] text-[11px] text-[#07c160] border-none">
              {statusInfo.text === '进行中' ? '进行中 · 无限时' : statusInfo.text}
            </span>
          )}
        </div>
      </div>
    )
  }

  return null
}
