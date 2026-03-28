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
import styles from './styles.module.css'
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
  onTabChange?: (tab: TabName) => void
}

interface TabItem {
  key: TabName
  label: string
  icon: React.ReactNode
  showCount?: boolean
  countKey?: keyof TabCounts
}

const tabs: TabItem[] = [
  { key: 'profile', label: '个人信息', icon: <UserOutlined /> },
  { key: 'assessment', label: '我的考核', icon: <FileTextOutlined />, showCount: false },
  {
    key: 'projects',
    label: '项目经历',
    icon: <FolderOutlined />,
    showCount: true,
    countKey: 'projects',
  },
  {
    key: 'competitions',
    label: '竞赛经历',
    icon: <TrophyOutlined />,
    showCount: true,
    countKey: 'competitions',
  },
  {
    key: 'internships',
    label: '实习经历',
    icon: <SolutionOutlined />,
    showCount: true,
    countKey: 'internships',
  },
]

export default function ProfileTabs({ activeTab, tabCounts, onTabChange }: ProfileTabsProps) {
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
    <nav className={styles.sectionTabs}>
      {tabs.map((tab) => (
        <button
          key={tab.key}
          onClick={() => handleClick(tab.key)}
          className={`${styles.tabBtn} ${activeTab === tab.key ? styles.tabBtnActive : ''}`}
        >
          {tab.icon}
          <span className={styles.tabLabel}>{tab.label}</span>
          {tab.showCount && <span className={styles.tabCount}>{getCount(tab)}</span>}
        </button>
      ))}
    </nav>
  )
}
