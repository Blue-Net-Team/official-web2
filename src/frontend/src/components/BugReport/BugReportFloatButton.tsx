'use client'

import { useState } from 'react'
import { FloatButton } from 'antd'
import { BugOutlined } from '@ant-design/icons'
import BugReportModal from './BugReportModal'

export default function BugReportFloatButton() {
  const [modalOpen, setModalOpen] = useState(false)

  return (
    <>
      <FloatButton icon={<BugOutlined />} tooltip="反馈问题" onClick={() => setModalOpen(true)} />
      <BugReportModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </>
  )
}
