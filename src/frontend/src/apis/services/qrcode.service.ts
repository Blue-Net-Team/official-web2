import { apiClient, publicClient } from '../client'
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

/**
 * 更新咨询群二维码请求 DTO
 */
export interface UpdateConsultationQrcodeRequestDTO {
  /** 文件ID */
  fileId: number
}

/**
 * 考核群二维码 DTO
 */
export interface AssessmentQrcodeDTO {
  /** 二维码ID */
  id: number
  /** 文件ID，用于下载二维码图片 */
  fileId: number
  /** 方向 */
  direction?: string
  /** 考核轮次 */
  epoch?: number
  /** 是否三方向共用 */
  isShared?: boolean
}

/**
 * 创建考核群二维码请求 DTO
 */
export interface CreateAssessmentQrcodeRequestDTO {
  /** 文件ID */
  fileId: number
  /** 方向 */
  direction?: string
  /** 考核轮次 */
  epoch: number
  /** 是否三方向共用 */
  isShared?: boolean
}

/**
 * 更新考核群二维码请求 DTO
 */
export interface UpdateAssessmentQrcodeRequestDTO {
  /** 文件ID */
  fileId?: number
  /** 方向 */
  direction?: string
  /** 考核轮次 */
  epoch?: number
  /** 是否三方向共用 */
  isShared?: boolean
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

  /**
   * 获取咨询群二维码列表 - 管理端接口
   * 对应后端 GET /api/v1/admin/qrcodes/consultation
   */
  async getConsultationQrcodesAdmin(): Promise<ResponseMessage<ConsultationQrcodeDTO[]>> {
    const response = await apiClient.get<ResponseMessage<ConsultationQrcodeDTO[]>>(
      '/admin/qrcodes/consultation'
    )
    return response.data
  },

  /**
   * 创建咨询群二维码 - 管理端接口
   * 对应后端 POST /api/v1/admin/qrcodes/consultation
   */
  async createConsultationQrcode(fileId: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>(
      `/admin/qrcodes/consultation?fileId=${fileId}`
    )
    return response.data
  },

  /**
   * 更新咨询群二维码 - 管理端接口
   * 对应后端 PUT /api/v1/admin/qrcodes/consultation/{id}
   */
  async updateConsultationQrcode(
    id: number,
    data: UpdateConsultationQrcodeRequestDTO
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      `/admin/qrcodes/consultation/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除咨询群二维码 - 管理端接口
   * 对应后端 DELETE /api/v1/admin/qrcodes/consultation/{id}
   */
  async deleteConsultationQrcode(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(
      `/admin/qrcodes/consultation/${id}`
    )
    return response.data
  },

  /**
   * 获取考核群二维码列表 - 管理端接口
   * 对应后端 GET /api/v1/admin/qrcodes/assessment
   */
  async getAssessmentQrcodes(
    direction?: string,
    epoch?: number
  ): Promise<ResponseMessage<AssessmentQrcodeDTO[]>> {
    const params = new URLSearchParams()
    if (direction) params.append('direction', direction)
    if (epoch) params.append('epoch', epoch.toString())

    const response = await apiClient.get<ResponseMessage<AssessmentQrcodeDTO[]>>(
      `/admin/qrcodes/assessment${params.toString() ? '?' + params.toString() : ''}`
    )
    return response.data
  },

  /**
   * 创建考核群二维码 - 管理端接口
   * 对应后端 POST /api/v1/admin/qrcodes/assessment
   */
  async createAssessmentQrcode(
    data: CreateAssessmentQrcodeRequestDTO
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.post<ResponseMessage<void>>('/admin/qrcodes/assessment', data)
    return response.data
  },

  /**
   * 更新考核群二维码 - 管理端接口
   * 对应后端 PUT /api/v1/admin/qrcodes/assessment/{id}
   */
  async updateAssessmentQrcode(
    id: number,
    data: UpdateAssessmentQrcodeRequestDTO
  ): Promise<ResponseMessage<void>> {
    const response = await apiClient.put<ResponseMessage<void>>(
      `/admin/qrcodes/assessment/${id}`,
      data
    )
    return response.data
  },

  /**
   * 删除考核群二维码 - 管理端接口
   * 对应后端 DELETE /api/v1/admin/qrcodes/assessment/{id}
   */
  async deleteAssessmentQrcode(id: number): Promise<ResponseMessage<void>> {
    const response = await apiClient.delete<ResponseMessage<void>>(
      `/admin/qrcodes/assessment/${id}`
    )
    return response.data
  },
}
