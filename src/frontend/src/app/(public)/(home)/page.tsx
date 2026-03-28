import bg1 from '@/assets/bg1.png'
import bg2 from '@/assets/bg2.png'
import styles from './styles.module.css'
import { Flex } from 'antd'
import TopContent from '@/components/Home/TopContent'
import { CompetitionService } from '@/apis/services/competition.service'
import AchievementAndResources from '@/components/Home/AchievementAndResources'
import FeaturedEquipment from '@/components/Home/FeaturedEquipment'
import TeamVibe from '@/components/Home/TeamVibe'
import { Suspense } from 'react'
import CompetitionsSkeleton from '@/components/Home/Competitions/skeleton'
import Competitions from '@/components/Home/Competitions'
import Wrapper from '@/components/Wrapper'
import DirectionIntroduce from '@/components/Home/DirectionIntroduce'
import RecruitmentProcess from '@/components/Home/RecruitmentProcess'

/**
 * 渲染竞赛表格异步组件，用于流式渲染
 * @returns 竞赛表格组件
 */
async function CompetitionsTable() {
  const competitionsBrief = await CompetitionService.getAllCompetitions()

  return (
    <Wrapper apiResponse={competitionsBrief}>
      <Competitions competitions={competitionsBrief.data || []} />
    </Wrapper>
  )
}

export default function Home() {
  return (
    <>
      <Flex
        vertical
        align="center"
        className={`${styles.container} ${styles.bg}`}
        style={
          {
            '--bg1': `url(${bg1.src})`,
            '--bg2': `url(${bg2.src})`,
          } as React.CSSProperties
        }
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
