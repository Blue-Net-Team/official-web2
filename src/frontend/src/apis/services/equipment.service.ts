import { publicClient } from '../client'

import { ResponseMessage, EquipmentDTO } from '../schema/type'

export const EquipmentService = {
  /**
   * 获取所有设备列表
   */
  getAllEquipments: async () => {
    const response = await publicClient.get<ResponseMessage<EquipmentDTO[]>>('/equipments')
    return response.data
  },
}
