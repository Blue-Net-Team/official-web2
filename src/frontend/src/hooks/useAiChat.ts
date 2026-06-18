'use client'

import { useCallback, useRef, useState } from 'react'
import { ChatMessage } from '@/apis/schema/ai-chat.dto'
import { streamChat } from '@/apis/services/ai-chat.service'

function generateId(): string {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

interface UseAiChatState {
  messages: ChatMessage[]
  conversationId: string | undefined
  isStreaming: boolean
  error: string | null
}

interface UseAiChatReturn extends UseAiChatState {
  sendMessage: (text: string) => Promise<void>
  reset: () => void
  cancel: () => void
}

export function useAiChat(): UseAiChatReturn {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [conversationId, setConversationId] = useState<string | undefined>(undefined)
  const [isStreaming, setIsStreaming] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const abortControllerRef = useRef<AbortController | null>(null)

  const cancel = useCallback(() => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort()
      abortControllerRef.current = null
    }
    setIsStreaming(false)
  }, [])

  const reset = useCallback(() => {
    cancel()
    setMessages([])
    setConversationId(undefined)
    setError(null)
  }, [cancel])

  const sendMessage = useCallback(
    async (text: string) => {
      if (!text.trim() || isStreaming) return

      setError(null)
      const userMessage: ChatMessage = {
        id: generateId(),
        role: 'user',
        content: text.trim(),
      }

      const assistantMessage: ChatMessage = {
        id: generateId(),
        role: 'assistant',
        content: '',
        reasoning: '',
        toolCalls: [],
        isStreaming: true,
      }

      setMessages((prev) => [...prev, userMessage, assistantMessage])

      const currentConversationId = conversationId ?? generateId()
      if (!conversationId) {
        setConversationId(currentConversationId)
      }

      const abortController = new AbortController()
      abortControllerRef.current = abortController
      setIsStreaming(true)

      try {
        for await (const chunk of streamChat({
          message: text.trim(),
          conversationId: currentConversationId,
          signal: abortController.signal,
        })) {
          setMessages((prev) => {
            const last = prev[prev.length - 1]
            if (!last || last.role !== 'assistant') return prev

            const updated: ChatMessage = { ...last }

            switch (chunk.type) {
              case 'reasoning':
                updated.reasoning = (updated.reasoning ?? '') + (chunk.content ?? '')
                break
              case 'content':
                updated.reasoningDone = true
                updated.content = (updated.content ?? '') + (chunk.content ?? '')
                break
              case 'tool_call': {
                updated.reasoningDone = true
                const toolCalls = updated.toolCalls ? [...updated.toolCalls] : []
                toolCalls.push({
                  id: generateId(),
                  name: chunk.tool_name ?? 'unknown',
                  args: chunk.tool_args,
                })
                updated.toolCalls = toolCalls
                break
              }
              case 'tool_result': {
                updated.reasoningDone = true
                const toolCalls = updated.toolCalls ? [...updated.toolCalls] : []
                const targetIndex = toolCalls.findLastIndex(
                  (tc) => tc.name === chunk.tool_name && tc.result === undefined
                )
                if (targetIndex !== -1) {
                  toolCalls[targetIndex] = {
                    ...toolCalls[targetIndex],
                    result: chunk.content ?? '',
                  }
                }
                updated.toolCalls = toolCalls
                break
              }
              case 'done':
                updated.reasoningDone = true
                updated.isStreaming = false
                break
              case 'error':
                updated.reasoningDone = true
                updated.error = chunk.content ?? '流式响应出错'
                updated.isStreaming = false
                break
              default:
                break
            }

            return [...prev.slice(0, -1), updated]
          })
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : 'AI 服务请求失败'
        setError(message)
        setMessages((prev) => {
          const last = prev[prev.length - 1]
          if (last && last.role === 'assistant') {
            return [...prev.slice(0, -1), { ...last, error: message, isStreaming: false }]
          }
          return prev
        })
      } finally {
        setIsStreaming(false)
        abortControllerRef.current = null
      }
    },
    [conversationId, isStreaming, cancel]
  )

  return {
    messages,
    conversationId,
    isStreaming,
    error,
    sendMessage,
    reset,
    cancel,
  }
}
