'use client'

import { useState, useEffect, useCallback, useRef } from 'react'
import { Masonry, Spin, Empty, Pagination } from 'antd'
import DarkVeil from '@/components/Reactbits/DarkVeil'
import AchievementCard from '@/components/Achievements/AchievementCard'
import AchievementStats from '@/components/Achievements/AchievementStats'
import AchievementFilter from '@/components/Achievements/AchievementFilter'
import { AchievementService } from '@/apis/services/achievement.service'
import { AchievementDTO, AchievementStatsDTO } from '@/apis/schema/type'
import { usePagination } from '@/hooks'

const PAGE_SIZE = 12
const YEARS = [2024, 2023, 2022, 2021, 2020]

export default function AchievementsPage() {
  const [stats, setStats] = useState<AchievementStatsDTO>({
    totalAchievements: 0,
    nationalCount: 0,
    provincialCount: 0,
    schoolCount: 0,
  })

  // 筛选条件
  const [type, setType] = useState<string | undefined>()
  const [awardLevel, setAwardLevel] = useState<string | undefined>()
  const [year, setYear] = useState<number | undefined>()

  const apiFn = useCallback(
    (page: number, pageSize: number) =>
      AchievementService.getAchievements({
        type,
        awardLevel,
        year,
        page,
        size: pageSize,
      }),
    [type, awardLevel, year]
  )

  const {
    data: achievements,
    total: totalElements,
    totalPages,
    loading,
    currentPage,
    setCurrentPage,
    reset,
  } = usePagination(apiFn, { pageSize: PAGE_SIZE })

  // 获取统计数据
  useEffect(() => {
    const fetchStats = async () => {
      try {
        const statsRes = await AchievementService.getAchievementStats()
        if (statsRes.code === 200 && statsRes.data) {
          setStats(statsRes.data)
        }
      } catch (error) {
        console.error('Failed to fetch achievement stats:', error)
      }
    }
    fetchStats()
  }, [])

  // 处理筛选条件变化
  const handleFilterChange = (newType?: string, newAwardLevel?: string, newYear?: number) => {
    setType(newType)
    setAwardLevel(newAwardLevel)
    setYear(newYear)
    reset()
  }

  const sectionRef = useRef<HTMLElement>(null)

  // 处理页码变化
  const handlePageChange = (page: number) => {
    setCurrentPage(page - 1)
    sectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      <div className="fixed inset-0 z-0">
        <DarkVeil hueShift={40} speed={0.6} offsetY={0.2} />
      </div>
      <main className="flex flex-col items-center w-full min-h-screen py-8 px-[147px] max-lg:px-10 max-md:px-4 box-border relative z-1">
        <section className="text-center mb-8 w-full pt-8">
          <h1 className="text-5xl max-md:text-4xl max-sm:text-[28px] font-bold text-white mb-4 bg-gradient-to-br from-white to-white/80 bg-clip-text text-transparent">
            团队成就
          </h1>
          <p className="text-lg max-sm:text-sm text-white/50 max-w-[600px] mx-auto leading-relaxed">
            记录团队在学术研究、技术创新和学科竞赛中取得的荣誉与成果
          </p>
        </section>

        <AchievementStats stats={stats} />
        <section className="w-full my-6 flex justify-end max-md:justify-center">
          <AchievementFilter
            type={type}
            awardLevel={awardLevel}
            year={year}
            years={YEARS}
            onFilterChange={handleFilterChange}
          />
        </section>
        <section ref={sectionRef} className="w-full mt-4 achievements-section">
          {loading ? (
            <div className="flex justify-center items-center min-h-[300px] w-full">
              <Spin size="large" />
            </div>
          ) : achievements.length === 0 ? (
            <Empty description="暂无成就数据" />
          ) : (
            <>
              <Masonry
                columns={{ xs: 1, sm: 2, lg: 3 }}
                gutter={{ xs: 16, sm: 16, lg: 16 }}
                items={achievements.map((achievement) => ({
                  key: achievement.id,
                  data: achievement,
                }))}
                itemRender={(itemInfo: { data: AchievementDTO }) => (
                  <AchievementCard achievement={itemInfo.data} />
                )}
              />
              {totalPages > 1 && (
                <div className="flex justify-center mt-8 py-4">
                  <Pagination
                    current={currentPage + 1}
                    total={totalElements}
                    pageSize={PAGE_SIZE}
                    onChange={handlePageChange}
                    showSizeChanger={false}
                    showTotal={(total: number) => `共 ${total} 项成就`}
                  />
                </div>
              )}
            </>
          )}
        </section>
      </main>
    </div>
  )
}
