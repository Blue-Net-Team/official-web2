/**
 * SmokedGlassTabs —— 烟色玻璃胶囊 Tab（全站统一 Tab 组件）
 *
 * 在动态光效背景上提供统一、可访问的 Tab 导航：
 * - API 与 antd Tabs 对齐（items / activeKey / onChange / children 均支持）
 * - 颜色由局部 ConfigProvider token 控制；材质/指示器几何由 CSS Module 承载
 * - 选中指示器为独立浓烟玻璃胶囊，在 Tab 间平移；文字高亮带颜色过渡
 *
 * 用法：
 * <SmokedGlassTabs
 *   items={[{ key: '1', label: '项目' }, ...]}
 *   activeKey={tab}
 *   onChange={setTab}
 * />
 *
 * @author IVEN-CN
 */
'use client'

import React, { useCallback, useEffect, useRef } from 'react'
import { ConfigProvider, Tabs, theme } from 'antd'
import type { TabsProps } from 'antd'
import styles from './SmokedGlassTabs.module.css'

export interface SmokedGlassTabsProps extends Omit<TabsProps, 'type' | 'size' | 'className'> {
  /** 主题色 token（如 '#6677ff'），驱动指示器边框/光晕/内高光（color-mix 派生） */
  accent?: string
  /** 浓淡档位：'deep'（默认）独立筛选条；'soft' 面板内嵌场景（如 Profile 内容区） */
  tone?: 'deep' | 'soft'
  className?: string
}

export default function SmokedGlassTabs({
  accent,
  tone = 'deep',
  className = '',
  ...rest
}: SmokedGlassTabsProps) {
  const rootRef = useRef<HTMLDivElement>(null)

  /** 量取当前激活 Tab 按钮的位置与宽度，写入 CSS 变量驱动指示器平移 */
  const updatePill = useCallback(() => {
    const root = rootRef.current
    if (!root) return
    const nav = root.querySelector('.ant-tabs-nav')
    const activeBtn = root.querySelector<HTMLElement>('.ant-tabs-tab-active .ant-tabs-tab-btn')
    if (!nav || !activeBtn) {
      root.style.setProperty('--sgt-pill-width', '0px')
      return
    }
    const navRect = nav.getBoundingClientRect()
    const btnRect = activeBtn.getBoundingClientRect()
    root.style.setProperty('--sgt-pill-left', `${btnRect.left - navRect.left}px`)
    root.style.setProperty('--sgt-pill-width', `${btnRect.width}px`)
  }, [])

  useEffect(() => {
    const root = rootRef.current
    const nav = root?.querySelector('.ant-tabs-nav')
    if (!root || !nav) return

    // 首帧布局完成后定位一次
    const raf = requestAnimationFrame(updatePill)

    // 激活态 class 变化（受控/非受控、键盘切换均覆盖）→ 重新定位
    const mo = new MutationObserver(updatePill)
    mo.observe(nav, { subtree: true, attributes: true, attributeFilter: ['class'] })

    // 容器尺寸变化（窗口缩放、字体加载、徽标数字位数变化）→ 重新定位
    const ro = new ResizeObserver(updatePill)
    ro.observe(nav)

    window.addEventListener('resize', updatePill)
    return () => {
      cancelAnimationFrame(raf)
      mo.disconnect()
      ro.disconnect()
      window.removeEventListener('resize', updatePill)
    }
  }, [updatePill])

  return (
    <ConfigProvider
      theme={{
        algorithm: theme.darkAlgorithm,
        components: {
          Tabs: {
            horizontalMargin: '0',
            inkBarColor: 'transparent',
            /* 颜色统一在此控制，避免与全局 token / CSS Module 互相覆盖 */
            itemColor: 'rgba(255, 255, 255, 0.55)',
            itemHoverColor: 'rgba(255, 255, 255, 0.9)',
            itemSelectedColor: '#ffffff',
            itemActiveColor: '#ffffff',
          },
        },
      }}
    >
      <div
        ref={rootRef}
        className={`${styles.tabs} ${tone === 'soft' ? styles.soft : ''} ${className}`}
        style={accent ? ({ '--sgt-accent': accent } as React.CSSProperties) : undefined}
      >
        <Tabs {...rest} />
      </div>
    </ConfigProvider>
  )
}
