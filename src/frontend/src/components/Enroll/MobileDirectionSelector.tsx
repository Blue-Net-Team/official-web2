'use client'

import React from 'react'
import Image from 'next/image'
import { Direction } from '@/apis/schema/type'
import { DIRECTIONS } from './constants'

interface MobileDirectionSelectorProps {
  selected: Direction
  onSelect: (direction: Direction) => void
}

const MobileDirectionSelector: React.FC<MobileDirectionSelectorProps> = ({
  selected,
  onSelect,
}) => {
  return (
    <div className="hidden max-lg:flex flex-col gap-[6px] animate-[slideIn_0.6s_cubic-bezier(0.4,0,0.2,1)_0.4s_both]">
      <label className="text-[13px] font-medium text-white/70 flex items-center gap-1">
        报名方向 <span className="text-[#ff6b35]">*</span>
      </label>
      <div className="grid grid-cols-3 max-sm:grid-cols-1 gap-3">
        {DIRECTIONS.map((dir) => (
          <label key={dir.key} className="cursor-pointer">
            <input
              type="radio"
              name="direction_mobile"
              value={dir.key}
              checked={selected === dir.key}
              onChange={() => onSelect(dir.key)}
              className="hidden"
            />
            <div
              className={`flex flex-col max-sm:flex-row items-center max-sm:justify-start gap-2 max-sm:gap-[14px] p-4 max-sm:p-[14px_16px] bg-white/[0.03] border rounded-xl transition-all ${
                selected === dir.key
                  ? dir.theme === 'computerVision'
                    ? 'border-[#6677ff] bg-gradient-to-br from-[rgba(102,119,255,0.15)] to-[rgba(47,39,176,0.1)] shadow-[0_0_20px_rgba(102,119,255,0.2)]'
                    : dir.theme === 'structuralDesign'
                      ? 'border-[#ff6b35] bg-gradient-to-br from-[rgba(255,107,53,0.15)] to-[rgba(255,140,66,0.1)] shadow-[0_0_20px_rgba(255,107,53,0.2)]'
                      : 'border-[#2ecc71] bg-gradient-to-br from-[rgba(46,204,113,0.15)] to-[rgba(39,174,96,0.1)] shadow-[0_0_20px_rgba(46,204,113,0.2)]'
                  : 'border-white/[0.08] hover:border-[rgba(102,119,255,0.3)] hover:bg-[rgba(102,119,255,0.05)]'
              }`}
            >
              <div
                className={`w-12 max-sm:w-11 h-12 max-sm:h-11 rounded-xl flex items-center justify-center overflow-hidden ${
                  dir.theme === 'computerVision'
                    ? 'bg-gradient-to-br from-[rgba(102,119,255,0.3)] to-[rgba(47,39,176,0.3)] shadow-[0_0_15px_rgba(102,119,255,0.3)]'
                    : dir.theme === 'structuralDesign'
                      ? 'bg-gradient-to-br from-[rgba(255,107,53,0.3)] to-[rgba(255,140,66,0.3)] shadow-[0_0_15px_rgba(255,107,53,0.3)]'
                      : 'bg-gradient-to-br from-[rgba(46,204,113,0.3)] to-[rgba(39,174,96,0.3)] shadow-[0_0_15px_rgba(46,204,113,0.3)]'
                }`}
              >
                <Image src={dir.icon} alt={dir.name} width={48} height={48} />
              </div>
              <span className="text-[13px] font-medium text-white/90">{dir.name}</span>
            </div>
          </label>
        ))}
      </div>
    </div>
  )
}

export default MobileDirectionSelector
