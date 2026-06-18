'use client'

import { FloatButton } from 'antd'
import { MessageOutlined } from '@ant-design/icons'

interface AiChatFloatButtonProps {
  onClick: () => void
}

export default function AiChatFloatButton({ onClick }: AiChatFloatButtonProps) {
  return <FloatButton icon={<MessageOutlined />} tooltip="AI 客服" onClick={onClick} />
}
