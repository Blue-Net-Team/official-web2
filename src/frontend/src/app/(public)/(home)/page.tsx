import bg1 from '@/assets/bg1.png'
import bg2 from '@/assets/bg2.png'
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

export const revalidate = 3600

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
        className="relative min-h-screen w-full max-w-full overflow-x-hidden bg-no-repeat bg-blend-screen bg-[position:right_-15%,right_30%] bg-[size:100%_auto,100%_auto] max-lg:bg-[position:right_-5%,right_35%] max-md:bg-[position:right_-3%,right_30%] max-md:bg-[size:120%_auto,150%_auto]"
        style={
          {
            backgroundImage: `url(${bg1.src}), url(${bg2.src})`,
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
