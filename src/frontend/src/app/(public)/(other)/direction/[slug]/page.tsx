import { notFound } from 'next/navigation'
import { getDirectionBySlug, getValidSlugs } from '@/components/Direction/data'
import { directionService } from '@/apis/services/direction.service'
import { LearningStep } from '@/components/Direction/types'
import HeroSection from '@/components/Direction/HeroSection'
import TechStack from '@/components/Direction/TechStack'
import LearningPath from '@/components/Direction/LearningPath'
import CareerSection from '@/components/Direction/CareerSection'
import RecruitmentInfo from '@/components/Direction/RecruitmentInfo'
import styles from './styles.module.css'

interface PageProps {
  params: Promise<{
    slug: string
  }>
}

// ISR 配置：每小时重新验证
export const revalidate = 3600

export async function generateStaticParams() {
  const slugs = getValidSlugs()
  return slugs.map((slug) => ({ slug }))
}

export async function generateMetadata({ params }: PageProps) {
  const { slug } = await params
  const direction = getDirectionBySlug(slug)

  if (!direction) {
    return { title: '页面未找到' }
  }

  return {
    title: `${direction.title} - 蓝网`,
    description: direction.subtitle,
  }
}

export default async function DirectionPage({ params }: PageProps) {
  const { slug } = await params
  const direction = getDirectionBySlug(slug)

  if (!direction) {
    notFound()
  }

  // 尝试从后端获取学习路径视频链接
  let learningPathWithVideos: LearningStep[] = direction.learningPath

  try {
    const response = await directionService.getLearningPath(slug)

    // API 调用成功且有数据时，合并静态数据与动态视频链接
    if (response.code === 200 && response.data?.steps) {
      learningPathWithVideos = direction.learningPath.map((step) => {
        const videoData = response.data!.steps.find((v) => v.stepNumber === step.step)
        return {
          ...step,
          videoLink: videoData?.videoLink || undefined,
        }
      })
    }
  } catch (error) {
    // API 失败时静默降级，使用静态数据
    console.error('Failed to fetch learning path data:', error)
  }

  const themeStyle = {
    '--theme-primary': direction.theme.primary,
    '--theme-secondary': direction.theme.secondary,
    '--theme-gradient-start': direction.theme.gradientStart,
    '--theme-gradient-end': direction.theme.gradientEnd,
  } as React.CSSProperties

  return (
    <div className={styles.container} style={themeStyle}>
      <HeroSection data={direction} />
      <TechStack data={direction.techStack} />
      <LearningPath data={learningPathWithVideos} />
      <CareerSection data={direction.careers} />
      <RecruitmentInfo data={direction.recruitment} directionSlug={direction.slug} />
    </div>
  )
}
