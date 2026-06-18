import { AI_CHAT_BASE_URL } from '@/apis/config'
import { AiStreamChunk, ChatRequest, ResetResponse } from '@/apis/schema/ai-chat.dto'

interface StreamChatOptions {
  message: string
  conversationId?: string
  signal?: AbortSignal
}

/**
 * 解析 SSE 行缓冲区，返回已完整接收的事件与剩余未闭合文本。
 * 处理 data: 行可能跨 chunk 边界的情况（尤其中文多字节）。
 */
export function parseSseBuffer(buffer: string): { events: AiStreamChunk[]; remainder: string } {
  const events: AiStreamChunk[] = []
  const parts = buffer.split('\n\n')
  const remainder = parts.pop() ?? ''

  for (const part of parts) {
    const dataLines = part
      .split('\n')
      .filter((line) => line.startsWith('data:'))
      .map((line) => line.slice(5).trim())

    if (dataLines.length === 0) continue

    // 多个 data: 行合并为同一事件体（规范允许）
    const payload = dataLines.join('\n')
    if (payload === '[DONE]') {
      events.push({ type: 'done' })
      continue
    }

    try {
      const parsed = JSON.parse(payload) as AiStreamChunk
      events.push(parsed)
    } catch {
      // 忽略无法解析的孤立片段，等待下一次补全
    }
  }

  return { events, remainder }
}

/**
 * 流式对话 SSE 请求。
 * 使用原生 fetch + ReadableStream，支持 AbortController 取消。
 */
export async function* streamChat(options: StreamChatOptions): AsyncGenerator<AiStreamChunk> {
  const { message, conversationId, signal } = options
  const body: ChatRequest = { message }
  if (conversationId) {
    body.conversation_id = conversationId
  }

  const response = await fetch(`${AI_CHAT_BASE_URL}/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
    signal,
  })

  if (!response.ok) {
    const text = await response.text().catch(() => '未知错误')
    throw new Error(`AI 服务请求失败: ${response.status} ${text}`)
  }

  const reader = response.body?.getReader()
  if (!reader) {
    throw new Error('响应体不可读')
  }

  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  try {
    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const { events, remainder } = parseSseBuffer(buffer)
      buffer = remainder

      for (const event of events) {
        yield event
      }
    }

    // 处理流结束后仍剩的尾部
    const { events } = parseSseBuffer(buffer + '\n\n')
    for (const event of events) {
      yield event
    }
  } finally {
    reader.releaseLock()
  }
}

/**
 * 重置指定会话。
 */
export async function resetConversation(conversationId?: string): Promise<ResetResponse> {
  const url = new URL(`${AI_CHAT_BASE_URL}/chat/reset`)
  if (conversationId) {
    url.searchParams.set('conversation_id', conversationId)
  }

  const response = await fetch(url.toString(), { method: 'POST' })
  if (!response.ok) {
    const text = await response.text().catch(() => '未知错误')
    throw new Error(`重置会话失败: ${response.status} ${text}`)
  }

  return (await response.json()) as ResetResponse
}
