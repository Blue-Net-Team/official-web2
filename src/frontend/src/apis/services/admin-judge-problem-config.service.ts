import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type {
  ConfirmJudgeLanguageLimitRequestDTO,
  JudgeProblemConfigDTO,
  ProgrammingLanguage,
  UpsertJudgeProblemConfigRequestDTO,
} from '@/apis/schema/assessment.dto'

/**
 * 管理端算法题判题配置 API。
 * 对应后端 /api/v1/admin/judge/questions/{questionId}/config 接口。
 */
export const adminJudgeProblemConfigService = {
  /**
   * 查询指定算法题当前判题配置。
   *
   * @param questionId 算法题目 ID。
   * @returns 当前判题配置响应。
   */
  async get(questionId: number): Promise<ResponseMessage<JudgeProblemConfigDTO>> {
    const response = await apiClient.get<ResponseMessage<JudgeProblemConfigDTO>>(
      `/admin/judge/questions/${questionId}/config`
    )
    return response.data
  },

  /**
   * 新增或替换指定算法题当前判题配置。
   *
   * @param questionId 算法题目 ID。
   * @param data 判题配置表单数据。
   * @returns 保存后的当前判题配置。
   */
  async upsert(
    questionId: number,
    data: UpsertJudgeProblemConfigRequestDTO
  ): Promise<ResponseMessage<JudgeProblemConfigDTO>> {
    const response = await apiClient.put<ResponseMessage<JudgeProblemConfigDTO>>(
      `/admin/judge/questions/${questionId}/config`,
      data
    )
    return response.data
  },

  /**
   * 请求 Judge Service 根据当前配置生成测试数据。
   *
   * @param questionId 算法题目 ID。
   * @returns 空响应。
   */
  async requestGeneration(questionId: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>(
      `/admin/judge/questions/${questionId}/config/generation-tasks`
    )
    return response.data
  },

  /**
   * 确认指定语言正式判题资源限制。
   *
   * @param questionId 算法题目 ID。
   * @param language 编程语言。
   * @param data 管理员确认的资源限制。
   * @returns 空响应。
   */
  async confirmLanguageLimit(
    questionId: number,
    language: ProgrammingLanguage,
    data: ConfirmJudgeLanguageLimitRequestDTO
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      `/admin/judge/questions/${questionId}/config/language-limits/${language}`,
      data
    )
    return response.data
  },
}
