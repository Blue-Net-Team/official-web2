'use client'

import { DirectionData } from '../types'
import styles from './styles.module.css'

interface HeroSectionProps {
  data: DirectionData
}

export default function HeroSection({ data }: HeroSectionProps) {
  return (
    <section className={styles.heroSection}>
      {/* 背景装饰元素 */}
      <div className={styles.decorations}>
        {/* 渐变圆形 1 */}
        <div
          className={styles.bgCircle1}
          style={{
            background: `radial-gradient(circle, var(--theme-primary) 0%, transparent 100%)`,
          }}
        />
        {/* 渐变圆形 2 */}
        <div
          className={styles.bgCircle2}
          style={{
            background: `radial-gradient(circle, var(--theme-secondary) 0%, transparent 100%)`,
          }}
        />
        {/* 网格线 */}
        <div className={styles.gridLine1} style={{ backgroundColor: 'var(--theme-primary)' }} />
        <div className={styles.gridLine2} style={{ backgroundColor: 'var(--theme-secondary)' }} />
        <div className={styles.gridLine3} style={{ backgroundColor: 'var(--theme-primary)' }} />
        {/* 六边形 */}
        <svg className={styles.hexagon} viewBox="0 0 100 100">
          <polygon
            points="50,5 95,27.5 95,72.5 50,95 5,72.5 5,27.5"
            fill="none"
            stroke="var(--theme-secondary)"
            strokeWidth="2"
          />
        </svg>
        {/* 方形 */}
        <div className={styles.square} style={{ border: '2px solid var(--theme-primary)' }} />
        {/* 装饰点 */}
        <div className={styles.dotA} style={{ backgroundColor: 'var(--theme-secondary)' }} />
        <div className={styles.dotB} style={{ backgroundColor: 'var(--theme-primary)' }} />
        <div className={styles.dotC} style={{ backgroundColor: 'var(--theme-secondary)' }} />
      </div>

      {/* 内容 */}
      <div className={styles.content}>
        <h1 className={styles.title}>{data.title}</h1>
        <p className={styles.subtitle}>{data.subtitle}</p>
      </div>
    </section>
  )
}
