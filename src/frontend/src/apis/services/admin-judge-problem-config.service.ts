import { apiClient } from '../client'
import { AI_CHAT_BASE_URL } from '../config'
import { ResponseMessage } from '../schema/type'
import type {
  ConfirmJudgeLanguageLimitRequestDTO,
  GenerateJudgeConfigDraftRequestDTO,
  JudgeConfigDraftDTO,
  JudgeProblemConfigDTO,
  ProgrammingLanguage,
  UpsertJudgeProblemConfigRequestDTO,
} from '@/apis/schema/assessment.dto'

/**
 * 管理端算法题判题配置 API。
 * 对应后端 /api/v1/admin/judge/questions/{questionId}/config 接口。
 */
export const adminJudgeProblemConfigService = {
  /** 与 AI 客服一致直连 AI Service，基于当前未保存题面生成临时草稿。 */
  async generateDraft(data: GenerateJudgeConfigDraftRequestDTO): Promise<JudgeConfigDraftDTO> {
    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), 150000)
    try {
      const response = await fetch(`${AI_CHAT_BASE_URL}/judge-config-drafts`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
        signal: controller.signal,
      })
      if (!response.ok) {
        const error = await response.json().catch(() => null)
        throw new Error(error?.detail || `AI 服务请求失败: ${response.status}`)
      }
      return (await response.json()) as JudgeConfigDraftDTO
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') {
        throw new Error('AI 生成超时，请稍后重试')
      }
      throw error
    } finally {
      clearTimeout(timeout)
    }
  },

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
