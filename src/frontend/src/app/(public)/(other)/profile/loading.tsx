import { Spin } from 'antd'
import styles from './styles.module.css'

export default function ProfileLoading() {
  return (
    <div className={styles.pageContainer}>
      <div className={styles.pageBg} />
      <div className={styles.loading}>
        <Spin size="large" />
      </div>
    </div>
  )
}
