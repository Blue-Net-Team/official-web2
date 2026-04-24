import { apiClient } from '../client'

import type {
  ResponseMessage,
  MessageTemplateInfoDTO,
  UpdateMessageTemplateRequestDTO,
} from '../schema/type'

export const adminMessageTemplateService = {
  /**
   * 获取消息模板列表
   * GET /admin/message-templates
   */
  async getList(): Promise<ResponseMessage<MessageTemplateInfoDTO[]>> {
    const response = await apiClient.get<ResponseMessage<MessageTemplateInfoDTO[]>>(
      '/admin/message-templates'
    )
    return response.data
  },

  /**
   * 获取消息模板详情
   * GET /admin/message-templates/{code}
   */
  async getDetail(code: string): Promise<ResponseMessage<MessageTemplateInfoDTO>> {
    const response = await apiClient.get<ResponseMessage<MessageTemplateInfoDTO>>(
      `/admin/message-templates/${code}`
    )
    return response.data
  },

  /**
   * 更新消息模板内容
   * PUT /admin/message-templates/{code}
   */
  async update(
    code: string,
    data: UpdateMessageTemplateRequestDTO
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      `/admin/message-templates/${code}`,
      data
    )
    return response.data
  },

  /**
   * 切换模板启禁用状态
   * POST /admin/message-templates/{code}/toggle
   */
  async toggle(code: string, enabled: boolean): Promise<ResponseMessage<boolean>> {
    const response = await apiClient.post<ResponseMessage<boolean>>(
      `/admin/message-templates/${code}/toggle`,
      null,
      { params: { enabled } }
    )
    return response.data
  },

  /**
   * 预览模板渲染效果
   * POST /admin/message-templates/{code}/preview
   */
  async preview(code: string, variables: Record<string, string>): Promise<ResponseMessage<string>> {
    const response = await apiClient.post<ResponseMessage<string>>(
      `/admin/message-templates/${code}/preview`,
      variables
    )
    return response.data
  },
}
