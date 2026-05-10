import { Suspense } from 'react'
import { Spin, Empty } from 'antd'
import Image from 'next/image'
import { VenueService } from '@/apis/services/venue.service'
import { EquipmentService } from '@/apis/services/equipment.service'
import type { VenueDTO, EquipmentDTO } from '@/apis/schema/type'
import { API_BASE_URL } from '@/apis/config'
import BackgroundDecorations from './BackgroundDecorations'

export const revalidate = 3600

function LoadingState() {
  return (
    <div className="flex justify-center items-center min-h-[200px] w-full">
      <Spin size="large" />
    </div>
  )
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className="flex justify-center items-center min-h-[200px] w-full">
      <Empty description={message} />
    </div>
  )
}

function VenueCard({ venue }: { venue: VenueDTO }) {
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

function EquipmentCard({ equipment }: { equipment: EquipmentDTO }) {
  const imageUrl = equipment.imageFileId
    ? `${API_BASE_URL}/file/download/${equipment.imageFileId}`
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

async function VenuesSection() {
  let venues: VenueDTO[] = []
  try {
    const response = await VenueService.getAllVenues()
    if (response.code === 200 && response.data) {
      venues = response.data
    }
  } catch {
    // 构建时 API 不可用，使用空数组
  }

  if (venues.length === 0) {
    return <EmptyState message="暂无场地数据" />
  }

  return (
    <div className="grid grid-cols-2 max-sm:grid-cols-1 gap-6 max-sm:gap-4 w-full">
      {venues.map((venue) => (
        <VenueCard key={venue.id} venue={venue} />
      ))}
    </div>
  )
}

async function EquipmentsSection() {
  let equipments: EquipmentDTO[] = []
  try {
    const response = await EquipmentService.getAllEquipments()
    if (response.code === 200 && response.data) {
      equipments = response.data
    }
  } catch {
    // 构建时 API 不可用，使用空数组
  }

  if (equipments.length === 0) {
    return <EmptyState message="暂无设备数据" />
  }

  return (
    <div className="grid grid-cols-3 max-sm:grid-cols-1 gap-6 max-sm:gap-4 w-full">
      {equipments.map((equipment) => (
        <EquipmentCard key={equipment.id} equipment={equipment} />
      ))}
    </div>
  )
}

export const metadata = {
  title: '实验室环境 - 蓝网团队',
  description: '专业的实验场地与先进的工程设备，为创新实践提供全方位支持',
}

export default function LabEnvironmentPage() {
  return (
    <div className="min-h-screen bg-[#0a0a0f] flex flex-col relative overflow-hidden">
      <BackgroundDecorations />

      <section className="flex flex-col items-center gap-4 py-[100px] px-[120px] max-xl:px-12 max-md:px-6 max-md:py-[60px] max-md:gap-3 relative z-1">
        <h1 className="text-[56px] max-md:text-[42px] max-sm:text-[32px] font-semibold text-white m-0 font-['Inter'] text-center">
          实验室环境
        </h1>
        <p className="text-xl max-md:text-lg max-sm:text-base font-normal text-[#a0a0b0] m-0 font-['Inter'] text-center max-w-[800px]">
          专业的实验场地与先进的工程设备，为创新实践提供全方位支持
        </p>
      </section>

      <section className="flex flex-col items-start gap-8 pt-[45px] pb-[60px] px-[120px] max-xl:px-12 max-md:px-6 max-md:pt-8 max-md:pb-12 max-md:gap-6 relative z-1 box-border w-full">
        <div className="flex flex-col gap-[23px] max-md:gap-4 items-start w-full">
          <h2 className="text-[36px] max-md:text-[30px] max-sm:text-2xl font-semibold text-white m-0 font-['Inter'] text-left w-full">
            我们的场地
          </h2>
          <p className="text-lg max-md:text-base max-sm:text-sm font-normal text-[#a0a0b0] m-0 font-['Inter'] text-left w-full">
            宽敞明亮的实验室环境，为创新实践提供理想空间
          </p>
        </div>
        <Suspense fallback={<LoadingState />}>
          <VenuesSection />
        </Suspense>
      </section>

      <section className="flex flex-col items-start gap-8 py-[60px] px-[120px] max-xl:px-12 max-md:px-6 max-md:py-12 max-md:gap-6 relative z-1 box-border w-full">
        <div className="flex flex-col gap-[23px] max-md:gap-4 items-start w-full">
          <h2 className="text-[36px] max-md:text-[30px] max-sm:text-2xl font-semibold text-white m-0 font-['Inter'] text-left w-full">
            我们的设备
          </h2>
          <p className="text-lg max-md:text-base max-sm:text-sm font-normal text-[#a0a0b0] m-0 font-['Inter'] text-left w-full">
            涵盖3D打印、嵌入式开发、电路设计等多领域的专业设备，满足各类创新项目需求
          </p>
        </div>
        <Suspense fallback={<LoadingState />}>
          <EquipmentsSection />
        </Suspense>
      </section>
    </div>
  )
}
