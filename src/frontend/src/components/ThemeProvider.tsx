'use client'

import { ConfigProvider, theme } from 'antd'
import { ReactNode } from 'react'

// ============================================================
// 主题常量定义
// 所有 Ant Design 组件样式应优先在此配置，避免内联 style
// 优先级：ThemeProvider > 组件级 theme 属性 > Tailwind className > style
// ============================================================

const primaryColor = '#fa8c16'
const primaryColorHover = '#ffa940'
const primaryColorActive = '#d46b08'
const darkItemBg = '#19191c'

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
    colorText: 'rgba(255, 255, 255, 0.8)',
    colorTextSecondary: 'rgba(255, 255, 255, 0.6)',
    colorTextTertiary: 'rgba(255, 255, 255, 0.45)',
    colorTextQuaternary: 'rgba(255, 255, 255, 0.25)',
    colorBorder: 'rgba(255, 255, 255, 0.08)',
    colorBgContainer: 'transparent',
  },
  components: {
    // 导航
    Menu: {
      darkItemBg: darkItemBg,
      darkItemColor: 'rgba(140, 140, 141, 1)',
      darkItemHoverColor: '#ffffff',
      darkItemSelectedColor: primaryColor,
      activeBarBorderWidth: 0,
      darkItemSelectedBg: 'transparent',
      darkSubMenuItemBg: darkItemBg,
    },
    // 布局
    Layout: {
      headerBg: 'rgba(25, 25, 28, 1)',
      bodyBg: '#000000',
      footerBg: 'rgba(35, 35, 38, 1)',
      triggerBg: darkItemBg,
      siderBg: darkItemBg,
    },
    // 按钮
    Button: {
      colorPrimary: primaryColor,
      colorPrimaryHover: primaryColorHover,
      colorPrimaryActive: primaryColorActive,
      primaryShadow: `0 4px 20px ${primaryColor}40`,
    },
    // 标签页
    Tabs: {
      inkBarColor: primaryColor,
      itemActiveColor: primaryColor,
      itemHoverColor: primaryColorHover,
      itemSelectedColor: primaryColor,
    },
    // 输入框
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
    // 选择器
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
    // 表格
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
      fontSizeLG: 14,
    },
    Tree: {
      colorBgContainer: 'rgba(255, 255, 255, 0.02)',
      nodeHoverBg: 'rgba(255, 255, 255, 0.06)',
      nodeSelectedBg: `rgba(250, 140, 22, 0.1)`,
      directoryNodeSelectedBg: `rgba(250, 140, 22, 0.1)`,
      directoryNodeSelectedColor: primaryColor,
      colorText: 'rgba(255, 255, 255, 0.8)',
      titleHeight: 32,
      indentSize: 20,
    },
    // 分页
    Pagination: {
      colorPrimary: primaryColor,
      colorPrimaryHover: primaryColorHover,
      colorText: 'rgba(255, 255, 255, 0.6)',
      colorTextDisabled: 'rgba(255, 255, 255, 0.25)',
      colorBgTextHover: 'rgba(255, 255, 255, 0.05)',
      colorBgTextActive: 'rgba(255, 255, 255, 0.08)',
    },
    // 卡片
    Card: {
      colorBgContainer: 'rgba(255, 255, 255, 0.03)',
      colorBorderSecondary: 'rgba(255, 255, 255, 0.08)',
      colorTextHeading: 'rgba(255, 255, 255, 0.85)',
    },
    // 表单
    Form: {
      colorText: 'rgba(255, 255, 255, 0.8)',
      colorTextHeading: 'rgba(255, 255, 255, 0.85)',
      colorTextSecondary: 'rgba(255, 255, 255, 0.6)',
    },
    // 模态框
    Modal: {
      colorBgElevated: '#1a1a1f',
      colorText: 'rgba(255, 255, 255, 0.8)',
      colorTextHeading: 'rgba(255, 255, 255, 0.85)',
      colorIcon: 'rgba(255, 255, 255, 0.45)',
      colorIconHover: 'rgba(255, 255, 255, 0.85)',
    },
    // 抽屉
    Drawer: {
      colorBgElevated: '#1a1a1f',
      colorText: 'rgba(255, 255, 255, 0.8)',
      colorTextHeading: 'rgba(255, 255, 255, 0.85)',
      colorIcon: 'rgba(255, 255, 255, 0.45)',
      colorIconHover: 'rgba(255, 255, 255, 0.85)',
    },
    // 加载
    Spin: {
      colorPrimary: primaryColor,
    },
    // 徽标
    Badge: {
      colorText: 'rgba(255, 255, 255, 0.8)',
    },
    // 描述列表
    Descriptions: {
      colorText: 'rgba(255, 255, 255, 0.8)',
      colorTextSecondary: 'rgba(255, 255, 255, 0.6)',
      colorSplit: 'rgba(255, 255, 255, 0.08)',
    },
    // 单选框
    Radio: {
      colorPrimary: primaryColor,
      colorText: 'rgba(255, 255, 255, 0.8)',
    },
    // 复选框
    Checkbox: {
      colorPrimary: primaryColor,
      colorText: 'rgba(255, 255, 255, 0.8)',
    },
    // 结果页
    Result: {
      colorText: 'rgba(255, 255, 255, 0.8)',
      colorTextHeading: 'rgba(255, 255, 255, 0.85)',
      colorTextSecondary: 'rgba(255, 255, 255, 0.6)',
    },
    // 空状态
    Empty: {
      colorText: 'rgba(255, 255, 255, 0.45)',
      colorTextDisabled: 'rgba(255, 255, 255, 0.25)',
    },
    // 气泡确认
    Popconfirm: {
      colorText: 'rgba(255, 255, 255, 0.8)',
      colorTextHeading: 'rgba(255, 255, 255, 0.85)',
    },
    // 警告提示
    Alert: {
      colorText: 'rgba(255, 255, 255, 0.8)',
      colorTextHeading: 'rgba(255, 255, 255, 0.85)',
    },
    // 下拉菜单
    Dropdown: {
      colorBgElevated: '#1a1a1f',
      colorText: 'rgba(255, 255, 255, 0.8)',
      controlItemBgHover: 'rgba(255, 255, 255, 0.06)',
      controlItemBgActive: 'rgba(250, 140, 22, 0.1)',
      controlItemBgActiveHover: 'rgba(250, 140, 22, 0.15)',
    },
    // 时间轴
    Timeline: {
      colorText: 'rgba(255, 255, 255, 0.8)',
      colorTextSecondary: 'rgba(255, 255, 255, 0.6)',
    },
    // 步进器
    Steps: {
      colorPrimary: primaryColor,
      colorText: 'rgba(255, 255, 255, 0.8)',
      colorTextSecondary: 'rgba(255, 255, 255, 0.6)',
      colorTextDisabled: 'rgba(255, 255, 255, 0.3)',
    },
    // 开关
    Switch: {
      colorPrimary: primaryColor,
      colorPrimaryHover: primaryColorHover,
    },
  },
}

export default function ThemeProvider({ children }: { children: ReactNode }) {
  return <ConfigProvider theme={themeConfig}>{children}</ConfigProvider>
}
