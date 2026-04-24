import { publicClient } from '../client'

import { CompetitionResponseDTO, PageDTO, ResponseMessage } from '../schema/type'

export const competitionService = {
  getAllCompetitions: async (): Promise<ResponseMessage<CompetitionResponseDTO[]>> => {
    const response =
      await publicClient.get<ResponseMessage<CompetitionResponseDTO[]>>('/competitions')
    return response.data
  },
  getCompetitionsPage: async (
    page: number,
    size: number
  ): Promise<ResponseMessage<PageDTO<CompetitionResponseDTO>>> => {
    const response = await publicClient.get<ResponseMessage<PageDTO<CompetitionResponseDTO>>>(
      '/competitions/page',
      { params: { page, size } }
    )
    return response.data
  },
}
