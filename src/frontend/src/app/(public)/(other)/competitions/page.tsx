'use client'

import { useRef } from 'react'
import { Spin, Empty, Pagination } from 'antd'
import { competitionService } from '@/apis/services/competition.service'
import { CompetitionResponseDTO } from '@/apis/schema/type'
import CompetitionCard from '@/components/CompetitionCard'
import BackgroundDecorations from './BackgroundDecorations'
import { usePagination } from '@/hooks'

const PAGE_SIZE = 10

const fetchCompetitionsPage = (page: number, pageSize: number) =>
  competitionService.getCompetitionsPage(page, pageSize)

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
      {competitions.map((competition) => (
        <CompetitionCard key={competition.id} competition={competition} />
      ))}
    </div>
  )
}

export default function CompetitionsPage() {
  const {
    data: competitions,
    total: totalElements,
    totalPages,
    loading,
    currentPage,
    setCurrentPage,
  } = usePagination(fetchCompetitionsPage, { pageSize: PAGE_SIZE })

  const headerRef = useRef<HTMLElement>(null)

  const handlePageChange = (page: number) => {
    setCurrentPage(page - 1)
    headerRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <div className="min-h-screen bg-black px-[147px] max-md:px-12 max-sm:px-6 py-20 max-md:py-[60px] max-sm:py-10 flex flex-col gap-12 max-sm:gap-8 relative overflow-hidden">
      <BackgroundDecorations />
      <header
        ref={headerRef}
        className="competitions-header flex flex-col gap-4 max-sm:gap-3 relative z-1"
      >
        <h1 className="text-5xl max-md:text-4xl max-sm:text-[28px] font-bold text-white m-0 font-['Inter']">
          团队参加的竞赛
        </h1>
        <p className="text-xl max-md:text-lg max-sm:text-sm font-normal text-white/60 m-0 font-['Inter']">
          记录我们在各类竞赛中的成长与突破
        </p>
      </header>

      {loading ? (
        <div className="flex justify-center items-center min-h-[400px]">
          <Spin size="large" />
        </div>
      ) : competitions.length === 0 ? (
        <EmptyState />
      ) : (
        <>
          <CompetitionsContent competitions={competitions} />
          {totalPages > 1 && (
            <div className="flex justify-center py-4 relative z-1">
              <Pagination
                current={currentPage + 1}
                total={totalElements}
                pageSize={PAGE_SIZE}
                onChange={handlePageChange}
                showSizeChanger={false}
                showTotal={(total: number) => `共 ${total} 项竞赛`}
              />
            </div>
          )}
        </>
      )}
    </div>
  )
}
