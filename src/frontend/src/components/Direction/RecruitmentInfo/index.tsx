'use client'

import { useRouter } from 'next/navigation'
import { RecruitmentInfo as RecruitmentInfoType, DirectionSlug } from '../types'
import styles from './styles.module.css'

interface RecruitmentInfoProps {
  data: RecruitmentInfoType
  directionSlug: DirectionSlug
}

// 方向slug到报名页面direction参数的映射
const slugToDirectionParam: Record<DirectionSlug, string> = {
  cv: 'COMPUTER_VISION',
  embed: 'EMBEDDED',
  struct: 'STRUCTURAL_DESIGN',
}

export default function RecruitmentInfo({ data, directionSlug }: RecruitmentInfoProps) {
  const router = useRouter()

  const handleApply = () => {
    const directionParam = slugToDirectionParam[directionSlug]
    router.push(`/enroll?direction=${directionParam}`)
  }

  return (
    <section className={styles.section}>
      <h2 className={styles.title}>加入我们</h2>
      <div className={styles.card}>
        <h3 className={styles.cardTitle}>招新要求</h3>
        <ul className={styles.requirements}>
          {data.requirements.map((req, index) => (
            <li key={index}>{req}</li>
          ))}
        </ul>
        <button className={styles.applyButton} onClick={handleApply}>
          立即申请
        </button>
      </div>
    </section>
  )
}
