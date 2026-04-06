'use client'

import { TechItem } from '../types'
import { Row, Col } from 'antd'

interface TechStackProps {
  data: TechItem[]
}

export default function TechStack({ data }: TechStackProps) {
  return (
    <section className="py-20 px-[147px] bg-black max-lg:py-[60px] max-lg:px-20 max-md:py-12 max-md:px-6">
      <h2 className="text-4xl font-semibold text-white m-0 mb-6 max-lg:text-[32px] max-md:text-2xl max-md:mb-5">
        核心技术栈
      </h2>
      <Row gutter={[24, 24]} className="mt-6 max-md:!gap-3">
        {data.map((tech, index) => (
          <Col key={index} xs={24} sm={24} md={12} lg={6}>
            <div className="bg-[#111111] border border-[var(--theme-primary)] rounded-2xl p-6 h-40 flex flex-col gap-3 transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_8px_32px_rgba(0,0,0,0.3)] max-lg:h-[150px] max-lg:p-5 max-md:h-auto max-md:min-h-[100px] max-md:p-4 max-md:rounded-xl max-md:gap-2">
              <h3 className="text-xl font-semibold text-[var(--theme-primary)] m-0 max-lg:text-lg max-md:text-base">
                {tech.name}
              </h3>
              <p className="text-sm text-[#999999] m-0 leading-normal max-md:text-xs">
                {tech.description}
              </p>
            </div>
          </Col>
        ))}
      </Row>
    </section>
  )
}
