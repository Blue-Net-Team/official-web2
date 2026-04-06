import Image from 'next/image'

interface ProcessCardProps {
  icon: string
  title: string
  description: string
  btn?: React.ReactNode
}

const ProcessCard = ({ icon, title, description, btn }: ProcessCardProps) => {
  return (
    <div className="w-full max-w-[80%] h-fit lg:min-h-[320px] flex flex-col justify-start items-start gap-[9px] p-[32px_28px] box-border border border-[rgba(232,104,53,1)] rounded-card">
      <div className="flex flex-row items-center gap-[10px] sm:gap-[13px] mb-[9px]">
        <Image
          src={icon}
          alt={title}
          className="w-6 h-6 sm:w-7 sm:h-7 md:w-8 md:h-8 object-contain"
          width={32}
          height={32}
        />
        <span className="font-[Microsoft_YaHei] text-base sm:text-lg md:text-xl font-bold text-white leading-[1.3]">
          {title}
        </span>
      </div>
      <p className="w-full h-fit text-white font-[Microsoft_YaHei] text-[13px] sm:text-sm md:text-base font-normal leading-[17px] sm:leading-[19px] md:leading-[21px] text-left m-0">
        {description}
      </p>
      {btn}
    </div>
  )
}

export default ProcessCard
