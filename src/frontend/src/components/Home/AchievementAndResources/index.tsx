'use client'

import { Carousel } from 'antd'
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
  const sectionRef = useRef<HTMLDivElement>(null)
  const carouselRef = useRef<CarouselRef>(null)
  const [currentSlide, setCurrentSlide] = useState(0)

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth <= 768)
    }

    checkMobile()
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

  const totalSlides = content.length
  const currentSlideRef = useRef(currentSlide)
  const wheelDeltaRef = useRef(0)
  const wheelLockTimeoutRef = useRef<number | null>(null)

  const thumbSizePercent = 100 / totalSlides
  const thumbPositionPercent = currentSlide * thumbSizePercent

  useEffect(() => {
    currentSlideRef.current = currentSlide
  }, [currentSlide])

  useEffect(() => {
    const handleWheel = (event: WheelEvent) => {
      const section = sectionRef.current
      const carousel = carouselRef.current

      if (isMobile || !section || !carousel || event.deltaY === 0) {
        return
      }

      const rect = section.getBoundingClientRect()
      const viewportHeight = window.innerHeight || document.documentElement.clientHeight
      const direction = event.deltaY > 0 ? 1 : -1
      // 按滚动方向分别判断接管区间，避免从上下边缘刚露出一点时就劫持页面滚动。
      const isSectionPinned =
        direction > 0
          ? rect.top <= viewportHeight * 0.25 && rect.bottom >= viewportHeight * 0.65
          : rect.top <= viewportHeight * 0.1 && rect.bottom >= viewportHeight * 0.85

      if (!isSectionPinned) {
        wheelDeltaRef.current = 0
        return
      }

      const slide = currentSlideRef.current
      const canTurnNext = direction > 0 && slide < totalSlides - 1
      const canTurnPrev = direction < 0 && slide > 0

      if (!canTurnNext && !canTurnPrev) {
        wheelDeltaRef.current = 0
        return
      }

      event.preventDefault()

      if (wheelLockTimeoutRef.current) {
        return
      }

      // 将连续滚轮输入聚合为一次 Carousel 翻页，避免触控板一次滑动跳过多页。
      wheelDeltaRef.current += event.deltaY

      if (Math.abs(wheelDeltaRef.current) < 80) {
        return
      }

      wheelDeltaRef.current = 0

      if (direction > 0) {
        carousel.next()
      } else {
        carousel.prev()
      }

      wheelLockTimeoutRef.current = window.setTimeout(() => {
        wheelLockTimeoutRef.current = null
      }, 650)
    }

    window.addEventListener('wheel', handleWheel, { passive: false })

    return () => {
      window.removeEventListener('wheel', handleWheel)

      if (wheelLockTimeoutRef.current) {
        window.clearTimeout(wheelLockTimeoutRef.current)
      }
    }
  }, [isMobile, totalSlides])

  const handleSlideChange = (from: number, to: number) => {
    setCurrentSlide(to)
  }

  return (
    <div
      ref={sectionRef}
      className="flex flex-col gap-[28px] md:gap-[44px] h-fit md:h-[110vh] w-full box-border pl-5 pr-5 pt-5 pb-[30px] md:pl-[93px] md:pr-0 md:pt-[42px] md:pb-[80px]"
    >
      <h1 className="text-white text-[20px] md:text-[43px]">
        累计获奖超
        <span className="text-[#ff9a3c] text-[36px] md:text-[79px]">300</span>项
        <span className="hidden md:inline">，</span>
        <span className="block md:hidden"></span>拥有充足的实验室资源
      </h1>

      <div className="w-full flex flex-col-reverse md:flex-row items-center md:items-center gap-0">
        <div className="hidden md:block w-[5px] min-w-[5px] mr-[15px] h-full bg-[#153963] relative">
          <div
            className="absolute left-0 w-full bg-[rgba(255,111,60,0.6)] transition-[top_0.3s_ease] rounded-[2.5px]"
            style={{
              height: `${thumbSizePercent}%`,
              top: `${thumbPositionPercent}%`,
            }}
          />
        </div>
        <div className="block md:hidden w-full min-w-auto h-[5px] min-h-[5px] mr-0 mt-[15px] bg-[#153963] relative">
          <div
            className="absolute top-0 h-full bg-[rgba(255,111,60,0.6)] transition-[left_0.3s_ease] rounded-[2.5px]"
            style={{
              width: `${thumbSizePercent}%`,
              left: `${thumbPositionPercent}%`,
            }}
          />
        </div>
        <div className="p-0 md:pl-[15px] w-full md:flex-1 md:min-w-0">
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
                <div className="flex flex-col md:flex-row gap-[10px] h-auto md:h-[510px] min-w-0">
                  <div className="flex md:flex-1 md:h-full flex-col gap-[18px] md:gap-[26px]">
                    <h3 className="text-[18px] md:text-[35px] font-bold leading-[26px] md:leading-[46px] text-left">
                      {item.title}
                    </h3>
                    <p className="text-sm md:text-[20px] font-bold text-white h-[52px] md:h-auto">
                      {item.description}
                    </p>
                  </div>
                  <div className="relative flex-1 min-h-[250px] md:min-h-0 rounded-[20px] md:rounded-[20px_0_0_20px] overflow-hidden min-w-0">
                    <Image src={item.image} alt={item.title} fill className="object-cover" />
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
