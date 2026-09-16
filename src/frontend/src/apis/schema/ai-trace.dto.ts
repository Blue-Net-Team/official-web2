/**
 * AI 对话轨迹后台 DTO
 * 对应后端 /api/v1/admin/ai-traces/* 接口
 */

/** 处理动作 */
export type AiTraceAction = 'RETRIEVE' | 'REFUSE' | 'DIRECT'

/** 统计周期 */
export type AiTracePeriod = '24h' | '7d' | '30d'

/** 提问摘要，用于会话列表内嵌展示 */
export interface AiTurnSummaryDTO {
  /** 会话内序号，从 1 开始 */
  seq: number
  /** 用户原始提问文本 */
  userInput: string
  /** 意图标签，可能为空 */
  intent: string | null
  /** 处理动作 */
  action: AiTraceAction | null
  /** 该提问内的工具调用轮次 */
  toolRounds: number | null
  /** 耗时（毫秒） */
  durationMs: number | null
  /** 是否为不完整记录（客户端断流或采集异常） */
  degraded: boolean | null
  createdAt: string
}

/** 会话列表行 */
export interface AiConversationSummaryDTO {
  id: string
  createdAt: string
  lastActiveAt: string
  /** 消息数 = 该会话下的用户提问数（不含助手回复） */
  messageCount: number
  /** 内嵌的提问摘要，数量有上限 */
  turns: AiTurnSummaryDTO[]
  /** 因超出内联上限而未展示的提问数量 */
  hiddenTurnCount: number
}

/** 提问详情，含原始事件流与 prompt 快照 */
export interface AiTurnDetailDTO extends AiTurnSummaryDTO {
  /** 最终答案，被拦截或断流时可能为空 */
  answer: string | null
  /** 原始事件流的 JSON 字符串，需 JSON.parse */
  events: string | null
  /** 完整 prompt 快照的 JSON 字符串，未执行状态图时为 null */
  prompt: string | null
}

/** 会话详情 */
export interface AiConversationDetailDTO {
  id: string
  createdAt: string
  lastActiveAt: string
  messageCount: number
  turns: AiTurnDetailDTO[]
}

/** 概览指标 */
export interface AiTraceOverviewDTO {
  /** 会话总数（趋势口径） */
  conversationCount: number
  /** 用户提问数，即「消息数」 */
  questionCount: number
  /** 被拒答的提问数 */
  refusedCount: number
  /** 触发兜底语义检索的提问数 */
  fallbackCount: number
  /** 兜底检索率，0~1 */
  fallbackRate: number | null
}

/** 趋势数据点 */
export interface AiTrendPointDTO {
  time: string
  count: number
}

/** 分布条目 */
export interface AiCountItemDTO {
  label: string
  count: number
  /** 占比，0~1；后端未计算时为 null */
  ratio: number | null
}

/** 统计汇总 */
export interface AiTraceStatisticsDTO {
  overview: AiTraceOverviewDTO
  /** 会话量趋势（按会话聚合） */
  trend: AiTrendPointDTO[]
  /** 意图分布（按提问聚合） */
  intents: AiCountItemDTO[]
  /** 动作分布（按提问聚合） */
  actions: AiCountItemDTO[]
  /** 检索工具使用次数 */
  tools: AiCountItemDTO[]
  /** 拒答原因分布 */
  refusals: AiCountItemDTO[]
}

/**
 * 轨迹事件。
 * 对应 ai-service 的 trace/events.py，是 events JSON 数组的元素形状。
 */
export interface AiTraceEvent {
  type: 'intent' | 'pre_disclose' | 'reasoning' | 'tool_call' | 'tool_result' | 'content'
  /** reasoning / tool_result / content 的文本 */
  content?: string
  // intent 事件
  intent?: string
  confidence?: number
  action?: string
  // pre_disclose 事件
  tags?: string[]
  hits?: AiTraceTagHit[]
  // tool_call / tool_result 事件
  tool_name?: string
  tool_args?: Record<string, unknown>
  /** 提问内的工具调用轮次序号 */
  round?: number
  /** 该工具结果是否被轮次上限拦截而非真实执行所得 */
  blocked?: boolean
}

/** 预披露阶段命中的标签 */
export interface AiTraceTagHit {
  tag_name: string
  score: number
  chunks_count: number
}
