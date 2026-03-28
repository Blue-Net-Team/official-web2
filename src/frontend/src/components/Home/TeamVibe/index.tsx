'use client'

import styles from './styles.module.css'
import teamVibeImage from '@/assets/team_vibe.jpg'

export default function TeamVibe() {
  return (
    <div className={styles.container}>
      <h2 className={styles.mainTitle}>重新定义团队氛围</h2>

      <div
        className={styles.contentCard}
        style={
          {
            '--team-vibe-bg': `url(${teamVibeImage.src})`,
          } as React.CSSProperties
        }
      >
        <div className={styles.textArea}>
          <h3 className={styles.subTitle}>队内氛围融洽，技术精湛</h3>
          <p className={styles.description}>
            团队氛围轻松融洽，弹性工作，无竞赛、论文等硬性指标。旨在培养学生学习更多新技术应用到工程实践
          </p>
          <p className={styles.description}>
            进入团队后，可以跟学长和老师学习行业前沿技术，共同实现项目落地，丰富简历内容
          </p>
        </div>
      </div>
    </div>
  )
}
