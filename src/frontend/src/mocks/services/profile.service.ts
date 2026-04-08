/**
 * 个人主页 Mock 服务 - 考核部分
 *
 * 仅保留考核相关的 Mock 服务（后端考核 API 尚未实现）
 *
 * @author BlueNet Team
 */
import type { Assessment } from '@/types/profile'
import { mockAssessments } from '@/mocks/data/profile'

// ==================== 模拟延迟工具 ====================

/**
 * 模拟网络延迟
 * @param ms - 延迟毫秒数
 */
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

// ==================== 服务对象 ====================

/**
 * Mock Profile Service
 * 提供考核相关的 API 操作
 */
const MockProfileService = {
  // ==================== 考核相关 ====================

  /**
   * 获取考核列表
   * @returns 考核列表数据
   */
  async getAssessments(): Promise<Assessment[]> {
    await delay(400)
    return mockAssessments
  },
}

export default MockProfileService
