'use client'

import { useEffect, useRef } from 'react'
import { ChatMessage } from '@/apis/schema/ai-chat.dto'
import ChatBubble from './ChatBubble'
import { Empty } from 'antd'

interface MessageListProps {
  messages: ChatMessage[]
}

export default function MessageList({ messages }: MessageListProps) {
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  if (messages.length === 0) {
    return (
      <div className="flex h-full flex-col items-center justify-center text-white/40">
        <Empty description="有什么可以帮你的？" image={Empty.PRESENTED_IMAGE_SIMPLE} />
      </div>
    )
  }

  return (
    <div className="flex h-full flex-col gap-4 overflow-y-auto px-4 py-4">
      {messages.map((msg) => (
        <ChatBubble key={msg.id} message={msg} />
      ))}
      <div ref={bottomRef} />
    </div>
  )
}
