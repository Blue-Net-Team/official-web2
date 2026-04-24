'use client'

import { useState, useEffect, useMemo, createContext, useContext } from 'react'
import { useRouter, usePathname } from 'next/navigation'
import { useAuth } from '@/hooks'
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
  LineChartOutlined,
  SafetyOutlined,
  TeamOutlined,
  KeyOutlined,
  MailOutlined,
} from '@ant-design/icons'
import type { MenuProps } from 'antd'

export interface MenuItemConfig {
  /** 菜单唯一键。 */
  key: string
  /** 菜单展示名称。 */
  label: string
  /** 菜单跳转路径。 */
  path?: string
  /** 菜单图标。 */
  icon?: React.ReactNode
  /** 可访问该菜单的最低角色等级。 */
  minLevel: number
  /** 是否禁用该菜单。 */
  disabled?: boolean
  /** 子菜单配置。 */
  children?: MenuItemConfig[]
}

export const menuConfig: MenuItemConfig[] = [
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
    key: 'messageTemplate',
    label: '消息模板',
    path: '/admin/message-template',
    icon: <MailOutlined />,
    minLevel: 3,
  },
  {
    key: 'permission',
    label: '权限管理',
    icon: <SafetyOutlined />,
    minLevel: 3,
    children: [
      {
        key: 'permissionRole',
        label: '角色权限',
        path: '/admin/permissions/role',
        icon: <TeamOutlined />,
        minLevel: 3,
      },
      {
        key: 'permissionPermission',
        label: '权限角色',
        path: '/admin/permissions/permission',
        icon: <KeyOutlined />,
        minLevel: 3,
      },
    ],
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
        key: 'assessmentJudgeScore',
        label: '题目评分',
        path: '/admin/assessment/judge/score',
        icon: <CheckCircleOutlined />,
        minLevel: 1,
      },
      {
        key: 'assessmentJudgeDecision',
        label: '录用决策',
        path: '/admin/assessment/judge/decision',
        icon: <TeamOutlined />,
        minLevel: 2,
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

/** 按当前角色等级过滤后台导航菜单。 */
export function filterMenuItems(items: MenuItemConfig[], roleLevel: number): MenuProps['items'] {
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

interface AdminNavContextType {
  /** 当前是否为移动端布局。 */
  isMobile: boolean
  /** 移动端抽屉是否显示。 */
  drawerVisible: boolean
  /** 打开移动端导航抽屉。 */
  openDrawer: () => void
  /** 关闭移动端导航抽屉。 */
  closeDrawer: () => void
  /** 桌面端侧边栏是否折叠。 */
  collapsed: boolean
  /** 设置桌面端侧边栏折叠状态。 */
  onCollapse: (collapsed: boolean) => void
  /** 按权限过滤后的 AntD 菜单项。 */
  menuItems: MenuProps['items']
  /** 当前路由命中的菜单键。 */
  selectedKeys: string[]
  /** 菜单点击处理函数。 */
  onMenuClick: MenuProps['onClick']
}

const AdminNavContext = createContext<AdminNavContextType | null>(null)

/** 读取后台导航上下文。 */
export const useAdminNav = () => {
  const context = useContext(AdminNavContext)
  if (!context) {
    throw new Error('useAdminNav must be used within AdminNav')
  }
  return context
}

/** 提供后台导航状态、权限菜单和路由跳转能力。 */
const AdminNav = ({ children }: { children: React.ReactNode }) => {
  const [isMobile, setIsMobile] = useState(false)
  const [drawerVisible, setDrawerVisible] = useState(false)
  const [collapsed, setCollapsed] = useState(false)
  const router = useRouter()
  const pathname = usePathname()
  const { userInfo } = useAuth()

  const roleLevel = useMemo(() => getRoleLevel(userInfo?.roleName || ''), [userInfo?.roleName])

  useEffect(() => {
    /** 根据窗口宽度同步移动端状态。 */
    const checkMobile = () => {
      const mobile = window.matchMedia('(max-width: 767px)').matches
      setIsMobile(mobile)
      if (mobile) {
        setCollapsed(true)
      }
    }
    checkMobile()
    window.addEventListener('resize', checkMobile)
    return () => window.removeEventListener('resize', checkMobile)
  }, [])

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

  /** 根据菜单键查找路径并执行跳转。 */
  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    /** 在嵌套菜单配置中递归查找目标路径。 */
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
    if (path) {
      router.push(path)
      if (isMobile) {
        setDrawerVisible(false)
      }
    }
  }

  const contextValue: AdminNavContextType = {
    isMobile,
    drawerVisible,
    openDrawer: () => setDrawerVisible(true),
    closeDrawer: () => setDrawerVisible(false),
    collapsed,
    onCollapse: setCollapsed,
    menuItems,
    selectedKeys,
    onMenuClick: handleMenuClick,
  }

  return <AdminNavContext.Provider value={contextValue}>{children}</AdminNavContext.Provider>
}

export default AdminNav
