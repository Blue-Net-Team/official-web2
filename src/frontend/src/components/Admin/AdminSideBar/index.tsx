'use client'

import { Menu, MenuProps } from 'antd'
import Sider from 'antd/es/layout/Sider'
import React, { useMemo } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import authStore from '@/stores/authStore'
import { getRoleLevel } from '@/utils/RoleUtils'
import {
  HomeOutlined,
  UserAddOutlined,
  TrophyOutlined,
  StarOutlined,
  ClockCircleOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
  QuestionCircleOutlined,
  DashboardOutlined,
  LineChartOutlined,
} from '@ant-design/icons'

interface MenuItemConfig {
  key: string
  label: string
  path?: string
  icon?: React.ReactNode
  minLevel: number
  disabled?: boolean
  children?: MenuItemConfig[]
}

const menuConfig: MenuItemConfig[] = [
  {
    key: 'home',
    label: '回到首页',
    path: '/',
    icon: <HomeOutlined />,
    minLevel: 1,
  },
  {
    key: 'panel',
    label: '仪表盘',
    path: '/admin/panel',
    icon: <LineChartOutlined />,
    minLevel: 1,
  },
  {
    key: 'enroll',
    label: '报名管理',
    path: '/admin/enroll',
    icon: <UserAddOutlined />,
    minLevel: 1,
  },
  {
    key: 'competition',
    label: '竞赛管理',
    path: '/admin/competition',
    icon: <TrophyOutlined />,
    minLevel: 3,
  },
  {
    key: 'achievement',
    label: '成就管理',
    path: '/admin/achievement',
    icon: <StarOutlined />,
    minLevel: 3,
  },
  {
    key: 'assessment',
    label: '考核',
    icon: <CheckCircleOutlined />,
    minLevel: 1,
    children: [
      {
        key: 'assessmentTime',
        label: '考核时间',
        path: '/admin/assessment/time',
        icon: <ClockCircleOutlined />,
        minLevel: 2,
      },
      {
        key: 'assessmentQuestion',
        label: '考核题目',
        path: '/admin/assessment/question',
        icon: <FileTextOutlined />,
        minLevel: 2,
      },
      {
        key: 'assessmentJudge',
        label: '考核评判',
        path: '/admin/assessment/judge',
        icon: <CheckCircleOutlined />,
        minLevel: 1,
      },
    ],
  },
  {
    key: 'qa',
    label: 'QA管理',
    icon: <QuestionCircleOutlined />,
    path: '/admin/qa',
    minLevel: 1,
    disabled: true,
  },
]

function filterMenuItems(items: MenuItemConfig[], roleLevel: number): MenuProps['items'] {
  return items
    .filter((item) => roleLevel >= item.minLevel)
    .map((item) => {
      if (item.children) {
        const filteredChildren = item.children.filter((child) => roleLevel >= child.minLevel)
        if (filteredChildren.length === 0) return null
        return {
          key: item.key,
          label: item.label,
          icon: item.icon,
          children: filteredChildren.map((child) => ({
            key: child.key,
            label: child.label,
            icon: child.icon,
          })),
        }
      }
      return {
        key: item.key,
        label: item.label,
        icon: item.icon,
        disabled: item.disabled,
      }
    })
    .filter(Boolean)
}

const AdminSideBar = () => {
  const [collapsed, setCollapsed] = React.useState(false)
  const router = useRouter()
  const pathname = usePathname()
  const userInfo = authStore((state) => state.userInfo)

  const roleLevel = useMemo(() => getRoleLevel(userInfo?.roleName || ''), [userInfo?.roleName])

  const menuItems = useMemo(() => filterMenuItems(menuConfig, roleLevel), [roleLevel])

  const selectedKeys = useMemo(() => {
    const matched = menuConfig.find(
      (item) =>
        (item.path && pathname === item.path) ||
        item.children?.some((child) => child.path && pathname === child.path)
    )
    if (!matched) return ['panel']
    const childMatch = matched.children?.find((child) => child.path && pathname === child.path)
    return [childMatch ? childMatch.key : matched.key]
  }, [pathname])

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    const findPath = (items: MenuItemConfig[]): string | undefined => {
      for (const item of items) {
        if (item.key === key) return item.path
        if (item.children) {
          const childPath = findPath(item.children)
          if (childPath) return childPath
        }
      }
      return undefined
    }
    const path = findPath(menuConfig)
    if (path) router.push(path)
  }

  return (
    <Sider
      width={200}
      collapsible
      collapsed={collapsed}
      onCollapse={(value: boolean) => setCollapsed(value)}
      className="admin-sider overflow-auto [scrollbar-gutter:stable]"
      style={{ height: 'calc(100vh - 64px)' }}
    >
      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={selectedKeys}
        defaultOpenKeys={['assessment']}
        items={menuItems}
        onClick={handleMenuClick}
      />
    </Sider>
  )
}

export default AdminSideBar
