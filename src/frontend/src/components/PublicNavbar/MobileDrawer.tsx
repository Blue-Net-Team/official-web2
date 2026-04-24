'use client'

import { Drawer, Menu, ConfigProvider } from 'antd'
import { drawerThemeConfig } from './constants'
import type { MenuItem } from './useMenuItems'

interface MobileDrawerProps {
  open: boolean
  onClose: () => void
  items: MenuItem[]
  onMenuClick: (key: string) => void
}

export const MobileDrawer = ({ open, onClose, items, onMenuClick }: MobileDrawerProps) => {
  return (
    <ConfigProvider theme={drawerThemeConfig}>
      <Drawer
        placement="right"
        open={open}
        onClose={onClose}
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
          items={items}
          className="border-none bg-transparent"
          selectedKeys={[]}
          onClick={(e: { key: string }) => {
            onMenuClick(e.key)
            onClose()
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
  )
}
