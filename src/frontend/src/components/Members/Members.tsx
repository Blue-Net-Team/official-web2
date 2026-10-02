'use client'

import React, { useState, useEffect, useMemo, useRef } from 'react'
import { Badge, Pagination } from 'antd'
import type { TabsProps } from 'antd'
import { MemberCard } from './MemberCard'
import { MembersProps, FilterTab } from './Members.types'
import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { memberService } from '@/apis/services/member.service'
import type { MemberBriefDTO } from '@/apis/schema/type'
import { usePagination } from '@/hooks'
import SmokedGlassTabs from '@/components/SmokedGlassTabs'

const PAGE_SIZE = 16

export const Members: React.FC<MembersProps> = ({ initialPage = 0 }) => {
  const [activeFilter, setActiveFilter] = useState<Direction | 'ALL'>('ALL')
  const [directionCounts, setDirectionCounts] = useState<Record<Direction, number>>(
    Object.fromEntries(
      (Object.keys(DIRECTION_LABELS) as Direction[]).map((key) => [key, 0])
    ) as Record<Direction, number>
  )

  const {
    data: members,
    total: totalElements,
    totalPages,
    loading,
    currentPage,
    setCurrentPage,
  } = usePagination(
    (page, size) =>
      memberService.getMemberList({
        page,
        size,
        direction: activeFilter === 'ALL' ? undefined : activeFilter,
      }),
    { pageSize: PAGE_SIZE, initialPage }
  )

  useEffect(() => {
    const fetchCounts = async () => {
      try {
        // TODO: 当前通过获取所有成员来统计方向人数，建议后端提供 /members/statistics 接口以优化性能
        const response = await memberService.getMemberList({ size: 1000 })
        if (response.code !== 200 || !response.data) return
        const allMembers = response.data
        const counts: Record<Direction, number> = Object.fromEntries(
          (Object.keys(DIRECTION_LABELS) as Direction[]).map((key) => [key, 0])
        ) as Record<Direction, number>
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

  const filterItems: TabsProps['items'] = useMemo(
    () =>
      filterTabs.map((tab) => ({
        key: tab.key,
        label: (
          <span>
            {tab.label} <Badge count={tab.count} size="small" color="#6677ff" />
          </span>
        ),
      })),
    [filterTabs]
  )

  const handleFilterChange = (key: string) => {
    setActiveFilter(key as Direction | 'ALL')
    setCurrentPage(0)
  }

  const sectionRef = useRef<HTMLElement>(null)

  const handlePageChange = (page: number) => {
    setCurrentPage(page - 1)
    sectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  return (
    <>
      <section className="w-full px-16 pb-10 flex justify-center max-md:px-5 max-md:pb-6 max-[1024px]:px-10 max-[1024px]:pb-8">
        <SmokedGlassTabs
          activeKey={activeFilter}
          items={filterItems}
          onChange={handleFilterChange}
        />
      </section>

      <section
        ref={sectionRef}
        className="w-full px-16 pb-20 max-md:px-5 max-md:pb-10 max-[1024px]:px-10 max-[1024px]:pb-15"
        data-members-section
      >
        {loading ? (
          <div className="flex justify-center items-center min-h-[300px] text-white/60 text-base">
            加载中...
          </div>
        ) : (
          <>
            <div className="grid grid-cols-4 gap-6 max-w-[1400px] mx-auto max-[1200px]:grid-cols-3 max-[1024px]:gap-5 max-[900px]:grid-cols-2 max-md:grid-cols-1 max-md:gap-4">
              {members.map((member, index) => (
                <MemberCard key={member.id} member={member} index={index} />
              ))}
            </div>
            {totalPages > 1 && (
              <div className="flex justify-center mt-12 pt-8 border-t border-white/[0.08]">
                <Pagination
                  current={currentPage + 1}
                  total={totalElements}
                  pageSize={PAGE_SIZE}
                  onChange={handlePageChange}
                  showSizeChanger={false}
                  showTotal={(total: number) => `共 ${total} 人`}
                />
              </div>
            )}
          </>
        )}
      </section>
    </>
  )
}
