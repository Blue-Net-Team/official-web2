'use client'

import Image from 'next/image'
import { CareerCard as CareerCardType } from '../types'
import styles from './styles.module.css'

interface CareerSectionProps {
  data: CareerCardType[]
}

export default function CareerSection({ data }: CareerSectionProps) {
  return (
    <section className={styles.section}>
      <h2 className={styles.title}>职业发展方向</h2>
      <div className={styles.careerGrid}>
        {data.map((career, index) => (
          <div key={index} className={styles.careerCard}>
            <div className={styles.textContent}>
              <h3 className={styles.careerTitle}>{career.title}</h3>
              {career.details && career.details.length > 0 && (
                <ul className={styles.detailsList}>
                  {career.details.map((detail, detailIndex) => (
                    <li key={detailIndex} className={styles.detailItem}>
                      {detail}
                    </li>
                  ))}
                </ul>
              )}
            </div>
            {career.image && (
              <div className={styles.imageWrapper}>
                <Image
                  src={career.image}
                  alt={career.title}
                  fill
                  className={styles.careerImage}
                  sizes="(max-width: 768px) 100vw, 280px"
                />
              </div>
            )}
          </div>
        ))}
      </div>
    </section>
  )
}
