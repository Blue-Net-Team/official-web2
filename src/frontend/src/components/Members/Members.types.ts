import { Direction } from '@/apis/schema/enumerate'

export interface FilterTab {
  key: Direction | 'ALL'
  label: string
  count: number
}

export interface MembersProps {
  /** Initial page number (0-indexed) */
  initialPage?: number
}

export interface PaginationState {
  currentPage: number
  totalPages: number
  totalElements: number
  pageSize: number
}
