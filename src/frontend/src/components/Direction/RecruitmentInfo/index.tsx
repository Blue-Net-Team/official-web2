'use client'

import { useRouter } from 'next/navigation'
import { RecruitmentInfo as RecruitmentInfoType, DirectionSlug } from '../types'

interface RecruitmentInfoProps {
  data: RecruitmentInfoType
  directionSlug: DirectionSlug
}

// 方向slug到报名页面direction参数的映射
const slugToDirectionParam: Record<DirectionSlug, string> = {
  cv: 'COMPUTER_VISION',
  embed: 'EMBEDDED',
  struct: 'STRUCTURAL_DESIGN',
}

export default function RecruitmentInfo({ data, directionSlug }: RecruitmentInfoProps) {
  const router = useRouter()

  const handleApply = () => {
    const directionParam = slugToDirectionParam[directionSlug]
    router.push(`/enroll?direction=${directionParam}`)
  }

  return (
    <section className="pt-20 px-[147px] pb-[120px] bg-black max-lg:pt-[60px] max-lg:px-20 max-lg:pb-[100px] max-md:pt-12 max-md:px-6 max-md:pb-[60px]">
      <h2 className="text-4xl font-semibold text-white m-0 mb-6 max-lg:text-[32px] max-md:text-2xl max-md:mb-5">
        加入我们
      </h2>
      <div className="bg-[linear-gradient(135deg,var(--theme-gradient-start),var(--theme-gradient-end))] rounded-3xl p-10 px-12 flex flex-col gap-5 max-lg:p-8 max-lg:px-10 max-lg:gap-4 max-md:p-[28px] max-md:px-6 max-md:rounded-2xl max-md:gap-4">
        <h3 className="text-[28px] font-semibold text-white m-0 max-lg:text-2xl max-md:text-xl">
          招新要求
        </h3>
        <ul className="list-none p-0 m-0 flex flex-col gap-2 max-md:gap-1">
          {data.requirements.map((req, index) => (
            <li
              key={index}
              className="text-base font-normal text-[#cccccc] leading-[1.8] pl-4 relative before:content-['•'] before:absolute before:left-0 before:text-white max-lg:text-[15px] max-md:text-sm"
            >
              {req}
            </li>
          ))}
        </ul>
        <button
          className="self-start bg-white text-black border-none rounded-[25px] px-8 h-[50px] text-base font-semibold cursor-pointer transition-all duration-200 mt-2 hover:-translate-y-0.5 hover:shadow-[0_4px_16px_rgba(255,255,255,0.2)] active:translate-y-0 max-lg:px-7 max-lg:h-[46px] max-lg:text-[15px] max-md:w-full max-md:h-11 max-md:text-sm max-md:rounded-[22px] max-md:px-0 max-md:self-center"
          onClick={handleApply}
        >
          立即申请
        </button>
      </div>
    </section>
  )
}
