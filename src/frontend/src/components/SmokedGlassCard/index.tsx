'use client'

import type { CSSProperties, ReactNode } from 'react'
import styles from './SmokedGlassCard.module.css'

export interface SmokedGlassCardProps {
  /**
   * 浓淡档位：'deep'（默认）= 浓烟物体卡片，可交互；
   * 'soft' = 淡烟数据容器（面板），hover 无反馈
   */
  tone?: 'deep' | 'soft'
  /** 悬停浮起反馈，仅对 tone='deep' 生效（默认 false） */
  hoverable?: boolean
  /** 圆角大小（px），默认 16 */
  radius?: number
  /** 主题色 token（如 '#6677ff'），驱动边框/光晕/内高光（color-mix 派生） */
  accent?: string
  className?: string
  style?: CSSProperties
  children: ReactNode
}

/**
 * 烟色玻璃卡片 —— 全站唯一的玻璃质感卡片/面板组件。
 * 使用规范与材质原理见 SmokedGlassCard.module.css 头注释。
 */
export default function SmokedGlassCard({
  tone = 'deep',
  hoverable = false,
  radius = 16,
  accent,
  className = '',
  style,
  children,
}: SmokedGlassCardProps) {
  // 交互语义与 tone 绑定：可点击交互只属于卡片，容器/面板不提供 hover 反馈
  const interactive = hoverable && tone === 'deep'

  const glass =
    tone === 'soft'
      ? styles.soft
      : `${styles.smoked} ${interactive ? styles.smokedHoverable : ''} ${accent ? styles.accent : ''} ${interactive && accent ? styles.accentHoverable : ''}`

  const mergedStyle: CSSProperties = {
    borderRadius: radius,
    ...(accent ? ({ '--accent': accent } as CSSProperties) : {}),
    ...style,
  }

  return (
    <div className={`${glass} ${className}`} style={mergedStyle}>
      {children}
    </div>
  )
}
