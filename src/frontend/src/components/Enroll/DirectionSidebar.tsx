'use client'

import React from 'react'
import Image from 'next/image'
import { Direction } from '@/apis/schema/type'
import { DIRECTIONS } from './constants'
import SmokedGlassCard from '@/components/SmokedGlassCard'
import styles from '@/app/(public)/(other)/enroll/styles.module.css'

/** 各方向主题色，驱动 SmokedGlassCard 的 accent 边框/光晕与 hover/选中文字高亮 */
const THEME_COLORS: Record<string, string> = {
  computerVision: '#6677ff',
  structuralDesign: '#ff6b35',
  embedded: '#2ecc71',
}

interface DirectionSidebarProps {
  selected: Direction
  onSelect: (direction: Direction) => void
}

const DirectionSidebar: React.FC<DirectionSidebarProps> = ({ selected, onSelect }) => {
  return (
    <aside className="flex flex-col gap-4 mt-0 animate-[fadeInLeft_0.8s_cubic-bezier(0.4,0,0.2,1)_0.2s_both]">
      <div className="text-sm font-semibold text-white/50 mb-2 pl-2 uppercase tracking-[2px] font-['Orbitron']">
        选择方向
      </div>
      {DIRECTIONS.map((dir) => (
        <div key={dir.key} className="cursor-pointer" onClick={() => onSelect(dir.key)}>
          <SmokedGlassCard
            hoverable
            accent={THEME_COLORS[dir.theme]}
            radius={16}
            className={`${styles.directionItem} ${selected === dir.key ? styles.selected : ''}`}
            style={{ '--sgc-padding': '18px' } as React.CSSProperties}
          >
            <div className="flex items-center gap-[14px]">
              <div
                className={`w-11 h-11 rounded-xl flex items-center justify-center shrink-0 transition-transform overflow-hidden hover:scale-110 hover:rotate-[5deg] ${
                  dir.theme === 'computerVision'
                    ? 'bg-gradient-to-br from-[rgba(102,119,255,0.3)] to-[rgba(47,39,176,0.3)] shadow-[0_0_20px_rgba(102,119,255,0.3)]'
                    : dir.theme === 'structuralDesign'
                      ? 'bg-gradient-to-br from-[rgba(255,107,53,0.3)] to-[rgba(255,140,66,0.3)] shadow-[0_0_20px_rgba(255,107,53,0.3)]'
                      : 'bg-gradient-to-br from-[rgba(46,204,113,0.3)] to-[rgba(39,174,96,0.3)] shadow-[0_0_20px_rgba(46,204,113,0.3)]'
                }`}
              >
                <Image src={dir.icon} alt={dir.name} width={44} height={44} />
              </div>
              <div className="flex flex-col gap-1">
                <span className={`${styles.directionName} text-[15px] font-semibold text-white/95`}>
                  {dir.name}
                </span>
                <span className="text-xs text-white/50">{dir.desc}</span>
              </div>
            </div>
          </SmokedGlassCard>
        </div>
      ))}
    </aside>
  )
}

export default DirectionSidebar
