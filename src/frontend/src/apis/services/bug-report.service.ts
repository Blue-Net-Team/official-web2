import { publicClient, apiClient } from '../client'
import type { ResponseMessage, PageDTO } from '../schema/type'
import type {
  CreateBugReportRequestDTO,
  BugReportCreatedDTO,
  BugReportListItemDTO,
  BugReportDetailDTO,
  BugReportListQueryDTO,
} from '../schema/bug-report.dto'

export const bugReportService = {
  /**
   * 公开提交 Bug 报告
   * 对应后端 POST /api/v1/bug-reports
   */
  async create(data: CreateBugReportRequestDTO): Promise<ResponseMessage<BugReportCreatedDTO>> {
    const response = await publicClient.post<ResponseMessage<BugReportCreatedDTO>>(
      '/bug-reports',
      data
    )
    return response.data
  },
}

export const adminBugReportService = {
  /**
   * 管理端分页查询 Bug 报告列表
   * 对应后端 GET /api/v1/admin/bug-reports
   */
  async getList(
    params: BugReportListQueryDTO = {}
  ): Promise<ResponseMessage<PageDTO<BugReportListItemDTO>>> {
    const response = await apiClient.get<ResponseMessage<PageDTO<BugReportListItemDTO>>>(
      '/admin/bug-reports',
      { params }
    )
    return response.data
  },

  /**
   * 管理端查询 Bug 报告详情
   * 对应后端 GET /api/v1/admin/bug-reports/{id}
   */
  async getDetail(id: number): Promise<ResponseMessage<BugReportDetailDTO>> {
    const response = await apiClient.get<ResponseMessage<BugReportDetailDTO>>(
      `/admin/bug-reports/${id}`
    )
    return response.data
  },
}
