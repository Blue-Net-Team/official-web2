'use client'

import { Layout } from 'antd'
import Image from 'next/image'
import logoImage from '@/assets/logo.png'
import styles from './styles.module.css'

const { Header } = Layout

const AdminHeadBar = () => {
  return (
    <Header className={styles.header}>
      <div className={styles.logoContainer}>
        <div className={styles.logoWrapper}>
          <div className={styles.logoIconWrapper}>
            <Image src={logoImage} alt="bluenet logo" className={styles.logoImage} />
          </div>
          <div className={styles.logoText}>BLUENET ADMIN</div>
        </div>
      </div>
    </Header>
  )
}

export default AdminHeadBar
