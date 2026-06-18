import { publicClient } from '../client'

import type {
  ResponseMessage,
  PageDTO,
  SoftwareResourceDTO,
  SoftwareResourceListRequestDTO,
} from '../schema/type'

export const softwareResourceService = {
  /**
   * 获取软件资源列表
   * GET /software-resources
   */
  async list(
    params: SoftwareResourceListRequestDTO
  ): Promise<ResponseMessage<PageDTO<SoftwareResourceDTO>>> {
    const response = await publicClient.get<ResponseMessage<PageDTO<SoftwareResourceDTO>>>(
      '/software-resources',
      {
        params,
      }
    )
    return response.data
  },
}
