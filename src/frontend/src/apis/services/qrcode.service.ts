import { publicClient } from '../client'
import { ResponseMessage } from '../schema/type'

/**
 * 咨询群二维码 DTO
 */
export interface ConsultationQrcodeDTO {
  /** 二维码ID */
  id: number
  /** 文件ID，用于下载二维码图片 */
  fileId: number
}

export const qrcodeService = {
  /**
   * 获取咨询群二维码列表 - 公开接口，无需认证
   * 对应后端 GET /api/v1/qrcodes/consultation
   */
  async getConsultationQrcodes(): Promise<ResponseMessage<ConsultationQrcodeDTO[]>> {
    const response =
      await publicClient.get<ResponseMessage<ConsultationQrcodeDTO[]>>('/qrcodes/consultation')
    return response.data
  },
}
