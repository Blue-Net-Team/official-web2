'use client'

import { useEffect } from 'react'
import { Spin } from 'antd'
import { CalendarOutlined } from '@ant-design/icons'
import { useRouter } from 'next/navigation'
import { useAuth, useApi } from '@/hooks'
import DarkVeil from '@/components/Reactbits/DarkVeil'
import { assessmentTimeService } from '@/apis/services/assessment-time.service'
import { AssessmentCard } from '@/components/Assessment'
import { DIRECTION_LABELS as DirectionLabels } from '@/apis/schema/enumerate'

export default function AssessmentPage() {
  const router = useRouter()
  const { userInfo, isAuthenticated, checkAuthStatus } = useAuth()

  const {
    data: pageData,
    loading,
    execute: fetchAssessmentTimes,
  } = useApi(() => assessmentTimeService.getAssessmentTimes(0, 50))

  const assessmentTimes = pageData?.content
    ? [...pageData.content].sort((a, b) => a.epoch - b.epoch)
    : []

  useEffect(() => {
    const checkAuth = async () => {
      const isAuth = await checkAuthStatus()
      if (!isAuth) {
        router.replace('/login')
      }
    }
    checkAuth()
  }, [checkAuthStatus, router])

  useEffect(() => {
    if (isAuthenticated) {
      fetchAssessmentTimes()
    }
  }, [isAuthenticated, fetchAssessmentTimes])

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-[#0a0a0a] p-10 max-sm:p-6 relative overflow-x-hidden">
        <div className="fixed inset-0 z-0">
          <DarkVeil hueShift={-130} speed={0.6} offsetY={0.2} />
        </div>
        <div className="flex justify-center items-center min-h-[300px] relative z-1">
          <Spin size="large" />
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-[#0a0a0a] p-10 max-sm:p-6 relative overflow-x-hidden">
      <div className="fixed inset-0 z-0">
        <DarkVeil hueShift={-130} speed={0.6} offsetY={0.2} />
      </div>
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
