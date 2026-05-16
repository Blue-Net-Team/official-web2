import { apiClient } from '../client'
import { ResponseMessage } from '../schema/type'
import type {
  AssessmentTeamDTO,
  CreateAssessmentTeamRequestDTO,
  JoinAssessmentTeamRequestDTO,
  TransferLeaderRequestDTO,
  LeaveTeamRequestDTO,
} from '@/apis/schema/assessment.dto'

/**
 * 考核队伍服务 API
 * 对应后端 /api/v1/assessment-teams/* 接口
 */
export const assessmentTeamService = {
  /**
   * 创建队伍
   * POST /api/v1/assessment-teams
   */
  async createTeam(
    request: CreateAssessmentTeamRequestDTO
  ): Promise<ResponseMessage<AssessmentTeamDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentTeamDTO>>(
      '/assessment-teams',
      request
    )
    return response.data
  },

  /**
   * 预览队伍（通过邀请码）
   * POST /api/v1/assessment-teams/preview
   */
  async previewTeam(inviteCode: string): Promise<ResponseMessage<AssessmentTeamDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentTeamDTO>>(
      '/assessment-teams/preview',
      { inviteCode }
    )
    return response.data
  },

  /**
   * 加入队伍
   * POST /api/v1/assessment-teams/join
   */
  async joinTeam(
    request: JoinAssessmentTeamRequestDTO
  ): Promise<ResponseMessage<AssessmentTeamDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentTeamDTO>>(
      '/assessment-teams/join',
      request
    )
    return response.data
  },

  /**
   * 获取我的队伍
   * GET /api/v1/assessment-teams/my-team?assessmentTimeId={id}
   */
  async getMyTeam(assessmentTimeId: number): Promise<ResponseMessage<AssessmentTeamDTO | null>> {
    const response = await apiClient.get<ResponseMessage<AssessmentTeamDTO | null>>(
      '/assessment-teams/my-team',
      { params: { assessmentTimeId } }
    )
    return response.data
  },

  /**
   * 退出队伍
   * POST /api/v1/assessment-teams/leave
   */
  async leaveTeam(request: LeaveTeamRequestDTO): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>('/assessment-teams/leave', request)
    return response.data
  },

  /**
   * 转让队长
   * POST /api/v1/assessment-teams/transfer
   */
  async transferLeader(
    request: TransferLeaderRequestDTO
  ): Promise<ResponseMessage<AssessmentTeamDTO>> {
    const response = await apiClient.post<ResponseMessage<AssessmentTeamDTO>>(
      '/assessment-teams/transfer',
      request
    )
    return response.data
  },

  /**
   * 解散队伍
   * DELETE /api/v1/assessment-teams/{id}
   */
  async disbandTeam(teamId: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(`/assessment-teams/${teamId}`)
    return response.data
  },
}
