import { MenuProps } from 'antd'

export type MenuItem = Required<MenuProps>['items'][number]

export const getMenuItems = (roleLevel: number): MenuItem[] => {
  const defaultItems: MenuItem[] = [
    {
      label: '相关竞赛',
      key: 'competitions',
    },
    // {
    //   label: '团队成果',
    //   key: 'achievements',
    // },
    {
      label: '团队成员',
      key: 'members',
    },
    {
      label: '资源库',
      key: 'resources',
    },
  ]

  switch (roleLevel) {
    case -1: // 未登录用户
      defaultItems.push({
        label: '加入我们',
        key: 'enroll',
      })
      break
    case 0: // 考核用户
      defaultItems.push({
        label: '考核中心',
        key: 'assessment',
      })
      break
    case 1: // 团队成员
    case 2: // 方向管理员
    case 3: // 超级管理员
      defaultItems.push({
        label: '管理平台',
        key: 'managementPlatform',
      })
      break
  }

  // 添加开源仓库
  defaultItems.push({
    label: '开源仓库',
    key: 'repository',
  })

  return defaultItems
}
