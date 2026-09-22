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
  tagIds: number[]
  source: string
  vectorStatus: 'synced' | 'embedding'
}

export interface KnowledgeTagDTO {
  id: number
  tagName: string
  tagDescription: string
  chunksCount: number
  vectorStatus: 'synced' | 'embedding'
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
   * 更新标签（重命名/描述）
   * PUT /api/v1/admin/knowledge/tags/{id}
   */
  async updateTag(
    id: number,
    payload: { tagName?: string; description?: string }
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      `/admin/knowledge/tags/${id}`,
      payload
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

  /**
   * 新建标签
   * POST /api/v1/admin/knowledge/tags
   */
  async createTag(payload: {
    tagName: string
    description?: string
  }): Promise<ResponseMessage<number>> {
    const response = await apiClient.post<ResponseMessage<number>>('/admin/knowledge/tags', payload)
    return response.data
  },

  /**
   * 删除标签（自动解除全部分片关联），返回解除关联的分片数
   * DELETE /api/v1/admin/knowledge/tags/{id}
   */
  async deleteTag(id: number): Promise<ResponseMessage<number>> {
    const response = await apiClient.delete<ResponseMessage<number>>(`/admin/knowledge/tags/${id}`)
    return response.data
  },

  /**
   * 编辑分片（内容与标签，保存后异步重新向量化）
   * PUT /api/v1/admin/knowledge/chunks/{id}
   */
  async updateChunk(
    id: number,
    payload: { content: string; tagIds: number[] }
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      `/admin/knowledge/chunks/${id}`,
      payload
    )
    return response.data
  },

  /**
   * 删除分片（同时解除标签关联，文档分段数减一）
   * DELETE /api/v1/admin/knowledge/chunks/{id}
   */
  async deleteChunk(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/knowledge/chunks/${id}`)
    return response.data
  },

  /**
   * 重新上传文档附件（触发完整重新解析，文档ID不变）
   * POST /api/v1/admin/knowledge/docs/{id}/file
   */
  async replaceDocFile(id: number, file: File): Promise<ResponseMessage<void>> {
    const formData = new FormData()
    formData.append('file', file)
    const response = await apiClient.post<ResponseMessage<void>>(
      `/admin/knowledge/docs/${id}/file`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    )
    return response.data
  },
}
