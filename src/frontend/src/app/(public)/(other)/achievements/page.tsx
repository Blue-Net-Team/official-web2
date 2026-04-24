'use client'

import { useState, useEffect, useCallback, useRef } from 'react'
import { Masonry, Spin, Empty, Pagination } from 'antd'
import AchievementCard from '@/components/Achievements/AchievementCard'
import AchievementStats from '@/components/Achievements/AchievementStats'
import AchievementFilter from '@/components/Achievements/AchievementFilter'
import { AchievementService } from '@/apis/services/achievement.service'
import { AchievementDTO, AchievementStatsDTO } from '@/apis/schema/type'

const PAGE_SIZE = 12
const YEARS = [2024, 2023, 2022, 2021, 2020]

export default function AchievementsPage() {
  const [achievements, setAchievements] = useState<AchievementDTO[]>([])
  const [stats, setStats] = useState<AchievementStatsDTO>({
    totalAchievements: 0,
    nationalCount: 0,
    provincialCount: 0,
    schoolCount: 0,
  })
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(true)

  // 筛选条件
  const [type, setType] = useState<string | undefined>()
  const [awardLevel, setAwardLevel] = useState<string | undefined>()
  const [year, setYear] = useState<number | undefined>()

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

  const fetchAchievements = useCallback(async () => {
    setLoading(true)
    try {
      const response = await AchievementService.getAchievements({
        type,
        awardLevel,
        year,
        page: currentPage,
        size: PAGE_SIZE,
      })

      if (response.code === 200 && response.data) {
        setAchievements(response.data.content)
        setTotalPages(response.data.totalPages)
        setTotalElements(response.data.totalElements)
      } else {
        setAchievements([])
        setTotalPages(0)
        setTotalElements(0)
      }
    } catch (error) {
      console.error('Failed to fetch achievements:', error)
      setAchievements([])
      setTotalPages(0)
      setTotalElements(0)
    } finally {
      setLoading(false)
    }
  }, [type, awardLevel, year, currentPage])

  useEffect(() => {
    fetchAchievements()
  }, [fetchAchievements])

  // 处理筛选条件变化
  const handleFilterChange = (newType?: string, newAwardLevel?: string, newYear?: number) => {
    setType(newType)
    setAwardLevel(newAwardLevel)
    setYear(newYear)
    setCurrentPage(0) // 筛选条件变化时重置页码
  }

  const sectionRef = useRef<HTMLElement>(null)

  // 处理页码变化
  const handlePageChange = (page: number) => {
    setCurrentPage(page - 1)
    sectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <div className="w-full min-h-screen bg-[#0a0a0a] text-white relative overflow-x-hidden">
      <div className="fixed top-0 left-0 w-full h-full bg-[radial-gradient(ellipse_80%_50%_at_20%_40%,rgba(102,119,255,0.15)_0%,transparent_50%),radial-gradient(ellipse_60%_40%_at_80%_60%,rgba(255,107,53,0.1)_0%,transparent_50%),radial-gradient(ellipse_50%_30%_at_50%_100%,rgba(47,39,176,0.2)_0%,transparent_50%)] z-0 pointer-events-none" />
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
