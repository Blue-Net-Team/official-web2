'use client'

import { ConfigProvider, theme } from 'antd'
import { ReactNode } from 'react'

// 橙色主色调
const primaryColor = '#fa8c16'
const primaryColorHover = '#ffa940'
const primaryColorActive = '#d46b08'
const darkItemBg = '#19191c'

// 输入框样式变量
const inputBg = 'rgba(255, 255, 255, 0.05)'
const inputBorderColor = 'rgba(255, 255, 255, 0.1)'
const inputColor = '#ffffff'
const inputPlaceholderColor = 'rgba(140, 140, 141, 0.6)'
const inputDisabledBg = 'rgba(255, 255, 255, 0.02)'
const inputDisabledColor = 'rgba(140, 140, 141, 0.8)'

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
      colorBgContainer: inputBg,
      colorBorder: inputBorderColor,
      colorText: inputColor,
      colorTextPlaceholder: inputPlaceholderColor,
      colorBgContainerDisabled: inputDisabledBg,
      colorTextDisabled: inputDisabledColor,
      colorPrimary: primaryColor,
      colorPrimaryHover: primaryColorHover,
      colorPrimaryActive: primaryColorActive,
      activeBorderColor: primaryColor,
      hoverBorderColor: primaryColor,
      activeShadow: `0 0 0 3px ${primaryColor}26`,
    },
    InputNumber: {
      colorBgContainer: inputBg,
      colorBorder: inputBorderColor,
      colorText: inputColor,
      colorTextPlaceholder: inputPlaceholderColor,
      colorBgContainerDisabled: inputDisabledBg,
      colorTextDisabled: inputDisabledColor,
      colorPrimary: primaryColor,
      colorPrimaryHover: primaryColorHover,
      colorPrimaryActive: primaryColorActive,
      activeBorderColor: primaryColor,
      hoverBorderColor: primaryColor,
      activeShadow: `0 0 0 3px ${primaryColor}26`,
    },
    Select: {
      colorBgContainer: inputBg,
      colorBorder: inputBorderColor,
      colorText: inputColor,
      colorTextPlaceholder: inputPlaceholderColor,
      colorBgContainerDisabled: inputDisabledBg,
      colorTextDisabled: inputDisabledColor,
      colorPrimary: primaryColor,
      colorPrimaryHover: primaryColorHover,
      colorPrimaryActive: primaryColorActive,
      activeBorderColor: primaryColor,
      hoverBorderColor: primaryColor,
      activeShadow: `0 0 0 3px ${primaryColor}26`,
    },
    Table: {
      headerBg: 'rgba(255, 255, 255, 0.03)',
      headerColor: 'rgba(255, 255, 255, 0.4)',
      headerSortActiveBg: 'rgba(255, 255, 255, 0.06)',
      headerSortHoverBg: 'rgba(255, 255, 255, 0.05)',
      rowHoverBg: 'rgba(255, 255, 255, 0.03)',
      borderColor: 'rgba(255, 255, 255, 0.06)',
      colorBgContainer: 'transparent',
      cellPaddingBlock: 14,
      cellPaddingInline: 20,
      fontSize: 14,
      colorText: 'rgba(255, 255, 255, 0.8)',
      footerBg: 'transparent',
      footerColor: 'rgba(255, 255, 255, 0.35)',
    },
    Tag: {
      fontSize: 13,
      borderRadius: 6,
    },
    Upload: {
      colorFillAlter: '#ffffff03',
      colorBorder: '#ffffff1a',
      borderRadiusLG: 12,
      colorPrimaryHover: 'rgba(102, 119, 255, 0.4)',
      colorTextHeading: 'rgba(255, 255, 255, 0.45)',
      colorTextDescription: 'rgba(255, 255, 255, 0.3)',
      padding: '32px 20px',
      fontSizeLG: 14,
    },
  },
}

export default function ThemeProvider({ children }: { children: ReactNode }) {
  return <ConfigProvider theme={themeConfig}>{children}</ConfigProvider>
}
