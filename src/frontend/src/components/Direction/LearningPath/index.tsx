'use client'

import { Row, Col } from 'antd'
import { LearningStep } from '../types'
import styles from './styles.module.css'

interface LearningPathProps {
  data: LearningStep[]
}

export default function LearningPath({ data }: LearningPathProps) {
  const handleCardClick = (videoLink: string | undefined) => {
    if (videoLink) {
      window.open(videoLink, '_blank')
    }
  }

  return (
    <section className={styles.section}>
      <h2 className={styles.title}>学习路径</h2>
      <Row gutter={[16, 16]} align="middle" className={styles.pathContainer}>
        {data.map((step, index) => (
          <Col key={step.step} xs={24} lg={6}>
            <div className={styles.stepWrapper}>
              <div
                className={`${styles.stepCard} ${step.videoLink ? styles.clickable : ''}`}
                onClick={() => handleCardClick(step.videoLink)}
                role={step.videoLink ? 'button' : undefined}
                tabIndex={step.videoLink ? 0 : undefined}
              >
                <span className={styles.stepNumber}>{String(step.step).padStart(2, '0')}</span>
                <h3 className={styles.stepTitle}>{step.title}</h3>
                {step.videoLink && <span className={styles.videoHint}>点击观看视频</span>}
              </div>
              {/* 箭头 - 桌面端显示 */}
              {index < data.length - 1 && <span className={styles.arrow}>→</span>}
            </div>
          </Col>
        ))}
      </Row>
    </section>
  )
}
