'use client'

import { useState } from 'react'
import Image from 'next/image'
import { Tag } from 'antd'
import { CompetitionResponseDTO } from '@/apis/schema/type'
import { API_BASE_URL } from '@/apis/config'
import { COMPETITION_LEVEL_LABELS, COMPETITION_LEVEL_COLORS } from '@/types/competition'

interface CompetitionCardProps {
  competition: CompetitionResponseDTO
  index?: number
}

export default function CompetitionCard({ competition }: CompetitionCardProps) {
  const levelColor = COMPETITION_LEVEL_COLORS[competition.level]
  const levelLabel = COMPETITION_LEVEL_LABELS[competition.level]

  const hasCover = !!competition.coverFileId
  const [logoError, setLogoError] = useState(false)

  const coverUrl = hasCover ? `${API_BASE_URL}/file/download/${competition.coverFileId}` : null
  const logoUrl =
    competition.logoFileId && !logoError
      ? `${API_BASE_URL}/file/download/${competition.logoFileId}`
      : null

  return (
    <div
      className={
        'w-full glass-card md:min-h-[150px] rounded-3xl md:px-8 flex flex-col md:flex-row md:items-center relative overflow-hidden'
      }
    >
      {coverUrl && (
        <div
          className="z-[1] overflow-hidden
              w-full h-[140px] shrink-0 relative rounded-t-3xl
              md:absolute md:top-0 md:right-0 md:bottom-0 md:w-[70%] md:left-auto md:rounded-r-3xl md:rounded-t-none md:h-full
              md:[-webkit-mask-image:linear-gradient(to_right,transparent,black_75%)]
              md:[mask-image:linear-gradient(to_right,transparent,black_75%)]"
        >
          <Image src={coverUrl} alt="" fill className="object-cover" />
        </div>
      )}
      {/* 内容区域 */}
      <div
        className={`relative z-[2] flex flex-col gap-3 justify-center h-fit py-6 md:py-0 px-8 md:px-0`}
      >
        <div
          className="flex justify-between items-center gap-4
          max-md:flex-col max-md:items-start max-md:gap-3"
        >
          <div
            className="flex items-center gap-4 flex-1 min-w-0
            max-md:w-full max-md:justify-start max-md:gap-3"
          >
            {/* Logo */}
            {logoUrl && (
              <img
                src={logoUrl}
                alt={competition.name}
                className="h-[28px] w-auto object-contain shrink-0 max-md:h-5"
                onError={() => setLogoError(true)}
              />
            )}
            <h3
              className="text-[28px] font-bold text-white m-0 font-[Inter,sans-serif]
              max-md:text-xl"
            >
              {competition.name}
            </h3>
            <Tag
              color={levelColor}
              variant="outlined"
              className="!rounded-[14px] !px-3 !py-0.5 !text-xs !font-bold
                !shrink-0 font-[Inter,sans-serif] max-md:!rounded-xl"
            >
              {levelLabel}
            </Tag>
          </div>
          {competition.month && (
            <span
              className="text-2xl font-normal text-white/40 shrink-0 font-[Inter,sans-serif]
              max-md:text-[18px]"
            >
              {competition.month}
            </span>
          )}
        </div>

        <div className="flex flex-col gap-2">
          {competition.organizer && (
            <div className="flex items-center gap-2 flex-wrap max-md:gap-1">
              <span
                className="text-base font-normal text-white/60 shrink-0 font-[Inter,sans-serif]
                max-md:text-sm"
              >
                主办单位：
              </span>
              <span
                className="text-base font-normal text-white font-[Inter,sans-serif]
                max-md:text-sm"
              >
                {competition.organizer}
              </span>
            </div>
          )}
          <p
            className="text-base font-normal text-white/80 leading-[1.5] m-0 font-[Inter,sans-serif]
            max-md:text-sm"
          >
            {competition.summary}
          </p>
        </div>
      </div>
    </div>
  )
}
