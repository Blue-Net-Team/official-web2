'use client'

import { CSSProperties } from 'react'
import Image from 'next/image'
import { VenueDTO } from '@/apis/schema/type'
import { PUBLIC_API_BASE_URL } from '@/apis/config'
import SmokedGlassCard from '@/components/SmokedGlassCard'

export function VenueCard({ venue }: { venue: VenueDTO }) {
  const imageUrl = venue.imageFileId
    ? `${PUBLIC_API_BASE_URL}/file/download/${venue.imageFileId}`
    : null

  return (
    <SmokedGlassCard
      hoverable
      radius={16}
      className="overflow-hidden"
      style={{ '--sgc-padding': '0' } as CSSProperties}
    >
      <div className="relative w-full h-[280px] max-sm:h-[200px] overflow-hidden">
        {imageUrl ? (
          <Image
            src={imageUrl}
            alt={venue.name}
            fill
            sizes="(max-width: 640px) 100vw, 50vw"
            className="absolute inset-0 w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full bg-gradient-to-br from-[rgba(74,144,226,0.2)] to-[rgba(232,104,53,0.2)]" />
        )}
      </div>
      <div className="p-6 max-sm:p-4 flex flex-col gap-3">
        <h3 className="text-xl max-sm:text-lg font-semibold text-white m-0 font-['Inter']">
          {venue.name}
        </h3>
        {venue.subtitle && (
          <p className="text-sm font-normal text-[#4a9eff] m-0 font-['Inter']">{venue.subtitle}</p>
        )}
        {venue.description && (
          <p className="text-sm font-normal text-[#a0a0b0] m-0 font-['Inter'] leading-relaxed">
            {venue.description}
          </p>
        )}
      </div>
    </SmokedGlassCard>
  )
}
