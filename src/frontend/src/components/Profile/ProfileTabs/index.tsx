/**
 * Tab导航组件 - 客户端组件
 *
 * 功能：
 * - 渲染5个Tab按钮（个人信息、考核、项目、竞赛、实习）
 * - 显示各Tab对应的数据数量Badge
 * - 支持客户端Tab切换
 *
 * @param activeTab - 当前激活的Tab
 * @param tabCounts - 各Tab的数据计数
 * @param onTabChange - Tab切换回调
 *
 * @author BlueNet Team
 */
'use client'

import type { TabName, TabCounts } from '@/types/profile'
import {
  UserOutlined,
  FileTextOutlined,
  FolderOutlined,
  TrophyOutlined,
  SolutionOutlined,
} from '@ant-design/icons'

interface ProfileTabsProps {
  activeTab: TabName
  tabCounts: TabCounts
  roleName?: string
  onTabChange?: (tab: TabName) => void
}

interface TabItem {
  key: TabName
  label: string
  icon: React.ReactNode
  showCount?: boolean
  countKey?: keyof TabCounts
  requireMember?: boolean
}

const allTabs: TabItem[] = [
  { key: 'profile', label: '个人信息', icon: <UserOutlined /> },
  { key: 'assessment', label: '我的考核', icon: <FileTextOutlined />, showCount: false },
  {
    key: 'projects',
    label: '项目经历',
    icon: <FolderOutlined />,
    showCount: true,
    countKey: 'projects',
    requireMember: true,
  },
  {
    key: 'competitions',
    label: '竞赛经历',
    icon: <TrophyOutlined />,
    showCount: true,
    countKey: 'competitions',
    requireMember: true,
  },
  {
    key: 'internships',
    label: '实习经历',
    icon: <SolutionOutlined />,
    showCount: true,
    countKey: 'internships',
    requireMember: true,
  },
]

export default function ProfileTabs({
  activeTab,
  tabCounts,
  roleName,
  onTabChange,
}: ProfileTabsProps) {
  const isMemberOrAbove = roleName !== 'CANDIDATE'
  const tabs = allTabs.filter((tab) => !tab.requireMember || isMemberOrAbove)
  const getCount = (tab: TabItem): number => {
    if (tab.countKey) {
      return tabCounts[tab.countKey] || 0
    }
    return 0
  }

  const handleClick = (tab: TabName) => {
    if (onTabChange) {
      onTabChange(tab)
    }
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
