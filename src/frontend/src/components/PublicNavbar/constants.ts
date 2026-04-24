export const DEFAULT_USER_INFO = {
  id: 0,
  username: '',
  roleName: '',
  direction: null,
  avatarFileId: null,
}

export const loginButtonStyle: React.CSSProperties = {
  backgroundColor: '#ff6f3c',
  borderColor: '#ff6f3c',
  borderRadius: 16,
  padding: '0 18px',
  height: 29,
  fontSize: 14,
  color: '#ffe4c5',
}

export const drawerThemeConfig = {
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
