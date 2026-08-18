import { ResponseMessage } from './type'

/**
 * 学习路径步骤
 * 对应后端 LearningStepDTO.java
 */
export interface LearningStepDTO {
  /** 步骤ID */
  id: number
  /** 步骤序号 */
  stepNumber: number
  /** 步骤标题 */
  title: string
  /** 相关链接URL，可能为 null */
  relatedLink: string | null
}

/**
 * 创建/更新学习步骤请求
 * 对应后端 CreateLearningStepRequestDTO / UpdateLearningStepRequestDTO
 */
export interface LearningStepRequestDTO {
  /** 步骤序号 */
  stepNumber: number
  /** 步骤标题 */
  title: string
  /** 相关链接URL，可选 */
  relatedLink?: string | null
}

/**
 * 方向学习路径数据
 * 对应后端 DirectionLearningPathDTO.java
 */
export interface DirectionLearningPathDTO {
  /** 方向标识（slug） */
  direction: string
  /** 方向名称 */
  directionName: string
  /** 学习路径步骤列表 */
  steps: LearningStepDTO[]
}

/**
 * 方向学习路径 API 响应类型
 */
export type DirectionLearningPathResponse = ResponseMessage<DirectionLearningPathDTO>
