'use client'

import Image from 'next/image'
import { useRouter } from 'next/navigation'

export interface DirectionCardProps {
  title: string
  desc: string
  cover: string
  icon: string
  linkTo: string
}

const DirectionCard = ({ title, desc, cover, icon, linkTo }: DirectionCardProps) => {
  const router = useRouter()

  return (
    <div className="w-full h-full flex flex-col justify-center items-center [perspective:1000px]">
      <div
        onClick={() => router.push(linkTo)}
        className="w-fit h-full flex flex-col justify-start items-start rounded-[32px] bg-[rgba(11,8,41,0.56)] overflow-hidden border-2 border-transparent transition-all duration-[400ms] [cubic-bezier(0.23,1,0.32,1)] cursor-pointer [transform-style:preserve-3d] [perspective:1000px] hover:border-[rgba(102,119,255,0.8)] hover:bg-[rgba(20,16,70,0.75)] hover:-translate-y-2 hover:[transform:translateY(-8px)_rotateZ(-2deg)] hover:[box-shadow:0_20px_40px_rgba(102,119,255,0.25),0_0_60px_rgba(102,119,255,0.15),inset_0_1px_0_rgba(255,255,255,0.1)] group"
      >
        <Image
          src={cover}
          width={250}
          height={150}
          alt={title}
          className="transition-all duration-[400ms] [cubic-bezier(0.23,1,0.32,1)] group-hover:scale-[1.02] group-hover:brightness-110"
        />

        <div className="w-full h-full flex flex-row justify-start items-start gap-[15px] p-2 px-3 pb-4">
          <Image
            src={icon}
            width={32}
            height={32}
            alt={title}
            className="transition-all duration-[400ms] [cubic-bezier(0.23,1,0.32,1)] group-hover:scale-110 group-hover:[filter:drop-shadow(0_0_10px_rgba(102,119,255,0.6))]"
          />

          <div className="flex flex-col">
            <h3 className="font-[Microsoft_YaHei] text-[20px] font-normal leading-[26px] text-left transition-all duration-300 group-hover:text-[rgba(140,155,255,1)] group-hover:[text-shadow:0_0_20px_rgba(102,119,255,0.5)]">
              {title}
            </h3>
            <span className="text-white font-[Microsoft_YaHei] text-[16px] font-normal leading-[21px] text-left transition-all duration-300 group-hover:text-[rgba(255,255,255,0.95)]">
              {desc}
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}

export default DirectionCard
