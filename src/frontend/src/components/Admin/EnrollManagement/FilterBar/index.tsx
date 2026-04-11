'use client'

import { Input, Select } from 'antd'
import type { EnrollStatus } from '@/apis/schema/enumerate'
import type { Direction } from '@/apis/schema/enumerate'

const { Search } = Input

export interface FilterValues {
  keyword: string
  status: EnrollStatus | undefined
  direction: Direction | undefined
}

interface FilterBarProps {
  value: FilterValues
  onChange: (value: FilterValues) => void
}

const statusOptions = [
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已拒绝' },
]

const directionOptions = [
  { value: 'COMPUTER_VISION', label: '计算机视觉' },
  { value: 'STRUCTURAL_DESIGN', label: '结构设计' },
  { value: 'EMBEDDED', label: '嵌入式开发' },
]

export const FilterBar: React.FC<FilterBarProps> = ({ value, onChange }) => {
  return (
    <div className="flex flex-col md:flex-row gap-3">
      <Search
        placeholder="搜索姓名或学号..."
        allowClear
        onSearch={(val) => onChange({ ...value, keyword: val })}
        style={{ width: '100%', maxWidth: 280 }}
      />
      <Select
        placeholder="全部状态"
        allowClear
        options={statusOptions}
        value={value.status}
        onChange={(val) => onChange({ ...value, status: val })}
        style={{ width: '100%', maxWidth: 140 }}
      />
      <Select
        placeholder="全部方向"
        allowClear
        options={directionOptions}
        value={value.direction}
        onChange={(val) => onChange({ ...value, direction: val })}
        style={{ width: '100%', maxWidth: 160 }}
      />
    </div>
  )
}
