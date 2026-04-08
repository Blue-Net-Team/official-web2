'use client'

import { useState, useEffect, useRef, useMemo } from 'react'

interface CountdownTimerProps {
  /** 考核截止时间（ISO格式） */
  deadline: string
  /** 开始时间（可选，用于计算进度环比例） */
  startedAt?: string
  /** 倒计时归零回调 */
  onTimeUp?: () => void
}

/** 获取剩余秒数 */
function getRemainingSeconds(deadline: string): number {
  const now = Date.now()
  const end = new Date(deadline).getTime()
  return Math.max(0, Math.floor((end - now) / 1000))
}

/** 格式化秒数为 MM:SS */
function formatTime(totalSeconds: number): string {
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  const pad = (n: number) => String(n).padStart(2, '0')
  if (hours > 0) return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
  return `${pad(minutes)}:${pad(seconds)}`
}

/** 格式化总时长为 MM:SS */
function formatTotal(totalSeconds: number): string {
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60
  const pad = (n: number) => String(n).padStart(2, '0')
  if (hours > 0) return `/ ${pad(hours)}:${pad(minutes)}:${pad(seconds)}`
  return `/ ${pad(minutes)}:${pad(seconds)}`
}

/** 根据剩余比例获取颜色 */
function getColor(ratio: number): string {
  if (ratio > 0.5) return '#07c160'
  if (ratio > 0.25) return '#fa8c16'
  return '#ff4d4f'
}

export default function CountdownTimer({ deadline, startedAt, onTimeUp }: CountdownTimerProps) {
  const [remainingSeconds, setRemainingSeconds] = useState(() => getRemainingSeconds(deadline))
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null)
  const hasFiredRef = useRef(false)

  const totalSeconds = useMemo(() => {
    if (startedAt) {
      const start = new Date(startedAt).getTime()
      const end = new Date(deadline).getTime()
      return Math.max(1, Math.floor((end - start) / 1000))
    }
    return getRemainingSeconds(deadline) || 1
  }, [deadline, startedAt])

  useEffect(() => {
    // 如果已经到期，立即触发回调
    const initial = getRemainingSeconds(deadline)
    if (initial === 0 && !hasFiredRef.current) {
      hasFiredRef.current = true
      onTimeUp?.()
    }

    timerRef.current = setInterval(() => {
      const current = getRemainingSeconds(deadline)
      setRemainingSeconds(current)
      if (current <= 0) {
        if (timerRef.current) clearInterval(timerRef.current)
        if (!hasFiredRef.current) {
          hasFiredRef.current = true
          onTimeUp?.()
        }
      }
    }, 1000)

    return () => {
      if (timerRef.current) clearInterval(timerRef.current)
    }
  }, [deadline, onTimeUp])

  const ratio = totalSeconds > 0 ? remainingSeconds / totalSeconds : 0
  const color = getColor(ratio)

  // SVG 圆环参数 - 匹配设计稿 140x140, 6px 描边
  const size = 140
  const strokeWidth = 6
  const radius = (size - strokeWidth) / 2
  const circumference = 2 * Math.PI * radius
  const strokeDashoffset = circumference * (1 - Math.min(1, Math.max(0, ratio)))

  return (
    <div style={{ position: 'relative', width: size, height: size }}>
      <svg
        width={size}
        height={size}
        viewBox={`0 0 ${size} ${size}`}
        style={{ transform: 'rotate(-90deg)' }}
      >
        {/* 背景圆环 */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke="rgba(255, 255, 255, 0.06)"
          strokeWidth={strokeWidth}
        />
        {/* 进度圆环 */}
        <circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          fill="none"
          stroke={color}
          strokeWidth={strokeWidth}
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          style={{ transition: 'stroke-dashoffset 1s linear, stroke 0.5s ease' }}
        />
      </svg>
      {/* 中心文字 */}
      <div
        style={{
          position: 'absolute',
          top: '50%',
          left: '50%',
          transform: 'translate(-50%, -50%)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 2,
        }}
      >
        <span
          style={{
            fontSize: 28,
            fontWeight: 700,
            fontFamily: 'Inter, monospace',
            color: '#ffffff',
            letterSpacing: 1,
            lineHeight: 1,
            transition: 'color 0.5s ease',
          }}
        >
          {formatTime(remainingSeconds)}
        </span>
        <span
          style={{
            fontSize: 12,
            color: 'rgba(255, 255, 255, 0.3)',
            fontFamily: 'Inter, monospace',
          }}
        >
          {formatTotal(totalSeconds)}
        </span>
      </div>
    </div>
  )
}
