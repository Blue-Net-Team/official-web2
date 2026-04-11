'use client'

import { Layout } from 'antd'
import Image from 'next/image'
import { MenuOutlined } from '@ant-design/icons'
import logoImage from '@/assets/logo.png'
import { useAdminNav } from './index'

const { Header } = Layout

const AdminHeadBar = () => {
  const { isMobile, openDrawer } = useAdminNav()

  return (
    <Header className="h-16 flex items-center justify-between bg-[#19191c] px-4 sm:px-16">
      <div className="flex items-center">
        <div className="flex items-center gap-1.5">
          <div className="w-9 h-9 bg-transparent rounded flex items-center justify-center">
            <Image src={logoImage} alt="bluenet logo" className="w-9 h-9" />
          </div>
          <div className="text-sm font-bold text-white">BLUENET ADMIN</div>
        </div>
      </div>
      {isMobile && (
        <div
          onClick={openDrawer}
          className="flex items-center justify-center w-12 h-12 cursor-pointer"
        >
          <MenuOutlined style={{ color: '#fff', fontSize: 18 }} />
        </div>
      )}
    </Header>
  )
}

export default AdminHeadBar
