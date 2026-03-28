'use client'

import { Flex } from 'antd'
import styles from './styles.module.css'
import Image from 'next/image'
import { useRouter } from 'next/navigation'

export interface DirectionCardProps {
  title: string
  desc: string
  cover: string
  icon: string
  linkTo: string
}

const DirectionCard = ({ title, desc, cover, icon, linkTo }: DirectionCardProps) => {
  const router = useRouter()

  return (
    <div className={styles.cardContainer}>
      <div className={styles.card} onClick={() => router.push(linkTo)}>
        <Image src={cover} width={250} height={150} alt={title} />

        <div className={styles.content}>
          <Image src={icon} width={32} height={32} alt={title} />

          <div className={styles.textWrapper}>
            <h3 className={styles.title}>{title}</h3>
            <span className={styles.desc}>{desc}</span>
          </div>
        </div>
      </div>
    </div>
  )
}

export default DirectionCard
