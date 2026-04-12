import { CompetitionBriefDTO } from '@/apis/schema/type'
import { API_BASE_URL } from '@/apis/config'
import { COMPETITION_LEVEL_LABELS, COMPETITION_LEVEL_COLORS } from '@/types/competition'

interface CompetitionCardProps {
  competition: CompetitionBriefDTO
  showImage?: boolean
  index?: number
}

export default function CompetitionCard({
  competition,
  showImage = false,
  index = 0,
}: CompetitionCardProps) {
  const levelColor = COMPETITION_LEVEL_COLORS[competition.level]
  const levelLabel = COMPETITION_LEVEL_LABELS[competition.level]

  const hasImage = showImage && !!competition.introduceImageFileId

  const cardStyle: React.CSSProperties = {
    animationDelay: `${index * 0.1}s`,
    ...(hasImage
      ? {
          backgroundImage: `url(${API_BASE_URL}/file/download/${competition.introduceImageFileId})`,
        }
      : {}),
  }

  return (
    <div
      className={`group relative w-full h-[200px] rounded-3xl p-10 px-8 flex flex-col gap-8 overflow-hidden
        opacity-0 animate-[fadeInUp_0.6s_ease_forwards]
        hover:-translate-y-1
        max-md:h-auto max-md:min-h-[200px] max-md:p-8 max-md:px-6 max-md:rounded-[20px] max-md:gap-6
        ${hasImage ? 'bg-cover bg-center' : 'glass-card'}
      `}
      style={cardStyle}
    >
      {hasImage && (
        <div
          className="absolute inset-0 rounded-3xl z-[1] glass-card
            group-hover:bg-[rgba(255,255,255,0.12)]"
        />
      )}
      <div className="relative z-[2] flex flex-col gap-3 h-full justify-center">
        <div
          className="flex justify-between items-center gap-4
          max-md:flex-col max-md:items-start max-md:gap-3"
        >
          <div
            className="flex items-center gap-4 flex-1 min-w-0
            max-md:w-full max-md:justify-between max-md:gap-3"
          >
            <h3
              className="text-[28px] font-bold text-white m-0 font-[Inter,sans-serif]
              max-md:text-xl"
            >
              {competition.name}
            </h3>
            <span
              className="w-20 h-7 rounded-[14px] flex items-center justify-center
                text-xs font-bold text-white shrink-0 font-[Inter,sans-serif]
                relative overflow-hidden
                transition-[transform,box-shadow] duration-200
                group-hover:scale-105 group-hover:shadow-[0_0_20px_rgba(255,255,255,0.1)]
                max-md:w-16 max-md:h-6 max-md:rounded-xl
                before:content-[''] before:absolute before:top-0 before:-left-full
                before:w-full before:h-full
                before:bg-[linear-gradient(90deg,transparent,rgba(255,255,255,0.2),transparent)]
                before:transition-[left] before:duration-500
                group-hover:before:left-full"
              style={{ backgroundColor: levelColor }}
            >
              {levelLabel}
            </span>
          </div>
          {competition.month && (
            <span
              className="text-2xl font-normal text-white/40 shrink-0 font-[Inter,sans-serif]
              max-md:text-[18px]"
            >
              {competition.month}
            </span>
          )}
        </div>

        <div className="flex flex-col gap-2">
          {competition.organizer && (
            <div className="flex items-center gap-2 flex-wrap max-md:gap-1">
              <span
                className="text-base font-normal text-white/60 shrink-0 font-[Inter,sans-serif]
                max-md:text-sm"
              >
                主办单位：
              </span>
              <span
                className="text-base font-normal text-white font-[Inter,sans-serif]
                max-md:text-sm"
              >
                {competition.organizer}
              </span>
            </div>
          )}
          <p
            className="text-base font-normal text-white/80 leading-[1.5] m-0 font-[Inter,sans-serif]
            max-md:text-sm"
          >
            {competition.summary}
          </p>
        </div>
      </div>
    </div>
  )
}
