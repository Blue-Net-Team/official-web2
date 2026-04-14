import { publicClient } from '../client'

import { CompetitionResponseDTO, ResponseMessage } from '../schema/type'

export const CompetitionService = {
  getAllCompetitions: async () => {
    const response =
      await publicClient.get<ResponseMessage<CompetitionResponseDTO[]>>('/competitions')
    return response.data
  },
}
