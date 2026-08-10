'use client'

import React, { Suspense } from 'react'
import { ConfigProvider, Spin } from 'antd'
import DarkVeil from '@/components/Reactbits/DarkVeil'
import DirectionSidebar from '@/components/Enroll/DirectionSidebar'
import EnrollForm from '@/components/Enroll/EnrollForm'
import ConsultationQrcode from '@/components/Enroll/ConsultationQrcode'
import EnrollFormDownloadCard from '@/components/Enroll/EnrollFormDownloadCard'
import { useEnrollForm } from '@/components/Enroll/hooks/useEnrollForm'
import { customTheme } from '@/components/Enroll/constants'

const EnrollPageContent: React.FC = () => {
  const {
    form,
    selectedDirection,
    handleDirectionSelect,
    avatarPreview,
    uploadingAvatar,
    uploadProgress,
    handleAvatarSelect,
    introLength,
    handleIntroChange,
    colleges,
    loadingColleges,
    submitting,
    handleSubmit,
    messageApi,
  } = useEnrollForm()

  return (
    <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      <ConfigProvider theme={customTheme}>
        <div className="fixed inset-0 z-0">
          <DarkVeil hueShift={-130} speed={0.6} offsetY={0.2} />
        </div>

        <main className="w-full min-h-screen flex justify-center items-start pt-[100px] max-lg:pt-[100px] px-10 max-lg:px-5 pb-[60px] gap-[30px] max-lg:flex-col max-lg:items-center relative z-1">
          <div className="flex flex-col gap-6 shrink-0 w-[220px] max-lg:hidden animate-[fadeInLeft_0.8s_cubic-bezier(0.4,0,0.2,1)_0.2s_both]">
            <DirectionSidebar selected={selectedDirection} onSelect={handleDirectionSelect} />
            <EnrollFormDownloadCard />
            <ConsultationQrcode />
          </div>

          <EnrollForm
            form={form}
            selectedDirection={selectedDirection}
            handleDirectionSelect={handleDirectionSelect}
            avatarPreview={avatarPreview}
            uploadingAvatar={uploadingAvatar}
            uploadProgress={uploadProgress}
            handleAvatarSelect={handleAvatarSelect}
            introLength={introLength}
            handleIntroChange={handleIntroChange}
            colleges={colleges}
            loadingColleges={loadingColleges}
            submitting={submitting}
            handleSubmit={handleSubmit}
            messageApi={messageApi}
          />

          <div className="hidden max-lg:flex flex-col gap-6 w-full max-w-[600px]">
            <EnrollFormDownloadCard />
            <ConsultationQrcode popoverPlacement="top" />
          </div>
        </main>
      </ConfigProvider>
    </div>
  )
}

export default function EnrollPage() {
  return (
    <Suspense
      fallback={
        <div className="flex justify-center items-center min-h-screen">
          <Spin size="large" />
        </div>
      }
    >
      <EnrollPageContent />
    </Suspense>
  )
}
