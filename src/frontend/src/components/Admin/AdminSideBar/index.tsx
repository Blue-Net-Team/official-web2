'use client'

import { Menu, MenuProps } from 'antd'
import Sider from 'antd/es/layout/Sider'
import React from 'react'
import styles from './styles.module.css'

const AdminSideBar = () => {
  const [collapsed, setCollapsed] = React.useState(false)

  const menuItem: MenuProps['items'] = [
    {
      key: 'home',
      label: '回到首页',
    },
    {
      key: 'enroll',
      label: '报名',
    },
    {
      key: 'competition',
      label: '竞赛',
    },
    {
      key: 'achievement',
      label: '成就',
    },
    {
      key: 'assessment',
      label: '考核',
      children: [
        {
          key: 'assessmentTime',
          label: '考核时间',
        },
        {
          key: 'assessmentQuestion',
          label: '考核题目',
        },
        {
          key: 'assessmentJudge',
          label: '考核评判',
        },
      ],
    },
    {
      key: 'qa',
      label: 'QA管理',
    },
  ]

  return (
    <Sider
      width={200}
      collapsible
      collapsed={collapsed}
      onCollapse={(value: boolean) => setCollapsed(value)}
      className={styles.sider}
    >
      <Menu
        theme="dark"
        mode="inline"
        defaultSelectedKeys={['competition']}
        defaultOpenKeys={['competition']}
        items={menuItem}
      />
    </Sider>
  )
}

export default AdminSideBar
