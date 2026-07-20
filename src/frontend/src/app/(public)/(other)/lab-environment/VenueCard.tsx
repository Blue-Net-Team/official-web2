'use client'

import Image from 'next/image'
import { VenueDTO } from '@/apis/schema/type'
import { API_BASE_URL } from '@/apis/config'

export function VenueCard({ venue }: { venue: VenueDTO }) {
  const imageUrl = venue.imageFileId ? `${API_BASE_URL}/file/download/${venue.imageFileId}` : null

  return (
    <div className="bg-white/[0.05] backdrop-blur-xl border border-white/10 rounded-2xl overflow-hidden shadow-[0_4px_24px_rgba(0,0,0,0.25)] transition-all hover:-translate-y-1 hover:shadow-[0_8px_32px_rgba(0,0,0,0.35)] hover:bg-white/[0.08]">
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
    </div>
  )
}
