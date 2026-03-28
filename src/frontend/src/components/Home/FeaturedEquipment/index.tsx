'use client'

import { Button } from 'antd'
import { RightOutlined } from '@ant-design/icons'
import { useRouter } from 'next/navigation'
import Image from 'next/image'
import styles from './styles.module.css'
import equipmentBg from '@/assets/equipment_bg.png'
import printerIcon from '@/assets/3dPrinter.png'

export default function FeaturedEquipment() {
  const router = useRouter()

  const handleBrowseMore = () => {
    router.push('/equipment')
  }

  return (
    <div className={styles.container}>
      <div
        className={styles.contentCard}
        style={
          {
            '--equipment-bg': `url(${equipmentBg.src})`,
          } as React.CSSProperties
        }
      >
        <div className={styles.textContent}>
          <div className={styles.iconWrapper}>
            <Image src={printerIcon} alt="3D打印" width={52} height={52} />
          </div>
          <h2 className={styles.title}>3D打印与3轴数铣</h2>
          <p className={styles.description}>
            团队配备先进3D打印机和3轴数控铣床，为机器人开发深度赋能，帮我团队开发者更高效搭建机器人
          </p>
          <Button type="default" className={styles.browseButton} onClick={handleBrowseMore}>
            浏览更多团队装备
            <RightOutlined />
          </Button>
        </div>
      </div>
    </div>
  )
}
