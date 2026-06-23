export type AiStreamChunkType =
  | 'reasoning'
  | 'tool_call'
  | 'tool_result'
  | 'content'
  | 'done'
  | 'error'

export interface AiStreamChunk {
  type: AiStreamChunkType
  content?: string
  tool_name?: string
  tool_args?: Record<string, unknown>
}

export interface ToolCallItem {
  id: string
  name: string
  args?: Record<string, unknown>
  result?: string
}

export type ChatBlock =
  | { type: 'reasoning'; content: string; done?: boolean }
  | { type: 'tool_call'; toolCall: ToolCallItem }
  | { type: 'content'; content: string }

export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  reasoning?: string
  reasoningDone?: boolean
  toolCalls?: ToolCallItem[]
  blocks?: ChatBlock[]
  isStreaming?: boolean
  error?: string
}

export interface ChatRequest {
  message: string
  conversation_id?: string
}

export interface ResetResponse {
  success: boolean
  conversation_id?: string
}
