'use client'

import React, { useState, useEffect, useCallback, useMemo } from 'react'
import { Pagination } from 'antd'
import { MemberCard } from './MemberCard'
import { MembersProps, FilterTab } from './Members.types'
import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { MemberService } from '@/apis/services/member.service'
import { MemberBriefDTO } from '@/apis/schema/type'
import styles from './Members.module.css'

const PAGE_SIZE = 16

export const Members: React.FC<MembersProps> = ({ initialPage = 0 }) => {
  const [activeFilter, setActiveFilter] = useState<Direction | 'ALL'>('ALL')
  const [currentPage, setCurrentPage] = useState(initialPage)
  const [members, setMembers] = useState<MemberBriefDTO[]>([])
  const [totalElements, setTotalElements] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [directionCounts, setDirectionCounts] = useState<Record<Direction, number>>({
    COMPUTER_VISION: 0,
    STRUCTURAL_DESIGN: 0,
    EMBEDDED: 0,
  })

  // Fetch all member counts on mount for filter tabs
  useEffect(() => {
    const fetchCounts = async () => {
      try {
        const allMembers = await MemberService.getMemberList({ size: 1000 })
        const counts: Record<Direction, number> = {
          COMPUTER_VISION: 0,
          STRUCTURAL_DESIGN: 0,
          EMBEDDED: 0,
        }
        allMembers.content.forEach((member) => {
          if (member.direction in counts) {
            counts[member.direction as Direction]++
          }
        })
        setDirectionCounts(counts)
      } catch (error) {
        console.error('Failed to fetch member counts:', error)
      }
    }
    fetchCounts()
  }, [])

  const fetchMembers = useCallback(async () => {
    setLoading(true)
    try {
      const params = {
        page: currentPage,
        size: PAGE_SIZE,
        direction: activeFilter === 'ALL' ? undefined : activeFilter,
      }
      const response = await MemberService.getMemberList(params)
      setMembers(response.content)
      setTotalElements(response.totalElements)
      setTotalPages(response.totalPages)
    } catch (error) {
      console.error('Failed to fetch members:', error)
    } finally {
      setLoading(false)
    }
  }, [currentPage, activeFilter])

  useEffect(() => {
    fetchMembers()
  }, [fetchMembers])

  const filterTabs: FilterTab[] = useMemo(() => {
    const allCount = Object.values(directionCounts).reduce((sum, count) => sum + count, 0)
    const tabs: FilterTab[] = [{ key: 'ALL', label: '全部', count: allCount }]

    ;(Object.keys(directionCounts) as Direction[]).forEach((direction) => {
      tabs.push({
        key: direction,
        label: DIRECTION_LABELS[direction],
        count: directionCounts[direction],
      })
    })

    return tabs
  }, [directionCounts])

  const handleFilterChange = (filter: Direction | 'ALL') => {
    setActiveFilter(filter)
    setCurrentPage(0)
  }

  const handlePageChange = (page: number) => {
    setCurrentPage(page - 1)
    const membersSection = document.querySelector(`.${styles.membersSection}`)
    if (membersSection) {
      membersSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }

  return (
    <>
      <section className={styles.filterSection}>
        <div className={styles.filterTabs}>
          {filterTabs.map((tab) => (
            <button
              key={tab.key}
              className={`${styles.filterTab} ${
                activeFilter === tab.key ? styles.filterTabActive : ''
              }`}
              onClick={() => handleFilterChange(tab.key)}
            >
              {tab.label}
              <span className={styles.filterCount}>{tab.count}</span>
            </button>
          ))}
        </div>
      </section>

      <section className={styles.membersSection}>
        {loading ? (
          <div className={styles.loadingContainer}>加载中...</div>
        ) : (
          <>
            <div className={styles.membersGrid}>
              {members.map((member, index) => (
                <MemberCard key={member.id} member={member} index={index} />
              ))}
            </div>
            {totalPages > 1 && (
              <div className={styles.paginationContainer}>
                <Pagination
                  current={currentPage + 1}
                  total={totalElements}
                  pageSize={PAGE_SIZE}
                  onChange={handlePageChange}
                  showSizeChanger={false}
                  showTotal={(total) => `共 ${total} 人`}
                />
              </div>
            )}
          </>
        )}
      </section>
    </>
  )
}
