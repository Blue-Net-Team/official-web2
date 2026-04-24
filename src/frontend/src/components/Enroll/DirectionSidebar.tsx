'use client'

import React from 'react'
import Image from 'next/image'
import { Direction } from '@/apis/schema/type'
import { DIRECTIONS } from './constants'
import styles from '@/app/(public)/(other)/enroll/styles.module.css'

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
        <div
          key={dir.key}
          className={`${styles.directionItem} flex items-center gap-[14px] p-[18px] bg-[rgba(20,20,30,0.6)] border border-white/[0.08] rounded-2xl cursor-pointer transition-all duration-[400ms] cubic-bezier(0.4,0,0.2,1) relative overflow-hidden hover:translate-x-2 hover:border-[rgba(102,119,255,0.3)] hover:shadow-[0_0_30px_rgba(102,119,255,0.2)] ${
            selected === dir.key
              ? dir.theme === 'computerVision'
                ? 'border-[#6677ff] bg-gradient-to-br from-[rgba(102,119,255,0.15)] to-[rgba(47,39,176,0.1)] shadow-[0_0_30px_rgba(102,119,255,0.3),inset_0_0_20px_rgba(102,119,255,0.1)]'
                : dir.theme === 'structuralDesign'
                  ? 'border-[#ff6b35] bg-gradient-to-br from-[rgba(255,107,53,0.15)] to-[rgba(255,140,66,0.1)] shadow-[0_0_30px_rgba(255,107,53,0.3),inset_0_0_20px_rgba(255,107,53,0.1)]'
                  : 'border-[#2ecc71] bg-gradient-to-br from-[rgba(46,204,113,0.15)] to-[rgba(39,174,96,0.1)] shadow-[0_0_30px_rgba(46,204,113,0.3),inset_0_0_20px_rgba(46,204,113,0.1)]'
              : ''
          }`}
          onClick={() => onSelect(dir.key)}
        >
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
            <span className="text-[15px] font-semibold text-white/95">{dir.name}</span>
            <span className="text-xs text-white/50">{dir.desc}</span>
          </div>
        </div>
      ))}
    </aside>
  )
}

export default DirectionSidebar
