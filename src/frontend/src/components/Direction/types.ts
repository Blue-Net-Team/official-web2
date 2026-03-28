/** 方向 slug 类型 */
export type DirectionSlug = 'cv' | 'embed' | 'struct'

/** 主题色配置 */
export interface ThemeColors {
  primary: string
  secondary: string
  gradientStart: string
  gradientEnd: string
}

/** 技术栈项 */
export interface TechItem {
  name: string
  description: string
}

/** 学习路径步骤 */
export interface LearningStep {
  step: number
  title: string
  videoLink?: string
}

/** 职业卡片 */
export interface CareerCard {
  title: string
  details: string[]
  image?: string
}

/** 招新信息 */
export interface RecruitmentInfo {
  requirements: string[]
}

/** 方向完整数据 */
export interface DirectionData {
  slug: DirectionSlug
  title: string
  subtitle: string
  theme: ThemeColors
  techStack: TechItem[]
  learningPath: LearningStep[]
  careers: CareerCard[]
  recruitment: RecruitmentInfo
}
