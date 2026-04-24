/**
 * Tab导航组件 - 通用 Tab 导航
 *
 * 功能：
 * - 支持自定义 Tab 配置
 * - 显示各Tab对应的数据数量Badge
 * - 支持客户端Tab切换
 *
 * @author BlueNet Team
 */
'use client'

import React from 'react'
import type { TabCounts } from '@/apis/schema/type'

export interface TabConfig {
  key: string
  label: string
  icon: React.ReactNode
  showCount?: boolean
  countKey?: keyof TabCounts
}

interface ProfileTabsProps {
  activeTab: string
  tabs: TabConfig[]
  tabCounts?: TabCounts
  onTabChange?: (tab: string) => void
}

export default function ProfileTabs({
  activeTab,
  tabs,
  tabCounts = { projects: 0, competitions: 0, internships: 0 },
  onTabChange,
}: ProfileTabsProps) {
  const getCount = (tab: TabConfig): number => {
    if (tab.countKey) {
      return tabCounts[tab.countKey] || 0
    }
    return 0
  }

  const handleClick = (tab: string) => {
    onTabChange?.(tab)
  }

  return (
    <nav className="flex gap-2 mb-6 p-1 bg-white/[0.03] backdrop-blur-[20px] border border-white/[0.05] rounded-xl max-lg:flex-wrap max-[640px]:flex-wrap">
      {tabs.map((tab) => (
        <button
          key={tab.key}
          onClick={() => handleClick(tab.key)}
          className={`flex-1 py-3 px-5 border-none bg-transparent text-[rgba(140,140,141,1)] text-sm font-medium cursor-pointer rounded-lg transition-all duration-300 flex items-center justify-center gap-1.5 no-underline hover:text-white hover:bg-white/[0.05] max-[640px]:flex-[1_1_calc(50%-4px)] max-[640px]:py-[10px] max-[640px]:px-2 max-[640px]:text-xs ${
            activeTab === tab.key
              ? 'bg-[linear-gradient(135deg,#6677ff_0%,#2f27b0_100%)] text-white shadow-[0_4px_16px_rgba(102,119,255,0.3)]'
              : ''
          }`}
        >
          {tab.icon}
          <span className="inline">{tab.label}</span>
          {tab.showCount && (
            <span className="text-xs opacity-80 bg-white/20 py-[2px] px-2 rounded-[10px]">
              {getCount(tab)}
            </span>
          )}
        </button>
      ))}
    </nav>
  )
}
