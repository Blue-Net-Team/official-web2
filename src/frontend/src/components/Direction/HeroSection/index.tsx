'use client'

import { DirectionData } from '../types'

interface HeroSectionProps {
  data: DirectionData
}

export default function HeroSection({ data }: HeroSectionProps) {
  return (
    <section className="relative w-full h-[600px] overflow-hidden flex items-center px-[147px] max-lg:px-20 max-lg:h-[500px] max-md:px-6 max-md:h-[480px]">
      {/* 背景装饰元素 */}
      <div className="absolute inset-0 pointer-events-none overflow-hidden">
        {/* 渐变圆形 1 */}
        <div
          className="absolute w-[500px] h-[500px] right-[50px] -top-[100px] opacity-25 rounded-full blur-[60px] max-lg:w-[400px] max-lg:h-[400px] max-md:w-[300px] max-md:h-[300px] max-md:-right-[30px] max-md:-top-[30px]"
          style={{
            background: `radial-gradient(circle, var(--theme-primary) 0%, transparent 100%)`,
          }}
        />
        {/* 渐变圆形 2 */}
        <div
          className="absolute w-[350px] h-[350px] -left-[100px] bottom-[50px] opacity-20 rounded-full blur-[60px] max-lg:w-[300px] max-lg:h-[300px] max-md:w-[200px] max-md:h-[200px] max-md:-left-[50px] max-md:bottom-[80px]"
          style={{
            background: `radial-gradient(circle, var(--theme-secondary) 0%, transparent 100%)`,
          }}
        />
        {/* 网格线 */}
        <div
          className="absolute w-px h-[500px] right-[200px] top-[50px] opacity-20 max-md:hidden"
          style={{ backgroundColor: 'var(--theme-primary)' }}
        />
        <div
          className="absolute w-px h-[520px] right-[150px] top-[30px] opacity-15 max-md:hidden"
          style={{ backgroundColor: 'var(--theme-secondary)' }}
        />
        <div
          className="absolute w-px h-[480px] right-[100px] top-[70px] opacity-10 max-md:hidden"
          style={{ backgroundColor: 'var(--theme-primary)' }}
        />
        {/* 六边形 */}
        <svg
          className="absolute w-20 h-20 right-[150px] bottom-[100px] opacity-40 rotate-[15deg] max-md:w-[50px] max-md:h-[50px] max-md:right-[60px] max-md:bottom-[100px]"
          viewBox="0 0 100 100"
        >
          <polygon
            points="50,5 95,27.5 95,72.5 50,95 5,72.5 5,27.5"
            fill="none"
            stroke="var(--theme-secondary)"
            strokeWidth="2"
          />
        </svg>
        {/* 方形 */}
        <div
          className="absolute w-[50px] h-[50px] left-[120px] bottom-[80px] opacity-30 rotate-45 max-md:w-[30px] max-md:h-[30px] max-md:left-[30px] max-md:bottom-[120px]"
          style={{ border: '2px solid var(--theme-primary)' }}
        />
        {/* 装饰点 */}
        <div
          className="absolute w-2.5 h-2.5 right-[50px] top-[150px] rounded-full opacity-50 max-md:w-1.5 max-md:h-1.5 max-md:right-[80px] max-md:top-[100px]"
          style={{ backgroundColor: 'var(--theme-secondary)' }}
        />
        <div
          className="absolute w-2 h-2 right-[20px] top-[300px] rounded-full opacity-40 max-md:w-[5px] max-md:h-[5px] max-md:right-[60px] max-md:top-[200px]"
          style={{ backgroundColor: 'var(--theme-primary)' }}
        />
        <div
          className="absolute w-1.5 h-1.5 right-[100px] top-[250px] rounded-full opacity-60 max-md:hidden"
          style={{ backgroundColor: 'var(--theme-secondary)' }}
        />
      </div>

      {/* 内容 */}
      <div className="relative z-[1] max-w-[800px] flex flex-col gap-4 max-md:max-w-[327px] max-md:gap-3">
        <h1 className="text-[64px] font-bold leading-[1.2] text-white m-0 max-lg:text-5xl max-md:text-3xl">
          {data.title}
        </h1>
        <p className="text-2xl font-normal leading-[1.6] text-[#999999] m-0 max-w-[800px] max-lg:text-xl max-md:text-sm">
          {data.subtitle}
        </p>
      </div>
    </section>
  )
}
