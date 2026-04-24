'use client'

import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { useState, useEffect } from 'react'
import { Layout, Menu, Button } from 'antd'
import { MenuOutlined } from '@ant-design/icons'
import Image from 'next/image'
import logoImage from '@/assets/logo.png'
import authStore from '@/stores/authStore'
import { getRoleLevel } from '@/utils/RoleUtils'
import { DEFAULT_USER_INFO } from './constants'
import { getMenuItems } from './useMenuItems'
import type { MenuItem } from './useMenuItems'
import { LoginButton } from './LoginButton'
import { UserDropdown, UserInfoDisplay, loginDropdownMenuItems } from './UserDropdown'
import { MobileDrawer } from './MobileDrawer'

const { Header } = Layout

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
        router.push('/admin')
        return
      case 'repository':
        window.open('https://github.com/Blue-Net-Team', '_blank')
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

  const menuItems = getMenuItems(roleLevel)

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
   * 从公共菜单中获取移动端菜单项目
   * 移动端的菜单item会包含登录按钮或用户信息
   */
  const getMobileMenuItems = (): MenuItem[] => {
    const items = getMenuItems(roleLevel)
    const userInfoOrLoginBtn = isAuthenticated ? (
      <UserInfoDisplay userInfo={userInfo} />
    ) : (
      <LoginButton isMobile />
    )
    const loginState: MenuItem = isAuthenticated
      ? {
          label: userInfoOrLoginBtn,
          key: 'userinfo',
          children: loginDropdownMenuItems,
        }
      : {
          label: userInfoOrLoginBtn,
          key: 'userinfo',
        }

    return [loginState, ...items]
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

      {!isMobile &&
        (isAuthenticated ? (
          <UserDropdown userInfo={userInfo} onMenuClick={handleMenuClick} />
        ) : (
          <LoginButton />
        ))}

      {isMobile && (
        <Button
          type="text"
          className="text-white text-xl flex items-center justify-center hover:text-[#ff6f3c] hover:bg-transparent"
          icon={<MenuOutlined style={{ color: 'white' }} />}
          onClick={toggleDrawer}
        />
      )}

      <MobileDrawer
        open={drawerOpen}
        onClose={closeDrawer}
        items={getMobileMenuItems()}
        onMenuClick={handleMenuClick}
      />
    </Header>
  )
}

export default NavBar
