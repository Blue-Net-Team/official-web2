import { Suspense } from 'react'
import { Spin, Empty } from 'antd'
import { CompetitionService } from '@/apis/services/competition.service'
import CompetitionCard from '@/components/CompetitionCard'
import { CompetitionBriefDTO } from '@/apis/schema/type'
import BackgroundDecorations from './BackgroundDecorations'
import styles from './page.module.css'

function LoadingState() {
  return (
    <div className={styles.loadingContainer}>
      <Spin size="large" />
    </div>
  )
}

function EmptyState() {
  return (
    <div className={styles.emptyContainer}>
      <Empty description="暂无竞赛数据" />
    </div>
  )
}

function CompetitionsContent({ competitions }: { competitions: CompetitionBriefDTO[] }) {
  if (competitions.length === 0) {
    return <EmptyState />
  }

  return (
    <div className={styles.competitionsList}>
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

  const competitions: CompetitionBriefDTO[] = response.data

  return <CompetitionsContent competitions={competitions} />
}

export const metadata = {
  title: '团队参加的竞赛 - 蓝网团队',
  description: '记录我们在各类竞赛中的成长与突破',
}

export default function CompetitionsPage() {
  return (
    <div className={styles.pageContainer}>
      <BackgroundDecorations />
      <header className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>团队参加的竞赛</h1>
        <p className={styles.pageSubtitle}>记录我们在各类竞赛中的成长与突破</p>
      </header>

      <Suspense fallback={<LoadingState />}>
        <CompetitionsList />
      </Suspense>
    </div>
  )
}
