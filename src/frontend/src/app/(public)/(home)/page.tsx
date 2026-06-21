import { Flex } from 'antd'
import TopContent from '@/components/Home/TopContent'
import HomeBackground from './HomeBackground'
import { competitionService } from '@/apis/services/competition.service'
import { CompetitionResponseDTO } from '@/apis/schema/type'
import AchievementAndResources from '@/components/Home/AchievementAndResources'
import FeaturedEquipment from '@/components/Home/FeaturedEquipment'
import TeamVibe from '@/components/Home/TeamVibe'
import { Suspense } from 'react'
import CompetitionsSkeleton from '@/components/Home/Competitions/skeleton'
import Competitions from '@/components/Home/Competitions'
import DirectionIntroduce from '@/components/Home/DirectionIntroduce'
import RecruitmentProcess from '@/components/Home/RecruitmentProcess'

export const revalidate = 3600

async function CompetitionsTable() {
  let competitions: CompetitionResponseDTO[] = []
  try {
    const competitionsBrief = await competitionService.getAllCompetitions()
    competitions = competitionsBrief.data || []
  } catch (error) {
    // 构建时或运行时 API 不可用，使用空数组兜底；线上需查看容器日志定位 SSR 请求失败原因
    console.error('首页竞赛列表 SSR 获取失败:', error)
  }
  return <Competitions competitions={competitions} />
}

export default function Home() {
  return (
    <>
      <HomeBackground />
      <Flex
        vertical
        align="center"
        className="relative z-10 min-h-screen w-full max-w-full overflow-x-hidden"
      >
        <TopContent />
        <Suspense fallback={<CompetitionsSkeleton />}>
          <CompetitionsTable />
        </Suspense>
        <AchievementAndResources />
        <FeaturedEquipment />
        <TeamVibe />
        <DirectionIntroduce />
        <RecruitmentProcess />
      </Flex>
    </>
  )
}
