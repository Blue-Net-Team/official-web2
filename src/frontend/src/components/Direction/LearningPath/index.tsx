'use client'

import { Row, Col } from 'antd'
import { LearningStep } from '../types'

interface LearningPathProps {
  data: LearningStep[]
}

export default function LearningPath({ data }: LearningPathProps) {
  const handleCardClick = (relatedLink: string | null | undefined) => {
    if (relatedLink) {
      window.open(relatedLink, '_blank')
    }
  }

  return (
    <section className="py-20 px-[147px] bg-black max-lg:py-[60px] max-lg:px-20 max-md:py-12 max-md:px-6">
      <h2 className="text-4xl font-semibold text-white m-0 mb-6 max-lg:text-[32px] max-md:text-2xl max-md:mb-5">
        学习路径
      </h2>
      <Row gutter={[16, 16]} align="middle" className="mt-6 max-[991px]:!gap-3">
        {data.map((step, index) => (
          <Col key={step.id} xs={24} lg={6}>
            <div className="flex items-center gap-4 max-[991px]:flex-col max-[991px]:items-stretch max-[991px]:gap-3">
              <div
                className={`flex-1 bg-[#111111] rounded-xl p-8 px-6 flex flex-col gap-2 border border-[#222222] transition-colors duration-300 hover:border-[var(--theme-primary)] max-lg:p-6 max-lg:px-5 max-[991px]:flex-row max-[991px]:items-center max-[991px]:gap-4 max-[991px]:p-5 max-[991px]:px-4 ${step.relatedLink ? 'cursor-pointer' : ''}`}
                onClick={() => handleCardClick(step.relatedLink)}
                role={step.relatedLink ? 'button' : undefined}
                tabIndex={step.relatedLink ? 0 : undefined}
              >
                <span className="text-[32px] font-bold text-[var(--theme-primary)] leading-none max-lg:text-[28px] max-[991px]:text-2xl max-[991px]:shrink-0">
                  {String(step.stepNumber).padStart(2, '0')}
                </span>
                <h3 className="text-base font-medium text-white m-0 max-[991px]:text-sm max-[991px]:flex-1">
                  {step.title}
                </h3>
                {step.relatedLink && (
                  <span className="text-xs text-[var(--theme-primary)] opacity-80 max-[991px]:text-[11px] max-[991px]:shrink-0">
                    查看相关资料
                  </span>
                )}
              </div>
              {/* 箭头 - 桌面端显示 */}
              {index < data.length - 1 && (
                <span className="text-2xl font-bold text-[var(--theme-primary)] shrink-0 max-[991px]:hidden">
                  →
                </span>
              )}
            </div>
          </Col>
        ))}
      </Row>
    </section>
  )
}
