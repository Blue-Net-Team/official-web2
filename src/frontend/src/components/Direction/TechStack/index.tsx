'use client'

import { TechItem } from '../types'
import { Row, Col } from 'antd'
import styles from './styles.module.css'

interface TechStackProps {
  data: TechItem[]
}

export default function TechStack({ data }: TechStackProps) {
  return (
    <section className={styles.section}>
      <h2 className={styles.title}>核心技术栈</h2>
      <Row gutter={[24, 24]} className={styles.grid}>
        {data.map((tech, index) => (
          <Col key={index} xs={24} sm={24} md={12} lg={6}>
            <div className={styles.card}>
              <h3 className={styles.techName}>{tech.name}</h3>
              <p className={styles.techDesc}>{tech.description}</p>
            </div>
          </Col>
        ))}
      </Row>
    </section>
  )
}
