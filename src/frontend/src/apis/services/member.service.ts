import { publicClient } from '../client'
import {
  AchievementDTO,
  PageDTO,
  MemberBriefDTO,
  MemberDetailDTO,
  ResponseMessage,
  TabCounts,
  UserExperience,
} from '../schema/type'
import { Direction, ExperienceType } from '../schema/enumerate'

export interface MemberListParams {
  page?: number
  size?: number
  direction?: Direction
}

export type { ExperienceType } from '../schema/enumerate'

export const memberService = {
  /**
   * 获取团队成员列表
   * 公开接口，无需认证
   */
  getMemberList: async (
    params: MemberListParams = {}
  ): Promise<ResponseMessage<PageDTO<MemberBriefDTO>>> => {
    const response = await publicClient.get<ResponseMessage<PageDTO<MemberBriefDTO>>>('/members', {
      params: {
        page: params.page,
        size: params.size,
        direction: params.direction,
      },
    })
    return response.data
  },

  /**
   * 获取成员详细信息
   * 公开接口，无需认证
   * @param id 成员ID
   */
  getMemberById: async (id: number): Promise<ResponseMessage<MemberDetailDTO>> => {
    const response = await publicClient.get<ResponseMessage<MemberDetailDTO>>(`/members/${id}`)
    return response.data
  },

  /**
   * 获取成员经历列表
   * 公开接口，无需认证
   * @param memberId 成员ID
   * @param type 经历类型（可选）：project/internship
   */
  getMemberExperiences: async (
    memberId: number,
    type?: ExperienceType
  ): Promise<ResponseMessage<UserExperience[]>> => {
    const response = await publicClient.get<ResponseMessage<UserExperience[]>>(
      `/members/${memberId}/experiences`,
      {
        params: type ? { type } : undefined,
      }
    )
    return response.data
  },

  /**
   * 获取成员关联的官方成就列表
   * 公开接口，无需认证
   * @param memberId 成员ID
   */
  getMemberAchievements: async (memberId: number): Promise<ResponseMessage<AchievementDTO[]>> => {
    const response = await publicClient.get<ResponseMessage<AchievementDTO[]>>(
      `/members/${memberId}/achievements`
    )
    return response.data
  },

  /**
   * 获取成员经历统计（项目/个人成就/实习数量）
   * 公开接口，无需认证
   * 通过获取所有经历与成就后计算各类型数量
   * @param memberId 成员ID
   */
  getMemberTabCounts: async (memberId: number): Promise<TabCounts> => {
    const [experiencesResponse, achievementsResponse] = await Promise.all([
      memberService.getMemberExperiences(memberId),
      memberService.getMemberAchievements(memberId),
    ])
    const experiences = experiencesResponse.data || []
    const achievements = achievementsResponse.data || []
    return {
      projects: experiences.filter((e) => e.type === 'PROJECT').length,
      achievements: achievements.length,
      internships: experiences.filter((e) => e.type === 'INTERNSHIP').length,
    }
  },

  /**
   * 获取成员项目经历
   * 公开接口，无需认证
   * @param memberId 成员ID
   */
  getMemberProjects: async (memberId: number): Promise<ResponseMessage<UserExperience[]>> => {
    return memberService.getMemberExperiences(memberId, 'PROJECT')
  },

  /**
   * 获取成员实习经历
   * 公开接口，无需认证
   * @param memberId 成员ID
   */
  getMemberInternships: async (memberId: number): Promise<ResponseMessage<UserExperience[]>> => {
    return memberService.getMemberExperiences(memberId, 'INTERNSHIP')
  },
}
