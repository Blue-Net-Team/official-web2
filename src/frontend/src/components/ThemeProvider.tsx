'use client'

import { ConfigProvider, theme } from 'antd'
import { ReactNode } from 'react'

// 橙色主色调
const primaryColor = '#fa8c16'
const primaryColorHover = '#ffa940'
const primaryColorActive = '#d46b08'
const darkItemBg = '#19191c'

const themeConfig = {
  algorithm: theme.darkAlgorithm,
  token: {
    colorPrimary: primaryColor,
    colorPrimaryHover: primaryColorHover,
    colorPrimaryActive: primaryColorActive,
    borderRadius: 12,
  },
  components: {
    Menu: {
      darkItemBg: darkItemBg,
      darkItemColor: 'rgba(140, 140, 141, 1)',
      darkItemHoverColor: '#ffffff',
      darkItemSelectedColor: primaryColor,
      activeBarBorderWidth: 0,
      darkItemSelectedBg: 'transparent',
      darkSubMenuItemBg: darkItemBg,
    },
    Layout: {
      headerBg: 'rgba(25, 25, 28, 1)',
      bodyBg: '#000000',
      footerBg: 'rgba(35, 35, 38, 1)',
      triggerBg: darkItemBg,
      siderBg: darkItemBg,
    },
    Button: {
      colorPrimary: primaryColor,
      colorPrimaryHover: primaryColorHover,
      colorPrimaryActive: primaryColorActive,
      primaryShadow: `0 4px 20px ${primaryColor}40`,
    },
    Tabs: {
      inkBarColor: primaryColor,
      itemActiveColor: primaryColor,
      itemHoverColor: primaryColorHover,
      itemSelectedColor: primaryColor,
    },
    Input: {
      activeBorderColor: primaryColor,
      hoverBorderColor: primaryColorHover,
      activeShadow: `0 0 0 3px ${primaryColor}26`,
    },
    Select: {
      colorPrimary: primaryColor,
      colorPrimaryHover: primaryColorHover,
      colorPrimaryActive: primaryColorActive,
    },
  },
}

export default function ThemeProvider({ children }: { children: ReactNode }) {
  return <ConfigProvider theme={themeConfig}>{children}</ConfigProvider>
}
