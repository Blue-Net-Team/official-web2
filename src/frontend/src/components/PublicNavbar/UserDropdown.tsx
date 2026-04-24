'use client'

import { Dropdown, Flex } from 'antd'
import { LockOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons'
import Image from 'next/image'
import { API_BASE_URL } from '@/apis/config'
import type { UserInfo } from '@/apis/schema/type'
import type { MenuItem } from './useMenuItems'

type UserInfoLike = Pick<UserInfo, 'username' | 'avatarFileId'>

interface UserInfoDisplayProps {
  userInfo: UserInfoLike
}

export const UserInfoDisplay = ({ userInfo }: UserInfoDisplayProps) => {
  return (
    <Flex align="center" gap={10}>
      {/* 头像 */}
      {userInfo?.avatarFileId ? (
        <Image
          src={`${API_BASE_URL}/file/download/${userInfo.avatarFileId}`}
          alt="user avatar"
          className="w-10 h-10 rounded-full object-cover"
          width={40}
          height={40}
        />
      ) : (
        <UserOutlined className="text-2xl text-white" />
      )}
      {/* 用户名 */}
      <span className="darkText inline-block leading-normal select-none">{userInfo?.username}</span>
    </Flex>
  )
}

export const loginDropdownMenuItems: MenuItem[] = [
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

interface UserDropdownProps {
  userInfo: UserInfoLike
  onMenuClick: (key: string) => void
}

export const UserDropdown = ({ userInfo, onMenuClick }: UserDropdownProps) => {
  return (
    <Dropdown
      menu={{
        items: loginDropdownMenuItems,
        onClick: (e: { key: string }) => onMenuClick(e.key),
      }}
    >
      <Flex align="center" gap={10}>
        {/* 头像 */}
        {userInfo?.avatarFileId ? (
          <Image
            src={`${API_BASE_URL}/file/download/${userInfo.avatarFileId}`}
            alt="user avatar"
            className="w-10 h-10 rounded-full object-cover"
            width={40}
            height={40}
          />
        ) : (
          <UserOutlined className="text-2xl text-white" />
        )}
        {/* 用户名 */}
        <span className="darkText inline-block leading-normal select-none">
          {userInfo?.username}
        </span>
      </Flex>
    </Dropdown>
  )
}
