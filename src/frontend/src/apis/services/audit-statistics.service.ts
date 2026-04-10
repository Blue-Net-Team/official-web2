import { apiClient } from '../client'
import {
  ResponseMessage,
  TrendPointDTO,
  EndpointRankingDTO,
  EndpointLatencyDTO,
} from '../schema/type'

export const auditStatisticsService = {
  async getTrends(period: string = '7d'): Promise<ResponseMessage<TrendPointDTO[]>> {
    const response = await apiClient.get<ResponseMessage<TrendPointDTO[]>>(
      '/admin/audit/statistics/trends',
      { params: { period } }
    )
    return response.data
  },

  async getEndpointRanking(
    period: string = '7d',
    limit: number = 20
  ): Promise<ResponseMessage<EndpointRankingDTO[]>> {
    const response = await apiClient.get<ResponseMessage<EndpointRankingDTO[]>>(
      '/admin/audit/statistics/endpoints',
      { params: { period, limit } }
    )
    return response.data
  },

  async getEndpointLatencyRanking(
    period: string = '7d',
    limit: number = 20
  ): Promise<ResponseMessage<EndpointLatencyDTO[]>> {
    const response = await apiClient.get<ResponseMessage<EndpointLatencyDTO[]>>(
      '/admin/audit/statistics/latency',
      { params: { period, limit } }
    )
    return response.data
  },
}
