'use client'

import { Carousel } from 'antd'
import styles from './styles.module.css'
import c1 from '@/assets/c1.jpg'
import c2 from '@/assets/c2.jpg'
import c3 from '@/assets/c3.jpg'
import c4 from '@/assets/c4.png'
import { useEffect, useRef, useState } from 'react'
import type { CarouselRef } from 'antd/es/carousel'
import Image from 'next/image'

interface contentType {
  title: string
  description: string
  image: string
}

const AchievementAndResources = () => {
  const [isMobile, setIsMobile] = useState(false)

  // 监听窗口大小变化
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth <= 768) // 根据你的断点调整
    }

    checkMobile() // 初始化检查
    window.addEventListener('resize', checkMobile)
    return () => window.removeEventListener('resize', checkMobile)
  }, [])

  const content: contentType[] = [
    {
      title: '竞赛丰富，加入即参赛',
      description: '所有竞赛没有专业门槛，重要竞赛有学长、老师参与指导，奖项唾手可得',
      image: c1.src,
    },
    {
      title: '独特的先进装备',
      description: '实验室具有其他实验室不具备的先进装备，包括3D打印机、车床、铣床、激光切割机',
      image: c2.src,
    },
    {
      title: '开阔充足的办公区域',
      description:
        '实验室办公区域宽敞明亮，提供必要的工具，如焊刀、热风枪。办公室位置充足，可以轻松容纳团队成员在此办公',
      image: c3.src,
    },
    {
      title: '不限制专业报名',
      description:
        '实验室成员来自不同专业，从机械到计算机，都有成员加入，只要保持学习和技术热情，实验室都非常欢迎',
      image: c4.src,
    },
  ]

  const carouselRef = useRef<CarouselRef>(null)
  const [currentSlide, setCurrentSlide] = useState(0)
  const totalSlides = content.length

  // 计算滚动条的高度/宽度百分比
  const thumbSizePercent = 100 / totalSlides
  // 计算滚动条的位置
  const thumbPositionPercent = currentSlide * thumbSizePercent

  const handleSlideChange = (from: number, to: number) => {
    setCurrentSlide(to)
  }

  return (
    <div className={styles.content}>
      <h1 className={styles.title}>
        累计获奖超<span className={styles.bigTitle}>300</span>项
        <span className={styles.comma}>，</span>
        <span className={styles.lineBreak}></span>拥有充足的实验室资源
      </h1>

      {/* 左右布局，左边滚动条，右边内容 */}
      <div className={styles.carouselContainer}>
        {/* 滚动指示条 - 桌面端纵向 */}
        <div className={`${styles.scrollBar} ${styles.fullHeight} ${styles.desktopScrollBar}`}>
          <div
            className={styles.scrollThumb}
            style={{
              height: `${thumbSizePercent}%`,
              top: `${thumbPositionPercent}%`,
            }}
          />
        </div>
        {/* 滚动指示条 - 手机端横向 */}
        <div className={`${styles.scrollBar} ${styles.mobileScrollBar}`}>
          <div
            className={styles.scrollThumb}
            style={{
              width: `${thumbSizePercent}%`,
              left: `${thumbPositionPercent}%`,
            }}
          />
        </div>
        <div className={styles.carouselWrapper}>
          <Carousel
            ref={carouselRef}
            arrows={!isMobile}
            draggable={isMobile}
            dotPlacement={isMobile ? undefined : 'start'}
            infinite={false}
            dots={false}
            beforeChange={handleSlideChange}
          >
            {content.map((item, index) => (
              <div key={index}>
                <div className={styles.scrollContainer}>
                  <div className={styles.scrollText}>
                    <h3 className={styles.itemTitle}>{item.title}</h3>
                    <p className={styles.itemDescription}>{item.description}</p>
                  </div>
                  <div className={styles.imageContainer}>
                    <Image src={item.image} alt={item.title} fill style={{ objectFit: 'cover' }} />
                  </div>
                </div>
              </div>
            ))}
          </Carousel>
        </div>
      </div>
    </div>
  )
}

export default AchievementAndResources
