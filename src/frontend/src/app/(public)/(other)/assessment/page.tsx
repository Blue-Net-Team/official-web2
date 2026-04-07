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
import type { AssessmentTimeDTO, AssessmentStatus, Direction } from '@/types/assessment'
import { DirectionLabels } from '@/types/assessment'

function getAssessmentStatus(startTime: string, endTime: string): AssessmentStatus {
  const now = new Date().getTime()
  const start = new Date(startTime).getTime()
  const end = new Date(endTime).getTime()
  if (now < start) return 'not-started'
  if (now > end) return 'ended'
  return 'in-progress'
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function getStatusText(status: AssessmentStatus): string {
  const map: Record<AssessmentStatus, string> = {
    'not-started': '未开始',
    'in-progress': '进行中',
    ended: '已结束',
  }
  return map[status]
}

function getEpochLabel(epoch: number): string {
  const chineseNumbers = ['一', '二', '三', '四', '五', '六', '七', '八', '九', '十']
  return `第${chineseNumbers[epoch - 1] || epoch}轮考核`
}

export default function AssessmentPage() {
  const router = useRouter()
  const [assessmentTimes, setAssessmentTimes] = useState<AssessmentTimeDTO[]>([])
  const [loading, setLoading] = useState(true)
  const { userInfo, isAuthenticated, checkAuthStatus } = authStore()

  useEffect(() => {
    const checkAuth = async () => {
      const isAuth = await checkAuthStatus()
      if (!isAuth) {
        router.replace('/login')
      }
    }
    checkAuth()
  }, [checkAuthStatus, router])

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

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-[#0a0a0a] p-10 max-sm:p-6 relative overflow-x-hidden">
        <div className="fixed top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_80%_50%_at_20%_40%,rgba(102,119,255,0.15)_0%,transparent_50%),radial-gradient(ellipse_60%_40%_at_80%_60%,rgba(255,107,53,0.1)_0%,transparent_50%),radial-gradient(ellipse_50%_30%_at_50%_100%,rgba(47,39,176,0.2)_0%,transparent_50%)] z-0 pointer-events-none" />
        <div className="flex justify-center items-center min-h-[300px]">
          <Spin size="large" />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-[#0a0a0a] p-10 max-sm:p-6 relative overflow-x-hidden">
      <div className="fixed top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_80%_50%_at_20%_40%,rgba(102,119,255,0.15)_0%,transparent_50%),radial-gradient(ellipse_60%_40%_at_80%_60%,rgba(255,107,53,0.1)_0%,transparent_50%),radial-gradient(ellipse_50%_30%_at_50%_100%,rgba(47,39,176,0.2)_0%,transparent_50%)] z-0 pointer-events-none" />
      <div className="max-w-[960px] mx-auto relative z-1">
        <div className="mb-10">
          <h1 className="text-[28px] max-sm:text-[22px] font-bold text-white m-0 mb-2 bg-gradient-to-br from-white to-white/70 bg-clip-text text-transparent">
            考核时间安排
          </h1>
          <p className="text-sm text-white/45 m-0 mb-5">查看当前可参加的考核时间安排</p>
          {userInfo && (
            <div className="flex gap-2">
              <span className="inline-flex items-center px-3 py-1 rounded-md text-[13px] text-white/65 bg-white/[0.06] border border-white/[0.08] backdrop-blur-xl">
                {DirectionLabels[userInfo.direction as keyof typeof DirectionLabels] ||
                  userInfo.direction}
              </span>
              <span className="inline-flex items-center px-3 py-1 rounded-md text-[13px] text-white/65 bg-white/[0.06] border border-white/[0.08] backdrop-blur-xl">
                {userInfo.grade}
              </span>
            </div>
          )}
        </div>

        {loading ? (
          <div className="flex justify-center items-center min-h-[300px]">
            <Spin size="large" />
          </div>
        ) : assessmentTimes.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 px-6 text-center">
            <div className="w-16 h-16 rounded-2xl bg-white/[0.04] flex items-center justify-center text-[28px] text-white/25 mb-4">
              <CalendarOutlined />
            </div>
            <h3 className="text-base font-medium text-white/45 m-0 mb-2">暂无考核安排</h3>
            <p className="text-sm text-white/25 m-0">当前没有可参加的考核时间</p>
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {assessmentTimes.map((item) => {
              const status = getAssessmentStatus(item.startTime, item.endTime)
              const total = item.totalQuestions ?? 0
              const completed = item.completedQuestions ?? 0
              const progressPercent = total > 0 ? Math.round((completed / total) * 100) : 0

              const isInProgress = status === 'in-progress'
              const isEnded = status === 'ended'
              const isNotStarted = status === 'not-started'

              return (
                <div
                  key={item.id}
                  className={`relative bg-white/[0.04] border rounded-2xl p-6 max-sm:p-[18px] backdrop-blur-[24px] transition-all overflow-hidden hover:-translate-y-0.5 hover:bg-white/[0.06] ${
                    isInProgress
                      ? 'border-[rgba(102,119,255,0.2)] shadow-[0_0_20px_rgba(102,119,255,0.06),inset_0_1px_0_rgba(102,119,255,0.1)] hover:shadow-[0_8px_32px_rgba(102,119,255,0.12),inset_0_1px_0_rgba(102,119,255,0.15)] hover:border-[rgba(102,119,255,0.3)]'
                      : isEnded
                        ? 'border-[rgba(7,193,96,0.2)] shadow-[0_0_20px_rgba(7,193,96,0.06),inset_0_1px_0_rgba(7,193,96,0.1)] hover:shadow-[0_8px_32px_rgba(7,193,96,0.12),inset_0_1px_0_rgba(7,193,96,0.15)] hover:border-[rgba(7,193,96,0.3)]'
                        : 'border-white/[0.06] hover:shadow-[0_4px_16px_rgba(255,255,255,0.04)]'
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
                          isInProgress
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
                        <span className="text-base font-semibold text-white">
                          {getEpochLabel(item.epoch)}
                        </span>
                        <span className="text-[13px] text-white/45">
                          {DirectionLabels[item.direction as Direction] || item.direction}
                        </span>
                      </div>
                    </div>
                    <span
                      className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium whitespace-nowrap shrink-0 backdrop-blur-[8px] ${
                        isInProgress
                          ? 'bg-[rgba(102,119,255,0.15)] text-[#6677ff] border border-[rgba(102,119,255,0.2)]'
                          : isEnded
                            ? 'bg-[rgba(7,193,96,0.15)] text-[#07c160] border border-[rgba(7,193,96,0.2)]'
                            : 'bg-[rgba(140,140,141,0.1)] text-[#8c8c8d] border border-[rgba(140,140,141,0.15)]'
                      }`}
                    >
                      {getStatusText(status)}
                    </span>
                  </div>

                  <div className="flex flex-wrap gap-4 mb-4">
                    <div className="flex items-center gap-[6px] text-[13px] text-white/50">
                      <CalendarOutlined className="text-sm" />
                      <span>
                        {formatDate(item.startTime)} — {formatDate(item.endTime)}
                      </span>
                    </div>
                    {item.timeLimit && item.timeLimitMinutes ? (
                      <div className="flex items-center gap-[6px] text-[13px] text-[#fa8c16] font-medium">
                        <ClockCircleOutlined className="text-sm" />
                        <span>限时 {item.timeLimitMinutes} 分钟</span>
                      </div>
                    ) : (
                      <div className="flex items-center gap-[6px] text-[13px] text-white/50">
                        <ClockCircleOutlined className="text-sm" />
                        <span>不限时</span>
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
                        isInProgress
                          ? 'bg-gradient-to-br from-[#6677ff] to-[#2f27b0] text-white shadow-[0_4px_16px_rgba(102,119,255,0.3)] hover:shadow-[0_6px_24px_rgba(102,119,255,0.4)]'
                          : isEnded
                            ? 'bg-gradient-to-br from-[#07c160] to-[#05a34e] text-white shadow-[0_4px_16px_rgba(7,193,96,0.3)] hover:shadow-[0_6px_24px_rgba(7,193,96,0.4)]'
                            : 'bg-[rgba(140,140,141,0.15)] text-[#8c8c8d] cursor-not-allowed border border-[rgba(140,140,141,0.15)]'
                      }`}
                    >
                      {isInProgress ? '继续答题' : isEnded ? '查看详情' : '暂不可进入'}
                      {status !== 'not-started' && <RightOutlined className="text-xs" />}
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
