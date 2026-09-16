import { apiClient } from '../client'
import { PageDTO, ResponseMessage } from '../schema/type'
import type {
  AiConversationDetailDTO,
  AiConversationSummaryDTO,
  AiTracePeriod,
  AiTraceStatisticsDTO,
} from '@/apis/schema/ai-trace.dto'

/** 会话列表查询参数 */
export interface ListConversationsParams {
  /** 页码，从 0 开始 */
  page?: number
  size?: number
  /** 意图筛选，为空表示不限 */
  intent?: string
  /** 动作筛选：RETRIEVE / REFUSE / DIRECT */
  action?: string
  /** 用户提问关键词 */
  keyword?: string
  /** 会话最后活跃时间下界（ISO-8601） */
  startTime?: string
  /** 会话最后活跃时间上界（ISO-8601） */
  endTime?: string
}

/**
 * AI 对话轨迹后台 API（只读）
 * 对应后端 /api/v1/admin/ai-traces/* 接口
 */
export const aiTraceService = {
  /**
   * 分页查询会话列表
   * GET /api/v1/admin/ai-traces/conversations
   */
  async listConversations(
    params: ListConversationsParams
  ): Promise<ResponseMessage<PageDTO<AiConversationSummaryDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<AiConversationSummaryDTO>>>(
      '/admin/ai-traces/conversations',
      { params }
    )
    return response.data
  },

  /**
   * 查询会话详情（含完整事件流与 prompt 快照）
   * GET /api/v1/admin/ai-traces/conversations/{id}
   *
   * 会话不存在时后端返回成功状态码与 null 数据，不抛错。
   */
  async getConversationDetail(
    id: string
  ): Promise<ResponseMessage<AiConversationDetailDTO | null>> {
    const response = await apiClient.get<ResponseMessage<AiConversationDetailDTO | null>>(
      `/admin/ai-traces/conversations/${encodeURIComponent(id)}`
    )
    return response.data
  },

  /**
   * 查询统计汇总
   * GET /api/v1/admin/ai-traces/statistics
   */
  async getStatistics(period: AiTracePeriod): Promise<ResponseMessage<AiTraceStatisticsDTO>> {
    const response = await apiClient.get<ResponseMessage<AiTraceStatisticsDTO>>(
      '/admin/ai-traces/statistics',
      { params: { period } }
    )
    return response.data
  },
}
