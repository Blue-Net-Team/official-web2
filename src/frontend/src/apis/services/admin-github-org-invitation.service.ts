import { apiClient } from '../client'
import type {
  ResponseMessage,
  GitHubOrgInviteDetailDTO,
  GitHubOrgBatchInviteResultDTO,
} from '../schema/type'

export const adminGitHubOrgInvitationService = {
  async inviteUser(userId: number): Promise<ResponseMessage<GitHubOrgInviteDetailDTO>> {
    const response = await apiClient.post<ResponseMessage<GitHubOrgInviteDetailDTO>>(
      `/admin/github-org-invitations/users/${userId}`
    )
    return response.data
  },

  async inviteBatch(userIds: number[]): Promise<ResponseMessage<GitHubOrgBatchInviteResultDTO>> {
    const response = await apiClient.post<ResponseMessage<GitHubOrgBatchInviteResultDTO>>(
      '/admin/github-org-invitations/batch',
      { userIds }
    )
    return response.data
  },
}
