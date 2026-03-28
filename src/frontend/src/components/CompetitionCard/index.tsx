import { CompetitionBriefDTO } from '@/apis/schema/type'
import { COMPETITION_LEVEL_LABELS, COMPETITION_LEVEL_COLORS } from '@/types/competition'
import styles from './CompetitionCard.module.css'

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

  // 构建图片背景样式
  const cardStyle =
    showImage && competition.introduceImageFileId
      ? {
          backgroundImage: `url(/api/v1/file/download/${competition.introduceImageFileId})`,
          animationDelay: `${index * 0.1}s`,
        }
      : {
          animationDelay: `${index * 0.1}s`,
        }

  return (
    <div
      className={`${styles.card} ${showImage && competition.introduceImageFileId ? styles.cardWithImage : ''}`}
      style={cardStyle}
    >
      <div className={styles.cardContent}>
        <div className={styles.cardHeader}>
          <div className={styles.titleGroup}>
            <h3 className={styles.competitionName}>{competition.name}</h3>
            <span className={styles.levelBadge} style={{ backgroundColor: levelColor }}>
              {levelLabel}
            </span>
          </div>
          {competition.month && <span className={styles.month}>{competition.month}</span>}
        </div>

        <div className={styles.infoGroup}>
          {competition.organizer && (
            <div className={styles.organizer}>
              <span className={styles.label}>主办单位：</span>
              <span className={styles.value}>{competition.organizer}</span>
            </div>
          )}
          <p className={styles.description}>{competition.summary}</p>
        </div>
      </div>
    </div>
  )
}
