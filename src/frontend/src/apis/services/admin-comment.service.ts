import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type { CommentDTO, CommentRequestDTO } from '@/apis/schema/assessment.dto'

/**
 * 管理端评论 API
 * 对应后端 /api/v1/admin/comments/* 接口
 */
export const adminCommentService = {
  /** 添加评论 */
  async addComment(request: CommentRequestDTO): Promise<ResponseMessage<CommentDTO>> {
    const response = await apiClient.post<ResponseMessage<CommentDTO>>('/admin/comments', request)
    return response.data
  },

  /** 查询评论列表 */
  async listComments(answerId: number): Promise<ResponseMessage<CommentDTO[]>> {
    const response = await apiClient.get<ResponseMessage<CommentDTO[]>>('/admin/comments', {
      params: { answerId },
    })
    return response.data
  },

  /** 更新评论 */
  async updateComment(
    commentId: number,
    request: CommentRequestDTO
  ): Promise<ResponseMessage<CommentDTO>> {
    const response = await apiClient.put<ResponseMessage<CommentDTO>>(
      `/admin/comments/${commentId}`,
      request
    )
    return response.data
  },

  /** 删除评论 */
  async deleteComment(commentId: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/admin/comments/${commentId}`)
    return response.data
  },
}
