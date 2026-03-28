import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { FilterTab } from './Members.types'
import { MemberBriefDTO } from '@/apis/schema/type'

export function computeFilterTabs(members: MemberBriefDTO[]): FilterTab[] {
  const allCount = members.length

  const directionCounts: Record<Direction, number> = {
    COMPUTER_VISION: 0,
    STRUCTURAL_DESIGN: 0,
    EMBEDDED: 0,
  }

  members.forEach((member: MemberBriefDTO) => {
    if (member.direction in directionCounts) {
      directionCounts[member.direction as Direction]++
    }
  })

  const tabs: FilterTab[] = [{ key: 'ALL', label: '全部', count: allCount }]

  ;(Object.keys(directionCounts) as Direction[]).forEach((direction) => {
    tabs.push({
      key: direction,
      label: DIRECTION_LABELS[direction],
      count: directionCounts[direction],
    })
  })

  return tabs
}
