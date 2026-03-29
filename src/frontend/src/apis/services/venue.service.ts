import { publicClient } from '../client'

import { ResponseMessage, VenueDTO } from '../schema/type'

export const VenueService = {
  /**
   * 获取所有场地列表
   */
  getAllVenues: async () => {
    const response = await publicClient.get<ResponseMessage<VenueDTO[]>>('/venues')
    return response.data
  },
}
