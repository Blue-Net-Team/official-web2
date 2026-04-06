'use client'

import Image from 'next/image'
import { CareerCard as CareerCardType } from '../types'

interface CareerSectionProps {
  data: CareerCardType[]
}

export default function CareerSection({ data }: CareerSectionProps) {
  return (
    <section className="py-20 px-[147px] bg-black max-lg:py-[60px] max-lg:px-20 max-md:py-12 max-md:px-6">
      <h2 className="text-4xl font-semibold text-white m-0 mb-8 max-lg:text-[32px] max-md:text-2xl max-md:mb-5">
        职业发展方向
      </h2>
      <div className="flex flex-col gap-6 max-md:gap-4">
        {data.map((career, index) => (
          <div
            key={index}
            className="flex items-stretch justify-between bg-[#111111] border border-[var(--theme-primary)] rounded-2xl overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_8px_32px_rgba(0,0,0,0.3)] min-h-[180px] max-md:flex-col max-md:min-h-0"
          >
            <div className="flex-1 p-8 px-6 flex flex-col gap-3 max-w-[400px] max-lg:p-6 max-lg:px-5 max-lg:max-w-[300px] max-md:max-w-full max-md:p-5 max-md:gap-2">
              <h3 className="text-xl font-semibold text-[var(--theme-primary)] m-0 max-lg:text-lg max-md:text-base">
                {career.title}
              </h3>
              {career.details && career.details.length > 0 && (
                <ul className="list-none p-0 m-0 flex flex-col gap-2">
                  {career.details.map((detail, detailIndex) => (
                    <li key={detailIndex} className="text-sm font-medium text-white m-0">
                      {detail}
                    </li>
                  ))}
                </ul>
              )}
            </div>
            {career.image && (
              <div className="flex-1 max-w-[50%] min-h-[180px] shrink-0 relative overflow-hidden before:content-[''] before:absolute before:inset-0 before:z-[1] before:pointer-events-none before:bg-[linear-gradient(to_right,#111111_0%,transparent_50%)] max-md:before:bg-[linear-gradient(to_top,#111111_0%,transparent_50%)] max-lg:w-[200px] max-lg:min-h-[140px] max-md:max-w-full max-md:w-full max-md:h-[160px] max-md:order-[-1] max-md:min-h-[160px]">
                <Image
                  src={career.image}
                  alt={career.title}
                  fill
                  className="object-cover object-center"
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
