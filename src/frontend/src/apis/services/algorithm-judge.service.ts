import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type {
  AlgorithmRunRequestDTO,
  AlgorithmSubmitResponseDTO,
  CreateAnswerRequestDTO,
  JudgeJobPollingResponseDTO,
} from '@/apis/schema/assessment.dto'

/**
 * 算法判题服务 API
 * 对应后端 /api/v1/algorithm-judge/* 接口
 */
export const algorithmJudgeService = {
  async run(request: AlgorithmRunRequestDTO): Promise<ResponseMessage<AlgorithmSubmitResponseDTO>> {
    const response = await apiClient.post<ResponseMessage<AlgorithmSubmitResponseDTO>>(
      '/algorithm-judge/run',
      request
    )
    return response.data
  },

  async submit(
    request: CreateAnswerRequestDTO
  ): Promise<ResponseMessage<AlgorithmSubmitResponseDTO>> {
    const response = await apiClient.post<ResponseMessage<AlgorithmSubmitResponseDTO>>(
      '/algorithm-judge/submit',
      request
    )
    return response.data
  },

  async getJob(jobId: number): Promise<ResponseMessage<JudgeJobPollingResponseDTO>> {
    const response = await apiClient.get<ResponseMessage<JudgeJobPollingResponseDTO>>(
      `/algorithm-judge/jobs/${jobId}`
    )
    return response.data
  },
}
