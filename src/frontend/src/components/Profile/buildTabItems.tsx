/**
 * Tab 配置 → antd Tabs items 转换工具
 *
 * 将各 Profile 类页面共用的 TabConfig（key/label/icon/计数）映射为
 * SmokedGlassTabs（antd Tabs）的 items，label 内联图标与数量徽标。
 *
 * @author BlueNet Team
 */
import React from 'react'
import { Badge } from 'antd'
import type { TabsProps } from 'antd'
import type { TabCounts } from '@/apis/schema/type'

export interface TabConfig {
  key: string
  label: string
  icon: React.ReactNode
  showCount?: boolean
  countKey?: keyof TabCounts
}

export function buildTabItems(
  tabs: TabConfig[],
  tabCounts: TabCounts,
  badgeColor = '#6677ff'
): TabsProps['items'] {
  return tabs.map((tab) => ({
    key: tab.key,
    label: (
      <span>
        {tab.icon} {tab.label}
        {tab.showCount && tab.countKey && (
          <>
            {' '}
            <Badge count={tabCounts[tab.countKey] || 0} size="small" color={badgeColor} />
          </>
        )}
      </span>
    ),
  }))
}
