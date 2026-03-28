/**
 * 个人主页Mock服务
 *
 * 模拟用户画像相关的API服务，与后端接口保持一致
 *
 * @author BlueNet Team
 */
import type {
  UserInfo,
  UserProfileWithStats,
  Assessment,
  Experience,
  TabCounts,
  CreateExperienceRequest,
  UpdateExperienceRequest,
  UpdateProfileRequest,
  ExperienceType,
} from '@/types/profile'
import {
  mockUserInfo,
  mockUserStats,
  mockTabCounts,
  mockAssessments,
  mockExperiences,
} from '@/mocks/data/profile'

// ==================== 模拟延迟工具 ====================

/**
 * 模拟网络延迟
 * @param ms - 延迟毫秒数
 */
const delay = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

// ==================== 可变数据存储 ====================

/** 可变的经历列表（支持CRUD操作） */
let experiences = [...mockExperiences]

/** 可变的用户资料（支持更新操作） */
let userInfo = { ...mockUserInfo }

// ==================== 服务对象 ====================

/**
 * Mock Profile Service
 * 提供用户画像相关的所有API操作
 */
const MockProfileService = {
  // ==================== 用户信息相关 ====================

  /**
   * 获取当前用户信息
   * 对应后端 GET /api/v1/user/info
   * @returns 用户基本信息
   */
  async getUserInfo(): Promise<UserInfo> {
    await delay(300)
    return userInfo
  },

  /**
   * 更新用户信息
   * 对应后端 PUT /api/v1/user/info
   * @param data - 更新请求
   * @returns void
   */
  async updateProfile(data: UpdateProfileRequest): Promise<void> {
    await delay(500)
    userInfo = { ...userInfo, ...data }
  },

  /**
   * 获取Tab计数
   * 对应后端 GET /api/v1/user/tab-counts
   * @returns Tab计数
   */
  async getTabCounts(): Promise<TabCounts> {
    await delay(200)
    return {
      projects: experiences.filter((e) => e.type === 'project').length,
      competitions: experiences.filter((e) => e.type === 'competition').length,
      internships: experiences.filter((e) => e.type === 'internship').length,
    }
  },

  /**
   * 获取用户完整画像（包含统计和Tab计数）
   * 前端组合接口
   * @returns 用户画像数据
   */
  async getProfile(): Promise<UserProfileWithStats> {
    await delay(300)
    return {
      ...userInfo,
      stats: mockUserStats,
      tabCounts: await this.getTabCounts(),
    }
  },

  // ==================== 考核相关 ====================

  /**
   * 获取考核列表
   * @returns 考核列表数据
   */
  async getAssessments(): Promise<Assessment[]> {
    await delay(400)
    return mockAssessments
  },

  // ==================== 经历相关（统一接口） ====================

  /**
   * 获取经历列表
   * 对应后端 GET /api/v1/user/experiences
   * @param type - 经历类型过滤（可选）
   * @returns 经历列表
   */
  async getExperiences(type?: ExperienceType): Promise<Experience[]> {
    await delay(300)
    if (type) {
      return experiences.filter((e) => e.type === type)
    }
    return experiences
  },

  /**
   * 创建经历
   * 对应后端 POST /api/v1/user/experiences
   * @param data - 创建请求
   * @returns 创建的经历
   */
  async createExperience(data: CreateExperienceRequest): Promise<Experience> {
    await delay(500)
    const newExperience: Experience = {
      ...data,
      id: `exp-${Date.now()}`,
      name: data.name || data.company || '',
      startDate: data.startDate || '',
      endDate: data.endDate || '',
    } as Experience
    experiences.push(newExperience)
    return newExperience
  },

  /**
   * 更新经历
   * 对应后端 PUT /api/v1/user/experiences/{id}
   * @param id - 经历ID
   * @param data - 更新请求
   * @returns 更新后的经历
   */
  async updateExperience(id: string, data: UpdateExperienceRequest): Promise<Experience> {
    await delay(500)
    const index = experiences.findIndex((e) => e.id === id)
    if (index === -1) {
      throw new Error('Experience not found')
    }
    experiences[index] = { ...experiences[index], ...data }
    return experiences[index]
  },

  /**
   * 删除经历
   * 对应后端 DELETE /api/v1/user/experiences/{id}
   * @param id - 经历ID
   */
  async deleteExperience(id: string): Promise<void> {
    await delay(300)
    experiences = experiences.filter((e) => e.id !== id)
  },

  // ==================== 兼容旧接口（过渡期使用） ====================

  /**
   * @deprecated 使用 getExperiences('project') 代替
   */
  async getProjects(): Promise<Experience[]> {
    return this.getExperiences('project')
  },

  /**
   * @deprecated 使用 createExperience({ type: 'project', ... }) 代替
   */
  async createProject(data: Omit<Experience, 'id' | 'type'>): Promise<Experience> {
    return this.createExperience({ ...data, type: 'project' })
  },

  /**
   * @deprecated 使用 updateExperience 代替
   */
  async updateProject(id: string, data: Partial<Experience>): Promise<Experience> {
    return this.updateExperience(id, data)
  },

  /**
   * @deprecated 使用 deleteExperience 代替
   */
  async deleteProject(id: string): Promise<void> {
    return this.deleteExperience(id)
  },

  /**
   * @deprecated 使用 getExperiences('competition') 代替
   */
  async getCompetitions(): Promise<Experience[]> {
    return this.getExperiences('competition')
  },

  /**
   * @deprecated 使用 createExperience({ type: 'competition', ... }) 代替
   */
  async createCompetition(data: Omit<Experience, 'id' | 'type'>): Promise<Experience> {
    return this.createExperience({ ...data, type: 'competition' })
  },

  /**
   * @deprecated 使用 updateExperience 代替
   */
  async updateCompetition(id: string, data: Partial<Experience>): Promise<Experience> {
    return this.updateExperience(id, data)
  },

  /**
   * @deprecated 使用 deleteExperience 代替
   */
  async deleteCompetition(id: string): Promise<void> {
    return this.deleteExperience(id)
  },

  /**
   * @deprecated 使用 getExperiences('internship') 代替
   */
  async getInternships(): Promise<Experience[]> {
    return this.getExperiences('internship')
  },

  /**
   * @deprecated 使用 createExperience({ type: 'internship', ... }) 代替
   */
  async createInternship(data: Omit<Experience, 'id' | 'type'>): Promise<Experience> {
    return this.createExperience({ ...data, type: 'internship' })
  },

  /**
   * @deprecated 使用 updateExperience 代替
   */
  async updateInternship(id: string, data: Partial<Experience>): Promise<Experience> {
    return this.updateExperience(id, data)
  },

  /**
   * @deprecated 使用 deleteExperience 代替
   */
  async deleteInternship(id: string): Promise<void> {
    return this.deleteExperience(id)
  },

  // ==================== 辅助方法 ====================

  /**
   * 计算Tab计数
   * @returns 各Tab的数据计数
   */
  calculateTabCounts(): TabCounts {
    return {
      projects: experiences.filter((e) => e.type === 'project').length,
      competitions: experiences.filter((e) => e.type === 'competition').length,
      internships: experiences.filter((e) => e.type === 'internship').length,
    }
  },
}

export default MockProfileService
