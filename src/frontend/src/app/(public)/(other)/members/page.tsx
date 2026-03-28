import { Members } from '@/components/Members'
import styles from './styles.module.css'

export default function MembersPage() {
  return (
    <div className={styles.pageContainer}>
      <div className={styles.pageBg} />
      <main className={styles.mainContent}>
        <section className={styles.pageHeader}>
          <h1 className={styles.pageTitle}>团队成员</h1>
          <p className={styles.pageSubtitle}>汇聚各方向的技术精英，共同推动科技创新与发展</p>
        </section>
        <Members />
      </main>
    </div>
  )
}
