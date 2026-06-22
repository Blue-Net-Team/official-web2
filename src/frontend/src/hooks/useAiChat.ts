'use client'

import { useCallback, useRef, useState } from 'react'
import { ChatMessage, ToolCallItem } from '@/apis/schema/ai-chat.dto'
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
        blocks: [],
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
                updated.blocks = appendOrUpdateReasoning(updated.blocks, chunk.content ?? '')
                break
              case 'content':
                updated.reasoningDone = true
                updated.content = (updated.content ?? '') + (chunk.content ?? '')
                updated.blocks = markLastReasoningDone(updated.blocks)
                updated.blocks = appendOrUpdateContent(updated.blocks, chunk.content ?? '')
                break
              case 'tool_call': {
                updated.reasoningDone = true
                const toolCalls = updated.toolCalls ? [...updated.toolCalls] : []
                const newToolCall: ToolCallItem = {
                  id: generateId(),
                  name: chunk.tool_name ?? 'unknown',
                  args: chunk.tool_args,
                }
                toolCalls.push(newToolCall)
                updated.toolCalls = toolCalls
                updated.blocks = markLastReasoningDone(updated.blocks)
                updated.blocks = [
                  ...(updated.blocks ?? []),
                  { type: 'tool_call', toolCall: newToolCall },
                ]
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
                updated.blocks = updateToolResult(
                  updated.blocks,
                  chunk.tool_name ?? 'unknown',
                  chunk.content ?? ''
                )
                break
              }
              case 'done':
                updated.reasoningDone = true
                updated.isStreaming = false
                updated.blocks = markAllReasoningDone(updated.blocks)
                break
              case 'error':
                updated.reasoningDone = true
                updated.error = chunk.content ?? '流式响应出错'
                updated.isStreaming = false
                updated.blocks = markAllReasoningDone(updated.blocks)
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

function appendOrUpdateReasoning(
  blocks: ChatMessage['blocks'],
  delta: string
): ChatMessage['blocks'] {
  if (!blocks || blocks.length === 0) {
    return [{ type: 'reasoning', content: delta, done: false }]
  }
  const last = blocks[blocks.length - 1]
  if (last.type === 'reasoning') {
    return [...blocks.slice(0, -1), { ...last, content: last.content + delta, done: false }]
  }
  return [...blocks, { type: 'reasoning', content: delta, done: false }]
}

function markLastReasoningDone(blocks: ChatMessage['blocks']): ChatMessage['blocks'] {
  if (!blocks || blocks.length === 0) return blocks
  const last = blocks[blocks.length - 1]
  if (last.type === 'reasoning') {
    return [...blocks.slice(0, -1), { ...last, done: true }]
  }
  return blocks
}

function markAllReasoningDone(blocks: ChatMessage['blocks']): ChatMessage['blocks'] {
  if (!blocks) return blocks
  return blocks.map((b) => (b.type === 'reasoning' ? { ...b, done: true } : b))
}

function appendOrUpdateContent(
  blocks: ChatMessage['blocks'],
  delta: string
): ChatMessage['blocks'] {
  if (!blocks || blocks.length === 0) {
    return [{ type: 'content', content: delta }]
  }
  const last = blocks[blocks.length - 1]
  if (last.type === 'content') {
    return [...blocks.slice(0, -1), { ...last, content: last.content + delta }]
  }
  return [...blocks, { type: 'content', content: delta }]
}

function updateToolResult(
  blocks: ChatMessage['blocks'],
  toolName: string,
  result: string
): ChatMessage['blocks'] {
  if (!blocks) return blocks
  for (let i = blocks.length - 1; i >= 0; i--) {
    const b = blocks[i]
    if (b.type === 'tool_call' && b.toolCall.name === toolName && b.toolCall.result === undefined) {
      const updated = { ...b, toolCall: { ...b.toolCall, result } }
      return [...blocks.slice(0, i), updated, ...blocks.slice(i + 1)]
    }
  }
  return blocks
}
