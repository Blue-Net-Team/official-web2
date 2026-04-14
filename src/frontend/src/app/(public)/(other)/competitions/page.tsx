import { Suspense } from 'react'
import { Spin, Empty } from 'antd'
import { CompetitionService } from '@/apis/services/competition.service'
import CompetitionCard from '@/components/CompetitionCard'
import { CompetitionResponseDTO } from '@/apis/schema/type'
import BackgroundDecorations from './BackgroundDecorations'

export const revalidate = 3600

function LoadingState() {
  return (
    <div className="flex justify-center items-center min-h-[400px]">
      <Spin size="large" />
    </div>
  )
}

function EmptyState() {
  return (
    <div className="flex justify-center items-center min-h-[400px]">
      <Empty description="暂无竞赛数据" />
    </div>
  )
}

function CompetitionsContent({ competitions }: { competitions: CompetitionResponseDTO[] }) {
  if (competitions.length === 0) {
    return <EmptyState />
  }

  return (
    <div className="flex flex-col gap-5 max-sm:gap-4 relative z-1">
      {competitions.map((competition, index) => (
        <CompetitionCard
          key={competition.id}
          competition={competition}
          showImage={index === 0}
          index={index}
        />
      ))}
    </div>
  )
}

async function CompetitionsList() {
  const response = await CompetitionService.getAllCompetitions()

  if (response.code !== 200 || !response.data || response.data.length === 0) {
    return <EmptyState />
  }

  const competitions: CompetitionResponseDTO[] = response.data

  return <CompetitionsContent competitions={competitions} />
}

export const metadata = {
  title: '团队参加的竞赛 - 蓝网团队',
  description: '记录我们在各类竞赛中的成长与突破',
}

export default function CompetitionsPage() {
  return (
    <div className="min-h-screen bg-black px-[147px] max-md:px-12 max-sm:px-6 py-20 max-md:py-[60px] max-sm:py-10 flex flex-col gap-12 max-sm:gap-8 relative overflow-hidden">
      <BackgroundDecorations />
      <header className="flex flex-col gap-4 max-sm:gap-3 relative z-1">
        <h1 className="text-5xl max-md:text-4xl max-sm:text-[28px] font-bold text-white m-0 font-['Inter']">
          团队参加的竞赛
        </h1>
        <p className="text-xl max-md:text-lg max-sm:text-sm font-normal text-white/60 m-0 font-['Inter']">
          记录我们在各类竞赛中的成长与突破
        </p>
      </header>

      <Suspense fallback={<LoadingState />}>
        <CompetitionsList />
      </Suspense>
    </div>
  )
}
