import { Card, Flex, Tag } from 'antd'
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
    PAPER: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    PATENT: 'linear-gradient(135deg, #f093fb 0%, #f557c3 100%)',
    COMPETITION: 'linear-gradient(135deg, #1890ff 0%, #096dd9 100%)',
  }

  const typeIcon =
    achievement.type === 'PAPER' ? (
      <FileTextOutlined style={{ fontSize: 24, color: '#fff' }} />
    ) : achievement.type === 'PATENT' ? (
      <BulbOutlined style={{ fontSize: 24, color: '#fff' }} />
    ) : (
      <TrophyOutlined style={{ fontSize: 24, color: '#fff' }} />
    )

  const typeLabel =
    achievement.type === 'PAPER' ? '论文' : achievement.type === 'PATENT' ? '专利' : null

  const typeTagColor: Record<string, string> = {
    PAPER: 'purple',
    PATENT: 'magenta',
    COMPETITION: 'blue',
  }

  const year = achievement.achieveAt ? new Date(achievement.achieveAt).getFullYear() : null

  const isCompetition = achievement.type === 'COMPETITION'

  const displayName = isCompetition
    ? achievement.competitionShortName || achievement.competitionName || achievement.title
    : achievement.title

  const subInfo = achievement.relateTo

  return (
    <Card
      className="glass-card rounded-xl max-md:rounded-lg hover:-translate-y-0.5"
      styles={{
        body: {
          padding: '20px 24px',
        },
      }}
    >
      <Flex align="start" gap={16}>
        <div
          className="w-14 h-14 rounded-xl max-md:w-12 max-md:h-12 max-md:rounded-lg flex items-center justify-center shrink-0 overflow-hidden"
          style={{ background: typeGradient[achievement.type] || typeGradient.competition }}
        >
          {logoImageUrl ? (
            <Image
              src={logoImageUrl}
              alt={displayName}
              fill
              className="w-full h-full object-cover"
            />
          ) : (
            typeIcon
          )}
        </div>
        <Flex vertical className="flex-1 min-w-0">
          <Flex align="center" gap={8} wrap="wrap">
            <div className="text-base max-md:text-sm font-semibold text-white whitespace-nowrap overflow-hidden text-ellipsis">
              {displayName}
            </div>
            {typeLabel && !isCompetition && (
              <Tag
                color={typeTagColor[achievement.type]}
                className="text-xs font-medium border-none px-2 py-0.5 rounded"
              >
                {typeLabel}
              </Tag>
            )}
            {isCompetition && achievement.awardLevel && (
              <Tag
                color={awardLevelColor[achievement.awardLevel]}
                className="text-xs font-medium border-none px-2 py-0.5 rounded"
              >
                {AWARD_LEVEL_LABELS[achievement.awardLevel]}
              </Tag>
            )}
          </Flex>
          {subInfo && <div className="text-sm max-md:text-xs text-white/65 mt-1">{subInfo}</div>}
          {isCompetition && achievement.awardName && (
            <div className="text-sm max-md:text-xs text-white/70 mt-1">{achievement.awardName}</div>
          )}
          <Flex align="center" gap={16} className="mt-2">
            {year && <span className="text-xs text-white/50">{year}年</span>}
          </Flex>
        </Flex>
      </Flex>
    </Card>
  )
}

export default AchievementCard
