'use client'

import { Alert, Button, Flex } from 'antd'
import { CloseOutlined, DeleteOutlined, MessageOutlined } from '@ant-design/icons'
import { ChatMessage } from '@/apis/schema/ai-chat.dto'
import MessageList from './MessageList'
import ChatInput from './ChatInput'

interface AiChatPanelProps {
  messages: ChatMessage[]
  isStreaming: boolean
  error: string | null
  onSend: (text: string) => void
  onReset: () => void
  onClose: () => void
}

export default function AiChatPanel({
  messages,
  isStreaming,
  error,
  onSend,
  onReset,
  onClose,
}: AiChatPanelProps) {
  return (
    <div className="flex h-full flex-col">
      <div className="flex items-center justify-between border-b border-white/[0.06] px-4 py-3">
        <Flex align="center" gap={8}>
          <MessageOutlined className="text-lg text-[#fa8c16]" />
          <span className="text-base font-medium text-white/90">AI 客服</span>
        </Flex>
        <Flex gap={8}>
          <Button
            type="text"
            size="small"
            icon={<DeleteOutlined />}
            onClick={onReset}
            disabled={isStreaming}
          >
            清空
          </Button>
          <Button type="text" size="small" icon={<CloseOutlined />} onClick={onClose} />
        </Flex>
      </div>

      {error && <Alert message={error} type="error" showIcon className="mx-4 mt-3" banner />}

      <div className="min-h-0 flex-1">
        <MessageList messages={messages} />
      </div>

      <ChatInput loading={isStreaming} onSend={onSend} />
    </div>
  )
}
