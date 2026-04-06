'use client'

import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useState, useEffect } from 'react'
import { Layout, Menu, Button, Drawer, Dropdown, Flex, MenuProps, ConfigProvider } from 'antd'
import { LockOutlined, LogoutOutlined, MenuOutlined, UserOutlined } from '@ant-design/icons'
import Image from 'next/image'
import logoImage from '@/assets/logo.png'
import authStore from '@/stores/authStore'
import { getRoleLevel } from '@/utils/RoleUtils'

type MenuItem = Required<MenuProps>['items'][number]
const { Header } = Layout

const DEFAULT_USER_INFO = {
  id: 0,
  username: '',
  roleName: '',
  direction: null,
  avatarUrl: null,
}

const loginButtonStyle: React.CSSProperties = {
  backgroundColor: '#ff6f3c',
  borderColor: '#ff6f3c',
  borderRadius: 16,
  padding: '0 18px',
  height: 29,
  fontSize: 14,
  color: '#ffe4c5',
}

const drawerThemeConfig = {
  components: {
    Drawer: {
      colorBgElevated: '#19191c',
      colorBgMask: 'rgba(0, 0, 0, 0.45)',
      colorText: '#ffffff',
      colorTextDescription: 'rgba(140, 140, 141, 1)',
    },
    Menu: {
      darkItemBg: '#19191c',
      darkItemColor: 'rgba(140, 140, 141, 1)',
      darkItemHoverColor: '#ffffff',
      darkItemSelectedColor: '#ff6f3c',
      darkItemSelectedBg: 'transparent',
      darkSubMenuItemBg: '#19191c',
    },
  },
}

const NavBar = () => {
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [isMobile, setIsMobile] = useState(false)
  const isAuthenticated = authStore((state) => state.isAuthenticated)
  const userInfo = authStore((state) => state.userInfo) || DEFAULT_USER_INFO
  const logout = authStore((state) => state.logout)
  const router = useRouter()

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 768)
    }

    checkMobile()
    window.addEventListener('resize', checkMobile)

    return () => {
      window.removeEventListener('resize', checkMobile)
    }
  }, [])

  // 获取用户角色等级
  const roleLevel = getRoleLevel(userInfo?.roleName || '')

  /**
   * 用户登录之后用户信息显示的下拉菜单的项目
   */
  const loginDropdownMenuItems: MenuItem[] = [
    {
      label: '个人信息',
      key: 'profile',
      icon: <UserOutlined />,
    },
    {
      label: '修改密码',
      key: 'changePassword',
      icon: <LockOutlined />,
    },
    {
      label: '退出登录',
      key: 'logout',
      icon: <LogoutOutlined />,
    },
  ]

  /**
   * 处理菜单点击事件
   */
  const handleMenuClick = async (key: string) => {
    switch (key) {
      case 'competitions':
        router.push('/competitions')
        return
      case 'achievements':
        router.push('/achievements')
        return
      case 'members':
        router.push('/members')
        return
      case 'enroll':
        router.push('/enroll')
        return
      case 'assessment':
        router.push('/assessment')
        return
      case 'managementPlatform':
        router.push('/management')
        return
      case 'repository':
        router.push('/repository')
        return
      case 'profile':
        router.push('/profile')
        return
      case 'changePassword':
        router.push('/change-password')
        return
      case 'logout':
        await logout()
        router.push('/')
        return
    }
  }

  /**
   * 根据用户角色等级获取导航栏菜单项目
   */
  const getMenuItems = () => {
    const defaultItems = [
      {
        label: '相关竞赛',
        key: 'competitions',
      },
      {
        label: '团队成果',
        key: 'achievements',
      },
      {
        label: '团队成员',
        key: 'members',
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
  const menuItems = getMenuItems()

  /**
   * 从公共菜单中获取移动端菜单项目
   * 移动端的菜单item会包含登录按钮或用户信息
   */
  const getMobilMenuItemsFromCommon = () => {
    const items = getMenuItems()
    const userInfoOrLoginBtn = renderLoginBtnOrUserInfo()
    let loginState: MenuItem = {
      label: userInfoOrLoginBtn,
      key: 'userinfo',
    }
    if (isAuthenticated) {
      loginState = {
        label: userInfoOrLoginBtn,
        key: 'userinfo',
        children: loginDropdownMenuItems,
      }
    }

    return [loginState, ...items]
  }

  /**
   * 打开侧边抽屉
   */
  const toggleDrawer = () => {
    setDrawerOpen(!drawerOpen)
  }

  /**
   * 关闭侧边抽屉
   */
  const closeDrawer = () => {
    setDrawerOpen(false)
  }

  /**
   * 渲染用户信息，用于导航栏
   */
  const renderUserInfo = () => {
    return (
      <Flex align="center" gap={10}>
        {/* 头像 */}
        {userInfo?.avatarUrl ? (
          <Image
            src={userInfo.avatarUrl}
            alt="user avatar"
            className="w-10 h-10 rounded-full object-cover"
            width={40}
            height={40}
          />
        ) : (
          <UserOutlined style={{ fontSize: 24, color: '#fff' }} />
        )}
        {/* 用户名 */}
        <span className="darkText inline-block leading-normal select-none">
          {userInfo?.username}
        </span>
      </Flex>
    )
  }
  /**
   * 渲染登录按钮或用户信息
   */
  const renderLoginBtnOrUserInfo = () => {
    if (!isMobile) {
      if (isAuthenticated) {
        return (
          <Dropdown
            menu={{
              items: loginDropdownMenuItems,
              onClick: (e: { key: string }) => handleMenuClick(e.key),
            }}
          >
            {renderUserInfo()}
          </Dropdown>
        )
      }
      return (
        <Button
          type="primary"
          style={loginButtonStyle}
          onMouseEnter={(e) => {
            e.currentTarget.style.backgroundColor = '#ff8a5c'
            e.currentTarget.style.borderColor = '#ff8a5c'
            e.currentTarget.style.color = '#fff'
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.backgroundColor = '#ff6f3c'
            e.currentTarget.style.borderColor = '#ff6f3c'
            e.currentTarget.style.color = '#ffe4c5'
          }}
          onClick={() => router.push('/login')}
        >
          登录
        </Button>
      )
    } else {
      if (isAuthenticated) {
        return renderUserInfo()
      }
      return (
        <Button
          type="primary"
          style={{ ...loginButtonStyle, width: '100%' }}
          onMouseEnter={(e) => {
            e.currentTarget.style.backgroundColor = '#ff8a5c'
            e.currentTarget.style.borderColor = '#ff8a5c'
            e.currentTarget.style.color = '#fff'
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.backgroundColor = '#ff6f3c'
            e.currentTarget.style.borderColor = '#ff6f3c'
            e.currentTarget.style.color = '#ffe4c5'
          }}
          onClick={() => router.push('/login')}
        >
          登录
        </Button>
      )
    }
  }

  return (
    <Header className="bg-[#19191c] px-16 h-16 flex items-center justify-between max-md:px-6">
      <div className="flex items-center gap-10 max-md:gap-5">
        <div
          className="flex items-center gap-1.5 cursor-pointer group"
          onClick={() => router.push('/')}
        >
          <div className="w-9 h-9 bg-transparent rounded flex items-center justify-center">
            <Image src={logoImage} alt="bluenet logo" className="w-9 h-9" />
          </div>
          <Link
            href="/"
            className="text-sm font-bold text-white! select-none hover:text-[#ff6f3c]!"
          >
            BLUENET
          </Link>
        </div>

        {!isMobile && (
          <Menu
            theme="dark"
            mode="horizontal"
            items={menuItems}
            className="bg-transparent border-b-0"
            selectedKeys={[]}
            onClick={(e: { key: string }) => handleMenuClick(e.key)}
          />
        )}
      </div>

      {!isMobile && renderLoginBtnOrUserInfo()}

      {isMobile && (
        <Button
          type="text"
          className="text-white text-xl flex items-center justify-center hover:text-[#ff6f3c] hover:bg-transparent"
          icon={<MenuOutlined style={{ color: 'white' }} />}
          onClick={toggleDrawer}
        />
      )}

      <ConfigProvider theme={drawerThemeConfig}>
        <Drawer
          placement="right"
          open={drawerOpen}
          onClose={closeDrawer}
          size={250}
          styles={{
            header: {
              borderBottom: '1px solid #333',
              padding: '19.6px 24px',
            },
            body: {
              padding: '24px 16px',
              display: 'flex',
              flexDirection: 'column',
              gap: 16,
            },
          }}
        >
          <Menu
            mode="inline"
            theme="dark"
            items={getMobilMenuItemsFromCommon()}
            className="border-none bg-transparent"
            selectedKeys={[]}
            onClick={(e: { key: string }) => {
              handleMenuClick(e.key)
              closeDrawer()
            }}
            styles={{
              item: {
                color: '#fff',
                fontSize: 16,
                padding: '12px 16px',
                margin: 0,
                borderRadius: 8,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'flex-start',
                textAlign: 'left',
                lineHeight: 1.5,
                height: 'auto',
              },
            }}
          />
        </Drawer>
      </ConfigProvider>
    </Header>
  )
}

export default NavBar
