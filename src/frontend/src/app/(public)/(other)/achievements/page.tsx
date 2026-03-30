'use client'

import { useState, useEffect, useCallback } from 'react'
import { Masonry, Spin, Empty, Pagination } from 'antd'
import styles from './styles.module.css'
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

  // 获取成就列表
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

  // 处理页码变化
  const handlePageChange = (page: number) => {
    setCurrentPage(page - 1) // UI 显示从 1 开始，API 从 0 开始
    const achievementsSection = document.querySelector(`.${styles.achievementsSection}`)
    if (achievementsSection) {
      achievementsSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }

  return (
    <div className={styles.pageContainer}>
      <div className={styles.pageBg} />
      <main className={styles.mainContent}>
        <section className={styles.pageHeader}>
          <h1 className={styles.pageTitle}>团队成就</h1>
          <p className={styles.pageSubtitle}>
            记录团队在学术研究、技术创新和学科竞赛中取得的荣誉与成果
          </p>
        </section>

        <AchievementStats stats={stats} />
        <section className={styles.filterSection}>
          <AchievementFilter
            type={type}
            awardLevel={awardLevel}
            year={year}
            years={YEARS}
            onFilterChange={handleFilterChange}
          />
        </section>
        <section className={styles.achievementsSection}>
          {loading ? (
            <div className={styles.loadingContainer}>
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
                itemRender={(itemInfo) => <AchievementCard achievement={itemInfo.data} />}
              />
              {totalPages > 1 && (
                <div className={styles.paginationContainer}>
                  <Pagination
                    current={currentPage + 1}
                    total={totalElements}
                    pageSize={PAGE_SIZE}
                    onChange={handlePageChange}
                    showSizeChanger={false}
                    showTotal={(total) => `共 ${total} 项成就`}
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
