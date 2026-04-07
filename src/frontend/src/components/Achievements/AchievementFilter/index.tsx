'use client'

import { useRouter, usePathname } from 'next/navigation'
import { Select, Space } from 'antd'
import { AWARD_LEVEL_LABELS, ACHIEVEMENT_TYPE_LABELS } from '@/apis/schema/enumerate'

interface AchievementFilterProps {
  type?: string
  awardLevel?: string
  year?: number
  years: number[]
  onFilterChange?: (type?: string, awardLevel?: string, year?: number) => void
}

const AchievementFilter = ({
  type,
  awardLevel,
  year,
  years,
  onFilterChange,
}: AchievementFilterProps) => {
  const router = useRouter()
  const pathname = usePathname()

  const typeOptions = [
    { value: '', label: '全部类型' },
    ...Object.entries(ACHIEVEMENT_TYPE_LABELS).map(([value, label]) => ({
      value,
      label,
    })),
  ]

  const awardLevelOptions = [
    { value: '', label: '全部级别' },
    ...Object.entries(AWARD_LEVEL_LABELS).map(([value, label]) => ({
      value,
      label,
    })),
  ]

  const yearOptions = years.map((y) => ({ value: y, label: `${y}年` }))

  const buildUrl = (newType?: string, newAwardLevel?: string, newYear?: number) => {
    const params = new URLSearchParams()
    if (newType) {
      params.set('type', newType)
    }
    if (newAwardLevel) {
      params.set('awardLevel', newAwardLevel)
    }
    if (newYear) {
      params.set('year', String(newYear))
    }
    const queryString = params.toString()
    return queryString ? `${pathname}?${queryString}` : pathname
  }

  const handleTypeChange = (value?: string) => {
    if (onFilterChange) {
      onFilterChange(value, awardLevel, year)
    } else {
      router.push(buildUrl(value, awardLevel, year))
    }
  }

  const handleAwardLevelChange = (value?: string) => {
    if (onFilterChange) {
      onFilterChange(type, value, year)
    } else {
      router.push(buildUrl(type, value, year))
    }
  }

  const handleYearChange = (value?: number) => {
    if (onFilterChange) {
      onFilterChange(type, awardLevel, value)
    } else {
      router.push(buildUrl(type, awardLevel, value))
    }
  }

  return (
    <Space size={16} wrap className="flex gap-4 max-md:flex-col max-md:gap-3">
      <Select
        value={type}
        onChange={handleTypeChange}
        options={typeOptions}
        className="min-w-[120px] max-md:w-full"
        placeholder="成就类型"
        allowClear
      />
      <Select
        value={awardLevel}
        onChange={handleAwardLevelChange}
        options={awardLevelOptions}
        className="min-w-[120px] max-md:w-full"
        placeholder="奖项级别"
        allowClear
      />
      <Select
        value={year}
        onChange={handleYearChange}
        options={yearOptions}
        className="min-w-[120px] max-md:w-full"
        placeholder="获奖年份"
        allowClear
      />
    </Space>
  )
}

export default AchievementFilter
