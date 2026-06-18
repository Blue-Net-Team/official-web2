'use client'

import { useState } from 'react'
import { FloatButton } from 'antd'
import { BugReportFloatButton } from '@/components/BugReport'
import AiChatFloatButton from './AiChat/AiChatFloatButton'
import AiChatDrawer from './AiChat/AiChatDrawer'

export default function GlobalFloatActions() {
  const [chatOpen, setChatOpen] = useState(false)

  return (
    <>
      <FloatButton.Group shape="circle" style={{ right: 24, bottom: 24 }}>
        <AiChatFloatButton onClick={() => setChatOpen(true)} />
        <BugReportFloatButton />
      </FloatButton.Group>
      <AiChatDrawer open={chatOpen} onClose={() => setChatOpen(false)} />
    </>
  )
}
