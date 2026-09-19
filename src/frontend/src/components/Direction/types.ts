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

/** 学习路径步骤（与后端 LearningStepDTO 对齐）
 *  不含序号字段：展示编号由前端按数组位置派生 */
export interface LearningStep {
  id: number
  title: string
  relatedLink?: string | null
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
  careers: CareerCard[]
  recruitment: RecruitmentInfo
}
