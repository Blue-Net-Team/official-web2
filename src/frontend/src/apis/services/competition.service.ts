import { publicClient } from '../client'

import { CompetitionBriefDTO, ResponseMessage } from '../schema/type'

export const CompetitionService = {
  /**
   * 获取所有竞赛简介
   */
  getAllCompetitions: async () => {
    const response = await publicClient.get<ResponseMessage<CompetitionBriefDTO[]>>('/competitions')
    return response.data
  },
}
