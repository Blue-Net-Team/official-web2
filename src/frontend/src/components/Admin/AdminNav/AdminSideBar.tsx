'use client'

import { Menu, Drawer, ConfigProvider } from 'antd'
import Sider from 'antd/es/layout/Sider'
import { useAdminNav } from './index'

const drawerThemeConfig = {
  components: {
    Drawer: {
      colorBgElevated: '#19191c',
      colorBgMask: 'rgba(0, 0, 0, 0.45)',
    },
    Menu: {
      darkItemBg: '#19191c',
      darkSubMenuItemBg: '#19191c',
    },
  },
}

const AdminSideBar = () => {
  const {
    isMobile,
    drawerVisible,
    closeDrawer,
    collapsed,
    onCollapse,
    menuItems,
    selectedKeys,
    onMenuClick,
  } = useAdminNav()

  const menuComponent = (
    <Menu
      theme="dark"
      mode="inline"
      selectedKeys={selectedKeys}
      defaultOpenKeys={['assessment']}
      items={menuItems}
      onClick={onMenuClick}
    />
  )

  if (isMobile) {
    return (
      <ConfigProvider theme={drawerThemeConfig}>
        <Drawer
          placement="right"
          open={drawerVisible}
          onClose={closeDrawer}
          size={200}
          styles={{
            header: { minHeight: 64 },
          }}
        >
          {menuComponent}
        </Drawer>
      </ConfigProvider>
    )
  }

  return (
    <Sider
      width={200}
      collapsible
      collapsed={collapsed}
      onCollapse={onCollapse}
      className="admin-sider overflow-auto [scrollbar-gutter:stable]"
      style={{ height: 'calc(100vh - 64px)' }}
    >
      {menuComponent}
    </Sider>
  )
}

export default AdminSideBar
