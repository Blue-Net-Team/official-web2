'use client'

import { useState, useEffect, useCallback } from 'react'
import { Spin } from 'antd'
import { CalendarOutlined } from '@ant-design/icons'
import { useRouter } from 'next/navigation'
import authStore from '@/stores/authStore'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'
import { AssessmentCard } from '@/components/Assessment'
import type { AssessmentTimeDTO } from '@/apis/schema/assessment.dto'
import { DIRECTION_LABELS as DirectionLabels } from '@/apis/schema/enumerate'

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
            {assessmentTimes.map((item) => (
              <AssessmentCard key={item.id} assessment={item} />
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
