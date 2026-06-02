import { apiClient } from '../client'
import type { ResponseMessage, PageDTO } from '../schema/type'

export type DocParseStatus =
  | 'PENDING'
  | 'PARSING'
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELING'
  | 'CANCELED'

export interface KnowledgeDocDTO {
  id: number
  fileId: number
  title: string
  status: DocParseStatus
  chunkCount: number
  errorMessage: string
  createdAt: string
  updatedAt: string
}

export interface KnowledgeChunkDTO {
  id: number
  docId: number
  content: string
  tags: string[]
  source: string
}

export interface KnowledgeTagDTO {
  id: number
  tagName: string
  tagDescription: string
  chunksCount: number
}

export const knowledgeService = {
  /**
   * 上传知识库文档
   * POST /api/v1/admin/knowledge/docs
   */
  async uploadDocument(file: File, title?: string): Promise<ResponseMessage<KnowledgeDocDTO>> {
    const formData = new FormData()
    formData.append('file', file)
    if (title) {
      formData.append('title', title)
    }
    const response = await apiClient.post<ResponseMessage<KnowledgeDocDTO>>(
      '/admin/knowledge/docs',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    )
    return response.data
  },

  /**
   * 查询文档列表
   * GET /api/v1/admin/knowledge/docs
   */
  async listDocuments(page = 0, size = 20): Promise<ResponseMessage<PageDTO<KnowledgeDocDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<KnowledgeDocDTO>>>(
      '/admin/knowledge/docs',
      {
        params: { page, size },
      }
    )
    return response.data
  },

  /**
   * 查询文档详情
   * GET /api/v1/admin/knowledge/docs/{id}
   */
  async getDocumentDetail(id: number): Promise<ResponseMessage<KnowledgeDocDTO>> {
    const response = await apiClient.get<ResponseMessage<KnowledgeDocDTO>>(
      `/admin/knowledge/docs/${id}`
    )
    return response.data
  },

  /**
   * 重新解析文档
   * POST /api/v1/admin/knowledge/docs/{id}/reparse
   */
  async reparseDocument(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>(
      `/admin/knowledge/docs/${id}/reparse`
    )
    return response.data
  },

  /**
   * 取消解析文档
   * POST /api/v1/admin/knowledge/docs/{id}/cancel
   */
  async cancelParse(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>(
      `/admin/knowledge/docs/${id}/cancel`
    )
    return response.data
  },

  /**
   * 删除文档
   * DELETE /api/v1/admin/knowledge/docs/{id}
   */
  async deleteDocument(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/knowledge/docs/${id}`)
    return response.data
  },

  /**
   * 查询文档分段
   * GET /api/v1/admin/knowledge/docs/{id}/chunks
   */
  async listChunks(
    id: number,
    page = 0,
    size = 20
  ): Promise<ResponseMessage<PageDTO<KnowledgeChunkDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<KnowledgeChunkDTO>>>(
      `/admin/knowledge/docs/${id}/chunks`,
      {
        params: { page, size },
      }
    )
    return response.data
  },

  /**
   * 查询标签列表
   * GET /api/v1/admin/knowledge/tags
   */
  async listTags(page = 0, size = 20): Promise<ResponseMessage<PageDTO<KnowledgeTagDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<KnowledgeTagDTO>>>(
      '/admin/knowledge/tags',
      {
        params: { page, size },
      }
    )
    return response.data
  },

  /**
   * 更新标签描述
   * PUT /api/v1/admin/knowledge/tags/{id}
   */
  async updateTagDescription(id: number, description: string): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(`/admin/knowledge/tags/${id}`, {
      description,
    })
    return response.data
  },
}
