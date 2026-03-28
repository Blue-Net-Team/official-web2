import { Card, Flex, Tag } from 'antd'
import styles from './styles.module.css'
import { AchievementDTO } from '@/apis/schema/type'
import { AWARD_LEVEL_LABELS } from '@/apis/schema/enumerate'
import { TrophyOutlined, BulbOutlined, FileTextOutlined } from '@ant-design/icons'
import Image from 'next/image'
import { API_BASE_URL } from '@/apis/config'

interface AchievementCardProps {
  achievement: AchievementDTO
}

const AchievementCard = ({ achievement }: AchievementCardProps) => {
  const logoImageUrl = achievement.competitionLogoFileId
    ? `${API_BASE_URL}/file/download/${achievement.competitionLogoFileId}`
    : null

  const awardLevelColor: Record<string, string> = {
    national: 'gold',
    provincial: 'cyan',
    school: 'orange',
  }

  const typeGradient: Record<string, string> = {
    paper: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    patent: 'linear-gradient(135deg, #f093fb 0%, #f557c3 100%)',
    competition: 'linear-gradient(135deg, #1890ff 0%, #096dd9 100%)',
  }

  const typeIcon =
    achievement.type === 'paper' ? (
      <FileTextOutlined style={{ fontSize: 24, color: '#fff' }} />
    ) : achievement.type === 'patent' ? (
      <BulbOutlined style={{ fontSize: 24, color: '#fff' }} />
    ) : (
      <TrophyOutlined style={{ fontSize: 24, color: '#fff' }} />
    )

  const typeLabel =
    achievement.type === 'paper' ? '论文' : achievement.type === 'patent' ? '专利' : null

  const typeTagColor: Record<string, string> = {
    paper: 'purple',
    patent: 'magenta',
    competition: 'blue',
  }

  const year = achievement.achieveAt ? new Date(achievement.achieveAt).getFullYear() : null

  const isCompetition = achievement.type === 'competition'

  const displayName = isCompetition
    ? achievement.competitionShortName || achievement.competitionName || achievement.title
    : achievement.title

  const subInfo = achievement.relateTo

  return (
    <Card
      className={styles.card}
      styles={{
        body: {
          padding: '20px 24px',
        },
      }}
    >
      <Flex align="start" gap={16}>
        <div
          className={styles.logoWrapper}
          style={{ background: typeGradient[achievement.type] || typeGradient.competition }}
        >
          {logoImageUrl ? (
            <Image src={logoImageUrl} alt={displayName} fill className={styles.logo} />
          ) : (
            typeIcon
          )}
        </div>
        <Flex vertical className={styles.contentContainer}>
          <Flex align="center" gap={8} wrap="wrap">
            <div className={styles.title}>{displayName}</div>
            {typeLabel && !isCompetition && (
              <Tag color={typeTagColor[achievement.type]} className={styles.typeTag}>
                {typeLabel}
              </Tag>
            )}
            {isCompetition && achievement.awardLevel && (
              <Tag color={awardLevelColor[achievement.awardLevel]} className={styles.awardTag}>
                {AWARD_LEVEL_LABELS[achievement.awardLevel]}
              </Tag>
            )}
          </Flex>
          {subInfo && <div className={styles.subInfo}>{subInfo}</div>}
          {isCompetition && achievement.awardName && (
            <div className={styles.awardName}>{achievement.awardName}</div>
          )}
          <Flex align="center" gap={16} className={styles.metaInfo}>
            {year && <span className={styles.metaText}>{year}年</span>}
          </Flex>
        </Flex>
      </Flex>
    </Card>
  )
}

export default AchievementCard
