'use client'

import { ChatMessage } from '@/apis/schema/ai-chat.dto'
import MarkdownRenderer from '@/components/Assessment/MarkdownRenderer'
import ReasoningBlock from './ReasoningBlock'
import ToolCallCard from './ToolCallCard'

interface ChatBubbleProps {
  message: ChatMessage
}

export default function ChatBubble({ message }: ChatBubbleProps) {
  const isUser = message.role === 'user'

  return (
    <div className={`flex w-full ${isUser ? 'justify-end' : 'justify-start'}`}>
      <div
        className={`max-w-[85%] rounded-2xl px-4 py-3 ${
          isUser
            ? 'bg-[#fa8c16] text-black'
            : 'border border-white/[0.06] bg-white/[0.05] text-white/90'
        }`}
      >
        {!isUser && <ReasoningBlock reasoning={message.reasoning} done={message.reasoningDone} />}

        {!isUser && message.toolCalls?.map((tc) => <ToolCallCard key={tc.id} toolCall={tc} />)}

        {isUser ? (
          <div className="whitespace-pre-wrap text-sm">{message.content}</div>
        ) : (
          <div className="text-sm">
            {message.content.trim() ? (
              <MarkdownRenderer content={message.content} />
            ) : message.isStreaming ? (
              <span className="inline-block h-5 w-8 animate-pulse rounded bg-white/10" />
            ) : null}
          </div>
        )}

        {message.error && <div className="mt-2 text-xs text-red-400">{message.error}</div>}
      </div>
    </div>
  )
}
