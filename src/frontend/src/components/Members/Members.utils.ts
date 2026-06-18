import { Direction, DIRECTION_LABELS } from '@/apis/schema/enumerate'
import { FilterTab } from './Members.types'
import { MemberBriefDTO } from '@/apis/schema/type'

export function computeFilterTabs(members: MemberBriefDTO[]): FilterTab[] {
  const allCount = members.length

  const directionCounts: Record<Direction, number> = Object.fromEntries(
    (Object.keys(DIRECTION_LABELS) as Direction[]).map((key) => [key, 0])
  ) as Record<Direction, number>

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
