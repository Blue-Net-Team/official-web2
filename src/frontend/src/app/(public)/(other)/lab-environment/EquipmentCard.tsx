'use client'

import Image from 'next/image'
import { EquipmentDTO } from '@/apis/schema/type'
import { PUBLIC_API_BASE_URL } from '@/apis/config'

export function EquipmentCard({ equipment }: { equipment: EquipmentDTO }) {
  const imageUrl = equipment.imageFileId
    ? `${PUBLIC_API_BASE_URL}/file/download/${equipment.imageFileId}`
    : null

  return (
    <div className="bg-white/[0.05] backdrop-blur-xl border border-white/10 rounded-2xl overflow-hidden shadow-[0_4px_24px_rgba(0,0,0,0.25)] transition-all hover:-translate-y-1 hover:shadow-[0_8px_32px_rgba(0,0,0,0.35)] hover:bg-white/[0.08]">
      <div className="relative w-full h-[200px] max-sm:h-[160px] overflow-hidden">
        {imageUrl ? (
          <Image src={imageUrl} alt={equipment.name} fill className="object-cover" />
        ) : (
          <div className="w-full h-full bg-gradient-to-br from-[rgba(232,104,53,0.2)] to-[rgba(74,144,226,0.2)]" />
        )}
      </div>
      <div className="p-6 max-sm:p-4 flex flex-col gap-3">
        <h3 className="text-xl max-sm:text-lg font-semibold text-white m-0 font-['Inter']">
          {equipment.name}
        </h3>
        {equipment.brand && (
          <p className="text-sm font-normal text-[#4a9eff] m-0 font-['Inter']">{equipment.brand}</p>
        )}
        {equipment.description && (
          <p className="text-sm font-normal text-[#a0a0b0] m-0 font-['Inter'] leading-relaxed">
            {equipment.description}
          </p>
        )}
      </div>
    </div>
  )
}
