import { publicClient } from '../client'

import { CompetitionResponseDTO, PageDTO, ResponseMessage } from '../schema/type'

export const CompetitionService = {
  getAllCompetitions: async () => {
    const response =
      await publicClient.get<ResponseMessage<CompetitionResponseDTO[]>>('/competitions')
    return response.data
  },
  getCompetitionsPage: async (page: number, size: number) => {
    const response = await publicClient.get<ResponseMessage<PageDTO<CompetitionResponseDTO>>>(
      '/competitions/page',
      { params: { page, size } }
    )
    return response.data
  },
}
