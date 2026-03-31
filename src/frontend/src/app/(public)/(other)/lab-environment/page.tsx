import { Suspense } from 'react'
import { Spin, Empty } from 'antd'
import Image from 'next/image'
import { VenueService } from '@/apis/services/venue.service'
import { EquipmentService } from '@/apis/services/equipment.service'
import type { VenueDTO, EquipmentDTO } from '@/apis/schema/type'
import { API_BASE_URL } from '@/apis/config'
import BackgroundDecorations from './BackgroundDecorations'
import styles from './page.module.css'

export const revalidate = 3600

function LoadingState() {
  return (
    <div className={styles.loadingContainer}>
      <Spin size="large" />
    </div>
  )
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className={styles.emptyContainer}>
      <Empty description={message} />
    </div>
  )
}

// 场地卡片组件
function VenueCard({ venue }: { venue: VenueDTO }) {
  const imageUrl = venue.imageFileId ? `${API_BASE_URL}/file/download/${venue.imageFileId}` : null

  return (
    <div className={styles.venueCard}>
      <div className={styles.venueImageWrapper}>
        {imageUrl ? (
          <Image src={imageUrl} alt={venue.name} fill className={styles.venueImage} />
        ) : (
          <div className={styles.venueImagePlaceholder} />
        )}
      </div>
      <div className={styles.venueContent}>
        <h3 className={styles.venueName}>{venue.name}</h3>
        {venue.subtitle && <p className={styles.venueSubtitle}>{venue.subtitle}</p>}
        {venue.description && <p className={styles.venueDescriptionText}>{venue.description}</p>}
      </div>
    </div>
  )
}

// 设备卡片组件
function EquipmentCard({ equipment }: { equipment: EquipmentDTO }) {
  const imageUrl = equipment.imageFileId
    ? `${API_BASE_URL}/file/download/${equipment.imageFileId}`
    : null

  return (
    <div className={styles.equipmentCard}>
      <div className={styles.equipmentImageWrapper}>
        {imageUrl ? (
          <Image src={imageUrl} alt={equipment.name} fill className={styles.equipmentImage} />
        ) : (
          <div className={styles.equipmentImagePlaceholder} />
        )}
      </div>
      <div className={styles.equipmentContent}>
        <h3 className={styles.equipmentName}>{equipment.name}</h3>
        {equipment.brand && <p className={styles.equipmentBrand}>{equipment.brand}</p>}
        {equipment.description && (
          <p className={styles.equipmentDescription}>{equipment.description}</p>
        )}
      </div>
    </div>
  )
}

// 场地列表组件
async function VenuesSection() {
  const response = await VenueService.getAllVenues()

  if (response.code !== 200 || !response.data || response.data.length === 0) {
    return <EmptyState message="暂无场地数据" />
  }

  const venues: VenueDTO[] = response.data

  return (
    <div className={styles.venueGrid}>
      {venues.map((venue) => (
        <VenueCard key={venue.id} venue={venue} />
      ))}
    </div>
  )
}

// 设备列表组件
async function EquipmentsSection() {
  const response = await EquipmentService.getAllEquipments()

  if (response.code !== 200 || !response.data || response.data.length === 0) {
    return <EmptyState message="暂无设备数据" />
  }

  const equipments: EquipmentDTO[] = response.data

  return (
    <div className={styles.equipmentGrid}>
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
    <div className={styles.pageContainer}>
      <BackgroundDecorations />

      {/* Hero 区域 - 固定文本 */}
      <section className={styles.heroSection}>
        <h1 className={styles.heroTitle}>实验室环境</h1>
        <p className={styles.heroSubtitle}>
          专业的实验场地与先进的工程设备，为创新实践提供全方位支持
        </p>
      </section>

      {/* 场地展示区 - 标题固定，卡片从后端获取 */}
      <section className={styles.venueSection}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>我们的场地</h2>
          <p className={styles.sectionDescription}>宽敞明亮的实验室环境，为创新实践提供理想空间</p>
        </div>
        <Suspense fallback={<LoadingState />}>
          <VenuesSection />
        </Suspense>
      </section>

      {/* 设备展示区 - 标题固定，卡片从后端获取 */}
      <section className={styles.equipmentSection}>
        <div className={styles.sectionHeader}>
          <h2 className={styles.sectionTitle}>我们的设备</h2>
          <p className={styles.sectionDescription}>
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
